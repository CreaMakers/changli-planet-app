package com.creamaker.changli_planet_app.skin.data.cache

import com.tencent.mmkv.MMKV

object SkinCache {
    private val mmkv by lazy {
        MMKV.mmkvWithID("skin_cache")
    }
    fun saveAssetsName(skinPath: String) {
        mmkv.encode("skin_path", skinPath)
    }
    fun getAssetsName(): String {
        return mmkv.decodeString("skin_path", "skin_default") ?: "skin_default"
    }
    fun saveSkinDownloaded(skinName:String){
        mmkv.encode(skinName,true)
    }
    fun getSkinDownloaded(skinName:String):Boolean{
        return mmkv.decodeBool(skinName,false)
    }
    fun saveIsUsingSkin(skinName: String){
        mmkv.encode("is_using_skin",skinName)
    }
    fun getIsUsingSkin():String{
        return mmkv.decodeString("is_using_skin","skin_default") ?: "skin_default"
    }
}