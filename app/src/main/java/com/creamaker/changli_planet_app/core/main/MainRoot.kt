package com.creamaker.changli_planet_app.core.main

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.window.layout.WindowMetricsCalculator
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.common.ui.FeatureScreen
import com.creamaker.changli_planet_app.freshNews.contract.FreshNewsContract
import com.creamaker.changli_planet_app.freshNews.ui.CommentsActivity
import com.creamaker.changli_planet_app.freshNews.ui.UserHomeActivity
import com.creamaker.changli_planet_app.freshNews.ui.compose.FreshNewsScreen
import com.creamaker.changli_planet_app.freshNews.viewModel.FreshNewsViewModel
import com.creamaker.changli_planet_app.im.ui.compose.IMScreen
import com.creamaker.changli_planet_app.profileSettings.ui.compose.ProfileSettingsRoute
import com.creamaker.changli_planet_app.utils.PlanetConst
import com.creamaker.changli_planet_app.widget.dialog.GuestLimitedAccessDialog
import com.creamaker.changli_planet_app.widget.dialog.ImageSliderDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@Immutable
sealed interface MainDestination {
    val tabIndex: Int

    data object Feature : MainDestination {
        override val tabIndex: Int = 0
    }

    data object News : MainDestination {
        override val tabIndex: Int = 1
    }

    data object IM : MainDestination {
        override val tabIndex: Int = 2
    }

    data object Profile : MainDestination {
        override val tabIndex: Int = 3
    }

    companion object {
        val topLevel = listOf(Feature, News, IM, Profile)

        fun fromTabIndex(index: Int): MainDestination = when (index) {
            0 -> Feature
            1 -> News
            2 -> IM
            3 -> Profile
            else -> Feature
        }
    }
}

@Composable
fun MainRoot(
    onExit: () -> Unit,
    onNavigatorReady: ((MainDestination) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val navigationState = rememberMainNavigationState()
    val currentRoute = navigationState.currentTopLevel
    val latestOnNavigatorReady by rememberUpdatedState(onNavigatorReady)
    val isExpanded = rememberIsExpandedMainLayout()
    val selectTopLevel: (MainDestination) -> Unit = remember(context, navigationState) {
        { destination ->
            if (PlanetApplication.isExpired && destination == MainDestination.IM) {
                GuestLimitedAccessDialog(context).show()
            } else {
                navigationState.selectTopLevel(destination)
            }
        }
    }

    LaunchedEffect(navigationState) {
        latestOnNavigatorReady(selectTopLevel)
    }

    BackHandler {
        if (!navigationState.handleBack()) {
            onExit()
        }
    }

    MainShell(
        currentRoute = currentRoute,
        isExpanded = isExpanded,
        onRouteSelected = selectTopLevel,
    ) { hostModifier ->
        MainNavHost(
            navigationState = navigationState,
            modifier = hostModifier
        )
    }
}

@Composable
private fun MainShell(
    currentRoute: MainDestination,
    isExpanded: Boolean,
    onRouteSelected: (MainDestination) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.bgSecondaryColor)
    ) {
        val hostModifier = Modifier.fillMaxSize()

        if (isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            ) {
                MainRail(
                    currentRoute = currentRoute,
                    onRouteSelected = onRouteSelected
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopStart
                ) {
                    content(hostModifier)
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = AppTheme.colors.bgSecondaryColor,
                bottomBar = {
                    MainBottomBar(
                        currentRoute = currentRoute,
                        onRouteSelected = onRouteSelected
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopStart
                ) {
                    content(hostModifier)
                }
            }
        }
    }
}

@Composable
private fun MainNavHost(
    navigationState: MainNavigationState,
    modifier: Modifier = Modifier
) {
    val backStack = navigationState.currentBackStack

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { navigationState.handleBack() },
        entryProvider = { key ->
            NavEntry(key) {
                when (key) {
                    MainDestination.Feature -> FeatureScreen()
                    MainDestination.News -> MainNewsRoute()
                    MainDestination.IM -> IMScreen()
                    MainDestination.Profile -> ProfileSettingsRoute()
                }
            }
        }
    )
}

