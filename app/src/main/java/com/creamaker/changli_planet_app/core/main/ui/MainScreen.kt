package com.creamaker.changli_planet_app.core.main.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.main.navigation.MainTabDestination
import com.creamaker.changli_planet_app.core.main.navigation.MainTabNavigator
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.common.compose_ui.FunctionDestination
import com.creamaker.changli_planet_app.feature.common.compose_ui.openFunctionShortcut
import com.creamaker.changli_planet_app.feature.common.compose_ui.primaryFunctionShortcuts
import com.creamaker.changli_planet_app.feature.common.ui.FeatureScreen
import com.creamaker.changli_planet_app.im.ui.UnderConstructionScreen
import com.creamaker.changli_planet_app.overview.ui.compose.OverviewScreen
import com.creamaker.changli_planet_app.overview.viewmodel.OverviewViewModel
import com.creamaker.changli_planet_app.profileSettings.ui.compose.ProfileSettingsRoute
import com.creamaker.changli_planet_app.widget.view.rememberFloatingTabBarScrollConnection
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun MainScreen(
    navigator: MainTabNavigator,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollConnection = rememberFloatingTabBarScrollConnection()
    val hazeState = rememberHazeState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppTheme.colors.bgPrimaryColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            NavDisplay(
                backStack = navigator.displayBackStack,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollConnection)
                    .hazeSource(hazeState),
                onBack = { context.findActivity()?.finish() },
                transitionSpec = {
                    EnterTransition.None togetherWith ExitTransition.None
                },
                popTransitionSpec = {
                    EnterTransition.None togetherWith ExitTransition.None
                },
                entryProvider = entryProvider {
                    entry<MainTabDestination.Overview> {
                        OverviewTabRoute()
                    }
                    entry<MainTabDestination.Feature> {
                        FeatureScreen()
                    }
                    entry<MainTabDestination.News> {
                        UnderConstructionScreen()
                    }
                    entry<MainTabDestination.Profile> {
                        ProfileSettingsRoute()
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                MainBottomBar(
                    selectedDestination = navigator.currentDestination,
                    onDestinationSelected = navigator::select,
                    scrollConnection = scrollConnection,
                    hazeState = hazeState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun OverviewTabRoute() {
    val context = LocalContext.current
    val viewModel: OverviewViewModel = viewModel()

    OverviewScreen(
        viewModel = viewModel,
        onBindClick = { Route.goBindingUser(context) },
        onQuickActionClick = { actionId ->
            val shortcut = primaryFunctionShortcuts().firstOrNull { it.id == actionId }
            when {
                shortcut != null -> openFunctionShortcut(context, shortcut.destination)
                actionId == FunctionDestination.ScoreInquiry.name -> Route.goScoreInquiry(context)
                actionId == FunctionDestination.Electronic.name -> Route.goElectronic(context)
            }
        }
    )
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
