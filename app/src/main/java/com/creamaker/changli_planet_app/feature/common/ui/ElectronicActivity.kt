package com.creamaker.changli_planet_app.feature.common.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.creamaker.changli_planet_app.ElectronicAppWidget
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.databinding.ActivityElectronicBinding
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.CheckElectricity
import com.creamaker.changli_planet_app.feature.common.redux.action.ElectronicAction
import com.creamaker.changli_planet_app.feature.common.redux.store.ElectronicStore
import com.creamaker.changli_planet_app.utils.load
import com.creamaker.changli_planet_app.widget.dialog.WheelBottomDialog
import com.google.android.material.imageview.ShapeableImageView
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers


/**
 * 电费查询
 */
class ElectronicActivity : FullScreenActivity<ActivityElectronicBinding>() {
    companion object {
        val ALPHANUMERIC_REGEX = Regex("^[a-zA-Z0-9]*$")
    }

    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val back: ImageView by lazy { binding.back }
    private val school: TextView by lazy { binding.tvSchoolSelect }
    private val dor: TextView by lazy { binding.tvDormSelect }
    private val ele_query: TextView by lazy { binding.queryEle }
    private val door_number: EditText by lazy { binding.tvDoorInput }
    private val ele_image: ShapeableImageView by lazy { binding.eleImg }
    private val ele_num: TextView by lazy { binding.eleNum }
    private val ele_state :TextView by lazy { binding.eleState }
    private val electronicStore = ElectronicStore(this)
    private val schoolList: List<String> by lazy {
        resources.getStringArray(R.array.school_location).toList()
    }
    private val maxHeight by lazy {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        screenHeight / 2
    }
    private val dorList: List<String> by lazy {
        resources.getStringArray(R.array.dormitory).toList()
    }

