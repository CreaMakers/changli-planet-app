package com.example.changli_planet_app.Widget.Dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider
import com.example.changli_planet_app.Base.BaseDialog
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Widget.View.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import org.w3c.dom.Text
import java.io.File
import java.io.FileOutputStream

class UpdateDialog(
    context: Context,
    private val updateContent: String,
    private val apkUrl: String
) : BaseDialog(context) {
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var progressLayout: LinearLayout
    private lateinit var btnUpdate: TextView
    private lateinit var btnCancel: TextView
    private lateinit var tvUpdateContent: TextView

    private var downloadJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun init() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)
        tvUpdateContent = findViewById(R.id.update_content)

//        tvUpdateContent.text = updateContent.replace("\\n", "\n")
        tvUpdateContent.movementMethod = ScrollingMovementMethod()
        progressBar = findViewById(R.id.progress_bar)
        progressText = findViewById(R.id.progress_text)
        progressLayout = findViewById(R.id.progress_layout)
        btnUpdate = findViewById(R.id.btn_update)
        btnCancel = findViewById(R.id.btn_cancel)

        btnUpdate.setOnClickListener {
            startDownload()
        }
        btnCancel.setOnClickListener {
            downloadJob?.cancel()
            dismiss()
        }
    }

    override fun layoutId(): Int = R.layout.update_dialog

    private fun startDownload() {
        progressLayout.visibility = View.VISIBLE
        btnUpdate.isEnabled = false
        btnUpdate.text = "下载中..."

        downloadJob = coroutineScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    downloadApk()
                }
                installApk(file)
                dismiss()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    CustomToast.showMessage(context, "下载失败：${e.message}")
                    btnUpdate.isEnabled = true
                    btnUpdate.text = "重试"
                    progressLayout.visibility = View.GONE
                }
            }
        }
    }

    private suspend fun downloadApk(): File {
        val file = File(context.externalCacheDir, "update.apk")
        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url(apkUrl)
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body!!
            val length = body.contentLength()
            withContext(Dispatchers.IO) {
                val input = body.byteStream()
                val output = FileOutputStream(file)

                var byteCopied: Long = 0
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = input.read(buffer)

                while (bytes >= 0) {
                    output.write(buffer, 0, bytes)
                    byteCopied += bytes
                    bytes = input.read(buffer)

                    val progress = ((byteCopied * 100) / length).toInt()
                    withContext(Dispatchers.Main) {
                        progressBar.progress = progress
                        progressText.text = "$progress"
                    }
                }

                output.close()
                input.close()
            }
        }
        return file
    }

    private fun installApk(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }
}