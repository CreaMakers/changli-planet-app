package com.dcelysia.csust_spider.core

import com.tencent.mmkv.MMKV

object MMKVUtil {
    fun hasKey(mmkv: MMKV,key: String): Boolean = mmkv.containsKey(key)

    // 判断指定 key 对应的 String 是否有实际内容（非空白）
    fun hasNonEmptyString(mmkv: MMKV, key: String): Boolean =
        mmkv.decodeString(key)?.isNotBlank() == true

    // 判断 MMKV 内是否有任意键值（无键则为空）
    fun hasAnyKey(mmkv: MMKV): Boolean {
        val keys = mmkv.allKeys()
        return keys != null && keys.isNotEmpty()
    }
}