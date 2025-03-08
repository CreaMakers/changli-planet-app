package com.example.changli_planet_app.Activity

import android.content.res.Configuration
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityCampusMapBinding

class CampusMapActivity : FullScreenActivity() {
    private lateinit var binding: ActivityCampusMapBinding
    private var currentMapIndex = 0
    private val mapResources = listOf(
        R.drawable.jincun_map,
        R.drawable.yuntang_map
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCampusMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMapView()
        setupMapSelector()
    }

    private fun setupMapView() {
        binding.mapView.apply {
            maxScale = 6f
            minScale = 0.5f
            setDoubleTapZoomScale(2f)
            setImage(ImageSource.resource(mapResources[currentMapIndex]))
            setScaleAndCenter(1f, PointF(0f, 0f))
        }
    }

    private fun setupMapSelector() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("金盆岭校区", "云塘校区")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.mapSelector.apply {
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position != currentMapIndex) {
                        currentMapIndex = position
                        loadNewMap()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }


    override fun onDestroy() {
        binding.mapView.recycle()
        super.onDestroy()
    }

    private fun loadNewMap() {
        val center = binding.mapView.center
        val scale = binding.mapView.scale

        binding.mapView.setImage(ImageSource.resource(mapResources[currentMapIndex]))

        if (center != null) {
            binding.mapView.setScaleAndCenter(scale, center)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.mapView.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
    }
}