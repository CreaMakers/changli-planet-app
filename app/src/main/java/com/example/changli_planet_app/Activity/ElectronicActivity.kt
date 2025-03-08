package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.text.InputFilter
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewStub
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.changli_planet_app.Activity.Action.ElectronicAction
import com.example.changli_planet_app.Activity.Store.ElectronicStore
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Data.jsonbean.CheckElectricity
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Widget.Dialog.WheelBottomDialog
import com.example.changli_planet_app.databinding.ActivityElectronicBinding
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class ElectronicActivity : FullScreenActivity() {
    companion object {
        val ALPHANUMERIC_REGEX = Regex("^[a-zA-Z0-9]*$")
    }

    lateinit var binding: ActivityElectronicBinding
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val back: ImageView by lazy { binding.back }
    private val school: TextView by lazy { binding.school }
    private val dor: TextView by lazy { binding.dor }
    private val query_ele: TextView by lazy { binding.queryElec }
    private val dor_number: EditText by lazy { binding.dorNumber }
    private val viewstub: ViewStub by lazy { binding.stubTv }
    private val queryText: TextView by lazy { viewstub.inflate().findViewById(R.id.tv_result) }
    private val electronicStore = ElectronicStore(this)
    private val disposables by lazy { CompositeDisposable() }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
        initObserve()
    }

    private fun initView() {
        binding = ActivityElectronicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inputFilter(dor_number)
    }

    private fun initListener() {
        dor.setOnClickListener {
            val filteredList = when (school.text) {
                "云塘校区" -> dorList.subList(0, 45)
                "金盆岭校区" -> dorList.subList(45, dorList.size)
                else -> dorList
            }
            ClickWheel(filteredList)
        }
        school.setOnClickListener { ClickWheel(schoolList) }
        query_ele.setOnClickListener {
            electronicStore.dispatch(
                ElectronicAction.queryElectronic(
                    CheckElectricity(
                        school.text.toString(),
                        dor.text.toString(),
                        dor_number.text.toString()
                    )
                )
            )
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
                    if (viewstub.parent == null) {
                        val text = findViewById<TextView>(R.id.tv_result)
                        text.text = state.elec
                    } else {
                        val td = viewstub.inflate() as View
                        val text = td.findViewById<TextView>(R.id.tv_result)
                        text.text = state.elec
                    }
                    state.isElec = false
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

    override fun onResume() {
        super.onResume()
        school.text = mmkv.decodeString("school", "选择校区")
        dor.text = mmkv.decodeString("dor", "选择宿舍楼")
        dor_number.setText(mmkv.decodeString("dor_number", ""))
        electronicStore.dispatch(ElectronicAction.selectAddress(school.text.toString()))
        electronicStore.dispatch(ElectronicAction.selectBuild(dor.text.toString()))
    }

    override fun onDestroy() {
        super.onDestroy()
        mmkv.encode("school", school.text.toString())
        mmkv.encode("dor", dor.text.toString())
        mmkv.encode("dor_number", dor_number.text.toString())
        disposables.clear()
    }
}