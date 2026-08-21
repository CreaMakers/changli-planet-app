package com.creamaker.changli_planet_app.migration

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme

class DataMigrationActivity : ComponentActivity() {
    private val viewModel: DataMigrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSkinTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                MigrationScreen(
                    state = uiState,
                    onBack = ::finish,
                    onPrimaryAction = ::handlePrimaryAction
                )
            }
        }
        configure(packageName, intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configure(packageName, intent)
    }

    private fun configure(currentPackage: String, intent: Intent) {
        when (currentPackage) {
            MigrationContract.SOURCE_PACKAGE -> viewModel.showSource()
            MigrationContract.TARGET_PACKAGE -> configureTarget(intent)
            else -> viewModel.showUnsupported()
        }
    }

    private fun configureTarget(intent: Intent) {
        val migrationId = intent.getStringExtra(MigrationContract.EXTRA_MIGRATION_ID)
        val nonce = intent.getStringExtra(MigrationContract.EXTRA_NONCE)
        if (migrationId.isNullOrBlank() || nonce.isNullOrBlank()) {
            viewModel.showSourceRequired()
            return
        }
        viewModel.importData(migrationId, nonce)
    }

    private fun handlePrimaryAction() {
        when (viewModel.uiState.value.action) {
            MigrationAction.START -> startSourceMigration()
            MigrationAction.CHECK -> viewModel.checkReceipt()
            MigrationAction.OPEN_SOURCE -> openSourceApp()
            MigrationAction.FINISH -> finish()
            MigrationAction.NONE -> Unit
        }
    }

    private fun startSourceMigration() {
        val request = viewModel.prepareSourceMigration() ?: return
        val targetIntent = Intent(MigrationContract.ACTION_DATA_MIGRATION).apply {
            setPackage(MigrationContract.TARGET_PACKAGE)
            putExtra(MigrationContract.EXTRA_MIGRATION_ID, request.migrationId)
            putExtra(MigrationContract.EXTRA_NONCE, request.nonce)
        }
        runCatching { startActivity(targetIntent) }
            .onSuccess { viewModel.onTargetOpened() }
            .onFailure { viewModel.onTargetOpenFailed() }
    }

    private fun openSourceApp() {
        val sourceIntent = packageManager.getLaunchIntentForPackage(MigrationContract.SOURCE_PACKAGE)
        if (sourceIntent == null) {
            viewModel.showSourceMissing()
        } else {
            startActivity(sourceIntent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MigrationScreen(
    state: MigrationUiState,
    onBack: () -> Unit,
    onPrimaryAction: () -> Unit
) {
    Scaffold(
        containerColor = AppTheme.colors.bgPrimaryColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "数据迁移",
                        color = AppTheme.colors.titleTopColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "返回",
                            tint = AppTheme.colors.titleTopColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.bgTopBarColor
                )
            )
        },
        bottomBar = {
            if (state.action != MigrationAction.NONE) {
                Surface(
                    color = AppTheme.colors.bgCardColor,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = onPrimaryAction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.commonColor,
                            contentColor = AppTheme.colors.textButtonColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .height(52.dp)
                    ) {
                        Text(
                            text = state.actionLabel,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MigrationHero(state.stage)
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = state.headline,
                color = AppTheme.colors.primaryTextColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = state.description,
                color = AppTheme.colors.greyTextColor,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            state.importedCounts?.let { counts ->
                ImportedSummary(counts)
                Spacer(modifier = Modifier.height(16.dp))
            }
            MigrationDataCard()
            Spacer(modifier = Modifier.height(16.dp))
            PrivacyCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MigrationHero(stage: MigrationStage) {
    val accentColor = when (stage) {
        MigrationStage.SUCCESS -> AppTheme.colors.successGreenColor
        MigrationStage.ERROR -> AppTheme.colors.errorRedColor
        else -> AppTheme.colors.commonColor
    }
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = accentColor.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (stage) {
                MigrationStage.PROCESSING -> CircularProgressIndicator(
                    color = accentColor,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(54.dp)
                )
                MigrationStage.SUCCESS -> HeroStatusIcon(Icons.Rounded.CheckCircle, accentColor)
                MigrationStage.ERROR -> HeroStatusIcon(Icons.Rounded.ErrorOutline, accentColor)
                MigrationStage.WAITING -> HeroStatusIcon(Icons.Rounded.HourglassTop, accentColor)
                MigrationStage.INFO -> HeroStatusIcon(Icons.Rounded.Info, accentColor)
                MigrationStage.READY -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        VersionBadge("旧版", accentColor)
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier
                                .padding(horizontal = 14.dp)
                                .size(34.dp)
                        )
                        VersionBadge("新版", accentColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionBadge(label: String, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(68.dp)
            .background(color.copy(alpha = 0.16f), CircleShape)
    ) {
        Text(label, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HeroStatusIcon(icon: ImageVector, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(76.dp)
            .background(color.copy(alpha = 0.14f), CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(42.dp)
        )
    }
}

@Composable
private fun MigrationDataCard() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppTheme.colors.bgCardColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            Text(
                text = "本次迁移",
                color = AppTheme.colors.primaryTextColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            MigrationDataRow(Icons.Rounded.CalendarMonth, "手工课表", "自定义课程与上课安排")
            HorizontalDivider(
                modifier = Modifier.padding(start = 52.dp),
                color = AppTheme.colors.dividerColor.copy(alpha = 0.22f)
            )
            MigrationDataRow(Icons.Rounded.AccountBalanceWallet, "本地账本", "账本条目与统计数据")
            HorizontalDivider(
                modifier = Modifier.padding(start = 52.dp),
                color = AppTheme.colors.dividerColor.copy(alpha = 0.22f)
            )
            MigrationDataRow(Icons.Rounded.Palette, "主题偏好", "当前使用的应用主题")
        }
    }
}

@Composable
private fun MigrationDataRow(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(AppTheme.colors.commonColor.copy(alpha = 0.11f), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.commonColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = title,
                color = AppTheme.colors.primaryTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = AppTheme.colors.greyTextColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun PrivacyCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.bgButtonLowlightColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = AppTheme.colors.functionalTextColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "隐私与安全",
                    color = AppTheme.colors.functionalTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "数据仅在本机的两个应用之间传输。密码、登录凭证、Cookie 和缓存不会迁移，新版需要重新登录。",
                    color = AppTheme.colors.primaryTextColor,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun ImportedSummary(counts: ImportedCounts) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AppTheme.colors.successGreenColor.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CountItem(counts.courses, "课表")
            CountItem(counts.ledgerItems, "账本")
            CountItem(counts.preferences, "偏好")
        }
    }
}

@Composable
private fun CountItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            color = AppTheme.colors.successGreenColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = AppTheme.colors.greyTextColor,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}
