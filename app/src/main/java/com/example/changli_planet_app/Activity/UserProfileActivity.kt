package com.example.changli_planet_app.Activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.widget.Toast
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
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Data.jsonbean.UserProfileRequest
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Widget.View.CustomToast
import com.example.changli_planet_app.Widget.Dialog.PhotoPickerDialog
import com.example.changli_planet_app.Widget.Dialog.UserProfileWheelBottomDialog
import com.example.changli_planet_app.Util.Event.FinishEvent
import com.example.changli_planet_app.Util.GlideUtils
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

class UserProfileActivity : FullScreenActivity() {

    companion object {
        private const val REQUEST_CAMERA = 1001
        private const val REQUEST_GALLERY = 1002
        private const val REQUEST_PROVINCE = 1112
        private const val REQUEST_CITY = 1113

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
    private val location by lazy { binding.userProfileLocation }

    private val genderLayout by lazy { binding.genderLayout }
    private val gradeLayout by lazy { binding.gradeLayout }
    private val birthdayLayout by lazy { binding.birthdayLayout }
    private val locationLayout by lazy { binding.locationLayout }

    private val submit by lazy { binding.userProfileSubmit }

    val maxHeight by lazy {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        screenHeight / 2
    }
    // data

    private val genderList = listOf("男", "女", "保密")

    val gradeList = listOf(
        "保密~",
        "大一",
        "大二",
        "大三",
        "大四",
        "研一",
        "研二",
        "研三",
        "博一",
        "博二",
        "博三",
        "在职人员"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeState()
        store.dispatch(UserAction.initilaize())
        setAvatar.setOnClickListener { setAvatar() }
        back.setOnClickListener { finish() }
        birthdayLayout.setOnClickListener { setBirthday() }
        genderLayout.setOnClickListener { showGenderPickerDialog(genderList) }
        gradeLayout.setOnClickListener { showGradePickerDialog(gradeList) }
        locationLayout.setOnClickListener {
            val intent = Intent(this@UserProfileActivity, ProvinceActivity::class.java)
            startActivityForResult(intent, REQUEST_PROVINCE)
        }
        submit.setOnClickListener { updateUserProfile() }
        EventBus.getDefault().register(this)
    }

    private fun updateUserProfile() {
        // 头像去store中再填
        val userProfileRequest = UserProfileRequest(
            bio = bio.text.toString(),
            gender = when (gender.text.toString()) {
                "男" -> 0
                "女" -> 1
                else -> 2
            },
            grade = grade.text.toString(),
            birthDate = birthday.text.toString(),
            location = location.text.toString(),
            website = website.text.toString()
        )
        store.dispatch(UserAction.UpdateUserProfile(userProfileRequest, this))
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
                    grade.text = state.userProfile.grade
                    birthday.text = userProfile.birthDate
                    gender.text = genderList[state.userProfile.gender]
                    location.text = state.userProfile.location
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
        return "${year}-${String.format(Locale.getDefault(), "%02d-%02d", month + 1, day)}"
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

    private fun showGenderPickerDialog(list: List<String>) {
        clickWheel(list)
    }

    private fun showGradePickerDialog(list: List<String>) {
        clickWheel(list)
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

    private fun clickWheel(item: List<String>) {
        val wheel = UserProfileWheelBottomDialog(
            this@UserProfileActivity,
            store,
            maxHeight, {
                gender.text = it
            }, {
                grade.text = it
            }
        )
        wheel.setItem(item)
        wheel.show(supportFragmentManager, "UserProfileWheel")
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
            REQUEST_PROVINCE -> {
                val province = data?.getStringExtra("province")
                val city = data?.getStringExtra("city")
                location.text = "$province $city"
            }
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
        GlideUtils.loadWithThumbnail(this, avatar, uri)
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
        if (finishEvent.name == "updateUser") {
            finish()
        }
    }
}