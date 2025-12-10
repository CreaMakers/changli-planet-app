package com.creamaker.changli_planet_app.auth.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.auth.redux.action.AccountSecurityAction
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.core.noOpDelegate
import com.creamaker.changli_planet_app.databinding.ActivityForgetPasswordBinding
import com.creamaker.changli_planet_app.settings.redux.store.AccountSecurityStore
import com.creamaker.changli_planet_app.utils.Event.FinishEvent
import com.creamaker.changli_planet_app.utils.singleClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * 找回密码页面
 */
class ForgetPasswordActivity : FullScreenActivity<ActivityForgetPasswordBinding>() {
    private val email: EditText by lazy { binding.email }
    private val captcha: EditText by lazy { binding.captcha }
    private val getCaptcha: TextView by lazy { binding.getCaptcha }
    private val newPassword: EditText by lazy { binding.newPassword }
    private val confirmPassword: EditText by lazy { binding.confirmPassword }
    private val change: TextView by lazy { binding.changePassword }
    private val store= AccountSecurityStore()

    override fun createViewBinding(): ActivityForgetPasswordBinding = ActivityForgetPasswordBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initListener()
        initTextWatcher()
        EventBus.getDefault().register(this)
    }

    private fun initListener(){
        disposables.add(
            store.state().
                subscribe{state->
                    change.isEnabled=state.isEnable
                    if (state.isEnable) {
                        change.setBackgroundResource(R.drawable.bg_enable_button)
                    } else {
                        change.setBackgroundResource(R.drawable.disable_button)
                    }

                    if(!state.isCountDown&&state.email.isNotEmpty()){
                        getCaptcha.setTextColor(resources.getColor(R.color.color_text_functional))
                        getCaptcha.text="获取验证码"
                        getCaptcha.singleClick(delay = 3000){
                            store.dispatch(AccountSecurityAction.GetCaptcha)
                        }
                    }else{
                        if(state.countDown>0)getCaptcha.text=state.countDown.toString()
                        else getCaptcha.text="获取验证码"
                        getCaptcha.setTextColor(resources.getColor(R.color.color_text_grey))
                        getCaptcha.setOnClickListener(null)
                    }

                    binding.apply {
                        lengthIcon.setImageResource(if (state.isLengthValid) R.drawable.ic_confirm else R.drawable.ic_deny)
                    }
                }
        )
        store.dispatch(AccountSecurityAction.initilaize)
        change.setOnClickListener{
            store.dispatch(AccountSecurityAction.ChangeByEmail(this))
        }
    }

    private fun initTextWatcher(){
        setTextWatcher(email,"email")
        setTextWatcher(captcha,"captcha")
        setTextWatcher(newPassword,"password")
        setTextWatcher(confirmPassword,"confirmPassword")
    }

    private fun setTextWatcher(view: TextView, type:String){
        val textWatcher=object : TextWatcher by noOpDelegate() {
            private var isUpdating=false

            override fun afterTextChanged(s: Editable?) {
                if(isUpdating)return
                isUpdating=true
                store.dispatch(AccountSecurityAction.Input(view.text.toString(),type))
                isUpdating=false
            }
        }
        view.addTextChangedListener(textWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent){
        if(finishEvent.name=="changePasswordByEmail"){
            finish()
        }
    }
}