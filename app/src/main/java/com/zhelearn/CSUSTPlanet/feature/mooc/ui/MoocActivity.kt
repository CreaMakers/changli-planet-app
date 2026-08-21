package com.zhelearn.CSUSTPlanet.feature.mooc.ui

import android.os.Bundle
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.base.ComposeActivity
import com.zhelearn.CSUSTPlanet.common.data.local.mmkv.StudentInfoManager.studentId
import com.zhelearn.CSUSTPlanet.common.data.local.mmkv.StudentInfoManager.studentPassword
import com.zhelearn.CSUSTPlanet.core.PlanetApplication
import com.zhelearn.CSUSTPlanet.core.Route
import com.zhelearn.CSUSTPlanet.widget.view.CustomToast

class MoocActivity : ComposeActivity() {
    private val moocViewModel by lazy { (application as PlanetApplication).moocViewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (studentId.isEmpty() || studentPassword.isEmpty()) {
            CustomToast.showMessage(this, getString(R.string.bind_notification))
            Route.goBindingUser(this)
            finish()
            return
        }

        setComposeContent {
            MoocScreen(
                moocViewModel = moocViewModel,
                onBack = ::finish,
                onOpenCoursePage = { courseId ->
                    Route.goMoocCoursePage(this, courseId)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (studentId.isNotEmpty() && studentPassword.isNotEmpty() && moocViewModel.shouldAutoRefreshOnEnter()) {
            moocViewModel.refreshCourses()
        }
    }
}