@Composable
private fun MainBottomBar(
    currentRoute: MainDestination,
    onRouteSelected: (MainDestination) -> Unit
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = AppTheme.colors.bgPrimaryColor
    ) {
        MainDestination.topLevel.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination,
                onClick = { onRouteSelected(destination) },
                icon = {
                    Image(
                        painter = painterResource(id = destination.iconRes()),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(text = stringResource(id = destination.labelRes())) }
            )
        }
    }
}

@Composable
private fun MainRail(
    currentRoute: MainDestination,
    onRouteSelected: (MainDestination) -> Unit
) {
    NavigationRail(
        modifier = Modifier
            .width(92.dp)
            .fillMaxSize(),
        containerColor = AppTheme.colors.bgPrimaryColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MainDestination.topLevel.forEach { destination ->
                NavigationRailItem(
                    selected = currentRoute == destination,
                    onClick = { onRouteSelected(destination) },
                    icon = {
                        Image(
                            painter = painterResource(id = destination.iconRes()),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(text = stringResource(id = destination.labelRes())) }
                )
            }
        }
    }
}

@Composable
private fun MainNewsRoute(
    viewModel: FreshNewsViewModel = viewModel()
) {
    val context = LocalContext.current
    val commentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                PlanetConst.RESULT_OK -> {
                    result.data?.let { data ->
                        val newsId = data.getIntExtra("freshNewsId", -1)
                        val newCommentCount = data.getIntExtra("newLevel1CommentsCount", -1)
                        if (newsId != -1 && newCommentCount != -1) {
                            viewModel.processIntent(
                                FreshNewsContract.Intent.UpdateLocalCommentCount(
                                    newsId = newsId,
                                    count = newCommentCount
                                )
                            )
                        }
                    }
                }

                PlanetConst.RESULT_OK_NEWS_REFRESH -> {
                    result.data?.let { data ->
                        val account = data.getStringExtra("account")
                        val avatarUrl = data.getStringExtra("avatarUrl")
                        val userId = data.getIntExtra("userId", -1)
                        if (userId != -1 && account != null && avatarUrl != null) {
                            viewModel.processIntent(
                                FreshNewsContract.Intent.UpdateLocalUserInfo(
                                    userId = userId,
                                    name = account,
                                    avatar = avatarUrl
                                )
                            )
                        }
                    }
                }
            }
        }
    val userLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                PlanetConst.RESULT_OK -> {
                    result.data?.let { data ->
                        val newsId = data.getIntExtra("freshNewsId", -1)
                        val newCommentCount = data.getIntExtra("newLevel1CommentsCount", -1)
                        if (newsId != -1 && newCommentCount != -1) {
                            viewModel.processIntent(
                                FreshNewsContract.Intent.UpdateLocalCommentCount(
                                    newsId = newsId,
                                    count = newCommentCount
                                )
                            )
                        }
                    }
                }

                PlanetConst.RESULT_OK_NEWS_REFRESH -> {
                    result.data?.let { data ->
                        val account = data.getStringExtra("account")
                        val avatarUrl = data.getStringExtra("avatarUrl")
                        val userId = data.getIntExtra("userId", -1)
                        if (userId != -1 && account != null && avatarUrl != null) {
                            viewModel.processIntent(
                                FreshNewsContract.Intent.UpdateLocalUserInfo(
                                    userId = userId,
                                    name = account,
                                    avatar = avatarUrl
                                )
                            )
                        }
                    }
                }
            }
        }

    LaunchedEffect(Unit) {
        viewModel.processIntent(FreshNewsContract.Intent.RefreshNewsByTime(1, 10))
    }

    DisposableEffect(viewModel, context, commentLauncher) {
        val subscriber = MainNewsEventSubscriber(
            onOpenComments = {
                commentLauncher.launch(Intent(context, CommentsActivity::class.java))
            },
            onRefreshNews = {
                viewModel.processIntent(FreshNewsContract.Intent.RefreshNewsByTime(1, 10))
            }
        )
        EventBus.getDefault().register(subscriber)
        onDispose {
            EventBus.getDefault().unregister(subscriber)
        }
    }

    FreshNewsScreen(
        viewModel = viewModel,
        onImageClick = { imageList, position ->
            ImageSliderDialog(context, imageList, position).show()
        },
        onUserClick = { userId ->
            userLauncher.launch(
                Intent(context, UserHomeActivity::class.java).apply {
                    putExtra("userId", userId)
                }
            )
        }
    )
}

