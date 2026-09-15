package com.zhelearn.CSUSTPlanet.widget.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.base.BaseDialog
import com.zhelearn.CSUSTPlanet.common.service.DownloadProgressStore
import com.zhelearn.CSUSTPlanet.common.service.DownloadService
import com.zhelearn.CSUSTPlanet.common.service.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class UpdateDialog(
    context: Context,
    private val updateContent: String,
    private val apkUrl: String,
    private val isForceUpdate: Boolean = false
) : BaseDialog(context) {

    private lateinit var btnUpdate: TextView
    private lateinit var btnCancel: TextView
    private lateinit var tvUpdateContent: TextView
    private lateinit var progressLayout: View
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private var isDownloading = false
    private var collectJob: Job? = null
    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun init() {
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setWindowAnimations(R.style.DialogAnimation)
        tvUpdateContent = findViewById(R.id.update_content)
        progressLayout = findViewById(R.id.download_progress_layout)
        progressBar = findViewById(R.id.download_progress_bar)
        progressText = findViewById(R.id.download_progress_text)

        tvUpdateContent.text = updateContent
        tvUpdateContent.movementMethod = ScrollingMovementMethod()
        btnUpdate = findViewById(R.id.btn_update)
        btnCancel = findViewById(R.id.btn_cancel)

        syncUiByCurrentDownloadState()
        observeDownloadProgress()

        btnUpdate.setOnClickListener {
            if (isDownloading) return@setOnClickListener
            if (isForceUpdate && DownloadProgressStore.state.value.status == DownloadStatus.SUCCESS) {
                if (!DownloadService.installDownloadedApk(context)) {
                    DownloadProgressStore.markFailed(0)
                }
                return@setOnClickListener
            }
            isDownloading = true
            updateDownloadingUi(0)
            DownloadService.startDownload(context, apkUrl)
        }
        btnCancel.setOnClickListener {
            if (isForceUpdate) {
                exitApplication()
            } else {
                dismiss()
            }
        }
    }

    private fun syncUiByCurrentDownloadState() {
        val state = DownloadProgressStore.state.value
        when (state.status) {
            DownloadStatus.STARTED,
            DownloadStatus.PROGRESS -> {
                isDownloading = true
                updateDownloadingUi(state.progress)
            }

            DownloadStatus.SUCCESS -> {
                if (isForceUpdate) {
                    showInstallReadyUi()
                } else if (isShowing) {
                    dismiss()
                }
            }

            DownloadStatus.FAILED -> {
                showDownloadFailedUi()
            }

            else -> {
                progressLayout.visibility = View.GONE
                btnCancel.text = cancelButtonText(R.string.update_action_cancel)
                btnUpdate.setText(R.string.update_action_now)
            }
        }
    }

    private fun updateDownloadingUi(progress: Int) {
        progressLayout.visibility = View.VISIBLE
        progressBar.progress = progress
        progressText.text = context.getString(R.string.update_download_progress, progress)
        btnUpdate.isEnabled = false
        btnUpdate.alpha = 0.5f
        btnUpdate.setText(R.string.update_status_downloading)
        btnCancel.text = cancelButtonText(R.string.update_action_background_download)
    }

    private fun showDownloadFailedUi() {
        isDownloading = false
        progressLayout.visibility = View.VISIBLE
        progressText.setText(R.string.update_download_failed)
        btnUpdate.isEnabled = true
        btnUpdate.alpha = 1f
        btnUpdate.setText(R.string.update_action_retry_download)
        btnCancel.text = cancelButtonText(R.string.update_action_close)
    }

    private fun showInstallReadyUi() {
        isDownloading = false
        progressLayout.visibility = View.VISIBLE
        progressBar.progress = 100
        progressText.setText(R.string.update_download_complete)
        btnUpdate.isEnabled = true
        btnUpdate.alpha = 1f
        btnUpdate.setText(R.string.update_action_continue_install)
        btnCancel.setText(R.string.update_action_exit_app)
    }

    private fun cancelButtonText(defaultTextRes: Int): String {
        val textRes = if (isForceUpdate) R.string.update_action_exit_app else defaultTextRes
        return context.getString(textRes)
    }

    private fun exitApplication() {
        context.stopService(Intent(context, DownloadService::class.java))
        DownloadProgressStore.reset()
        dismiss()
        (context as? Activity)?.finishAffinity()
    }

    private fun observeDownloadProgress() {
        collectJob?.cancel()
        collectJob = uiScope.launch {
            DownloadProgressStore.state.collect { state ->
                when (state.status) {
                    DownloadStatus.STARTED,
                    DownloadStatus.PROGRESS -> {
                        isDownloading = true
                        updateDownloadingUi(state.progress)
                    }

                    DownloadStatus.SUCCESS -> {
                        if (isForceUpdate) {
                            showInstallReadyUi()
                        } else if (isShowing) {
                            dismiss()
                        }
                    }

                    DownloadStatus.FAILED -> {
                        showDownloadFailedUi()
                    }

                    DownloadStatus.IDLE -> {
                        isDownloading = false
                        progressLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun dismiss() {
        collectJob?.cancel()
        uiScope.cancel()
        super.dismiss()
    }

    override fun layoutId(): Int = R.layout.update_dialog
}
