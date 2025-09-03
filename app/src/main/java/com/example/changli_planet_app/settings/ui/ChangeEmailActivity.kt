package com.example.changli_planet_app.settings.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.R
import com.example.changli_planet_app.base.FullScreenActivity
import com.example.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.example.changli_planet_app.core.noOpDelegate
import com.example.changli_planet_app.databinding.ActivityChangeEmailBinding
import com.example.changli_planet_app.settings.redux.action.ChangeEmailAction
import com.example.changli_planet_app.settings.redux.store.ChangeEmailStore
import com.example.changli_planet_app.utils.Event.FinishEvent
import com.example.changli_planet_app.utils.singleClick
import com.example.changli_planet_app.widget.Dialog.NormalResponseDialog
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * 更改邮箱
 */
class ChangeEmailActivity : FullScreenActivity<ActivityChangeEmailBinding>() {

    private val back: ImageView by lazy { binding.backBtn }
    private val curPassword: EditText by lazy { binding.curPassword }
    private val email: EditText by lazy { binding.newEmail }
    private val captcha: EditText by lazy { binding.captcha }
    private val getCaptcha: TextView by lazy { binding.getCaptcha }
    private val change: TextView by lazy { binding.changeEmailBtn }

    private val store= ChangeEmailStore()

    override fun createViewBinding(): ActivityChangeEmailBinding = ActivityChangeEmailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainer) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )
            WindowInsetsCompat.CONSUMED
        }
        initListener()
        EventBus.getDefault().register(this)
    }

    private fun initListener(){
        disposables.add(
            store.state()
                .subscribe{state->
                    change.isEnabled=state.isEnable

                    if(!state.isCountDown&&state.newEmail.isNotEmpty()){
                        getCaptcha.setTextColor(resources.getColor(R.color.primary_blue))
                        getCaptcha.text="获取验证码"
                        getCaptcha.singleClick(delay = 3000){
                            store.dispatch(ChangeEmailAction.GetCaptcha)
                        }
                    }else{
                        if(state.countDown>0)getCaptcha.text=state.countDown.toString()
                        else getCaptcha.text="获取验证码"
                        getCaptcha.setTextColor(resources.getColor(R.color.color_7E7E7E))
                        getCaptcha.setOnClickListener(null)
                    }
                }
        )
        store.dispatch(ChangeEmailAction.Initilaize)

        setTextWatcher(email,"newEmail")
        setTextWatcher(curPassword,"curPassword")
        setTextWatcher(captcha,"captcha")

        change.setOnClickListener{ changeEmail() }
        back.setOnClickListener{ finish() }
    }

    private fun setTextWatcher(view: TextView, type:String){
        val textWatcher=object : TextWatcher by noOpDelegate() {
            private var isUpdating=false

            override fun afterTextChanged(s: Editable?) {
                if(isUpdating)return
                isUpdating=true
                store.dispatch(ChangeEmailAction.Input(view.text.toString(),type))
                isUpdating=false
            }
        }
        view.addTextChangedListener(textWatcher)
    }

    private fun changeEmail(){
        if(curPassword.text.toString()!= UserInfoManager.userPassword){
            NormalResponseDialog(
                this,
                "密码错误",
                "更改失败"
            ).show()
        }else{
            store.dispatch(ChangeEmailAction.Change(this))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        disposables.clear()
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent){
        if(finishEvent.name=="ChangeEmail"){
            finish()
        }
    }
}