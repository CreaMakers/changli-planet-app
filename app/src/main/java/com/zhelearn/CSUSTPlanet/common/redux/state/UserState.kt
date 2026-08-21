package com.zhelearn.CSUSTPlanet.common.redux.state

import com.zhelearn.CSUSTPlanet.common.data.remote.dto.UserProfile
import com.zhelearn.CSUSTPlanet.common.data.remote.dto.UserStats

/**
 * 用户相关全局 State。
 *
 * `userProfile`/`avatarUri` 仅作为展示用的轻量缓存，由 MOOC SSO 登录成功时更新。
 */
data class UserState(
    var userProfile: UserProfile = UserProfile(),
    var userStats: UserStats = UserStats(),
    var avatarUri: String = "",
    var uiForLoading: Boolean = true
)
