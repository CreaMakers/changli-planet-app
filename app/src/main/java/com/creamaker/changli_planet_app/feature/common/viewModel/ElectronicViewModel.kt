package com.creamaker.changli_planet_app.feature.common.viewModel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.core.mvi.MviViewModel
import com.creamaker.changli_planet_app.feature.common.contract.ElectronicContract
import com.creamaker.changli_planet_app.feature.common.data.repository.ElectricityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ElectronicViewModel : MviViewModel<ElectronicContract.Intent, ElectronicContract.State>() {
    private val repository by lazy { ElectricityRepository() }
    override fun initialState(): ElectronicContract.State = ElectronicContract.State()

    override fun processIntent(intent: ElectronicContract.Intent) {
        when (intent) {
            is ElectronicContract.Intent.SelectSchool -> {
                updateState { copy(address = intent.address) }
            }

            is ElectronicContract.Intent.SelectDorm -> {
                updateState { copy(buildId = intent.buildId) }
            }

            is ElectronicContract.Intent.QueryElectricity -> {
                repository.saveBinding(intent.address, intent.buildId, intent.nod)
                queryElectricity(forceRefresh = true)
            }

            is ElectronicContract.Intent.Init -> {
                updateState {
                    copy(address = intent.address, buildId = intent.buildId)
                }
                if (intent.address != "选择校区" && intent.buildId != "选择宿舍楼" && intent.nod.isNotEmpty()) {
                    queryElectricity(forceRefresh = false)
                }
            }
        }
    }

    private fun queryElectricity(forceRefresh: Boolean) {
        updateState { copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = if (forceRefresh) {
                    repository.query(force = true)
                } else {
                    repository.refreshIfNeeded()
                }

                if (result == null) {
                    updateState { copy(isLoading = false, elec = "无数据", isElec = true) }
                } else {
                    Log.d("ElectronicViewModel", result.rawValue)
                    updateState {
                        copy(isLoading = false, elec = result.rawValue, isElec = true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateState { copy(isLoading = false, elec = "查询失败", isElec = true) }
            }
        }
    }
}
