package com.example.changli_planet_app.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Core.FullScreenActivity
import androidx.fragment.app.Fragment
import com.example.changli_planet_app.Fragment.FoundThingFragment
import com.example.changli_planet_app.Fragment.LoseThingFragment
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityLoseBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab

class  LoseActivity : FullScreenActivity() {
    private lateinit var binding: ActivityLoseBinding
    private val tabLayout: TabLayout by lazy {binding.mytab}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityLoseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initFragment(LoseThingFragment.newInstance())             //默认打开失物招领
        setupTabSelectionListener()
    }
    private fun setupTabSelectionListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> { initFragment(LoseThingFragment.newInstance())}            // loseThing tab
                    1 -> { initFragment(FoundThingFragment.newInstance())}           // foundThing tab
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
        transactions.replace(R.id.lose_FrameLayout,fragment).commit()
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