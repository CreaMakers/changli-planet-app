package com.creamaker.changli_planet_app.feature.common.viewModel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.core.mvi.MviViewModel
import com.creamaker.changli_planet_app.feature.common.contract.ExamArrangementContract
import com.creamaker.changli_planet_app.feature.common.data.local.mmkv.ExamArrangementCache
import com.creamaker.changli_planet_app.feature.common.ui.adapter.model.Exam
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.education.data.remote.services.ExamArrangeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExamArrangementViewModel :
    MviViewModel<ExamArrangementContract.Intent, ExamArrangementContract.State>() {
    private val cache by lazy { ExamArrangementCache() }
    private val _effect = Channel<ExamArrangementContract.Effect>(Channel.BUFFERED)
    val effect: Flow<ExamArrangementContract.Effect> = _effect.receiveAsFlow()

    override fun initialState(): ExamArrangementContract.State = ExamArrangementContract.State()

    override fun processIntent(intent: ExamArrangementContract.Intent) {
        when (intent) {
            is ExamArrangementContract.Intent.LoadExamArrangement -> {
                fetchExamArrangement(intent.termTime)
            }
        }
    }

    private fun fetchExamArrangement(termTime: String) {
        updateState { copy(isLoading = true) }
        Log.d("ceshi", "fetchExamArrangement")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val middleDefer = async { ExamArrangeService.getExamArrange(termTime, "期中") }
                val endDefer = async { ExamArrangeService.getExamArrange(termTime, "期末") }

                val middleResult = middleDefer.await()
                val endResult = endDefer.await()

                if (middleResult is Resource.Success && endResult is Resource.Success) {
                    val middleList = middleResult.data
                    val endList = endResult.data

                    val combined = middleList + endList
                    cache.saveExamArrangement(combined)
                    val deduped = combined.distinctBy { exam ->
                        listOf(
                            exam.courseNameval,
                            exam.examTime,
                            exam.campus,
                            exam.examRoomval
                        )
                    }

                    val examModels = deduped.map {
                        Exam(it.courseNameval, it.examTime, it.campus, it.examRoomval)
                    }

                    withContext(Dispatchers.Main) {
                        updateState { copy(isLoading = false, exams = examModels) }
                        _effect.send(ExamArrangementContract.Effect.ShowToast("加载成功"))
                    }
                } else {
                    val errorMessage = when {
                        middleResult is Resource.Error -> middleResult.msg
                        endResult is Resource.Error -> endResult.msg
                        else -> "未知错误"
                    }
                    val combined = cache.getExamArrangement()
                    if (!combined.isNullOrEmpty()) {
                        val examModels = combined.distinctBy { exam ->
                            listOf(
                                exam.courseNameval,
                                exam.examTime,
                                exam.campus,
                                exam.examRoomval
                            )
                        }.map {
                            Exam(it.courseNameval, it.examTime, it.campus, it.examRoomval)
                        }
                        withContext(Dispatchers.Main) {
                            updateState { copy(isLoading = false, exams = examModels) }
                            _effect.send(ExamArrangementContract.Effect.ShowErrorDialog("${errorMessage},加载本地缓存"))
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            updateState { copy(isLoading = false) }
                            _effect.send(ExamArrangementContract.Effect.ShowErrorDialog(errorMessage))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    updateState { copy(isLoading = false) }
                    _effect.send(
                        ExamArrangementContract.Effect.ShowErrorDialog(
                            e.message ?: "未知错误"
                        )
                    )
                }
            }
        }
    }
}
