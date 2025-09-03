package com.example.changli_planet_app.feature.lostfound.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityPublishLoseThingBinding
import com.example.changli_planet_app.feature.lostfound.redux.action.LoseThingAction
import com.example.changli_planet_app.feature.lostfound.redux.store.LoseThingStore
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * 发布失物招领
 */
class PublishLoseThingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPublishLoseThingBinding
    private val store= LoseThingStore()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityPublishLoseThingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        store.state()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{state->
                if(!state.isEnable){
                    binding.losePublish.setBackgroundResource(R.drawable.un_enable_button)
                }
                else{
                    binding.losePublish.setBackgroundResource(R.drawable.enable_button)             //当标题，联系人电话，姓名完整时，可发布
                }
            }
        store.dispatch(LoseThingAction.initilaize)
        setTextWatcher()                                         //对输入的文字进行监听
        //setImageWatcher()                                        //对输入的图片进行监听


        binding.loseCancel.setOnClickListener{finish()}
    }

    private fun setTextWatcher(){
        val loseThingNameTextWatcher=object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(LoseThingAction.inputText(binding.loseThingName.text.toString(),"loseThingName"))
            }
        }
        val loseThingDescribeTextWatcher=object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(LoseThingAction.inputText(binding.loseThingDescribe.text.toString(),"loseThingDescribe"))
            }
        }
        val nameTextWatcher= object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(LoseThingAction.inputText(binding.losePeople.text.toString(),"name"))
            }
        }
        val phoneTextWatcher= object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(LoseThingAction.inputText(binding.losePeoplePhone.text.toString(),"phone"))
            }
        }
        binding.loseThingName.addTextChangedListener(loseThingNameTextWatcher)
        binding.loseThingDescribe.addTextChangedListener(loseThingDescribeTextWatcher)
        binding.losePeople.addTextChangedListener(nameTextWatcher)
        binding.losePeoplePhone.addTextChangedListener(phoneTextWatcher)
    }
    private fun setImageWatcher(){
        //TODO
    }
}