package com.creamaker.changli_planet_app.feature.common.viewModel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.core.mvi.MviViewModel
import com.creamaker.changli_planet_app.feature.common.contract.ElectronicContract
import com.example.csustdataget.CampusCard.CampusCardHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ElectronicViewModel : MviViewModel<ElectronicContract.Intent, ElectronicContract.State>() {

    private val _effect = Channel<ElectronicContract.Effect>(Channel.BUFFERED)
    val effect: Flow<ElectronicContract.Effect> = _effect.receiveAsFlow()

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
                queryElectricity(intent.address, intent.buildId, intent.nod)
            }

            is ElectronicContract.Intent.Init -> {
                updateState {
                    copy(address = intent.address, buildId = intent.buildId)
                }
                // If all fields present, trigger query? Activity logic did this manually.
                // We will keep it manual or triggered by separate Intent from Activity.
                if (intent.address != "选择校区" && intent.buildId != "选择宿舍楼" && intent.nod.isNotEmpty()) {
                    queryElectricity(intent.address, intent.buildId, intent.nod)
                }
            }
        }
    }

    private fun queryElectricity(address: String, buildId: String, nod: String) {
        updateState { copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val eleResponse = CampusCardHelper.queryElectricity(address, buildId, nod)

                withContext(Dispatchers.Main) {
                    if (eleResponse == null) {
                        updateState {
                            copy(isLoading = false, elec = "无数据", isElec = true)
                        }
                    } else {
                        Log.d("ElectronicViewModel", eleResponse.toString())
                        updateState {
                            copy(isLoading = false, elec = eleResponse.toString(), isElec = true)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(isLoading = false, elec = "查询失败", isElec = true)
                    }
                }
            }
        }
    }
}
