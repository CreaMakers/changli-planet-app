package com.creamaker.changli_planet_app.feature.mooc.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.creamaker.changli_planet_app.BuildConfig
import com.creamaker.changli_planet_app.base.BaseActivity
import com.creamaker.changli_planet_app.databinding.ActivityMoocCoursePageBinding
import com.creamaker.changli_planet_app.widget.dialog.NormalChosenDialog
import com.dcelysia.csust_spider.core.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Mooc 课程页面 WebView。
 *
 * 用于从「待提交作业」列表点击「前往课程」后，直接以已登录态打开课程主页
 * （`http://pt.csust.edu.cn/meol/jpk/course/layout/newpage/index.jsp?courseId=xxx`），
 * 免去再次登录。
 *
 * 登录态通过将 [RetrofitUtils.totalCookieJar]（spider 内部持久化的 OkHttp Cookie）
 * 同步到 WebView 的 [CookieManager] 实现。
 *
 * **注意：Android 的 [CookieManager] 是进程级全局状态**，本页进入时会主动清空
 * webkit 的 cookie 存储以避免被先前其他会话污染；退出时不清理，交由下次进入时
 * 重建，最大程度降低对其他 WebView 入口的副作用。
 */
class MoocCoursePageActivity : BaseActivity<ActivityMoocCoursePageBinding>() {

    companion object {
        const val EXTRA_COURSE_ID = "extra_course_id"

        private const val TAG = "MoocCoursePage"
        private const val BASE_URL = "http://pt.csust.edu.cn"
        private const val COURSE_URL_TEMPLATE =
            "$BASE_URL/meol/jpk/course/layout/newpage/index.jsp?courseId=%s"

        /**
         * 需要从 OkHttp 侧拉取 cookie 的 host 列表。
         * 覆盖 meol 业务、SSO 登录、统一门户；同步时会按 cookie 自身的 `domain`
         * 写入 webkit，因此这里只需要列出会产生 cookie 的"来源 host"。
         */
        private val COOKIE_SOURCE_URLS: List<HttpUrl> = listOf(
            "http://pt.csust.edu.cn",
            "https://authserver.csust.edu.cn",
            "https://ehall.csust.edu.cn",
            "https://cas.csust.edu.cn",
            "http://xk.csust.edu.cn"
        ).mapNotNull { it.toHttpUrlOrNull() }

        private val EXTERNAL_SAFE_SCHEMES = setOf("http", "https", "about", "javascript", "data")

        /** Cookie 同步的硬超时，防止个别 ROM 上 `removeAllCookies` / `setCookie` 回调不触发导致白屏。 */
        private const val COOKIE_SYNC_TIMEOUT_MS = 2000L

        /** 用于 [Handler.removeCallbacksAndMessages] 精确清除 cookie 超时任务的 token。 */
        private val TIMEOUT_TOKEN = Any()

        /** 相机拍照临时文件所在子目录（位于 [android.content.Context.getCacheDir]）。 */
        private const val CAMERA_CACHE_DIR = "webview_capture"
    }

    private val courseId: String by lazy {
        intent.getStringExtra(EXTRA_COURSE_ID).orEmpty()
    }

    private val targetUrl: String by lazy {
        String.format(COURSE_URL_TEMPLATE, courseId)
    }

    /**
     * `onCreateWindow` 临时构造的 popup WebView 列表。
     * 若目标页 `window.open` 后不产生任何导航（WebViewClient.shouldOverrideUrlLoading 不触发），
     * 这些 popup 就会挂在内存里，必须在 [onDestroy] 时一并销毁。
     */
    private val trackedPopups: MutableList<WebView> = mutableListOf()

    private val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    // ============================== 文件选择 / 拍照 ==============================

    /**
     * WebView `<input type="file">` 的回调。必须在用户完成/取消后**严格调用一次**
     * （传 null 代表取消），否则 WebView 会永久卡在等待状态。
     */
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    /** 拍照时 FileProvider 分配给相机 App 的输出 Uri。 */
    private var pendingCameraPhotoUri: Uri? = null

