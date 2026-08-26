package com.zhelearn.CSUSTPlanet.analytics

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.zhelearn.CSUSTPlanet.BuildConfig
import com.zhelearn.CSUSTPlanet.common.data.local.mmkv.StudentInfoManager
import org.matomo.sdk.Matomo
import org.matomo.sdk.TrackMe
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.CustomDimension
import org.matomo.sdk.extra.TrackHelper

object MatomoTrackerManager {
    private const val TAG = "MatomoTracker"
    private const val DISPATCH_INTERVAL_MILLIS = 10_000L
    private const val LIFECYCLE_CATEGORY = "Lifecycle"
    private const val ACTIVE_ACTION = "Active"
    private const val BACKGROUND_ACTION = "Background"

    @Volatile
    private var tracker: Tracker? = null

    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> trackLifecycle(ACTIVE_ACTION)
            Lifecycle.Event.ON_STOP -> {
                trackLifecycle(BACKGROUND_ACTION)
                tracker?.dispatch()
            }
            else -> Unit
        }
    }

    @Synchronized
    fun initialize(application: Application) {
        if (tracker != null) return

        runCatching {
            val instance = TrackerBuilder
                .createDefault(BuildConfig.MATOMO_URL, BuildConfig.MATOMO_SITE_ID)
                .build(Matomo.getInstance(application))

            instance.setDispatchInterval(DISPATCH_INTERVAL_MILLIS)
            configureCommonDimensions(instance)
            setTrackerUserId(instance, StudentInfoManager.studentId)

            tracker = instance
            ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
            Log.i(TAG, "Matomo tracker initialized")
        }
    }

    fun updateUserId(userId: String?) {
        tracker?.let { setTrackerUserId(it, userId) }
    }

    private fun configureCommonDimensions(instance: Tracker) {
        val version = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        val environment = if (BuildConfig.DEBUG) "Debug" else "Release"

        instance.addTrackingCallback(object : Tracker.Callback {
            override fun onTrack(trackMe: TrackMe): TrackMe {
                CustomDimension.setDimension(
                    trackMe,
                    BuildConfig.MATOMO_DIMENSION_ID_APP_VERSION,
                    version
                )
                CustomDimension.setDimension(
                    trackMe,
                    BuildConfig.MATOMO_DIMENSION_ID_APP_ENVIRONMENT,
                    environment
                )
                return trackMe
            }
        })
    }

    private fun setTrackerUserId(instance: Tracker, userId: String?) {
        instance.setUserId(userId?.trim()?.takeIf { it.isNotEmpty() } ?: "")
    }

    private fun trackLifecycle(action: String) {
        tracker?.let { instance ->
            TrackHelper
                .track()
                .event(LIFECYCLE_CATEGORY, action)
                .with(instance)
        }
    }
}
