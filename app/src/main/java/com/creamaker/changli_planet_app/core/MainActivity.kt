package com.creamaker.changli_planet_app.core

import android.Manifest
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.lifecycleScope
import com.creamaker.changli_planet_app.common.cache.CommonInfo
import com.creamaker.changli_planet_app.common.pool.TabAnimationPool
import com.creamaker.changli_planet_app.common.redux.action.UserAction
import com.creamaker.changli_planet_app.common.redux.store.UserStore
import com.creamaker.changli_planet_app.core.main.MainDestination
import com.creamaker.changli_planet_app.core.main.MainRoot
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.utils.event.SelectEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : ComponentActivity() {
    private val store by lazy { UserStore() }
    private var selectDestination: ((MainDestination) -> Unit)? = null

    override fun onResume() {
        super.onResume()
        store.dispatch(UserAction.initilaize())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EventBus.getDefault().register(this)
        CommonInfo.startTime = System.currentTimeMillis()
        val start = System.currentTimeMillis()

        PlanetApplication.startTime = System.currentTimeMillis()
        setContent {
            AppSkinTheme {
                MainRoot(
                    onExit = ::finish,
                    onNavigatorReady = { navigator -> selectDestination = navigator }
                )
            }
        }

        Log.d("MainActivity", "用时 ${System.currentTimeMillis() - start}")
        Looper.myQueue().addIdleHandler {
            getNetPermissions()
            val packageManager: PackageManager = packageManager
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            store.dispatch(
                UserAction.QueryIsLastedApk(
                    this@MainActivity,
                    PackageInfoCompat.getLongVersionCode(packageInfo),
                    packageInfo.packageName
                )
            )
            false
        }
    }

    private fun getNotificationPermissions() {
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION
            )
        }
    }

    private fun getNetPermissions() {
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_READ_TELEPHONE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_TELEPHONE) {
            getNotificationPermissions()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!PlanetApplication.isExpired) {
            lifecycleScope.launch {
                launch(Dispatchers.IO) {
                    store.dispatch(UserAction.GetCurrentUserStats(this@MainActivity))
                    store.dispatch(UserAction.GetCurrentUserProfile(this@MainActivity))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        TabAnimationPool.clear()
    }

    @Subscribe
    fun selectDestination(event: SelectEvent) {
        selectDestination?.invoke(MainDestination.fromTabIndex(event.eventType))
    }

    companion object {
        private const val REQUEST_READ_TELEPHONE = 1001
        private const val REQUEST_NOTIFICATION = 1002
    }
}
