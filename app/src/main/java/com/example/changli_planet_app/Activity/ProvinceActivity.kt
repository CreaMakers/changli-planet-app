package com.example.changli_planet_app.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.changli_planet_app.Adapter.ProvinceAdapter
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Data.model.LocationDataSource
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Widget.View.DividerItemDecoration
import com.example.changli_planet_app.databinding.ActivityProvinceBinding

class ProvinceActivity : FullScreenActivity() {
    private lateinit var binding: ActivityProvinceBinding
    private val REQUEST_CITY = 1113


    private val recyclerView by lazy { binding.provinceRecycler }
    private val autoLocation by lazy { binding.autoLocation }
    private val back by lazy { binding.provinceBack }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProvinceBinding.inflate(layoutInflater)
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
        recyclerView.adapter =
            ProvinceAdapter(LocationDataSource.getProvinceList(), ::goCityActivity)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration())
        back.setOnClickListener {
            finish()
        }
    }

    private fun goCityActivity(province: String) {
        val intent = Intent(this@ProvinceActivity, CityActivity::class.java)
        intent.putExtra("province", province)
        startActivityForResult(intent, REQUEST_CITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CITY && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data)
            finish()
        }
    }
}