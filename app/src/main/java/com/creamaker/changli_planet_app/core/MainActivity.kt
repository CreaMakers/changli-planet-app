package com.creamaker.changli_planet_app.core

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.core.app.ActivityCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.lifecycleScope
import com.creamaker.changli_planet_app.common.api.DrawerController
import com.creamaker.changli_planet_app.common.cache.CommonInfo
import com.creamaker.changli_planet_app.common.pool.TabAnimationPool
import com.creamaker.changli_planet_app.common.redux.action.UserAction
import com.creamaker.changli_planet_app.common.redux.store.UserStore
import com.creamaker.changli_planet_app.core.main.navigation.MainTabNavigator
import com.creamaker.changli_planet_app.core.main.ui.MainScreen
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.utils.event.SelectEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : ComponentActivity(), DrawerController {
    private val store by lazy { UserStore() }
    private val mainTabNavigator by lazy(LazyThreadSafetyMode.NONE) { MainTabNavigator() }

    override fun onResume() {
        super.onResume()
        store.dispatch(UserAction.initilaize())  //初始化用户信息，对游客模式无影响
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setCustomDensity(this, application, 412)
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
//        if (PlanetApplication.Companion.accessToken.isNullOrEmpty() && !PlanetApplication.Companion.is_tourist) {
//            Route.goLogin(this@MainActivity)
//            finish()
//            return
//       }
        ////  Route.goHome(this@MainActivity)
        CommonInfo.startTime = System.currentTimeMillis()
        enableEdgeToEdge()
        val start = System.currentTimeMillis()
        PlanetApplication.startTime = System.currentTimeMillis()
        setContent {
            AppSkinTheme {
                Surface(color = AppTheme.colors.bgPrimaryColor) {
                    MainScreen(navigator = mainTabNavigator)
                }
            }
        }
        Log.d("MainActivity", "用时 ${System.currentTimeMillis() - start}")
        // 检查版本更新
        Looper.myQueue().addIdleHandler { //添加通知权限
            getNetPermissions()
            val packageManager: PackageManager = this@MainActivity.packageManager
            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(this@MainActivity.packageName, 0)
            store.dispatch(
                UserAction.QueryIsLastedApk(  //检测是否需要更新，对游客模式无影响
                    this@MainActivity,
                    PackageInfoCompat.getLongVersionCode(packageInfo),
                    packageInfo.packageName
                )
            )
            false
        }
    }

    private fun getNotificationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION
                )
            }
        } else {
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        TabAnimationPool.clear()
    }

    override fun onStart() {
        super.onStart()
        if (!PlanetApplication.isExpired) {   //游客模式不获取用户信息
            lifecycleScope.launch {
                launch(Dispatchers.IO) {
                    store.dispatch(UserAction.GetCurrentUserStats(this@MainActivity))
                    store.dispatch(UserAction.GetCurrentUserProfile(this@MainActivity))
                }
            }
        }
    }

    private fun getNetPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_READ_TELEPHONE
            )
        } else {
            return
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_TELEPHONE -> getNotificationPermissions()
        }
    }

    override fun openDrawer() {
    }

    companion object {
        private const val REQUEST_READ_TELEPHONE = 1001
        private const val REQUEST_NOTIFICATION = 1002
    }

    @Subscribe
    fun onSelectEvent(selectEvent: SelectEvent) {
        mainTabNavigator.select(selectEvent.eventType)
    }

    private fun setCustomDensity(activity: Activity, application: Application, designWidthDp: Int) {
        val appDisplayMetrics = application.resources.displayMetrics
        val targetDensity = appDisplayMetrics.widthPixels / designWidthDp.toFloat()
        val targetDensityDpi = (targetDensity * 160).toInt()
        var nonCompatScaleDensity = appDisplayMetrics.scaledDensity

        application.registerComponentCallbacks(object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                if (newConfig.fontScale > 0) {
                    nonCompatScaleDensity = application.resources.displayMetrics.scaledDensity
                }
            }

            override fun onLowMemory() = Unit
        })

        val targetScaleDensity =
            targetDensity * (nonCompatScaleDensity / appDisplayMetrics.density)

        appDisplayMetrics.density = targetDensity
        appDisplayMetrics.densityDpi = targetDensityDpi
        appDisplayMetrics.scaledDensity = targetScaleDensity

        activity.resources.displayMetrics.density = targetDensity
        activity.resources.displayMetrics.densityDpi = targetDensityDpi
        activity.resources.displayMetrics.scaledDensity = targetScaleDensity
    }
}
