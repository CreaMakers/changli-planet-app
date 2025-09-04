package com.example.changli_planet_app.base

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.example.changli_planet_app.widget.Dialog.LoadingDialog
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class FullScreenActivity<VB: ViewBinding> : AppCompatActivity() {

    open val TAG = javaClass.simpleName

    protected lateinit var binding: VB
        private set

    /**
     * 子类必须实现此方法来创建ViewBinding实例
     */
    protected abstract fun createViewBinding(): VB

    protected val disposables = CompositeDisposable()

    private fun setCustomDensity(activity: Activity, application: Application, designWidthDp: Int) {
        val appDisplayMetrics = application.resources.displayMetrics

        val targetDensity = 1.0f * appDisplayMetrics.widthPixels / designWidthDp
        val targetDensityDpi = (targetDensity * 160).toInt()
        var sNonCompactScaleDensity = appDisplayMetrics.scaledDensity
        application.registerComponentCallbacks(object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                if (newConfig.fontScale > 0) {
                    sNonCompactScaleDensity = application.resources.displayMetrics.scaledDensity
                }
            }
            override fun onLowMemory() {
            }

        })
        val targetScaleDensity =
            targetDensity * (sNonCompactScaleDensity / appDisplayMetrics.density)

        appDisplayMetrics.density = targetDensity
        appDisplayMetrics.densityDpi = targetDensityDpi
        appDisplayMetrics.scaledDensity = targetScaleDensity

        val activityDisplayMetrics = activity.resources.displayMetrics
        activityDisplayMetrics.density = targetDensity
        activityDisplayMetrics.densityDpi = targetDensityDpi
        activityDisplayMetrics.scaledDensity = targetScaleDensity
    }

    private val loadingDialog by lazy { LoadingDialog(this@FullScreenActivity) }
    override fun onCreate(savedInstanceState: Bundle?) {
        setCustomDensity(this, application, 412)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        binding = createViewBinding()
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    fun showCatLoading() {
        loadingDialog.show()
    }

    fun dismissCatLoading() {
        loadingDialog.dismiss()
    }
}