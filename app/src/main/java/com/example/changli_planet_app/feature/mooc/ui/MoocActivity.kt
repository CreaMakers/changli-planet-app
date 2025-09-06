package com.example.changli_planet_app.feature.mooc.ui

import androidx.lifecycle.ViewModelProvider
import com.example.changli_planet_app.base.BaseActivity
import com.example.changli_planet_app.databinding.ActivityMoocBinding
import com.example.changli_planet_app.feature.mooc.viewmodel.MoocViewModel

class MoocActivity : BaseActivity<ActivityMoocBinding>() {

    private val moocViewModel: MoocViewModel by lazy { ViewModelProvider(this)[MoocViewModel::class.java] }

    override fun createViewBinding(): ActivityMoocBinding {
        return ActivityMoocBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
    }
}