package com.creamaker.changli_planet_app.feature.common.ui

import androidx.activity.viewModels
import com.creamaker.changli_planet_app.base.ComposeActivity
import com.creamaker.changli_planet_app.feature.common.compose_ui.ElectronicScreen
import com.creamaker.changli_planet_app.feature.common.viewModel.ElectronicViewModel

/**
 * 电费查询
 */
class ElectronicActivity : ComposeActivity() {

    private val viewModel: ElectronicViewModel by viewModels()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setComposeContent {
            ElectronicScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}
