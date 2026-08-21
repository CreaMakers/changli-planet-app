package com.zhelearn.CSUSTPlanet.migration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataMigrationViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val _uiState = MutableStateFlow(MigrationUiState())
    val uiState = _uiState.asStateFlow()
    private var importJob: Job? = null
    private var sourceConfigured = false
    private var activeImportMigrationId: String? = null

    var activeMigrationId: String? = null
        private set

    fun showSource() {
        if (sourceConfigured) return
        sourceConfigured = true
        _uiState.value = MigrationUiState(
            headline = "把重要数据带到新版",
            description = "安装新版长理星球后，可在本机安全迁移数据。整个过程不会经过公共存储。",
            stage = MigrationStage.READY,
            action = MigrationAction.START,
            actionLabel = "开始迁移"
        )
    }

    fun showUnsupported() {
        _uiState.value = MigrationUiState(
            headline = "当前版本暂不支持迁移",
            description = "请确认安装的是长理星球官方版本。",
            stage = MigrationStage.ERROR
        )
    }

    fun showSourceRequired() {
        _uiState.value = MigrationUiState(
            headline = "请从旧版发起迁移",
            description = "打开旧版长理星球，在设置中选择“数据迁移”，新版会自动继续。",
            stage = MigrationStage.INFO,
            action = MigrationAction.OPEN_SOURCE,
            actionLabel = "打开旧版长理星球"
        )
    }

    fun prepareSourceMigration(): IntentRequest? {
        if (!MigrationSecurity.isInstalledWithMatchingSignature(appContext, MigrationContract.TARGET_PACKAGE)) {
            _uiState.value = _uiState.value.copy(
                headline = "还没有找到新版",
                description = "请先从可信渠道安装新版长理星球，再回到这里重试。",
                stage = MigrationStage.ERROR,
                action = MigrationAction.START,
                actionLabel = "重新检查"
            )
            return null
        }
        val session = MigrationSessionStore(appContext).create()
        activeMigrationId = session.migrationId
        return IntentRequest(session.migrationId, session.nonce)
    }

    fun onTargetOpened() {
        _uiState.value = _uiState.value.copy(
            headline = "已交给新版处理",
            description = "请在新版完成迁移。旧版数据会继续保留，确认无误后再卸载旧版。",
            stage = MigrationStage.WAITING,
            action = MigrationAction.CHECK,
            actionLabel = "检查迁移结果"
        )
    }

    fun onTargetOpenFailed() {
        _uiState.value = _uiState.value.copy(
            headline = "无法打开新版",
            description = "请确认新版已正确安装，然后重新尝试。",
            stage = MigrationStage.ERROR,
            action = MigrationAction.START,
            actionLabel = "重新尝试"
        )
    }

    fun checkReceipt() {
        val migrationId = activeMigrationId ?: return
        _uiState.value = when (MigrationSessionStore(appContext).receiptStatus(migrationId)) {
            MigrationContract.STATUS_SUCCESS -> _uiState.value.copy(
                headline = "迁移已经完成",
                description = "请在新版检查课表和账本。确认无误后，可以卸载旧版。",
                stage = MigrationStage.SUCCESS,
                action = MigrationAction.FINISH,
                actionLabel = "完成"
            )
            MigrationContract.STATUS_FAILED -> _uiState.value.copy(
                headline = "这次迁移没有完成",
                description = "旧版数据仍然完整保留，可以安全地重新发起迁移。",
                stage = MigrationStage.ERROR,
                action = MigrationAction.START,
                actionLabel = "重新迁移"
            )
            else -> _uiState.value.copy(
                headline = "仍在等待新版",
                description = "请先回到新版完成迁移，再来检查结果。",
                stage = MigrationStage.WAITING
            )
        }
    }

    fun importData(migrationId: String, nonce: String) {
        if (activeImportMigrationId == migrationId) return
        activeImportMigrationId = migrationId
        _uiState.value = MigrationUiState(
            headline = "正在迁移数据",
            description = "正在安全读取、校验并写入新版，请不要关闭应用。",
            stage = MigrationStage.PROCESSING
        )
        importJob = viewModelScope.launch {
            try {
                val imported = withContext(Dispatchers.IO) {
                    LegacyMigrationClient(appContext).import(migrationId, nonce)
                }
                _uiState.value = MigrationUiState(
                    headline = "迁移完成",
                    description = "数据已经安全写入新版。重新登录后即可继续使用。",
                    stage = MigrationStage.SUCCESS,
                    action = MigrationAction.FINISH,
                    actionLabel = "完成",
                    importedCounts = ImportedCounts(
                        courses = imported.courseCount,
                        ledgerItems = imported.ledgerCount,
                        preferences = imported.preferenceCount
                    )
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.value = MigrationUiState(
                    headline = "迁移没有完成",
                    description = "旧版数据没有删除。请返回旧版重新发起迁移。",
                    stage = MigrationStage.ERROR,
                    action = MigrationAction.FINISH,
                    actionLabel = "关闭"
                )
            }
        }
    }

    fun showSourceMissing() {
        _uiState.value = _uiState.value.copy(
            headline = "没有找到旧版",
            description = "设备上没有可读取数据的旧版长理星球。",
            stage = MigrationStage.ERROR,
            action = MigrationAction.NONE,
            actionLabel = ""
        )
    }
}

data class IntentRequest(val migrationId: String, val nonce: String)

data class MigrationUiState(
    val headline: String = "准备迁移",
    val description: String = "",
    val stage: MigrationStage = MigrationStage.READY,
    val action: MigrationAction = MigrationAction.NONE,
    val actionLabel: String = "",
    val importedCounts: ImportedCounts? = null
)

data class ImportedCounts(
    val courses: Int,
    val ledgerItems: Int,
    val preferences: Int
)

enum class MigrationStage { READY, INFO, PROCESSING, WAITING, SUCCESS, ERROR }

enum class MigrationAction { START, CHECK, OPEN_SOURCE, FINISH, NONE }
