package com.creamaker.changli_planet_app.auth.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.auth.redux.action.LoginAndRegisterAction
import com.creamaker.changli_planet_app.auth.redux.store.LoginAndRegisterStore
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.core.noOpDelegate
import com.creamaker.changli_planet_app.databinding.ActivityBindEmailBinding
import com.creamaker.changli_planet_app.utils.Event.FinishEvent
import com.creamaker.changli_planet_app.utils.singleClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * 绑定邮箱页面
 */
class BindEmailActivity : FullScreenActivity<ActivityBindEmailBinding>() {
    private val email: EditText by lazy { binding.email }
    private val captcha: EditText by lazy { binding.captcha }
    private val getCaptcha: TextView by lazy { binding.getCaptcha }
    private val store = LoginAndRegisterStore()
    private val bindButton: TextView by lazy { binding.bind }

    override fun createViewBinding(): ActivityBindEmailBinding = ActivityBindEmailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initObserve()
        initTextWatcher()
        val account=intent.getStringExtra("username")?:""
        val password=intent.getStringExtra("password")?:""
        store.dispatch(LoginAndRegisterAction.input(account,"account"))
        store.dispatch(LoginAndRegisterAction.input(password,"password"))
        EventBus.getDefault().register(this)
        bindButton.singleClick(3000){
            store.dispatch(LoginAndRegisterAction.Register(this))
        }
    }
    private fun initObserve(){
        disposables.add(
            store.state().subscribe{state->
                if(!state.canBind){
                    bindButton.isEnabled=state.canBind
                    bindButton.setBackgroundResource(R.drawable.disable_button)
                }else{
                    bindButton.isEnabled=state.canBind
                    bindButton.setBackgroundResource(R.drawable.enable_button)
                }

                if(!state.isCountDown&&state.email.isNotEmpty()){
                    getCaptcha.setTextColor(resources.getColor(R.color.color_text_functional))
                    getCaptcha.text="获取验证码"
                    getCaptcha.singleClick (delay = 3000){
                        store.dispatch(LoginAndRegisterAction.GetCaptcha)
                    }
                }else{
                    if(state.countDown>0)getCaptcha.text=state.countDown.toString()
                    else getCaptcha.text="获取验证码"
                    getCaptcha.setTextColor(resources.getColor(R.color.color_text_grey))
                    getCaptcha.setOnClickListener(null)
                }
            }
        )
        store.dispatch(LoginAndRegisterAction.initilaize)
    }
    private fun initTextWatcher(){
        val emailTextWatcher=object: TextWatcher by noOpDelegate() {
            private var isUpdating=false

            override fun afterTextChanged(s: Editable?) {
                if(isUpdating)return
                isUpdating=true
                store.dispatch(LoginAndRegisterAction.input(email.text.toString(),"email"))
                isUpdating=false
            }
        }
        val captchaTextWatcher=object: TextWatcher by noOpDelegate() {
            private var isUpdating=false

            override fun afterTextChanged(s: Editable?) {
                if(isUpdating)return
                isUpdating=true
                store.dispatch(LoginAndRegisterAction.input(captcha.text.toString(),"captcha"))
                isUpdating=false
            }
        }
        email.addTextChangedListener(emailTextWatcher)
        captcha.addTextChangedListener(captchaTextWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        disposables.clear()
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent){
        if(finishEvent.name=="bindEmail"){
            finish()
        }
    }
}