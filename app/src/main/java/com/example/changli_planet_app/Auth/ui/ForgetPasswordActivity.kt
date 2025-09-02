package com.example.changli_planet_app.Auth.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.example.changli_planet_app.Activity.Action.AccountSecurityAction
import com.example.changli_planet_app.Activity.Store.AccountSecurityStore
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.noOpDelegate
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.Event.FinishEvent
import com.example.changli_planet_app.Utils.singleClick
import com.example.changli_planet_app.databinding.ActivityForgetPasswordBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * 找回密码页面
 */
class ForgetPasswordActivity : FullScreenActivity() {
    private lateinit var binding: ActivityForgetPasswordBinding
    private val email: EditText by lazy { binding.email }
    private val captcha: EditText by lazy { binding.captcha }
    private val getCaptcha: TextView by lazy { binding.getCaptcha }
    private val newPassword: EditText by lazy { binding.newPassword }
    private val confirmPassword: EditText by lazy { binding.confirmPassword }
    private val change: TextView by lazy { binding.changePassword }
    private val store= AccountSecurityStore()
    private val disposables by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                        change.setBackgroundResource(R.drawable.enable_button)
                    } else {
                        change.setBackgroundResource(R.drawable.disable_button)
                    }

                    if(!state.isCountDown&&state.email.isNotEmpty()){
                        getCaptcha.setTextColor(resources.getColor(R.color.primary_blue))
                        getCaptcha.text="获取验证码"
                        getCaptcha.singleClick(delay = 3000){
                            store.dispatch(AccountSecurityAction.GetCaptcha)
                        }
                    }else{
                        if(state.countDown>0)getCaptcha.text=state.countDown.toString()
                        else getCaptcha.text="获取验证码"
                        getCaptcha.setTextColor(resources.getColor(R.color.color_7E7E7E))
                        getCaptcha.setOnClickListener(null)
                    }

                    binding.apply {
                        lengthIcon.setImageResource(if(state.isLengthValid) R.drawable.dui else R.drawable.cuo)
                        upperLowerIcon.setImageResource(if(state.hasUpperAndLower) R.drawable.dui else R.drawable.cuo)
                        numberSpecialIcon.setImageResource(if(state.hasNumberAndSpecial) R.drawable.dui else R.drawable.cuo)
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