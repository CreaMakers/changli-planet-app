package com.creamaker.changli_planet_app.auth.ui

import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.auth.data.remote.dto.UserEmail
import com.creamaker.changli_planet_app.auth.redux.action.LoginAndRegisterAction
import com.creamaker.changli_planet_app.auth.redux.store.LoginAndRegisterStore
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.noOpDelegate
import com.creamaker.changli_planet_app.databinding.ActivityLoginByEmailBinding
import com.creamaker.changli_planet_app.utils.Event.FinishEvent
import com.creamaker.changli_planet_app.utils.singleClick
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class LoginByEmailActivity : FullScreenActivity<ActivityLoginByEmailBinding>() {
    private val email:EditText by lazy { binding.email }
    private val captcha:EditText by lazy { binding.captcha }
    private val getCaptcha:TextView by lazy { binding.getCaptcha }
    private val login:TextView by lazy { binding.login }
    private val agreementCheckBox: CheckBox by lazy { binding.agreementCheckbox }
    private val forgetPassword:TextView by lazy { binding.forget }
    private val loginByAccount:TextView by lazy { binding.loginAccount }
    private val route: TextView by lazy { binding.route }
    val store=LoginAndRegisterStore()

    override fun createViewBinding(): ActivityLoginByEmailBinding = ActivityLoginByEmailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initListener()
        setUnderLine()
        setTextWatcher()
        EventBus.getDefault().register(this)
    }

    private fun initListener(){
        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{state->
                    login.isEnabled=state.isEnableByEmail
                    if (state.isEnableByEmail) {
                        login.setBackgroundResource(R.drawable.enable_button)
                    } else {
                        login.setBackgroundResource(R.drawable.disable_button)
                    }

                    if(!state.isCountDown&&state.email.isNotEmpty()){
                        getCaptcha.setTextColor(resources.getColor(R.color.color_text_functional))
                        getCaptcha.text="获取验证码"
                        getCaptcha.singleClick (delay = 3000){
                            store.dispatch(LoginAndRegisterAction.GetCaptchaByLogin)
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
        login.setOnClickListener{
            store.dispatch(LoginAndRegisterAction.LoginByEmail(
                UserEmail(
                    email = email.text.toString(),
                    verifyCode = captcha.text.toString()
                ),
                this
            ))
        }
        agreementCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                store.dispatch(LoginAndRegisterAction.InputLoginByEmail("checked", "checkbox"))
            } else {
                store.dispatch(LoginAndRegisterAction.InputLoginByEmail("unchecked", "checkbox"))
            }

        }
    }

    private fun setTextWatcher(){
        val emailTextWatcher=object:TextWatcher by noOpDelegate(){
            private var isUpdating=false

            override fun afterTextChanged(s: Editable?) {
                if(isUpdating)return
                isUpdating=true
                store.dispatch(LoginAndRegisterAction.InputLoginByEmail(email.text.toString(),"email"))
                isUpdating=false
            }
        }
        val captchaTextWatcher=object:TextWatcher by noOpDelegate(){
            private var isUpdating=false

            override fun afterTextChanged(s: Editable?) {
                if(isUpdating)return
                isUpdating=true
                store.dispatch(LoginAndRegisterAction.InputLoginByEmail(captcha.text.toString(),"captcha"))
                isUpdating=false
            }
        }
        email.addTextChangedListener(emailTextWatcher)
        captcha.addTextChangedListener(captchaTextWatcher)
    }

    private fun setUnderLine() {
        getUnderLineScope(route,6,8)
        getUnderLineScope(forgetPassword,0,4)
        getUnderLineScope(loginByAccount,0,4)
        route.setOnClickListener {
            Route.goRegister(this)
        }
        loginByAccount.setOnClickListener{
            Route.goLoginForcibly(this)
        }
        forgetPassword.setOnClickListener{
            Route.goForgetPassword(this)
        }
    }

    private fun getUnderLineScope(view: TextView,start:Int,end:Int){
        var underlinetext = SpannableString(view.text.toString())
        underlinetext.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.text = underlinetext
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent){
        if(finishEvent.name=="LoginByEmail"){
            finish()
        }
    }
}