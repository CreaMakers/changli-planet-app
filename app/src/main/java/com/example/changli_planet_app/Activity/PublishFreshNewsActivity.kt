package com.example.changli_planet_app.Activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.changli_planet_app.Activity.Contract.FreshNewsContract
import com.example.changli_planet_app.Activity.ViewModel.FreshNewsViewModel
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Util.singleClick
import com.example.changli_planet_app.Widget.Dialog.PhotoPickerDialog
import com.example.changli_planet_app.Widget.View.CustomToast
import com.example.changli_planet_app.databinding.ActivityPublishFreshNewsBinding
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PublishFreshNewsActivity : FullScreenActivity() {
    private lateinit var binding: ActivityPublishFreshNewsBinding
    private val viewModel: FreshNewsViewModel by viewModels()
    private var currentPhotoUri: Uri? = null
    private val maxImagesAll = 9
    private var maxImageSize: Int = -1

    companion object {
        private const val REQUEST_CAMERA = 1001
        private const val REQUEST_GALLERY = 1002

        // 将质量压缩到85%左右
        private const val COMPRESS_QUALITY = 85
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        binding = ActivityPublishFreshNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        maxImageSize = dpToPx(115)          //给maxImageSize赋addImage大小的初值

        setContentMinHeight()
        setTextWatcher()

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (!state.isEnable) {
                    binding.publish.setOnClickListener(null)
                    binding.publish.setBackgroundResource(R.drawable.un_enable_button)
                } else {
                    binding.publish.singleClick(delay = 3000) {
                        viewModel.processIntent(FreshNewsContract.Intent.Publish())
                    }
                    binding.publish.setBackgroundResource(R.drawable.enable_button)
                }
            }
        }

        binding.addImage.setOnClickListener {
            if (binding.flexContainer.childCount - 1 < maxImagesAll) addImage()
            else CustomToast.showMessage(PlanetApplication.appContext, "最多只能添加9张图片哦~")
        }


        binding.cancel.setOnClickListener {
            viewModel.processIntent(FreshNewsContract.Intent.ClearAll())
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    public fun closeActivity(event: FreshNewsContract.Event) {
        when (event) {
            FreshNewsContract.Event.closePublish -> finish()
            else -> {}
        }
    }

    private fun addImage() {
        PhotoPickerDialog(
            this,
            onGalleryClick = { checkGalleryPermission() },
            onCameraClick = { checkCameraPermission() }
        ).show()
    }

    private fun setContentMinHeight() {                  //设置内容输入框的最小高度
        // 获取屏幕高度
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        binding.content.minHeight = screenHeight / 3
    }

    private fun setTextWatcher() {
        val titleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.processIntent(
                    FreshNewsContract.Intent.InputMessage(
                        binding.title.text.toString(),
                        "title"
                    )
                )
            }
        }

        val contentTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.processIntent(
                    FreshNewsContract.Intent.InputMessage(
                        binding.content.text.toString(),
                        "content"
                    )
                )
            }
        }
        binding.title.addTextChangedListener(titleTextWatcher)
        binding.content.addTextChangedListener(contentTextWatcher)
    }

    private fun checkGalleryPermission() {
        when {
            // 对于 Android 13 (API 33) 及以上版本
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                            == PackageManager.PERMISSION_GRANTED -> openGallery()

                    else -> ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        REQUEST_GALLERY
                    )
                }
            }
            // 对于 Android 10 及以上版本
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                            == PackageManager.PERMISSION_GRANTED -> openGallery()

                    else -> ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_GALLERY
                    )
                }
            }
            // 对于 Android 10 以下版本
            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                            == PackageManager.PERMISSION_GRANTED -> openGallery()

                    else -> ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        REQUEST_GALLERY
                    )
                }
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            // Android 10 及以上版本
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                when {
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED -> openCamera()

                    else -> ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA
                    )
                }
            }
            // Android 10 以下版本可能需要额外的存储权限
            else -> {
                when {
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            == PackageManager.PERMISSION_GRANTED -> openCamera()

                    else -> ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        REQUEST_CAMERA
                    )
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            intent.resolveActivity(packageManager)?.let {
                startActivityForResult(intent, REQUEST_CAMERA)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".png",
            storageDir
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }

            REQUEST_GALLERY -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_CAMERA -> currentPhotoUri?.let { insertImage(it) }
            REQUEST_GALLERY -> data?.data?.let { insertImage(it) }
        }
    }

    private fun insertImage(sourceUri: Uri) {
        val imageView = createImageView(sourceUri)
        val insertPosition = binding.flexContainer.indexOfChild(binding.addImage)
        binding.flexContainer.addView(imageView, insertPosition)

        lifecycleScope.launch(Dispatchers.IO) {
            val compressFile = compressImage(sourceUri)
            withContext(Dispatchers.Main) {
                viewModel.processIntent(FreshNewsContract.Intent.AddImage(compressFile))
            }
        }
    }

    private fun createImageView(uri: Uri): View {
        return LayoutInflater.from(this)
            .inflate(R.layout.item_image_thumbnail, binding.flexContainer, false).apply {
                layoutParams = FlexboxLayout.LayoutParams(
                    binding.addImage.layoutParams.width,
                    binding.addImage.layoutParams.height
                ).apply {
                    //flexBasisPercent=0.3f
                    marginEnd = dpToPx(10)
                    bottomMargin = dpToPx(10)
                }

//            findViewById<ImageView>(R.id.ivThumbnail).setImageURI(uri)
                val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                val imageView = findViewById<ImageView>(R.id.ivThumbnail)
                loadImageURI(
                    uri,
                    progressBar,
                    imageView
                )                             //之前遇到一个bug，加载的图片为null
                findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
                    removeImageView(this)
                }
            }
    }

    private fun loadImageURI(uri: Uri, progressBar: ProgressBar, imageView: ImageView) {
        Glide.with(this)
            .load(uri)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    imageView.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    imageView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    return false
                }
            })
            .error(R.drawable.ic_error_vector)
            .into(imageView)
    }

    private fun removeImageView(view: View) {
        binding.flexContainer.removeView(view)
        val position = binding.flexContainer.indexOfChild(view)
        if (position != -1) {
            viewModel.processIntent(FreshNewsContract.Intent.RemoveImage(position))
        }
    }

    private fun compressImage(uri: Uri): File {
        //获取图片原始尺寸
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        //确实压缩比例
        val scale = calculateInSampleSize(
            options.outWidth,
            options.outHeight,
            maxImageSize,
            maxImageSize
        )

        //压缩图片
        options.apply {
            inJustDecodeBounds = false
            inSampleSize = scale
        }
        val bitmap = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IOException("无法加载图片")

        //创建临时文件并上传
        val outputFile = File(cacheDir, "temp_${System.currentTimeMillis()}.png")
        FileOutputStream(outputFile)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, out)
            out.flush()
        }
        return outputFile
    }


    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        var scale = 1
        if (width > targetWidth || height > targetHeight) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            while (halfWidth / scale >= targetWidth && halfHeight / scale >= targetHeight) {
                scale *= 2
            }
        }
        return scale
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}