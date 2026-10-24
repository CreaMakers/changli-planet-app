package com.zhelearn.CSUSTPlanet.feature.physicslab.data.remote

import android.util.Log
import com.dcelysia.csust_spider.physicsexperiment.PhysicsLabHelper
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabIndex
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabScore
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabSelectable
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabTask
import com.dcelysia.csust_spider.physicsexperiment.data.remote.error.PhysicsLabError
import com.zhelearn.CSUSTPlanet.core.PlanetApplication
import com.zhelearn.CSUSTPlanet.core.network.ApiResponse
import com.zhelearn.CSUSTPlanet.feature.physicslab.data.local.PhysicsLabContentCache
import com.zhelearn.CSUSTPlanet.utils.NetworkUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 一次查询的结果。[error] 额外保留底层异常类型：`ApiResponse.Error` 只有一句文案，
 * 而"缺平台密码"（要弹输入框）和"统一认证失败"（要重新绑定学号）的交互完全不同。
 */
data class PhysicsLabQueryResult<T>(
    val response: ApiResponse<T>,
    val error: PhysicsLabError? = null,
    val fromCache: Boolean = false
) {
    val needsPlatformPassword: Boolean get() = error is PhysicsLabError.NeedManualLogin

    val needsRebind: Boolean get() = error is PhysicsLabError.VpnLoginFailed
}

/**
 * 物理实验数据源：网络优先 + 本地缓存兜底。会话续期不归这里管 ——
 * 查询 client 上挂了 `PhysicsLabRetryInterceptor`，落地登录页会自己重登并重试一次。
 */
object PhysicsLabRemoteDataSource {

    private const val TAG = "PhysicsLabRemote"
    private const val ERR_UNKNOWN = "物理实验平台加载失败"

    suspend fun loadIndex(studentId: String): PhysicsLabQueryResult<PhysicsLabIndex> =
        query(
            cached = { PhysicsLabContentCache.getIndex(studentId) },
            save = { PhysicsLabContentCache.saveIndex(studentId, it) },
            request = { PhysicsLabHelper.getIndex() }
        )

    suspend fun loadTasks(studentId: String): PhysicsLabQueryResult<List<PhysicsLabTask>> =
        query(
            cached = { PhysicsLabContentCache.getTasks(studentId) },
            save = { PhysicsLabContentCache.saveTasks(studentId, it) },
            request = { PhysicsLabHelper.getMyExperiments() }
        )

    suspend fun loadScores(studentId: String): PhysicsLabQueryResult<List<PhysicsLabScore>> =
        query(
            cached = { PhysicsLabContentCache.getScores(studentId) },
            save = { PhysicsLabContentCache.saveScores(studentId, it) },
            request = { PhysicsLabHelper.getScores() }
        )

    /** 本学期可选：不落缓存 —— 离线时只有这一组显示失败，其它组照旧走缓存。 */
    suspend fun loadSelectable(studentId: String): PhysicsLabQueryResult<PhysicsLabSelectable> =
        query(
            cached = { null },
            save = { },
            request = { PhysicsLabHelper.getSelectableExperiments() }
        )

    private suspend fun <T> query(
        cached: () -> T?,
        save: (T) -> Unit,
        request: suspend () -> T
    ): PhysicsLabQueryResult<T> = withContext(Dispatchers.IO) {
        // 没平台密码时平台层必然登不上，省掉一次统一认证与网关往返
        if (!PhysicsLabHelper.hasPlatformPassword) {
            return@withContext fallback(
                PhysicsLabError.NeedManualLogin("尚未设置物理实验平台密码"),
                cached
            )
        }
        try {
            val data = request()
            save(data)
            PhysicsLabQueryResult(ApiResponse.Success(data))
        } catch (e: CancellationException) {
            throw e
        } catch (e: PhysicsLabError) {
            Log.w(TAG, "物理实验查询失败：${e.message}")
            fallback(e, cached)
        } catch (e: Exception) {
            // 非业务异常（网络层为主）：原文只进日志，界面用可读文案
            Log.e(TAG, "物理实验查询异常", e)
            fallback(PhysicsLabError.NetworkError(networkErrorMessage(e)), cached)
        }
    }

    /**
     * 把网络类异常翻译成用户看得懂的话。系统原文是英文技术文案（如
     * `Unable to resolve host …`），不适合直接显示；这里复用 [NetworkUtil]
     * 区分"设备没网"和"连不上服务器"。
     */
    private fun networkErrorMessage(e: Exception): String = when {
        e is UnknownHostException || e is ConnectException || !hasNetwork() -> "网络不可用，请检查网络连接"
        e is SocketTimeoutException -> "连接超时，请稍后重试"
        e is SSLException -> "安全连接失败，请稍后重试"
        else -> e.message?.takeIf { it.isNotBlank() } ?: ERR_UNKNOWN
    }

    /** 判网失败时按"有网"处理，避免把服务器问题误报成设备断网。 */
    private fun hasNetwork(): Boolean = runCatching {
        NetworkUtil.getNetworkType(PlanetApplication.appContext) != NetworkUtil.NetworkType.None
    }.getOrDefault(true)

    /** 失败降级：有缓存就继续展示（标记 [PhysicsLabQueryResult.fromCache]），没有才把错误交给 UI。 */
    private fun <T> fallback(error: PhysicsLabError, cached: () -> T?): PhysicsLabQueryResult<T> {
        val hit = runCatching { cached() }.getOrNull()
        return if (hit != null) {
            PhysicsLabQueryResult(ApiResponse.Success(hit), error, fromCache = true)
        } else {
            PhysicsLabQueryResult(ApiResponse.Error(error.message ?: ERR_UNKNOWN), error)
        }
    }
}
