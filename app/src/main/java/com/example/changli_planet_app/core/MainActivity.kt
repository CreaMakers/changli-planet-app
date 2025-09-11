package com.example.changli_planet_app.core

import android.Manifest
import android.app.Activity
import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentCallbacks
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.changli_planet_app.R
import com.example.changli_planet_app.TimeTableAppWidget
import com.example.changli_planet_app.common.api.DrawerController
import com.example.changli_planet_app.common.cache.CommonInfo
import com.example.changli_planet_app.common.pool.TabAnimationPool
import com.example.changli_planet_app.common.redux.action.UserAction
import com.example.changli_planet_app.common.redux.store.UserStore
import com.example.changli_planet_app.databinding.ActivityMainBinding
import com.example.changli_planet_app.feature.common.ui.FeatureFragment
import com.example.changli_planet_app.freshNews.ui.NewsFragment
import com.example.changli_planet_app.im.ui.IMFragment
import com.example.changli_planet_app.profileSettings.ui.ProfileSettingsFragment
import com.google.android.material.tabs.TabLayout
import com.gradle.scan.plugin.internal.dep.io.netty.util.internal.StringUtil
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), DrawerController {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    private val fragments = mutableMapOf<Int, Fragment>()
    private var currentTabPosition: Int = 0

    private val tabLayout: TabLayout by lazy { binding.tabLayout }

    private var isDrawerAnimating = false

    private val disposables by lazy { CompositeDisposable() }

    private val store by lazy { UserStore() }


    fun setCustomDensity(activity: Activity, application: Application, designWidthDp: Int) {
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


    override fun onResume() {
        super.onResume()
        store.dispatch(UserAction.initilaize())
        refreshWidget()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (StringUtil.isNullOrEmpty(PlanetApplication.Companion.accessToken)) {
            Route.goLogin(this@MainActivity)
            finish()
            return
        }
        CommonInfo.startTime = System.currentTimeMillis()
        setCustomDensity(this, application, 412)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val start = System.currentTimeMillis()
        PlanetApplication.Companion.startTime = System.currentTimeMillis()
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 主内容避开导航栏
            binding.main.setPadding(
                binding.main.paddingLeft,
                binding.main.top,
                binding.main.paddingRight,
                systemBars.bottom
            )
            insets
        }

        if (savedInstanceState == null) {
            val firstFragment = FeatureFragment.Companion.newInstance()
            fragments[0] = firstFragment
            supportFragmentManager.beginTransaction()
                .add(R.id.frag, firstFragment)
                .commit()
        }
        setupTabs()
        lifecycleScope.launch {
            launch(Dispatchers.Main) {
                setupTabSelectionListener()
            }
            launch(Dispatchers.IO) {
                store.dispatch(UserAction.GetCurrentUserStats(this@MainActivity))
                store.dispatch(UserAction.GetCurrentUserProfile(this@MainActivity))
            }
        }
        Log.d("MainActivity", "用时 ${System.currentTimeMillis() - start}")
        // 检查版本更新
        Looper.myQueue().addIdleHandler {
            getNotificationPermissions()//添加通知权限
            val packageManager: PackageManager = this@MainActivity.packageManager
            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(this@MainActivity.packageName, 0)
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

    private fun refreshWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, TimeTableAppWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isNotEmpty()) {
            val intent = Intent(this, TimeTableAppWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            sendBroadcast(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentTab", currentTabPosition)  //保存最后的tab下标
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentTabPosition = savedInstanceState.getInt("currentTab") //恢复最后的tab下标

        supportFragmentManager.fragments.forEach { fragment ->
            val key = when (fragment) {
                is FeatureFragment -> 0
                is ProfileSettingsFragment -> 3
                is NewsFragment -> 2
                is IMFragment -> 1
                else -> throw IllegalStateException("Invalid fragment")
            }
            fragments.put(key, fragment)     //重新添加fragment
        }

        tabLayout.selectTab(tabLayout.getTabAt(currentTabPosition)) //恢复tabLayout
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        TabAnimationPool.clear()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            launch(Dispatchers.IO) {
                store.dispatch(UserAction.GetCurrentUserStats(this@MainActivity))
                store.dispatch(UserAction.GetCurrentUserProfile(this@MainActivity))
            }
        }
    }

    private fun setupTabs() {
        // 动态添加 tabs
        val featureTab = tabLayout.newTab().setIcon(R.drawable.nfeature).setText(R.string.function)
        val postTab = tabLayout.newTab().setIcon(R.drawable.nnews).setText(R.string.news)
        val imTab = tabLayout.newTab().setIcon(R.drawable.nchat).setText(R.string.chat)
        val profileTab = tabLayout.newTab().setIcon(R.drawable.nprofile).setText(R.string.profile_home)
        tabLayout.addTab(featureTab)
        tabLayout.addTab(postTab)
        tabLayout.addTab(imTab)
        tabLayout.addTab(profileTab)
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
    private fun getNotificationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION
            )
        } else {
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_TELEPHONE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getNetPermissions()
                }
            REQUEST_NOTIFICATION ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getNotificationPermissions()
                }

        }
    }

    private fun setupTabSelectionListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (currentTabPosition == tab.position) return

                val fragment = fragments.getOrPut(tab.position) {
                    when (tab.position) {
                        0 -> FeatureFragment.Companion.newInstance()
                        1 -> NewsFragment.Companion.newInstance()
                        2 -> IMFragment.Companion.newInstance()
                        3 -> ProfileSettingsFragment.Companion.newInstance()
                        else -> throw IllegalStateException("Invalid position")
                    }
                }
                switchFragment(fragment)
                currentTabPosition = tab.position
                animateTabSelect(tab) // 动画效果

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // 可选：处理未选中事件
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // 可选：处理重新选中事件
            }
        })
    }

    private fun switchFragment(newFragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        fragments[currentTabPosition]?.let {
            transaction.hide(it)
        }
        if (newFragment.isAdded) {
            transaction.show(newFragment)
        } else {
            transaction.add(R.id.frag, newFragment)
        }
        transaction.commit()

    }

    private fun initFragment(fragment: Fragment) {
        val fragmentationTemp = supportFragmentManager
        val transactions = fragmentationTemp.beginTransaction()
        transactions.replace(R.id.frag, fragment).commit()
    }

    fun animateTabSelect(tab: TabLayout.Tab) {
        TabAnimationPool.animateTabSelect(tab)
    }

    override fun openDrawer() {

    }

    companion object {
        private const val REQUEST_READ_TELEPHONE = 1001
        private const val REQUEST_NOTIFICATION = 1002
    }
}