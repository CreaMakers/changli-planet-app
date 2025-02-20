package com.example.changli_planet_app.Activity.Action

import android.content.Context
import com.example.changli_planet_app.Data.jsonbean.UserProfileRequest
import java.io.File

sealed class UserAction {
    class GetCurrentUserProfile(val context: Context) : UserAction()
    class GetCurrentUserStats(val context: Context) : UserAction()
    class UpdateAvatar(val uri: String) : UserAction()
    class UploadAvatar(val file: File) : UserAction()
    class UpdateUserProfile(val userProfileRequest: UserProfileRequest, val context: Context) :
        UserAction()

    class QueryIsLastedApk(val context: Context, val versionCode: Long, val versionName: String) :
        UserAction()

    class initilaize : UserAction()
}