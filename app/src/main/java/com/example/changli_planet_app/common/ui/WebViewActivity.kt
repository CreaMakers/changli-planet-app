package com.example.changli_planet_app.common.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.base.BaseActivity
import com.example.changli_planet_app.databinding.ActivityFeedbackBinding

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.wvFeedback) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = insets.top
            view.layoutParams = layoutParams

            WindowInsetsCompat.CONSUMED
        }
        binding.wvFeedback.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                }
            }
            loadUrl(showUrl)
        }
    }
}