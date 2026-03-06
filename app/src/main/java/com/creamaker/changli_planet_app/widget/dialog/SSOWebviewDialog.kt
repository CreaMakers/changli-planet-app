package com.example.changli_planet_app.widget.Dialog

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import com.creamaker.changli_planet_app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import okhttp3.Cookie

class SSOWebviewDialog(
    val loginResult: (String, String, String, String, List<Cookie>) -> Unit,
    val onDismissCallback: (() -> Unit)? = null,
) : BottomSheetDialogFragment() {

    private  var account  = ""
    private  var password = ""
    private lateinit var cookies : List<Cookie>
    private  var loginMode = LoginMode.Username

    enum class LoginMode {Username,Dynamic}

    private lateinit var webview : WebView
    private lateinit var space: View
    private var  isPagedFinished = false


    override fun onStart() {
        super.onStart()
        // 防止软键盘弹出时 BottomSheet 被顶起导致 WebView 莫名上移
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_sso_webview, container, false)
        space = view.findViewById<View>(R.id.null_space)
        webview = view.findViewById<WebView>(R.id.SSO_webview)
        if (isPagedFinished) {
            space.visibility = View.GONE
        }
        val internal = view.findViewById<LinearLayout>(R.id.internal_sso_layout)
        val back = internal.findViewById<TextView>(R.id.sso_back)
        back.setOnClickListener { dismiss() }
        initWebview()
        return view
    }



    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebview() {
        webview.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled =true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webview.addJavascriptInterface(object{
            @JavascriptInterface
            fun onFieldChanged(field: String,value: String){
                when(field){
                    "username" -> account = value
                    "password" -> password =value
                }
            }
        },"AndroidBridge")

        webview.webViewClient = object : WebViewClient() {


            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("Qingyue","onPageFinished")
                val js = """
                        (function() {
                            function watchInput(id, fieldName) {
                                var input = document.getElementById(id);
                                if (!input) return;
                                function sendValue() {
                                    AndroidBridge.onFieldChanged(fieldName, input.value);
                                }
                                input.addEventListener('input', sendValue);
                                var observer = new MutationObserver(sendValue);
                                observer.observe(input, { attributes: true, attributeFilter: ['value'] });
                            }
                            watchInput('username', 'username');
                            watchInput('password', 'password');
                        })();
                    """.trimIndent()
                view?.evaluateJavascript(js, null)
                isPagedFinished = true

            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val urlString = request?.url.toString()
                Log.d("Qingyue",urlString)
                if (urlString.isEmpty()) {
                    return false
                }
                when {
                    urlString.contains("login?type=userNameLogin") -> loginMode = LoginMode.Username
                    urlString.contains("login?type=dynamicLogin") -> loginMode = LoginMode.Dynamic
                }
                Log.d("Qingyue",loginMode.toString())
                if (urlString == "https://ehall.csust.edu.cn/index.html" ||
                    urlString == "https://ehall.csust.edu.cn/default/index.html"
                ) {
                    val cookieManager = CookieManager.getInstance()
                    val cookieString = cookieManager.getCookie(urlString)
                    Log.d("Qingyue","cookieString:${cookieString}")
//                    cookies = cookieString.split(";").mapNotNull {
//                        try {
//                            val cookie = HttpCookie.parse(urlString).firstOrNull() as Cookie
//                            Log.d("Qingyue",cookie.toString())
//                            cookie
//                        } catch (e: Exception) {
//                            null
//                        }
//                    }
                    cookies = cookieString.split(";").mapNotNull { cookieStr ->
                        try {
                            val trimmedCookie = cookieStr.trim()
                            val parts = trimmedCookie.split("=", limit = 2)
                            if (parts.size == 2) {
                                val name = parts[0].trim()
                                val value = parts[1].trim()
                                Cookie.Builder()
                                    .name(name)
                                    .value(value)
                                    .domain("ehall.csust.edu.cn") // 设置域名
                                    .path("/") // 设置路径
                                    .build()
                            } else null
                        } catch (e: Exception) {
                            Log.e("SSOWebviewDialog", "Cookie parsing error: ${e.message}")
                            null
                        }
                    }
                    Log.d("Qingyue", cookies.toString())
                    if (account.isNotEmpty()) {
                        loginResult(account,password,loginMode.toString(),urlString,cookies)
                        dismiss()
                    }
                    return false
                } else {
                    return false
                }
            }
        }
        webview.loadUrl(
            "https://authserver.csust.edu.cn/authserver/login?service=https%3A%2F%2Fehall.csust.edu.cn%2Flogin"
        )
    }

    override fun onDestroy() {
        webview.apply {
            stopLoading()
            clearCache(true)
            clearHistory()
            destroy()
        }
        super.onDestroy()
    }
}