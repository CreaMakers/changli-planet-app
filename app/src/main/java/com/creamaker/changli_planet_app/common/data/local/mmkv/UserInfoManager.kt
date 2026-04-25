package com.creamaker.changli_planet_app.common.data.local.mmkv

import com.creamaker.changli_planet_app.core.PlanetApplication
import com.tencent.mmkv.MMKV

object UserInfoManager {
    private val mmkv by lazy { MMKV.mmkvWithID("import_cache") }

    private const val KEY_USERID = "user_id"
    private const val KEY_USERNAME = "account"
    private const val KEY_USER_PASSWORD = "user_password"
    private const val KEY_AVATAR = "user_avatar"

    private const val KEY_USER_ACCOUNT = "user_account"
    private const val KEY_EMAIL = "user_email"

    var userId: Int
        get() = mmkv.getInt(KEY_USERID, -1)
        set(value) {
            mmkv.putInt(KEY_USERID, value)
        }

    var username: String
        get() = mmkv.getString(KEY_USERNAME, "") ?: ""
        set(value) {
            mmkv.putString(KEY_USERNAME, value)
        }

    var account: String
        get() = mmkv.getString(KEY_USER_ACCOUNT, "") ?: ""
        set(value) {
            mmkv.putString(KEY_USER_ACCOUNT, value)
        }

    var userPassword: String
        get() = mmkv.getString(KEY_USER_PASSWORD, "") ?: ""
        set(value) {
            mmkv.putString(KEY_USER_PASSWORD, value)
        }

    var userAvatar: String
        get() = mmkv.getString(KEY_AVATAR, "https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg")
            ?: "https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg"
        set(value) {
            mmkv.putString(KEY_AVATAR, value)
        }

    var userEmail:String
        get() = mmkv.getString(KEY_EMAIL,"")?:""
        set(value){
            mmkv.putString(KEY_EMAIL,value)
        }

    fun clear() {
        username = ""
        userPassword = ""
        PlanetApplication.accessToken = null
    }

    /**
     * 切换学号成功后的清理：仅移除"不会被新登录覆盖"的旧账号残留字段（邮箱 / userId）。
     *
     * 注意：
     * - username / account / userPassword —— 已由 [UserAction.BindingStudentNumber] 的 SSO 成功分支
     *   写入为新账号的值，这里不要清。
     * - userAvatar —— 同样会在 SSO 成功分支被写为新账号头像（UserStore 中），因此不能在此清除，
     *   否则 UI 会拿回默认占位头像，造成"切换账号后头像丢失"的回归。
     */
    fun clearStaleProfile() {
        mmkv.removeValueForKey(KEY_EMAIL)
        mmkv.removeValueForKey(KEY_USERID)
    }
}