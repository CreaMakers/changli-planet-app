package com.creamaker.changli_planet_app.core

import android.Manifest
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.common.api.DrawerController
import com.creamaker.changli_planet_app.common.cache.CommonInfo
import com.creamaker.changli_planet_app.common.pool.TabAnimationPool
import com.creamaker.changli_planet_app.common.redux.action.UserAction
import com.creamaker.changli_planet_app.common.redux.store.UserStore
import com.creamaker.changli_planet_app.databinding.ActivityMainBinding
import com.creamaker.changli_planet_app.feature.common.ui.FeatureFragment
import com.creamaker.changli_planet_app.freshNews.ui.NewsFragment
import com.creamaker.changli_planet_app.im.ui.IMFragment
import com.creamaker.changli_planet_app.profileSettings.ui.ProfileSettingsFragment
import com.creamaker.changli_planet_app.utils.Event.SelectEvent
import com.creamaker.changli_planet_app.widget.dialog.GuestLimitedAccessDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : FullScreenActivity<ActivityMainBinding>(), DrawerController {
    private lateinit var drawerLayout: DrawerLayout
    private val fragments = mutableMapOf<Int, Fragment>()
    private var currentTabPosition: Int = 0
    private val tabLayout: TabLayout by lazy { binding.tabLayout }
    override fun createViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    private val store by lazy { UserStore() }
    private var suppress = false

    override fun onResume() {
        super.onResume()
        store.dispatch(UserAction.initilaize())  //初始化用户信息，对游客模式无影响
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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


        PlanetApplication.Companion.startTime = System.currentTimeMillis()
        setContentView(binding.root)


        drawerLayout = binding.drawerLayout


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

        if( !PlanetApplication.Companion.is_tourist) {  //游客模式不获取用户信息
                launch(Dispatchers.IO) {
                  store.dispatch(UserAction.GetCurrentUserStats(this@MainActivity))
                   store.dispatch(UserAction.GetCurrentUserProfile(this@MainActivity))
              }
           }
        }
        Log.d("MainActivity", "用时 ${System.currentTimeMillis() - start}")
        // 检查版本更新
        Looper.myQueue().addIdleHandler { //添加通知权限
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
        EventBus.getDefault().unregister(this)
        disposables.clear()
        TabAnimationPool.clear()
    }

    override fun onStart() {
        super.onStart()
        if(!PlanetApplication.is_tourist) {   //游客模式不获取用户信息
            lifecycleScope.launch {
                launch(Dispatchers.IO) {
                    store.dispatch(UserAction.GetCurrentUserStats(this@MainActivity))
                    store.dispatch(UserAction.GetCurrentUserProfile(this@MainActivity))
                }
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

        }
    }

    private fun setupTabSelectionListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (suppress) return
                if (currentTabPosition == tab.position) return

                val needBlock = (tab.position == 1 && PlanetApplication.is_tourist) // 自行替换条件
                if (needBlock) {
                    GuestLimitedAccessDialog(this@MainActivity).show()

                    // 还原到上一个Tab，不触发你的切换逻辑
                    suppress = true
                    tabLayout.getTabAt(currentTabPosition)?.let { tabLayout.selectTab(it) }
                    suppress = false
                    return
                }

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
        //修改

        if(newFragment == fragments[1] && PlanetApplication.is_expired){
            Route.goLogin(this@MainActivity)
        }

        if(newFragment == fragments[3] && PlanetApplication.is_expired){
            Route.goLogin(this@MainActivity)
        }

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
    }
    @Subscribe
    fun selectProfileFragment(selectEvent: SelectEvent){
        tabLayout.selectTab(tabLayout.getTabAt(selectEvent.eventType))
    }
    @Subscribe
    fun selectFeatureFragment(selectEvent: SelectEvent){
        tabLayout.selectTab(tabLayout.getTabAt(selectEvent.eventType))
    }
}