    override fun createViewBinding(): ActivityElectronicBinding = ActivityElectronicBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
        initObserve()
        recoverInstance(savedInstanceState)
    }

    private fun recoverInstance(savedInstanceState: Bundle?) {
        if (savedInstanceState != null){
            Log.d("Qingyue","saved")
            school.text = savedInstanceState.getString("ele_school", "选择校区")
            dor.text = savedInstanceState.getString("ele_dor", "选择宿舍楼")
            door_number.setText(savedInstanceState.getString("ele_door", ""))

        }
    }

    private fun initView() {

        ViewCompat.setOnApplyWindowInsetsListener(binding.tbQuery){ view, windowInsets->
            val insets=windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )
            WindowInsetsCompat.CONSUMED
        }
        inputFilter(door_number)
        binding.sivRoomNumber.load(R.drawable.ic_electricity_door)
        binding.sivSchoolRegion.load(R.drawable.ic_electricity_school)
        binding.sivDormBuilding.load(R.drawable.ic_electricity_dorm)
    }

    private fun initListener() {
        door_number.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                door_number.hint = ""
            }
            else{
                if (door_number.text.isEmpty()){
                    door_number.hint = getString(R.string.selectDoorNum)
                }
            }
        }
        dor.setOnClickListener {
            val filteredList = when (school.text) {
                "云塘校区" -> dorList.subList(0, 45)
                "金盆岭校区" -> dorList.subList(45, dorList.size)
                else -> dorList
            }
            ClickWheel(filteredList)
        }
        school.setOnClickListener { ClickWheel(schoolList) }
        ele_query.setOnClickListener {
            // 处理宿舍楼和房间号逻辑
            val processedDoorNumber = processDormAndRoom(dor.text.toString(), door_number.text.toString())
            electronicStore.dispatch(
                ElectronicAction.queryElectronic(
                    CheckElectricity(
                        school.text.toString(),
                        dor.text.toString(),
                        processedDoorNumber
                    )
                )
            )
            mmkv.encode("school", school.text.toString())
            mmkv.encode("dor", dor.text.toString())
            mmkv.encode("door_number", door_number.text.toString())
            refreshWidget()
        }
        back.setOnClickListener { finish() }
    }

    private fun initObserve() {
        disposables.add(electronicStore._state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                school.text = state.address
                dor.text = state.buildId
                if (state.isElec) {
                    // 1. 使用正则表达式提取字符串中的数字（包括小数）
                    val regex = Regex("(\\d*\\.?\\d+)")
                    val matchResult = regex.find(state.elec)

                    // 2. 将提取到的数字字符串转换为 Float，如果失败则默认为 -1.0f
                    val electronicValue = matchResult?.value?.toFloatOrNull()
                    when{
                        electronicValue == null ->{
                            ele_image.load(R.drawable.ic_electricity_default)
                            ele_num.text = getString(R.string.ele_query_false)
                            ele_state.text =getString(R.string.ele_state_unknown)
                        }
                        electronicValue in 0.0f..20f -> {
                            ele_image.load(R.drawable.ic_electricity_none)
                            ele_num.text = getString(R.string.ele_queryNow,electronicValue.toString())
                            ele_state.text = getString(R.string.ele_state_low)
                            ele_state.setTextColor(getColor(R.color.color_base_red))
                        }
                        electronicValue in 20.1f..100f ->{
                            ele_image.load(R.drawable.ic_electricity_low)
                            ele_num.text = getString(R.string.ele_queryNow,electronicValue.toString())
                            ele_state.text = getString(R.string.ele_state_normal)
                            ele_state.setTextColor(getColor(R.color.color_base_yellow))
                        }
                        electronicValue > 100f ->{
                            ele_image.load(R.drawable.ic_electricity_high)
                            ele_num.text = getString(R.string.ele_queryNow,electronicValue.toString())
                            ele_state.text = getString(R.string.ele_state_high)
                            ele_state.setTextColor(getColor(R.color.color_base_green))
                        }

                    }
                }
            })
    }

    private fun inputFilter(editText: EditText) {
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            // 允许的字符是英文字母和数字
            val regex = ALPHANUMERIC_REGEX
            // 如果输入内容符合正则表达式，则允许输入，否则返回空字符串禁止输入
            if (regex.matches(source)) source else ""
        }
        editText.filters = arrayOf(inputFilter)
    }

    private fun ClickWheel(item: List<String>) {
        val Wheel = WheelBottomDialog(electronicStore, maxHeight)
        Wheel.setItem(item)
        Wheel.show(supportFragmentManager, "wheel")
    }

    /**
     * 点击外部区域隐藏键盘并清除EditText焦点
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = android.graphics.Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 隐藏键盘
     */
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        school.text = mmkv.decodeString("school", "选择校区")
        dor.text = mmkv.decodeString("dor", "选择宿舍楼")
        door_number.setText(mmkv.decodeString("door_number", ""))
        electronicStore.dispatch(ElectronicAction.selectAddress(school.text.toString()))
        electronicStore.dispatch(ElectronicAction.selectBuild(dor.text.toString()))
        if (school.text == "选择校区" || dor.text == "选择宿舍楼" || door_number.text.isEmpty()) {
            // 如果是初始状态，则显示默认UI
            ele_image.load(R.drawable.ic_electricity_default)
            ele_num.text = getString(R.string.ele_queryDefault)
            ele_state.text = getString(R.string.ele_state_unknown)
            mmkv.encode("isFirstLaunch", false)
        } else {
            // 如果不是初始状态，则自动查询
            val processedDoorNumber = processDormAndRoom(dor.text.toString(), door_number.text.toString())
            electronicStore.dispatch(
                ElectronicAction.queryElectronic(
                    CheckElectricity(
                        school.text.toString(),
                        dor.text.toString(),
                        processedDoorNumber
                    )
                )
            )
        }


    }

    private fun refreshWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(PlanetApplication.appContext)
        val componentName = ComponentName(PlanetApplication.appContext, ElectronicAppWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isNotEmpty()) {
            val intent = Intent(PlanetApplication.appContext, ElectronicAppWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            sendBroadcast(intent)
        }
    }

    /**
     * 处理宿舍楼和房间号逻辑
     * 当dor包含'A'或'B'且door_number中没有字母时，在nod参数中添加相应的字母
     */
    private fun processDormAndRoom(dor: String, doorNumber: String): String {
        // 检查dor是否包含'A'或'B'
        val containsA = dor.contains('A')
        val containsB = dor.contains('B')

        // 检查door_number是否包含字母
        val doorContainsLetter = doorNumber.any { it.isLetter() }

        // 如果dor包含'A'或'B'且door_number不包含字母，则添加相应字母
        return when {
            containsA && !doorContainsLetter -> "A$doorNumber"
            containsB && !doorContainsLetter -> "B$doorNumber"
            else -> doorNumber
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("ele_school",school.text.toString())
        outState.putString("ele_dor",dor.text.toString())
        outState.putString("ele_door",door_number.text.toString())


    }

    override fun onDestroy() {
        super.onDestroy()
        mmkv.encode("school", school.text.toString())
        mmkv.encode("dor", dor.text.toString())
        mmkv.encode("door_number", door_number.text.toString())
        disposables.clear()
    }
}