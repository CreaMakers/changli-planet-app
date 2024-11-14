package com.example.changli_planet_app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.changli_planet_app.Fragment.FeatureFragment
import com.example.changli_planet_app.Fragment.FindFragment
import com.example.changli_planet_app.Fragment.IMFragment
import com.example.changli_planet_app.PlanetApplication
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        PlanetApplication.startTime = System.currentTimeMillis()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initFragment(FeatureFragment.newInstance())
        setupTabs()
        setupTabSelectionListener()
    }
    override fun onStart() {
        super.onStart()
        binding.trace.text =  (System.currentTimeMillis() - PlanetApplication.startTime).toString()
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
                    1 -> initFragment(FindFragment.newInstance())     // look tab
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
    private fun initFragment(fragment: Fragment){
        val fragmentationTemp = supportFragmentManager
        val transactions = fragmentationTemp.beginTransaction()
        transactions.replace(R.id.frag,fragment).commit()
    }
    fun animateTabSelect(tab : Tab){
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