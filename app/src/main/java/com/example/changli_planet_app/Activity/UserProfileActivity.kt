package com.example.changli_planet_app.Activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.NumberPicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.changli_planet_app.Activity.Action.UserAction
import com.example.changli_planet_app.Activity.Store.UserStore
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.R
import com.example.changli_planet_app.UI.CustomToast
import com.example.changli_planet_app.UI.PhotoPickerDialog
import com.example.changli_planet_app.Util.Event.FinishEvent
import com.example.changli_planet_app.databinding.ActivityUserProfileBinding
import com.yalantis.ucrop.UCrop
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UserProfileActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA = 1001
        private const val REQUEST_GALLERY = 1002

        // 图片最大尺寸大于90 保证图片清晰度
        private const val MAX_IMAGE_SIZE = 180

        // 将质量压缩到85%左右
        private const val COMPRESS_QUALITY = 85
    }

    private var currentPhotoUri: Uri? = null

    private lateinit var binding: ActivityUserProfileBinding

    private val store by lazy { UserStore() }
    private val disposable by lazy { CompositeDisposable() }

    // xml view
    private val setAvatar by lazy { binding.personProfileSetAvatar }
    private val avatar by lazy { binding.userProfileAvatar }
    private val back by lazy { binding.personProfileBack }

    private val account by lazy { binding.userProfileName }
    private val bio by lazy { binding.userProfileBio }
    private val grade by lazy { binding.userProfileGrade }
    private val gender by lazy { binding.userProfileGender }
    private val birthday by lazy { binding.userProfileBirthday }
    private val website by lazy { binding.userProfileWebsite }

    private val submit by lazy { binding.userProfileSubmit }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        observeState()
        store.dispatch(UserAction.initilaize())
        setAvatar.setOnClickListener { setAvatar() }
        back.setOnClickListener { finish() }
        birthday.setOnClickListener { setBirthday() }
        gender.setOnClickListener { showGenderPickerDialog() }
        EventBus.getDefault().register(this)
    }

    private fun observeState() {
        disposable.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    val userProfile = state.userProfile
                    loadAvatar(state.avatarUri)
                    account.text = UserInfoManager.username
                    bio.setText(userProfile.bio)
                    grade.text = userProfile.grade
                    birthday.text = userProfile.birthDate
                    gender.text = when (userProfile.gender) {
                        0 -> "男"
                        1 -> "女"
                        else -> "保密"
                    }
                    website.setText(userProfile.website)
                }
        )
    }


    private fun setAvatar() {
        PhotoPickerDialog(
            this,
            onGalleryClick = { checkGalleryPermission() },
            onCameraClick = { checkCameraPermission() }
        ).show()
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        return "${year}-${String.format(Locale.getDefault(), "%02d-%02d", month, day)}"
    }

    private fun setBirthday() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this@UserProfileActivity,
            { _, year, month, day ->
                birthday.text = formatDate(year, month, day)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun showGenderPickerDialog() {
        // 定义性别选项数组
        val genders = arrayOf("男", "女", "保密")

        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = genders.size - 1
            displayedValues = genders
            wrapSelectorWheel = false
        }

        // 使用 AlertDialog 包含 NumberPicker
        AlertDialog.Builder(this)
            .setTitle("请选择性别")
            .setView(numberPicker)
            .setPositiveButton("确定") { dialog, _ ->
                // 获取选中的数组下标对应的性别
                val selectedGender = genders[numberPicker.value]
                // 例如可更新某个 TextView 显示选中的性别
                gender.text = selectedGender
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun checkCameraPermission() {
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

    private fun checkGalleryPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }

            else -> ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_GALLERY
            )
        }
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
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
            REQUEST_CAMERA -> currentPhotoUri?.let { startCrop(it) }
            REQUEST_GALLERY -> data?.data?.let { startCrop(it) }
            UCrop.REQUEST_CROP -> handleCropResult(data)
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = createCropDestinationUri()
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
            .withOptions(getCropOptions())
            .start(this)
    }

    private fun getCropOptions(): UCrop.Options {
        return UCrop.Options().apply {
            setCircleDimmedLayer(true) // 设置圆形裁剪
            setShowCropFrame(false) // 隐藏裁剪框
            setShowCropGrid(false) // 隐藏网格
            setCompressionQuality(COMPRESS_QUALITY) // 设置压缩质量
            setHideBottomControls(true) // 隐藏底部控制栏
            setToolbarColor(ContextCompat.getColor(this@UserProfileActivity, R.color.white))
            setStatusBarColor(ContextCompat.getColor(this@UserProfileActivity, R.color.white))
        }
    }

    private fun createCropDestinationUri(): Uri {
        val file = File(this.cacheDir, "cropped_${System.currentTimeMillis()}.png")
        return FileProvider.getUriForFile(
            this,
            "${this.packageName}.fileprovider",
            file
        )
    }

    private fun loadAvatar(uri: String) {
        Glide.with(this)
            .load(uri)
            .override(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(avatar)
    }


    /**
     * 处理裁剪结果
     * @param data Intent 数据
     */
    private fun handleCropResult(data: Intent?) {
        val resultUri = UCrop.getOutput(data!!)
        resultUri?.let { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val compressedFile = compressImage(uri) // 压缩图片
                    withContext(Dispatchers.Main) {
                        store.dispatch(
                            UserAction.UpdateAvatar(
                                compressedFile.toUri().toString()
                            )
                        ) // 更新头像
                        store.dispatch(UserAction.UploadAvatar(compressedFile))
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        CustomToast.showMessage(
                            this@UserProfileActivity,
                            "图片处理失败：${e.message}"
                        )
                    }
                }
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

    /**
     * 压缩图片
     * @param uri 要压缩的图片 Uri
     * @return 压缩后的文件
     */
    private fun compressImage(uri: Uri): File {
        // 获取图片原始尺寸
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        // 计算压缩比例
        val scale = calculateInSampleSize(
            options.outWidth,
            options.outHeight,
            MAX_IMAGE_SIZE,
            MAX_IMAGE_SIZE
        )

        // 加载压缩后的图片
        options.apply {
            inJustDecodeBounds = false
            inSampleSize = scale
        }

        val bitmap = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IOException("无法加载图片")

        // 保存压缩后的图片
        val outputFile = createImageFile()
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, out)
            out.flush()
        }
        bitmap.recycle()
        return outputFile
    }

    /**
     * 计算图片压缩比例
     */
    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent) {
        if (finishEvent.name == "updateUserProfile") {
            finish()
        }
    }
}