package com.creamaker.changli_planet_app.common.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.creamaker.changli_planet_app.base.BaseActivity
import com.creamaker.changli_planet_app.databinding.ActivityFeedbackBinding
import com.creamaker.changli_planet_app.widget.dialog.NormalChosenDialog

class WebViewActivity : BaseActivity<ActivityFeedbackBinding>() {

    companion object Companion {
        private const val URL_TAG = "url_tag"
    }

    private val showUrl: String by lazy {
        intent.getStringExtra(URL_TAG) ?: ""
    }
    override fun createViewBinding(): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(layoutInflater)
    }

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
        val progressBar = binding.loadingProgress
        binding.wvFeedback.apply {
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
                        progressBar.visibility = android.view.View.VISIBLE
                    } else {
                        progressBar.visibility = android.view.View.GONE
                    }
                }

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    val targetUrl = view?.hitTestResult?.extra
                    if (!targetUrl.isNullOrBlank()) {
                        if (shouldOpenExternal(targetUrl)) {
                            showOpenExternalDialog(targetUrl)
                        } else {
                            view.loadUrl(targetUrl)
                        }
                        return false
                    }

                    val popupWebView = WebView(this@WebViewActivity).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val popupUrl = request?.url?.toString().orEmpty()
                                return consumePopupUrl(popupUrl, view)
                            }

                            private fun consumePopupUrl(url: String, popupView: WebView?): Boolean {
                                if (url.isBlank()) {
                                    popupView?.destroy()
                                    return true
                                }
                                if (shouldOpenExternal(url)) {
                                    showOpenExternalDialog(url)
                                } else {
                                    binding.wvFeedback.loadUrl(url)
                                }
                                popupView?.destroy()
                                return true
                            }
                        }
                    }

                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                    if (transport == null) {
                        popupWebView.destroy()
                        showMessage("该链接建议在系统浏览器中打开")
                        return false
                    }
                    transport.webView = popupWebView
                    resultMsg.sendToTarget()
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    progressBar.isIndeterminate = true
                    progressBar.visibility = android.view.View.VISIBLE
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
                    request: android.webkit.WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        progressBar.visibility = android.view.View.GONE
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

            loadUrl(showUrl)
        }

    }

    private fun shouldOpenExternal(url: String): Boolean {
        if (url.isBlank()) return false
        val scheme = runCatching { Uri.parse(url).scheme?.lowercase() }.getOrNull() ?: return false
        return scheme !in setOf("http", "https", "about", "javascript", "data", "file")
    }

    private fun showDownloadDialog(url: String) {
        NormalChosenDialog(
            context = this,
            title = "检测到下载",
            content = "是否使用系统浏览器继续下载安卓安装包？",
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
        binding.wvFeedback.apply {
            webChromeClient = null
            webViewClient = WebViewClient()
            setDownloadListener(null)
            loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            clearHistory()
            clearCache(true)
            destroy()
        }
        super.onDestroy()
    }
}
