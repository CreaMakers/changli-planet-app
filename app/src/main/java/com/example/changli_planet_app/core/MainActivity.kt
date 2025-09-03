package com.example.changli_planet_app.core

import android.Manifest
import android.animation.LayoutTransition
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
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.changli_planet_app.R
import com.example.changli_planet_app.TimeTableAppWidget
import com.example.changli_planet_app.common.api.DrawerController
import com.example.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.example.changli_planet_app.common.pool.TabAnimationPool
import com.example.changli_planet_app.common.redux.action.UserAction
import com.example.changli_planet_app.common.redux.store.UserStore
import com.example.changli_planet_app.databinding.ActivityMainBinding
import com.example.changli_planet_app.discover.ui.ChatGroupFragment
import com.example.changli_planet_app.feature.common.ui.FeatureFragment
import com.example.changli_planet_app.freshNews.ui.NewsFragment
import com.example.changli_planet_app.im.ui.IMFragment
import com.example.changli_planet_app.utils.GlideUtils
import com.example.changli_planet_app.utils.NetworkUtil
import com.example.changli_planet_app.widget.Dialog.NormalChosenDialog
import com.example.changli_planet_app.widget.View.CustomToast
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.gradle.scan.plugin.internal.dep.io.netty.util.internal.StringUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), DrawerController {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    private val fragments = mutableMapOf<Int, Fragment>()
    private var currentTabPosition: Int = 0

    private val tabLayout: TabLayout by lazy { binding.tabLayout }

    // 侧边栏头像部分
    private val mainAvatarLinear: LinearLayout by lazy { binding.mainAvatar }
    private val drawerAccount: TextView by lazy { binding.drawerAccount }
    private val drawerAvatar: ShapeableImageView by lazy { binding.drawerAvatar }
    private val drawerStuNumber: TextView by lazy { binding.mainStuNumber }

    // 主要设置
    private val notificationSettings: LinearLayout by lazy { binding.notificationSettings }
    private val privacySettings: LinearLayout by lazy { binding.privacySettings }
    private val accountSecurity: LinearLayout by lazy { binding.accountSecurity }

    // 常用功能
    private val clearCache: LinearLayout by lazy { binding.clearCache }
    private val changeStudentId: LinearLayout by lazy { binding.changeStudentId }
    private val themeSettings: LinearLayout by lazy { binding.themeSettings }
    private val messageCenter: LinearLayout by lazy { binding.messageCenter }

    // 帮助与支持
    private val helpCenter: LinearLayout by lazy { binding.helpCenter }
    private val aboutUs: LinearLayout by lazy { binding.aboutUs }
    private val feedback: LinearLayout by lazy { binding.feedback }

    // 退出登录按钮
    private val logoutButton: MaterialButton by lazy { binding.logoutButton }

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
        drawerAccount.text = UserInfoManager.account
        refreshWidget()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (StringUtil.isNullOrEmpty(PlanetApplication.Companion.accessToken)) {
            Route.goLogin(this@MainActivity)
            finish()
            return
        }
        setCustomDensity(this, application, 412)
        super.onCreate(savedInstanceState)
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

            // 抽屉导航避开导航栏
            binding.drawerContent.setPadding(
                binding.drawerContent.paddingLeft,
                binding.drawerContent.paddingTop,
                binding.drawerContent.paddingRight,
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
            // 2. UI相关的初始化放在一组（在主线程执行）
            launch(Dispatchers.Main) {
                drawerAccount.text = UserInfoManager.account
                initDrawerImages()
                setupTabSelectionListener()
            }
            launch(Dispatchers.IO) {
                store.dispatch(UserAction.GetCurrentUserStats(this@MainActivity))
                store.dispatch(UserAction.GetCurrentUserProfile(this@MainActivity))
            }
            launch {
                initClickListeners()
                setupDrawerLayout()
            }
        }
        observeState()
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

    private fun initDrawerImages() {
        // 加载功能图标
        val imageIds = listOf(
            R.id.notification_img to R.drawable.notification2,
            R.id.privacy_img to R.drawable.yingsi,
            R.id.account_img to R.drawable.zhanghao,
            R.id.clear_cache_img to R.drawable.qingchu,
            R.id.student_id_img to R.drawable.bianji_,
            R.id.theme_img to R.drawable.zhuti_tiaosepan,
            R.id.message_img to R.drawable.xiaoxi,
            R.id.help_img to R.drawable.bangzhu,
            R.id.about_img to R.drawable.guanyuwomen,
            R.id.feedback_img to R.drawable.yijianfankui
        )

        // 批量加载功能图标
        imageIds.forEach { (viewId, drawableId) ->
            GlideUtils.load(this, findViewById(viewId), drawableId, false)
        }

        val arrowIds = listOf(
            R.id.notification_arrow,
            R.id.privacy_arrow,
            R.id.account_arrow,
            R.id.clear_cache_arrow,
            R.id.student_id_arrow,
            R.id.theme_arrow,
            R.id.message_arrow,
            R.id.help_arrow,
            R.id.about_arrow,
            R.id.feedback_arrow
        )

        // 批量加载箭头图标
        arrowIds.forEach { arrowId ->
            GlideUtils.load(
                this,
                findViewById(arrowId),
                R.drawable.baseline_arrow_forward_ios_24,
                false
            )
        }

    }

    private fun observeState() {
        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    drawerAccount.text = state.userProfile.account
                    GlideUtils.loadWithThumbnail(this, drawerAvatar, state.userProfile.avatarUrl)
                    drawerStuNumber.text = state.userStats.studentNumber
                }
        )
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
                is ChatGroupFragment -> 1
                is NewsFragment -> 2
                is IMFragment -> 3
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

    private fun setupDrawerLayout() {
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerStateChanged(newState: Int) {
                when (newState) {
                    DrawerLayout.STATE_DRAGGING, DrawerLayout.STATE_SETTLING -> {
                        // 抽屉开始移动时，冻结 Fragment 的布局更新
                        isDrawerAnimating = true
                        fragments[currentTabPosition]?.view?.let { fragmentView ->
                            fragmentView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            // 禁用布局更新
                            (fragmentView as? ViewGroup)?.layoutTransition = null
                        }
                    }

                    DrawerLayout.STATE_IDLE -> {
                        // 抽屉停止时，恢复 Fragment 的布局更新
                        isDrawerAnimating = false
                        fragments[currentTabPosition]?.view?.let { fragmentView ->
                            fragmentView.setLayerType(View.LAYER_TYPE_NONE, null)
                            // 恢复布局更新
                            (fragmentView as? ViewGroup)?.layoutTransition = LayoutTransition()
                        }
                    }
                }
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // 在滑动时禁用主内容区域的移动
                binding.main.translationX = 0f
            }
        })


    }

    private fun initClickListeners() {

        mainAvatarLinear.setOnClickListener {
            // 处理点击头像框后逻辑
            getNetPermissions()
            if (NetworkUtil.Companion.getNetworkType(this) != NetworkUtil.NetworkType.None) { //检查网络是否连接
                Route.goUserProfile(this@MainActivity)
            } else {
                CustomToast.Companion.showMessage(this, "网络未连接")
            }
        }
        drawerAvatar.setOnClickListener {
            getNetPermissions()
            if (NetworkUtil.Companion.getNetworkType(this) != NetworkUtil.NetworkType.None) { //检查网络是否连接
                Route.goUserProfile(this@MainActivity)
            } else {
                CustomToast.Companion.showMessage(this, "网络未连接")
            }
        }
        notificationSettings.setOnClickListener {
            // 处理通知设置点击
        }

        privacySettings.setOnClickListener {
            // 处理隐私设置点击
        }

        accountSecurity.setOnClickListener {
            // 处理账号安全点击
            Route.goAccountSecurity(this)
        }

        clearCache.setOnClickListener {
            NormalChosenDialog(
                this,
                "将清除实用工具的所有缓存",
                "确定要清除缓存嘛₍ᐢ.ˬ.⑅ᐢ₎",
                onConfirm = {
                    PlanetApplication.Companion.clearContentCache()
                }
            ).show()

        }

        changeStudentId.setOnClickListener {
            // 处理绑定学号点击
            Route.goBindingUser(this)
        }

        themeSettings.setOnClickListener {
            // 处理主题设置点击
        }

        messageCenter.setOnClickListener {
            // 处理消息中心点击
        }

        helpCenter.setOnClickListener {
            // 处理帮助中心点击
        }

        aboutUs.setOnClickListener {
            // 处理关于我们点击
        }

        feedback.setOnClickListener {
            // 处理意见反馈点击
        }

        logoutButton.setOnClickListener {
            NormalChosenDialog(
                this,
                "将清除本地所有缓存",
                "是否登出",
                onConfirm = {
                    PlanetApplication.Companion.clearCacheAll()
                    Route.goLoginForcibly(this@MainActivity)
                }
            ).show()
        }
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
        val lookTab = tabLayout.newTab().setIcon(R.drawable.nfind).setText(R.string.find)
        val postTab = tabLayout.newTab().setIcon(R.drawable.nnews).setText(R.string.news)
        val imTab = tabLayout.newTab().setIcon(R.drawable.nchat).setText(R.string.chat)
        tabLayout.addTab(featureTab)
        tabLayout.addTab(lookTab)
        tabLayout.addTab(postTab)
        tabLayout.addTab(imTab)
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
                        1 -> ChatGroupFragment.Companion.newInstance()
                        2 -> NewsFragment.Companion.newInstance()
                        3 -> IMFragment.Companion.newInstance()
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
        val startTime = System.currentTimeMillis()
        binding.drawerLayout.openDrawer(GravityCompat.START)
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                Log.d("DrawerPerformance", "Drawer opened in $duration ms")
                binding.drawerLayout.removeDrawerListener(this)
            }
        })
    }

    companion object {
        private const val REQUEST_READ_TELEPHONE = 1001
        private const val REQUEST_NOTIFICATION = 1002
    }
}