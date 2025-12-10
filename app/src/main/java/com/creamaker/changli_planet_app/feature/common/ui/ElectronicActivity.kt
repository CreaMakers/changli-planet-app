package com.creamaker.changli_planet_app.feature.common.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.creamaker.changli_planet_app.ElectronicAppWidget
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.databinding.ActivityElectronicBinding
import com.creamaker.changli_planet_app.feature.common.contract.ElectronicContract
import com.creamaker.changli_planet_app.feature.common.viewModel.ElectronicViewModel
import com.creamaker.changli_planet_app.utils.load
import com.creamaker.changli_planet_app.widget.dialog.WheelBottomDialog
import com.google.android.material.imageview.ShapeableImageView
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 电费查询
 */
class ElectronicActivity : FullScreenActivity<ActivityElectronicBinding>() {
    companion object {
        val ALPHANUMERIC_REGEX = Regex("^[a-zA-Z0-9]*$")
    }

    private val viewModel: ElectronicViewModel by viewModels()
    private val mmkv by lazy { MMKV.defaultMMKV() }

    private val back: ImageView by lazy { binding.back }
    private val school: TextView by lazy { binding.tvSchoolSelect }
    private val dor: TextView by lazy { binding.tvDormSelect }
    private val ele_query: TextView by lazy { binding.queryEle }
    private val door_number: EditText by lazy { binding.tvDoorInput }
    private val ele_image: ShapeableImageView by lazy { binding.eleImg }
    private val ele_num: TextView by lazy { binding.eleNum }
    private val ele_state :TextView by lazy { binding.eleState }

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
            // ViewModel handles state, but we might want to sync UI if needed immediately?
            // Usually VM state is preserved. If VM is recreated, we might need savedInstanceState or MMKV.
            // Let's rely on onResume to load from MMKV or Init intent.
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
            showWheelDialog(filteredList) { selected ->
                viewModel.processIntent(ElectronicContract.Intent.SelectDorm(selected))
            }
        }
        school.setOnClickListener {
            showWheelDialog(schoolList) { selected ->
                viewModel.processIntent(ElectronicContract.Intent.SelectSchool(selected))
            }
        }
        ele_query.setOnClickListener {
            val processedDoorNumber = processDormAndRoom(dor.text.toString(), door_number.text.toString())
            viewModel.processIntent(
                ElectronicContract.Intent.QueryElectricity(
                    school.text.toString(),
                    dor.text.toString(),
                    processedDoorNumber
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
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                school.text = state.address
                dor.text = state.buildId

                if (state.isElec) {
                    val regex = Regex("(\\d*\\.?\\d+)")
                    val matchResult = regex.find(state.elec)
                    val electronicValue = matchResult?.value?.toFloatOrNull()

                    when{
                        electronicValue == null ->{
                            ele_image.load(R.drawable.ic_electricity_default)
                            ele_num.text = getString(R.string.ele_query_false)
                            ele_state.text = getString(R.string.ele_state_unknown)
                        }
                        electronicValue in 0.0f..20f -> {
                            ele_image.load(R.drawable.ic_electricity_none)
                            ele_num.text =
                                getString(R.string.ele_queryNow, electronicValue.toString())
                            ele_state.text = getString(R.string.ele_state_low)
                            ele_state.setTextColor(getColor(R.color.color_base_red))
                        }
                        electronicValue in 20.1f..100f ->{
                            ele_image.load(R.drawable.ic_electricity_low)
                            ele_num.text =
                                getString(R.string.ele_queryNow, electronicValue.toString())
                            ele_state.text = getString(R.string.ele_state_normal)
                            ele_state.setTextColor(getColor(R.color.color_base_yellow))
                        }
                        electronicValue > 100f ->{
                            ele_image.load(R.drawable.ic_electricity_high)
                            ele_num.text =
                                getString(R.string.ele_queryNow, electronicValue.toString())
                            ele_state.text = getString(R.string.ele_state_high)
                            ele_state.setTextColor(getColor(R.color.color_base_green))
                        }
                    }
                }
            }
        }

        // No effects currently used, but set up for future
        lifecycleScope.launch {
            viewModel.effect.collect { effect ->
                // Handle effects
            }
        }
    }

    private fun inputFilter(editText: EditText) {
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            val regex = ALPHANUMERIC_REGEX
            if (regex.matches(source)) source else ""
        }
        editText.filters = arrayOf(inputFilter)
    }

    private fun showWheelDialog(item: List<String>, onSelect: (String) -> Unit) {
        val wheel = WheelBottomDialog(maxHeight, onSelect)
        wheel.setItem(item)
        wheel.show(supportFragmentManager, "wheel")
    }

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

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        val savedSchool = mmkv.decodeString("school", "选择校区") ?: "选择校区"
        val savedDor = mmkv.decodeString("dor", "选择宿舍楼") ?: "选择宿舍楼"
        val savedDoorNum = mmkv.decodeString("door_number", "") ?: ""

        door_number.setText(savedDoorNum)

        if (savedSchool == "选择校区" || savedDor == "选择宿舍楼" || savedDoorNum.isEmpty()) {
            ele_image.load(R.drawable.ic_electricity_default)
            ele_num.text = getString(R.string.ele_queryDefault)
            ele_state.text = getString(R.string.ele_state_unknown)
            mmkv.encode("isFirstLaunch", false)

            // Sync initial state to VM without triggering query
            viewModel.processIntent(
                ElectronicContract.Intent.Init(
                    savedSchool,
                    savedDor,
                    savedDoorNum
                )
            )
        } else {
            val processedDoorNumber = processDormAndRoom(savedDor, savedDoorNum)
            // Trigger load if data exists
            viewModel.processIntent(
                ElectronicContract.Intent.Init(
                    savedSchool,
                    savedDor,
                    processedDoorNumber
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

    private fun processDormAndRoom(dor: String, doorNumber: String): String {
        val containsA = dor.contains('A')
        val containsB = dor.contains('B')
        val doorContainsLetter = doorNumber.any { it.isLetter() }

        return when {
            containsA && !doorContainsLetter -> "A$doorNumber"
            containsB && !doorContainsLetter -> "B$doorNumber"
            else -> doorNumber
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("ele_school", school.text.toString())
        outState.putString("ele_dor", dor.text.toString())
        outState.putString("ele_door", door_number.text.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        mmkv.encode("school", school.text.toString())
        mmkv.encode("dor", dor.text.toString())
        mmkv.encode("door_number", door_number.text.toString())
    }
}