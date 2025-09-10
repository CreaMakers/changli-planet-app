package com.example.changli_planet_app.feature.lostfound.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.R
import com.example.changli_planet_app.base.FullScreenActivity
import com.example.changli_planet_app.databinding.ActivityPublishFoundThingBinding
import com.example.changli_planet_app.feature.lostfound.redux.action.FoundThingAction
import com.example.changli_planet_app.feature.lostfound.redux.store.FoundThingStore
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * 失误招领类
 */
class PublishFoundThingActivity : FullScreenActivity<ActivityPublishFoundThingBinding>() {
    private val store= FoundThingStore()
    override fun createViewBinding(): ActivityPublishFoundThingBinding = ActivityPublishFoundThingBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        store.state()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{state->
                if(!state.isEnable){
                    binding.foundPublish.setBackgroundResource(R.drawable.un_enable_button)
                }
                else{
                    binding.foundPublish.setBackgroundResource(R.drawable.enable_button)           //当标题，物品描述完整时，可发布
                }
            }
        store.dispatch(FoundThingAction.initilaize)
        setTextWatcher()                                                   //对输入的文字进行监听
        //setImageWatcher()                                                  //对输入的图片进行监听

        binding.foundCancel.setOnClickListener{finish()}
    }
    private fun setTextWatcher(){
        val foundThingNameTextWatcher=object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(FoundThingAction.inputText(binding.foundThingName.text.toString(),"foundThingName"))
            }
        }
        val foundThingDescribeTextWatcher=object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(FoundThingAction.inputText(binding.foundThingDescribe.text.toString(),"foundThingDescribe"))
            }
        }
        val nameTextWatcher= object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(FoundThingAction.inputText(binding.foundPeople.text.toString(),"name"))
            }
        }
        val idTextWatcher= object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(FoundThingAction.inputText(binding.foundPeopleId.text.toString(),"id"))
            }
        }
        binding.foundThingName.addTextChangedListener(foundThingNameTextWatcher)
        binding.foundThingDescribe.addTextChangedListener(foundThingDescribeTextWatcher)
        binding.foundPeople.addTextChangedListener(nameTextWatcher)
        binding.foundPeopleId.addTextChangedListener(idTextWatcher)
    }
    private fun setImageWatcher(){
        //TODO
    }
}