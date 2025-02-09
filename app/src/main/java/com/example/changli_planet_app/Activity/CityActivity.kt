package com.example.changli_planet_app.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.changli_planet_app.Adapter.CityAdapter
import com.example.changli_planet_app.Data.model.LocationDataSource
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Widget.View.DividerItemDecoration
import com.example.changli_planet_app.databinding.ActivityCityBinding

class CityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCityBinding
    private val recyclerView by lazy { binding.cityRecycler }
    private val province by lazy { intent.getStringExtra("province") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView.adapter = CityAdapter(LocationDataSource.getCity(province!!), ::clickItem)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration())
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