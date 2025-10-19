package com.creamaker.changli_planet_app.settings.ui

import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.common.cache.LocationDataSource
import com.creamaker.changli_planet_app.databinding.ActivityProvinceBinding
import com.creamaker.changli_planet_app.settings.ui.adapter.ProvinceAdapter
import com.creamaker.changli_planet_app.widget.view.DividerItemDecoration

/**
 * 个人主页设置选择省份
 */
class ProvinceActivity : FullScreenActivity<ActivityProvinceBinding>() {
    private val REQUEST_CITY = 1113


    private val recyclerView by lazy { binding.provinceRecycler }
    private val autoLocation by lazy { binding.autoLocation }
    private val back by lazy { binding.provinceBack }

    override fun createViewBinding(): ActivityProvinceBinding = ActivityProvinceBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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