package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Fragment.FeatureFragment
import com.example.changli_planet_app.Fragment.NewsFragment
import com.example.changli_planet_app.Fragment.IMFragment
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Fragment.ChatGroupFragment
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    private val tabLayout: TabLayout by lazy { binding.tabLayout }

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        PlanetApplication.startTime = System.currentTimeMillis()
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        initFragment(FeatureFragment.newInstance())
        setupTabs()
        setupTabSelectionListener()
        initClickListeners()
    }

    private fun initClickListeners() {
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
            // 处理清除缓存点击
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
            UserInfoManager.clear()
            Route.goLogin(this@MainActivity)
            finish()
        }
    }


    override fun onStart() {
        super.onStart()
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

    private fun setupTabSelectionListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> initFragment(FeatureFragment.newInstance())  // feature tab
                    1 -> initFragment(ChatGroupFragment.newInstance())     // look tab
                    2 -> initFragment(FeatureFragment.newInstance())     // post tab
                    3 -> initFragment(IMFragment.newInstance())        // im tab
                }
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

    private fun initFragment(fragment: Fragment) {
        val fragmentationTemp = supportFragmentManager
        val transactions = fragmentationTemp.beginTransaction()
        transactions.replace(R.id.frag, fragment).commit()
    }

    fun animateTabSelect(tab: Tab) {
        val tabView = tab.view
        tabView.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(200)
            .withEndAction {
                tabView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }
}