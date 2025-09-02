package com.example.changli_planet_app.Auth.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.example.changli_planet_app.Activity.Action.LoginAndRegisterAction
import com.example.changli_planet_app.Activity.Store.LoginAndRegisterStore
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.noOpDelegate
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.Event.FinishEvent
import com.example.changli_planet_app.Utils.singleClick
import com.example.changli_planet_app.databinding.ActivityBindEmailBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * 绑定邮箱页面
 */
class BindEmailActivity : FullScreenActivity() {
    private lateinit var binding: ActivityBindEmailBinding

    private val email: EditText by lazy { binding.email }
    private val captcha: EditText by lazy { binding.captcha }
    private val getCaptcha: TextView by lazy { binding.getCaptcha }
    private val store = LoginAndRegisterStore()
    private val bindButton: TextView by lazy { binding.bind }
    private val disposables by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBindEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                    getCaptcha.setTextColor(resources.getColor(R.color.primary_blue))
                    getCaptcha.text="获取验证码"
                    getCaptcha.singleClick (delay = 3000){
                        store.dispatch(LoginAndRegisterAction.GetCaptcha)
                    }
                }else{
                    if(state.countDown>0)getCaptcha.text=state.countDown.toString()
                    else getCaptcha.text="获取验证码"
                    getCaptcha.setTextColor(resources.getColor(R.color.color_7E7E7E))
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