private class MainNewsEventSubscriber(
    private val onOpenComments: () -> Unit,
    private val onRefreshNews: () -> Unit
) {
    @Subscribe
    fun openComments(event: FreshNewsContract.Event) {
        if (event is FreshNewsContract.Event.openComments) {
            onOpenComments()
        }
    }

    @Subscribe
    fun refreshNews(event: FreshNewsContract.Event) {
        if (event is FreshNewsContract.Event.RefreshNewsList) {
            onRefreshNews()
        }
    }
}

@Stable
private class MainNavigationState(
    startDestination: MainDestination
) {
    private val backStacks = mutableStateMapOf(
        MainDestination.Feature to mutableStateListOf<MainDestination>(MainDestination.Feature),
        MainDestination.News to mutableStateListOf<MainDestination>(MainDestination.News),
        MainDestination.IM to mutableStateListOf<MainDestination>(MainDestination.IM),
        MainDestination.Profile to mutableStateListOf<MainDestination>(MainDestination.Profile)
    )

    var currentTopLevel by mutableStateOf(startDestination)
        private set

    val currentBackStack
        get() = backStacks.getValue(currentTopLevel)

    fun selectTopLevel(destination: MainDestination) {
        currentTopLevel = destination
        val stack = backStacks.getValue(destination)
        if (stack.isEmpty()) {
            stack += destination
        }
    }

    fun handleBack(): Boolean {
        return if (currentTopLevel != MainDestination.Feature) {
            currentTopLevel = MainDestination.Feature
            true
        } else {
            false
        }
    }
}

@Composable
private fun rememberMainNavigationState(): MainNavigationState {
    var currentTabIndex by rememberSaveable { mutableStateOf(MainDestination.Feature.tabIndex) }
    val state = remember { MainNavigationState(MainDestination.fromTabIndex(currentTabIndex)) }

    LaunchedEffect(currentTabIndex) {
        state.selectTopLevel(MainDestination.fromTabIndex(currentTabIndex))
    }
    LaunchedEffect(state.currentTopLevel) {
        currentTabIndex = state.currentTopLevel.tabIndex
    }

    return state
}

@Composable
private fun rememberIsExpandedMainLayout(): Boolean {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val activity = context as? Activity ?: return false
    val realDensity = Resources.getSystem().displayMetrics.density
    val widthPx = remember(configuration, activity) {
        WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(activity)
            .bounds
            .width()
    }
    return widthPx / realDensity >= 840f
}

private fun MainDestination.iconRes(): Int = when (this) {
    MainDestination.Feature -> R.drawable.nfeature
    MainDestination.News -> R.drawable.nnews
    MainDestination.IM -> R.drawable.nchat
    MainDestination.Profile -> R.drawable.nprofile
}

private fun MainDestination.labelRes(): Int = when (this) {
    MainDestination.Feature -> R.string.function
    MainDestination.News -> R.string.news
    MainDestination.IM -> R.string.chat
    MainDestination.Profile -> R.string.profile_home
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun MainShellCompactPreview() {
    com.creamaker.changli_planet_app.core.theme.AppSkinTheme {
        MainShell(
            currentRoute = MainDestination.Feature,
            isExpanded = false,
            onRouteSelected = {}
        ) { modifier ->
            Surface(
                modifier = modifier,
                color = AppTheme.colors.bgPrimaryColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "Compact Main Preview", color = AppTheme.colors.primaryTextColor)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun MainShellExpandedPreview() {
    com.creamaker.changli_planet_app.core.theme.AppSkinTheme {
        MainShell(
            currentRoute = MainDestination.News,
            isExpanded = true,
            onRouteSelected = {}
        ) { modifier ->
            Surface(
                modifier = modifier,
                color = AppTheme.colors.bgPrimaryColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "Expanded Main Preview", color = AppTheme.colors.primaryTextColor)
                }
            }
        }
    }
}
