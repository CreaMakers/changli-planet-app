package com.example.changli_planet_app.feature.common.redux.action

import com.example.changli_planet_app.feature.common.data.remote.dto.CheckElectricity

/**
 * 电费查询Action
 */
sealed class ElectronicAction {
    data class selectAddress(val address:String):ElectronicAction()
    data class selectBuild(val buildId:String):ElectronicAction()
    data class queryElectronic(val checkElectricity: CheckElectricity):ElectronicAction()

}