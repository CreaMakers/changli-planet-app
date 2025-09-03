package com.example.changli_planet_app.feature.common.redux.state

/**
 * 每个页面都应有数据类来管理数据
 */

data class ElectronicState(
    var address: String,
    var buildId: String,
) {
    var nod: String = ""
    var isElec: Boolean = false
    var elec: String = ""
}