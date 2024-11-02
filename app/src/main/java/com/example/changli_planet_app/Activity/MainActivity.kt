package com.example.changli_planet_app.Activity
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.changli_planet_app.Fragment.Feature
import com.example.changli_planet_app.Fragment.Find
import com.example.changli_planet_app.Fragment.IM
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val tabLayout : TabLayout by lazy { binding.tabLayout }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startTime = System.currentTimeMillis()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initFragment(Feature.newInstance())
        setupTabs()
        setupTabSelectionListener()
        val endTime = System.currentTimeMillis()
        Log.d("theTime","${startTime - endTime}")
    }
    private fun setupTabs() {
        // 动态添加 tabs
        val featureTab = tabLayout.newTab().setIcon(R.drawable.feature).setText(R.string.function)
        val lookTab = tabLayout.newTab().setIcon(R.drawable.search).setText(R.string.find)
        val postTab = tabLayout.newTab().setIcon(R.drawable.news).setText(R.string.news)
        val imTab = tabLayout.newTab().setIcon(R.drawable.chat).setText(R.string.chat)

        tabLayout.addTab(featureTab)
        tabLayout.addTab(lookTab)
        tabLayout.addTab(postTab)
        tabLayout.addTab(imTab)
    }
    private fun setupTabSelectionListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab != null) {
                    animateTabSelect(tab)
                }
                when (tab.position) {
                    0 -> initFragment(Feature.newInstance())  // feature tab
                    1 -> initFragment(Find.newInstance())     // look tab
                    2 -> initFragment(Feature.newInstance())     // post tab
                    3 -> initFragment(IM.newInstance())        // im tab
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

    private fun initFragment(fragment: Fragment){
        val fragmentationTemp = supportFragmentManager
        val transactions = fragmentationTemp.beginTransaction()
        transactions.replace(R.id.frag,fragment).commit()
    }
    fun animateTabSelect(tab : Tab){
        val tabView = tab.view
        val animatorIn = ObjectAnimator.ofFloat(tabView,"scaleX",1f,0.8f,1f)
        val animatorOn = ObjectAnimator.ofFloat(tabView,"scaleY",1f,0.8f,1f)
        animatorIn.duration = 200
        animatorOn.duration = 200
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animatorIn,animatorOn)
        animatorSet.start()
    }
}