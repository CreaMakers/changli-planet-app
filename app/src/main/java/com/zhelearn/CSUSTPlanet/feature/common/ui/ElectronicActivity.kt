package com.zhelearn.CSUSTPlanet.feature.common.ui

import androidx.activity.viewModels
import com.zhelearn.CSUSTPlanet.base.ComposeActivity
import com.zhelearn.CSUSTPlanet.feature.common.compose_ui.ElectronicScreen
import com.zhelearn.CSUSTPlanet.feature.common.viewModel.ElectronicViewModel

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
