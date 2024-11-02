package com.example.changli_planet_app.Activity
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.R
import com.example.changli_planet_app.UI.WheelBottomDialog
import com.example.changli_planet_app.Util.Event.SelectEvent
import com.example.changli_planet_app.Util.EventBusLifecycleObserver
import com.example.changli_planet_app.databinding.ActivityElectronicBinding
import com.example.changli_planet_app.databinding.ActivityLoginBinding
import org.greenrobot.eventbus.Subscribe

class Electronic : AppCompatActivity() {
    lateinit var binding: ActivityElectronicBinding
    private val back:ImageView by lazy { binding.back }
    private val school:TextView by lazy { binding.school }
    private val dor:TextView by lazy { binding.dor }
    private val query_ele:TextView by lazy { binding.queryElec }
    private val dor_number:EditText by lazy { binding.dorNumber }
    private val schoolList:List<String> = listOf("金盆岭校区","云塘校区")
    private val dorList:List<String> =
        listOf("16栋A区","16栋B区","17栋","弘毅轩1栋A区","弘毅轩1栋B区","弘毅轩2栋A区1-6楼",
        "弘毅轩2栋B区","弘毅轩2栋C区","弘毅轩2栋D区","弘毅轩3栋A区","弘毅轩3栋B区","弘毅轩4栋A区",
        "弘毅轩4栋B区","留学生宿舍","敏行轩1栋A区","敏行轩1栋B区","敏行轩2栋A区","敏行轩2栋B区","敏行轩3栋A区",
        "敏行轩3栋B区","敏行轩4栋A区","敏行轩4栋B区","行健轩1栋A区","行健轩1栋B区","行健轩2栋A区","行健轩2栋B区",
        "行健轩3栋A区","行健轩3栋B区","行健轩4栋A区","行健轩4栋B区","行健轩5栋A区","行健轩5栋B区","行健轩6栋A区",
        "行健轩6栋B区","至诚轩1栋A区","至诚轩1栋B区","至诚轩2栋A区","至诚轩2栋B区","至诚轩3栋A区","至诚轩3栋B区",
        "至诚轩4栋A区","至诚轩4栋B区","西苑1栋","西苑2栋","西苑3栋","西苑4栋","西苑5栋","西苑6栋","西苑7栋","西苑8栋",
        "西苑9栋","西苑10栋","西苑11栋","东苑4栋","东苑5栋","东苑6栋","东苑9栋","东苑14栋","东苑14栋","南苑3栋","南苑4栋",
        "南苑5栋","南苑7栋","南苑8栋",)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycle.addObserver(EventBusLifecycleObserver(this))
        binding = ActivityElectronicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ClickWheel(dor,dorList)
        ClickWheel(school,schoolList)
        inputFilter(dor_number)
        query_ele.setOnClickListener {
            binding.stubTv.inflate() as TextView
        }
        back.setOnClickListener {
            finish()
        }
    }
    private fun inputFilter(editText: EditText){
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            // 允许的字符是英文字母和数字
            val regex = Regex("^[a-zA-Z0-9]*$")
            // 如果输入内容符合正则表达式，则允许输入，否则返回空字符串禁止输入
            if (regex.matches(source)) source else ""
        }
        editText.filters = arrayOf(inputFilter)
    }
    private fun ClickWheel(button: TextView,item:List<String>){
        button.setOnClickListener {
            val Wheel = WheelBottomDialog()
            Wheel.setItem(item)
            Wheel.show(supportFragmentManager,"wheel")
        }
    }
    @Subscribe
    fun onClickText(selectEvent: SelectEvent){
        if(selectEvent.eventType==2){
            if(selectEvent.text.contains("校区")){
                school.text = selectEvent.text
            }else dor.text = selectEvent.text
        }
    }
}