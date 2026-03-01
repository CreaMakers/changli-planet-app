package com.creamaker.changli_planet_app.common.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.creamaker.changli_planet_app.base.BaseActivity
import com.creamaker.changli_planet_app.databinding.ActivityFeedbackBinding

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
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    progressBar.isIndeterminate = true
                    progressBar.visibility = android.view.View.VISIBLE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    progressBar.visibility = android.view.View.GONE
                }
            }

            loadUrl(showUrl)
        }

    }

    override fun onDestroy() {
        binding.wvFeedback.apply {
            loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            clearHistory()
            clearCache(true)
            destroy()
        }
        super.onDestroy()
    }
}