package com.creamaker.changli_planet_app.widget.dialog

import android.content.Context
import android.graphics.Color
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.BaseDialog
import com.creamaker.changli_planet_app.common.service.DownloadProgressStore
import com.creamaker.changli_planet_app.common.service.DownloadService
import com.creamaker.changli_planet_app.common.service.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class UpdateDialog(
    context: Context,
    private val updateContent: String,
    private val apkUrl: String
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
            isDownloading = true
            updateDownloadingUi(0)
            DownloadService.startDownload(context, apkUrl)
        }
        btnCancel.setOnClickListener {
            dismiss()
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
                if (isShowing) {
                    dismiss()
                }
            }

            DownloadStatus.FAILED -> {
                progressLayout.visibility = View.VISIBLE
                progressText.text = "下载失败，请重试"
                btnUpdate.text = "重试下载"
                btnCancel.text = "关闭"
            }

            else -> {
                progressLayout.visibility = View.GONE
                btnCancel.text = "取消"
                btnUpdate.text = "立即更新"
            }
        }
    }

    private fun updateDownloadingUi(progress: Int) {
        progressLayout.visibility = View.VISIBLE
        progressBar.progress = progress
        progressText.text = "下载进度：${progress}%"
        btnUpdate.isEnabled = false
        btnUpdate.alpha = 0.5f
        btnUpdate.text = "下载中"
        btnCancel.text = "后台下载"
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
                        isDownloading = false
                        updateDownloadingUi(100)
                        if (isShowing) {
                            dismiss()
                        }
                    }

                    DownloadStatus.FAILED -> {
                        isDownloading = false
                        progressLayout.visibility = View.VISIBLE
                        progressText.text = "下载失败，请重试"
                        btnUpdate.isEnabled = true
                        btnUpdate.alpha = 1f
                        btnUpdate.text = "重试下载"
                        btnCancel.text = "关闭"
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