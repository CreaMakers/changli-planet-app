package com.creamaker.changli_planet_app.settings.ui

import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.common.cache.LocationDataSource
import com.creamaker.changli_planet_app.databinding.ActivityCityBinding
import com.creamaker.changli_planet_app.settings.ui.adapter.CityAdapter
import com.creamaker.changli_planet_app.widget.view.DividerItemDecoration

/**
 * 个人主页设置城市选择Activity
 */
class CityActivity : FullScreenActivity<ActivityCityBinding>() {
    private val recyclerView by lazy { binding.cityRecycler }
    private val back by lazy { binding.cityBack }
    private val province by lazy { intent.getStringExtra("province") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    override fun createViewBinding(): ActivityCityBinding = ActivityCityBinding.inflate(layoutInflater)

    private fun initView() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar){ view, windowInsets->
            val insets=windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )
            WindowInsetsCompat.CONSUMED
        }
        recyclerView.adapter = CityAdapter(LocationDataSource.getCity(province!!), ::clickItem)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration())
        back.setOnClickListener { finish() }
    }

    private fun clickItem(city: String) {
        val resultIntent = Intent().apply {
            putExtra("province", province)
            putExtra("city", city)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}