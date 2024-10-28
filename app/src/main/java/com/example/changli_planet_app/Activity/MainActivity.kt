package com.example.changli_planet_app.Activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TableLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.changli_planet_app.Fragment.Feature
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import java.sql.Time
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private val tabLayout : TabLayout by lazy { binding.tabLayout }
    private var startTime:Long = 0
    private var endTime:Long= 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTime = System.currentTimeMillis()
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initFragment(Feature.newInstance())
        //为导航栏设置动画和颜色
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(p0: TabLayout.Tab?) {
                if (p0 != null) {
                    animateTabSelect(p0)
                }
            }
            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }
        })
        endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        Log.d("MyUI","CreatTime:${duration}")
    }
    private fun initFragment(fragment: Fragment){
        val fragmentationTemp = supportFragmentManager
        val transactions = fragmentationTemp.beginTransaction()
        transactions.replace(R.id.frag,fragment).commit()
    }
    fun animateTabSelect(tab : TabLayout.Tab){
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