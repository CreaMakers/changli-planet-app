package com.creamaker.changli_planet_app.skin.data.remote.repository

import com.creamaker.changli_planet_app.skin.data.remote.api.SkinApi
import com.creamaker.changli_planet_app.utils.RetrofitUtils


class SkinRepository private constructor() {
    companion object {
        val instance by lazy { SkinRepository() }
        // 假设 RetrofitUtils 已经配置好
        val service by lazy { RetrofitUtils.instanceSkin.create(SkinApi::class.java) }
    }

    suspend fun getSkinList(page: Int, pageSize: Int) = service.getAllSkins(page, pageSize)

    suspend fun downloadSkin(name: String, id: Int) = service.downloadSkinFile(name,id)
}
