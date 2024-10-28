package com.example.changli_planet_app.Activity
import android.os.Bundle
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
    private val schoolList:List<String> = listOf("金盆岭校区","云塘校区")
    private val dorList:List<String> = listOf("16栋A区","16栋B区","17栋","弘毅轩1栋A区","弘毅轩1栋B区","弘毅轩2栋A区1-6楼","弘毅轩2栋B区","弘毅轩2栋C区","弘毅轩2栋D区")
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
        dor.setOnClickListener {
            wheel(dorList)
        }
        school.setOnClickListener {
            wheel(schoolList)
        }
        back.setOnClickListener {
            finish()
        }
    }
    private fun wheel(item:List<String>){
        val Wheel = WheelBottomDialog()
        Wheel.setItem(item)
        Wheel.show(supportFragmentManager,"wheel")
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