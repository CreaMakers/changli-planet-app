package com.example.changli_planet_app.Activity.Action

import android.content.Context
import java.io.File

sealed class UserAction {
    class GetCurrentUserProfile(val context: Context) : UserAction()
    class GetCurrentUserStats(val context: Context) : UserAction()
    class UpdateAvatar(val uri: String) : UserAction()
    class UploadAvatar(val file: File) : UserAction()
    class initilaize: UserAction()
}