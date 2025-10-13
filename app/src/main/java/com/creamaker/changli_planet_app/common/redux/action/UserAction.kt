package com.creamaker.changli_planet_app.common.redux.action

import android.content.Context
import com.creamaker.changli_planet_app.settings.data.remote.dto.UserProfileRequest
import java.io.File

/**
 * 用户有关的Action
 */
sealed class UserAction {
    class GetCurrentUserProfile(val context: Context) : UserAction()
    class GetCurrentUserStats(val context: Context) : UserAction()
    class UpdateAvatar(val uri: String) : UserAction()
    class UploadAvatar(val file: File) : UserAction()
    class UpdateUserProfile(val userProfileRequest: UserProfileRequest, val context: Context) :
        UserAction()
    class UpdateLocation(val location:String):UserAction()
    class QueryIsLastedApk(val context: Context, val versionCode: Long, val versionName: String) :
        UserAction()
    class BindingStudentNumber(val context: Context, val student_number: String,val refresh: ()-> Unit) : UserAction()
    class initilaize : UserAction()
}