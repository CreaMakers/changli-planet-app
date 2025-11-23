package com.creamaker.changli_planet_app.base

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.creamaker.changli_planet_app.skin.views.SkinConstraintLayout
import com.creamaker.changli_planet_app.skin.views.SkinCoordinatorLayout
import com.creamaker.changli_planet_app.skin.views.SkinEditText
import com.creamaker.changli_planet_app.skin.views.SkinFrameLayout
import com.creamaker.changli_planet_app.skin.views.SkinImageView
import com.creamaker.changli_planet_app.skin.views.SkinLinearLayout
import com.creamaker.changli_planet_app.skin.views.SkinMaterialCardView
import com.creamaker.changli_planet_app.skin.views.SkinMaxHeightLinearLayout
import com.creamaker.changli_planet_app.skin.views.SkinRecyclerView
import com.creamaker.changli_planet_app.skin.views.SkinRelativeLayout
import com.creamaker.changli_planet_app.skin.views.SkinTabLayout
import com.creamaker.changli_planet_app.skin.views.SkinTextView
import com.creamaker.changli_planet_app.skin.views.SkinView
import com.creamaker.changli_planet_app.skin.views.SkinWheelView
import com.creamaker.changli_planet_app.widget.dialog.LoadingDialog
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class FullScreenActivity<VB : ViewBinding> : AppCompatActivity() {

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

        LayoutInflater.from(this).factory2 = object : LayoutInflater.Factory2 {
            override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
                return onCreateView(null, name, context, attrs)
            }

            override fun onCreateView(
                parent: View?,
                name: String,
                context: Context,
                attrs: AttributeSet
            ): View? {
                when (name) {
                    "TextView" -> {

                        return SkinTextView(context, attrs)
                    }

                    "com.google.android.material.card.MaterialCardView" -> {
                        return SkinMaterialCardView(context, attrs)
                    }

                    "androidx.constraintlayout.widget.ConstraintLayout" -> {
                        return SkinConstraintLayout(context, attrs)
                    }

                    "ImageView" -> {
                        return SkinImageView(context, attrs)
                    }

                    "com.google.android.material.tabs.TabLayout" -> {
                        return SkinTabLayout(context, attrs)
                    }

                    "LinearLayout" ->{
                        return SkinLinearLayout(context, attrs)
                    }
                    "View" ->{
                        return SkinView(context,attrs)
                    }
                    "androidx.recyclerview.widget.RecyclerView" ->{
                        return SkinRecyclerView(context, attrs)
                    }
                    "EditText" ->{
                        return SkinEditText(context, attrs)
                    }
                    "androidx.coordinatorlayout.widget.CoordinatorLayout" -> {
                        return SkinCoordinatorLayout(context, attrs)
                    }
                    "RelativeLayout" ->{
                        return SkinRelativeLayout(context,attrs)
                    }
                    "com.creamaker.changli_planet_app.widget.view.MaxHeightLinearLayout" ->{
                        return SkinMaxHeightLinearLayout(context,attrs)
                    }
                    "FrameLayout" ->{
                        return SkinFrameLayout(context,attrs)
                    }
                    "com.github.gzuliyujiang.wheelview.widget.WheelView" ->{
                        return SkinWheelView(context,attrs)
                    }
                    else -> return null
                }
                return null
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
//        window.statusBarColor = Color.BLACK
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