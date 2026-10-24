package com.zhelearn.CSUSTPlanet.feature.physicslab.ui

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.base.ComposeActivity
import com.zhelearn.CSUSTPlanet.common.data.local.mmkv.StudentInfoManager
import com.zhelearn.CSUSTPlanet.core.Route
import com.zhelearn.CSUSTPlanet.core.theme.AppTheme
import com.zhelearn.CSUSTPlanet.feature.physicslab.ui.compose.PhysicsLabScreen
import com.zhelearn.CSUSTPlanet.widget.view.CustomToast

/** 大学物理实验（物理实验教学管理系统）。只读：实验项目、我的实验课表、我的成绩。 */
class PhysicsLabActivity : ComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (StudentInfoManager.studentId.isEmpty() || StudentInfoManager.studentPassword.isEmpty()) {
            CustomToast.showMessage(this, getString(R.string.bind_notification))
            Route.goBindingUser(this)
            finish()
            return
        }

        setComposeContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppTheme.colors.bgPrimaryColor
            ) {
                PhysicsLabScreen(
                    onBack = { finish() },
                    onGoBinding = { Route.goBindingUser(this, isSwitchAccount = true) }
                )
            }
        }
    }
}
