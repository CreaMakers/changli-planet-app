package com.dcelysia.baselineprofile

import android.os.SystemClock
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TARGET_PACKAGE = "com.example.changli_planet_app"
private const val SHORT_TIMEOUT_MS = 1_500L
private const val OVERVIEW_TIMEOUT_MS = 8_000L
private const val BINDING_TIMEOUT_MS = 25_000L
private const val HOMEWORK_TIMEOUT_MS = 12_000L
private const val HOMEWORK_DWELL_MS = 8_000L
private const val FAST_EXIT_DWELL_MS = 5_000L
private const val STUDENT_ID = "===="
private const val STUDENT_PASSWORD = "===="
private const val PERMISSION_CONTROLLER_PACKAGE = "com.android.permissioncontroller"
private const val PACKAGE_INSTALLER_PACKAGE = "com.android.packageinstaller"

private val permissionButtonSelectors = listOf(
    By.res(PERMISSION_CONTROLLER_PACKAGE, "permission_allow_foreground_only_button"),
    By.res(PERMISSION_CONTROLLER_PACKAGE, "permission_allow_one_time_button"),
    By.res(PERMISSION_CONTROLLER_PACKAGE, "permission_allow_button"),
    By.res(PERMISSION_CONTROLLER_PACKAGE, "permission_allow_always_button"),
    By.res(PACKAGE_INSTALLER_PACKAGE, "permission_allow_foreground_only_button"),
    By.res(PACKAGE_INSTALLER_PACKAGE, "permission_allow_one_time_button"),
    By.res(PACKAGE_INSTALLER_PACKAGE, "permission_allow_button"),
    By.res(PACKAGE_INSTALLER_PACKAGE, "permission_allow_always_button"),
    By.res("android", "button1"),
    By.text("Allow"),
    By.text("While using the app"),
    By.text("Only this time"),
    By.text("允许"),
    By.text("仅在使用该应用时允许"),
    By.text("使用时允许")
)

/**
 * This test class generates a basic startup baseline profile for the target package.
 *
 * We recommend you start with this but add important user flows to the profile to improve their performance.
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the "Generate Baseline Profile" run configuration in Android Studio or
 * the equivalent `generateBaselineProfile` gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 *
 * When using this class to generate a baseline profile, only API 33+ or rooted API 28+ are supported.
 *
 * The minimum required version of androidx.benchmark to generate a baseline profile is 1.2.0.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        var isFirstCollectionRun = true

        // The application id for the running build variant is read from the instrumentation arguments.
        rule.collect(
            packageName = TARGET_PACKAGE,
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()
            dismissPermissionDialogs()
            waitForOverview()

            if (isFirstCollectionRun && requiresInitialBinding()) {
                openOverviewViewAllButton(index = 0)
                bindStudentAccount(STUDENT_ID, STUDENT_PASSWORD)
                waitForOverview()
                openOverviewViewAllButton(index = 1)
                waitForHomeworkScreen()
                SystemClock.sleep(HOMEWORK_DWELL_MS)
            } else {
                SystemClock.sleep(FAST_EXIT_DWELL_MS)
            }

            isFirstCollectionRun = false
            pressHome()
        }
    }
}

private fun MacrobenchmarkScope.dismissPermissionDialogs() {
    repeat(4) {
        val button = permissionButtonSelectors.firstNotNullOfOrNull { selector ->
            device.findObject(selector)
        } ?: permissionButtonSelectors.firstNotNullOfOrNull { selector ->
            device.wait(Until.findObject(selector), 400)
        } ?: return
        button.click()
        device.waitForIdle()
        SystemClock.sleep(300)
    }
}

private fun MacrobenchmarkScope.waitForOverview() {
    waitUntil(OVERVIEW_TIMEOUT_MS) {
        dismissPermissionDialogs()
        device.hasObject(By.text("概况")) && device.hasObject(By.text("查看全部"))
    }
    check(device.hasObject(By.text("概况"))) {
        "Overview screen did not load."
    }
    check(device.hasObject(By.text("查看全部"))) {
        "Overview shortcuts did not render."
    }
}

private fun MacrobenchmarkScope.requiresInitialBinding(): Boolean {
    dismissPermissionDialogs()
    return device.wait(Until.hasObject(By.text("先绑定学号")), SHORT_TIMEOUT_MS)
}

private fun MacrobenchmarkScope.openOverviewViewAllButton(index: Int) {
    dismissPermissionDialogs()
    waitForOverview()
    val button = device.findObjects(By.text("查看全部")).getOrNull(index)
        ?: error("Missing overview action button at index $index.")
    button.click()
    device.waitForIdle()
}

private fun MacrobenchmarkScope.bindStudentAccount(studentId: String, studentPassword: String) {
    val studentIdInput = requireObject(By.res(TARGET_PACKAGE, "et_student_id"), BINDING_TIMEOUT_MS)
    studentIdInput.text = studentId

    val passwordInput = requireObject(By.res(TARGET_PACKAGE, "et_student_password"), SHORT_TIMEOUT_MS)
    passwordInput.text = studentPassword

    requireObject(By.res(TARGET_PACKAGE, "save_user"), SHORT_TIMEOUT_MS).click()
    device.waitForIdle()

    waitUntil(BINDING_TIMEOUT_MS) {
        dismissPermissionDialogs()
        device.hasObject(By.text("概况"))
    }
    check(device.hasObject(By.text("概况"))) {
        "Binding flow did not return to the overview screen."
    }
}

private fun MacrobenchmarkScope.waitForHomeworkScreen() {
    val loaded = device.wait(Until.hasObject(By.text("强制刷新")), HOMEWORK_TIMEOUT_MS) ||
        device.wait(
            Until.hasObject(By.text("展开课程后可查看待提交作业和待测试")),
            SHORT_TIMEOUT_MS
        ) ||
        device.wait(Until.hasObject(By.text("待提交作业")), SHORT_TIMEOUT_MS)
    check(loaded) {
        "Homework screen did not load."
    }
}

private fun MacrobenchmarkScope.requireObject(selector: BySelector, timeoutMs: Long): UiObject2 {
    return requireNotNull(device.wait(Until.findObject(selector), timeoutMs)) {
        "Unable to find object for selector: $selector"
    }
}

private inline fun MacrobenchmarkScope.waitUntil(
    timeoutMs: Long,
    condition: MacrobenchmarkScope.() -> Boolean
) {
    val deadline = SystemClock.uptimeMillis() + timeoutMs
    while (SystemClock.uptimeMillis() < deadline) {
        if (condition()) return
        SystemClock.sleep(250)
    }
}