    /** 拍照输出对应的本地文件，用于失败/取消时清理磁盘残留。 */
    private var pendingCameraPhotoFile: File? = null

    /** 文件/图库选择器：`ACTION_GET_CONTENT` 统一处理单选/多选。 */
    private val fileChooserLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = if (result.resultCode == RESULT_OK) result.data else null
            deliverFileChooserResult(parseFileChooserResult(data))
        }

    /** 拍照：成功后把 [pendingCameraPhotoUri] 交给 WebView。 */
    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uris = if (success) pendingCameraPhotoUri?.let { arrayOf(it) } else null
            pendingCameraPhotoUri = null
            deliverFileChooserResult(uris)
        }

    /** 拍照权限请求：授权后直接走 [launchCameraCapture]，拒绝则回传 null。 */
    private val cameraPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                if (!launchCameraCapture()) {
                    deliverFileChooserResult(null)
                    showMessage("无法启动相机")
                }
            } else {
                deliverFileChooserResult(null)
                showMessage("未授予相机权限")
            }
        }

    // ===========================================================================

    override fun createViewBinding(): ActivityMoocCoursePageBinding =
        ActivityMoocCoursePageBinding.inflate(layoutInflater)

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
        super.initView()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = insets.top
            layoutParams.bottomMargin = insets.bottom
            view.layoutParams = layoutParams
            WindowInsetsCompat.CONSUMED
        }

        if (courseId.isBlank()) {
            Toast.makeText(this, "课程信息无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        registerBackPressHandler()
        setupWebView()
        loadWithCookies()
        pruneCameraCacheAsync()
    }

    private fun registerBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val webView = binding.wvCourse
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        val progressBar = binding.loadingProgress
        binding.wvCourse.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(true)

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if (newProgress < 100) {
                        progressBar.isIndeterminate = false
                        progressBar.progress = newProgress
                        progressBar.visibility = View.VISIBLE
                    } else {
                        progressBar.visibility = View.GONE
                    }
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    return handleShowFileChooser(filePathCallback, fileChooserParams)
                }

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    // meol 里很多作业链接是 window.open 弹新窗口；我们直接在当前 WebView 载入。
                    val hitUrl = view?.hitTestResult?.extra
                    if (!hitUrl.isNullOrBlank()) {
                        if (shouldOpenExternal(hitUrl)) {
                            showOpenExternalDialog(hitUrl)
                        } else {
                            view.loadUrl(hitUrl)
                        }
                        return false
                    }

                    // 没有 transport 时要把刚构造的 popup 销毁，避免泄漏。
                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                    if (transport == null) {
                        showMessage("该链接建议在系统浏览器中打开")
                        return false
                    }

                    val popupWebView = WebView(this@MoocCoursePageActivity).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val popupUrl = request?.url?.toString().orEmpty()
                                if (popupUrl.isBlank()) {
                                    disposePopup(view)
                                    return true
                                }
                                if (shouldOpenExternal(popupUrl)) {
                                    showOpenExternalDialog(popupUrl)
                                } else {
                                    binding.wvCourse.loadUrl(popupUrl)
                                }
                                disposePopup(view)
                                return true
                            }
                        }
                    }
                    trackedPopups.add(popupWebView)
                    transport.webView = popupWebView
                    resultMsg.sendToTarget()
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    progressBar.isIndeterminate = true
                    progressBar.visibility = View.VISIBLE
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString().orEmpty()
                    return if (shouldOpenExternal(url)) {
                        showOpenExternalDialog(url)
                        true
                    } else {
                        false
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        progressBar.visibility = View.GONE
                    }
                }
            }

            setDownloadListener { url, _, _, _, _ ->
                if (url.isBlank()) {
                    showMessage("下载链接无效")
                } else {
                    showDownloadDialog(url)
                }
            }
        }
    }

    /**
     * 同步 cookie 再加载 URL。步骤：
     *
     * 1. IO 线程：从 spider 的 CookieJar 拉取目标 host 的 cookie，生成 `Set-Cookie` 片段。
     *    这里用 `okhttp3.Cookie.toString()` 生成，自带 hostOnly / SameSite 的正确处理。
     * 2. 主线程：先 `removeAllCookies` 清洗掉全局 CookieManager 残留，避免跨会话污染；
     *    再按 cookie 自身 domain 对应的 URL 调用带 callback 的 setCookie；
     *    全部回调完成后 flush 并 loadUrl，保证首帧就是已登录态。
     */
    private fun loadWithCookies() {
        lifecycleScope.launch {
            val entries = withContext(Dispatchers.IO) { collectCookies() }
            if (isFinishing || isDestroyed) return@launch
            syncCookiesToWebView(entries) {
                // 同步完成后再加载，避免 setCookie 异步未生效导致首帧未登录。
                if (!isFinishing && !isDestroyed) {
                    binding.wvCourse.loadUrl(targetUrl)
                }
            }
        }
    }

    /**
     * 从 popup 跟踪列表移除并 destroy。幂等：传 null 或重复调用都无副作用。
     */
    private fun disposePopup(webView: WebView?) {
        if (webView == null) return
        trackedPopups.remove(webView)
        runCatching {
            webView.stopLoading()
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
    }



    private fun handleShowFileChooser(
        callback: ValueCallback<Array<Uri>>?,
        params: WebChromeClient.FileChooserParams?
    ): Boolean {
        if (callback == null) return false

        // 用户若在短时间内连点 input，旧请求会先被 cancel（callback.onReceiveValue(null)），
        // 新请求再接管。旧 launcher 的后续回调进来时会因 filePathCallback == null 安全短路。
        releasePendingFileCallback()
        filePathCallback = callback

        val acceptTypes = params?.acceptTypes.orEmpty().filter { it.isNotBlank() }
        val isCaptureCamera = params?.isCaptureEnabled == true &&
                acceptTypes.any { it.startsWith("image/") }

        val launched = if (isCaptureCamera) {
            requestCameraThenCapture()
        } else {
            launchFilePicker(acceptTypes, params?.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE)
        }

        if (!launched) {
            // 启动失败必须立刻释放 callback，否则 WebView 卡住
            deliverFileChooserResult(null)
            showMessage("无法打开文件选择器")
        }
        return true
    }

    /**
     * 相机入口统一入口：若已有 CAMERA 权限直接拍照；否则先请求权限，结果回调里再启动。
     * 返回 `true` 表示流程已被接管（权限请求也算接管，回调会负责后续）。
     */
    private fun requestCameraThenCapture(): Boolean {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        return if (granted) {
            launchCameraCapture()
        } else {
            runCatching {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                true
            }.getOrElse {
                Log.e(TAG, "request camera permission failed", it)
                false
            }
        }
    }

    private fun launchFilePicker(mimeTypes: List<String>, allowMultiple: Boolean): Boolean {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeTypes.firstOrNull()?.takeIf { it.contains('/') } ?: "*/*"
            if (mimeTypes.size > 1) {
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toTypedArray())
            }
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return runCatching {
            fileChooserLauncher.launch(Intent.createChooser(intent, "选择文件"))
            true
        }.getOrElse {
            Log.e(TAG, "launchFilePicker failed", it)
            false
        }
    }

    private fun launchCameraCapture(): Boolean {
        val photoUri = createCameraOutputUri() ?: return false
        pendingCameraPhotoUri = photoUri
        return runCatching {
            takePictureLauncher.launch(photoUri)
            true
        }.getOrElse {
            Log.e(TAG, "launchCameraCapture failed", it)
            pendingCameraPhotoUri = null
            false
        }
    }

    private fun createCameraOutputUri(): Uri? {
        return runCatching {
            val dir = File(cacheDir, CAMERA_CACHE_DIR).apply { if (!exists()) mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(dir, "IMG_$timestamp.jpg")
            pendingCameraPhotoFile = file
            FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        }.getOrElse {
            Log.e(TAG, "createCameraOutputUri failed", it)
            pendingCameraPhotoFile = null
            null
        }
    }

    /** 后台清理一次 camera 临时目录：删除超过 24 小时的旧文件，避免无限堆积。 */
    private fun pruneCameraCacheAsync() {
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val dir = File(cacheDir, CAMERA_CACHE_DIR)
                if (!dir.isDirectory) return@runCatching
                val cutoff = System.currentTimeMillis() - 24L * 60 * 60 * 1000
                dir.listFiles()?.forEach {
                    if (it.isFile && it.lastModified() < cutoff) it.delete()
                }
            }
        }
    }

    /** 把 `ACTION_GET_CONTENT` 的结果解析为 Uri 数组；支持单选（`data.data`）和多选（`data.clipData`）。 */
    private fun parseFileChooserResult(data: Intent?): Array<Uri>? {
        if (data == null) return null
        val clipData = data.clipData
        if (clipData != null && clipData.itemCount > 0) {
            // ClipData.Item.uri 允许为 null（比如 item 仅含 text），此处过滤掉
            val uris = (0 until clipData.itemCount).mapNotNull { clipData.getItemAt(it).uri }
            return uris.takeIf { it.isNotEmpty() }?.toTypedArray()
        }
        val single = data.data ?: return null
        return arrayOf(single)
    }

    /**
     * 给 WebView 的 filePathCallback 回传结果，保证**只回调一次**。
     * 该方法必须在主线程调用（AR 回调本身就在主线程）。
     *
     * 若传入空数组会被规范化成 null（部分 WebView 内核对空数组判定为"选择了 0 个文件"，
     * 会停留在半提交状态）。
     */
    private fun deliverFileChooserResult(uris: Array<Uri>?) {
        // 拍照失败/取消：无条件清掉本次生成的临时文件（有些相机 App 会写入部分 EXIF 头，
        // 文件非空但无效，用 length==0 判定会漏删）
        if (uris == null) {
            pendingCameraPhotoFile?.takeIf { it.exists() }?.delete()
        }
        pendingCameraPhotoFile = null
        pendingCameraPhotoUri = null

        val callback = filePathCallback ?: return
        filePathCallback = null
        val normalized = uris?.takeIf { it.isNotEmpty() }
        runCatching { callback.onReceiveValue(normalized) }
    }

    /** Activity 销毁或请求被新请求覆盖时，释放上一次悬挂的 callback，避免 WebView 永久等待。 */
    private fun releasePendingFileCallback() {
        filePathCallback?.let {
            filePathCallback = null
            runCatching { it.onReceiveValue(null) }
        }
        pendingCameraPhotoFile?.takeIf { it.exists() }?.delete()
        pendingCameraPhotoFile = null
        pendingCameraPhotoUri = null
    }

    // ===========================================================================

    /**
     * 从 [RetrofitUtils.totalCookieJar] 汇集本页需要的 cookie，并按
     * `domain|path|name` 去重（符合 RFC 6265 的 cookie 身份定义）。
     */
    private fun collectCookies(): List<CookieEntry> {
        val jar = RetrofitUtils.totalCookieJar
        val dedup = LinkedHashMap<String, CookieEntry>(16)
        COOKIE_SOURCE_URLS.forEach { httpUrl ->
            val cookies = runCatching { jar.loadForRequest(httpUrl) }.getOrElse { emptyList() }
            cookies.forEach { cookie ->
                val key = "${cookie.domain}|${cookie.path}|${cookie.name}"
                if (dedup.containsKey(key)) return@forEach
                val scheme = if (cookie.secure) "https" else "http"
                val writeUrl = "$scheme://${cookie.domain}"
                dedup[key] = CookieEntry(writeUrl, cookie.toString())
            }
        }
        val result = dedup.values.toList()
        Log.d(TAG, "collectCookies: size=${result.size}")
        return result
    }

    /**
     * 将 cookie 同步到全局 WebView CookieManager。
     *
     * 实现要点：
     * - 先 `removeAllCookies` 清理遗留，再逐条 `setCookie(url, value, callback)`，
     *   全部回调完成后 `flush` 并加载目标页，保证首帧即登录态。
     * - `AtomicBoolean completed` + 主线程 [Handler] `postDelayed` 构造 **2 秒超时兜底**，
     *   防止个别 ROM 上 cookie 操作回调丢失导致永久白屏。
     * - 所有 `onComplete` 调用都走主线程 [Handler]，规避 webkit 在部分设备上可能把回调
     *   抛到工作线程引发 `WrongThreadException`。
     */
    private fun syncCookiesToWebView(entries: List<CookieEntry>, onComplete: () -> Unit) {
        val cookieManager = CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(binding.wvCourse, true)
        }

        val completed = AtomicBoolean(false)
        val finish: () -> Unit = {
            if (completed.compareAndSet(false, true)) {
                mainHandler.removeCallbacksAndMessages(TIMEOUT_TOKEN)
                runCatching { cookieManager.flush() }
                mainHandler.post(onComplete)
            }
        }

        // 硬超时兜底
        mainHandler.postAtTime({
            Log.w(TAG, "syncCookiesToWebView: timeout, load anyway")
            finish()
        }, TIMEOUT_TOKEN, android.os.SystemClock.uptimeMillis() + COOKIE_SYNC_TIMEOUT_MS)

        cookieManager.removeAllCookies { cleared ->
            Log.d(TAG, "removeAllCookies cleared=$cleared")
            if (completed.get()) return@removeAllCookies
            if (entries.isEmpty()) {
                finish()
                return@removeAllCookies
            }

            val remaining = AtomicInteger(entries.size)
            val onEach: (Boolean) -> Unit = { success ->
                if (!success && BuildConfig.DEBUG) Log.w(TAG, "setCookie failed")
                if (remaining.decrementAndGet() == 0) {
                    finish()
                }
            }
            entries.forEach { (writeUrl, header) ->
                cookieManager.setCookie(writeUrl, header, onEach)
            }
        }
    }

    private data class CookieEntry(
        val writeUrl: String,
        val setCookieHeader: String
    )

    private fun shouldOpenExternal(url: String): Boolean {
        if (url.isBlank()) return false
        val scheme = runCatching { Uri.parse(url).scheme?.lowercase() }.getOrNull() ?: return false
        return scheme !in EXTERNAL_SAFE_SCHEMES
    }

    private fun showDownloadDialog(url: String) {
        NormalChosenDialog(
            context = this,
            title = "检测到下载",
            content = "是否使用系统浏览器继续下载？",
            confirmText = "继续下载",
            cancelText = "取消",
            onConfirm = { openInExternalApp(url, "正在跳转到系统浏览器...") }
        ).show()
    }

    private fun showOpenExternalDialog(url: String) {
        NormalChosenDialog(
            context = this,
            title = "打开外部链接",
            content = "该链接需要由系统应用处理，是否继续打开？",
            confirmText = "打开",
            cancelText = "取消",
            onConfirm = { openInExternalApp(url) }
        ).show()
    }

    private fun openInExternalApp(url: String, successMessage: String? = null) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            successMessage?.let { showMessage(it) }
        } catch (_: ActivityNotFoundException) {
            showMessage("未找到可处理该链接的应用")
        } catch (e: Exception) {
            showMessage("打开失败: ${e.message}")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        // 释放悬挂的文件选择 callback，避免 WebView 永久等待
        releasePendingFileCallback()
        // 先清理所有未被导航触发的 popup WebView，避免泄漏
        trackedPopups.toList().forEach { disposePopup(it) }
        trackedPopups.clear()

        binding.wvCourse.apply {
            // 先从父容器移除，避免 destroy 时 WindowManager 报错
            (parent as? ViewGroup)?.removeView(this)
            webChromeClient = null
            webViewClient = WebViewClient()
            setDownloadListener(null)
            stopLoading()
            loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            clearHistory()
            // 不清 cache：WebView 缓存进程内共享，清了会影响 WebViewActivity
            destroy()
        }
        super.onDestroy()
    }
}
