package com.example.changli_planet_app.Settings.ui

import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.changli_planet_app.Base.FullScreenActivity
import com.example.changli_planet_app.Data.model.LocationDataSource
import com.example.changli_planet_app.Settings.ui.adapter.CityAdapter
import com.example.changli_planet_app.Widget.View.DividerItemDecoration
import com.example.changli_planet_app.databinding.ActivityCityBinding

/**
 * 个人主页设置城市选择Activity
 */
class CityActivity : FullScreenActivity() {
    private lateinit var binding: ActivityCityBinding
    private val recyclerView by lazy { binding.cityRecycler }
    private val back by lazy { binding.cityBack }
    private val province by lazy { intent.getStringExtra("province") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        binding = ActivityCityBinding.inflate(layoutInflater)
        setContentView(binding.root)
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