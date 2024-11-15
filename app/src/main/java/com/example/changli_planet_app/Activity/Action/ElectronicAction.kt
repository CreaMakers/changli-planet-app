package com.example.changli_planet_app.Activity.Action

import com.example.changli_planet_app.Data.jsonbean.CheckElectricity

/**
 * 定义了每个页面的事件
 */
sealed class ElectronicAction {
    data class selectAddress(val address:String):ElectronicAction()
    data class selectBuild(val buildId:String):ElectronicAction()
    data class selectNod(val nod:String):ElectronicAction()
    data class queryElectronic(val checkElectricity: CheckElectricity):ElectronicAction()
}