package com.creamaker.changli_planet_app.feature.common.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.common.compose_ui.FunctionSection
import com.creamaker.changli_planet_app.feature.common.compose_ui.primaryFunctionShortcuts
import com.creamaker.changli_planet_app.feature.common.compose_ui.secondaryFunctionShortcuts
import com.creamaker.changli_planet_app.feature.common.compose_ui.toFunctionItemData

class FeatureFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance() = FeatureFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppSkinTheme {
                    FeatureScreen()
                }
            }
        }
    }
}

@Composable
fun FeatureScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val mainFunctionItems = remember {
        primaryFunctionShortcuts().map { it.toFunctionItemData(context) }
    }
    val otherFunctionItems = remember {
        secondaryFunctionShortcuts().map { it.toFunctionItemData(context) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.bgPrimaryColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FeatureHeaderSection(avatarUrl = "https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                FunctionSection(
                    title = "常用功能",
                    items = mainFunctionItems,
                )
                Spacer(modifier = Modifier.height(16.dp))
                FunctionSection(
                    title = "其他",
                    items = otherFunctionItems
                )
            }
        }
    }
}

@Composable
private fun FeatureHeaderSection(avatarUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .statusBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.planet_logo),
            contentDescription = "背景图片",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FeatureScreenPreview() {
    MaterialTheme {
        FeatureScreen()
    }
}
