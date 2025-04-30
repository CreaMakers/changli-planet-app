package com.example.changli_planet_app.Activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Activity.Action.AccountSecurityAction
import com.example.changli_planet_app.Activity.Store.AccountSecurityStore
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.Event.FinishEvent
import com.example.changli_planet_app.databinding.ActivityAccountSecurityBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class AccountSecurityActivity : FullScreenActivity() {
    private lateinit var binding: ActivityAccountSecurityBinding
    private val bindingBack by lazy { binding.bindingBack }
    private val disposables by lazy { CompositeDisposable() }
    private val curPasswordEt by lazy { binding.curPasswordEt }
    private val curPasswordImg by lazy { binding.curPasswordImg }
    private val newPasswordEt by lazy { binding.newPasswordEt }
    private val newPasswordImg by lazy { binding.newPasswordImg }
    private val confirmPasswordEt by lazy { binding.confirmPasswordEt }
    private val confirmPasswordImg by lazy { binding.confirmPasswordImg }
    private val strongPasswordPrb by lazy { binding.strongPasswordPrb }
    private val strength8Img by lazy { binding.strength8Img }
    private val containBigAndSmallImg by lazy { binding.containBigAndSmallImg }
    private val containNumberIconImg by lazy { binding.containNumberIconImg }
    private val changePasswordBtn by lazy { binding.changePasswordBtn }
    private val store = AccountSecurityStore()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
        initObserve()
    }

    private fun initView() {
        store.dispatch(AccountSecurityAction.initilaize)
        binding = ActivityAccountSecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val passwordTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(AccountSecurityAction.UpdateSafeType(newPasswordEt.text.toString()))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        newPasswordEt.addTextChangedListener(passwordTextWatcher)
        inputFilterPassword(curPasswordEt)
        inputFilterPassword(newPasswordEt)
        inputFilterPassword(confirmPasswordEt)
        EventBus.getDefault().register(this)
    }

    private fun initListener() {
        changePasswordBtn.setOnClickListener { changePassword() }
        curPasswordImg.setOnClickListener { store.dispatch(AccountSecurityAction.UpdateVisible("curPasswordImg")) }
        newPasswordImg.setOnClickListener { store.dispatch(AccountSecurityAction.UpdateVisible("newPasswordImg")) }
        confirmPasswordImg.setOnClickListener { store.dispatch(AccountSecurityAction.UpdateVisible("confirmPasswordImg")) }
        bindingBack.setOnClickListener { finish() }
    }

    private fun initObserve() {
        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    updateCondition(state.isLengthValid, strength8Img)
                    updateCondition(state.hasUpperAndLower, containBigAndSmallImg)
                    updateCondition(state.hasNumberAndSpecial, containNumberIconImg)
                    updatePasswordVisibility(
                        state.curPasswordVisible,
                        curPasswordEt,
                        curPasswordImg
                    )
                    updatePasswordVisibility(
                        state.newPasswordVisible,
                        newPasswordEt,
                        newPasswordImg
                    )
                    updatePasswordVisibility(
                        state.confirmPasswordVisible,
                        confirmPasswordEt,
                        confirmPasswordImg
                    )
                    updateProgressBar(state.safeType)
                }
        )
    }

    private fun changePassword() {
        val curPassword = curPasswordEt.text.toString()
        val newPassword = newPasswordEt.text.toString()
        val confirmPassword = confirmPasswordEt.text.toString()

        if (!curPassword.equals(UserInfoManager.userPassword)) {
            showMessage("旧密码错误，请重新输入")
            return
        }
        if (!newPassword.equals(confirmPassword)) {
            showMessage("新密码与确认密码不一致，请重新输入")
            return
        }
        if (strongPasswordPrb.progress != 100) {
            showMessage("新密码不满足要求，请重新输入")
            return
        }
        store.dispatch(AccountSecurityAction.ChangePassword(this,curPasswordEt.text.toString(),newPassword, confirmPassword))
    }

    fun updatePasswordVisibility(isVisible: Boolean, et: EditText, img: ImageView) {
        // 检查当前密码是否可见
        val isCurrentlyVisible = et.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        // 只有当当前状态与目标状态不同时才切换
        if (isCurrentlyVisible != isVisible) {
            if (isVisible) {
                et.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                img.setImageResource(R.drawable.yincang)
            } else {
                et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                img.setImageResource(R.drawable.buyincang)
            }
            // 保持光标位置在文本末尾
            et.setSelection(et.text.length)
        }
    }

    fun updateCondition(isVisible: Boolean, img: ImageView) {
        if (isVisible) {
            img.setImageResource(R.drawable.dui)
        } else {
            img.setImageResource(R.drawable.cuo)
        }
    }

    fun updateProgressBar(safeType: Int) {
        when (safeType) {
            0 -> {
                strongPasswordPrb.progress = 0
            }

            1 -> {
                strongPasswordPrb.progress = 33
                strongPasswordPrb.progressTintList = ColorStateList.valueOf(Color.RED)
            }

            2 -> {
                strongPasswordPrb.progress = 66
                strongPasswordPrb.progressTintList = ColorStateList.valueOf(Color.YELLOW)
            }

            3 -> {
                strongPasswordPrb.progress = 100
                strongPasswordPrb.progressTintList =
                    ColorStateList.valueOf(Color.parseColor("#6396F1"))
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).apply {
            val cardView = CardView(applicationContext).apply {
                radius = 25f
                cardElevation = 8f
                setCardBackgroundColor(getColor(R.color.score_bar))
                useCompatPadding = true
            }
            val textView = TextView(applicationContext).apply {
                text = message
                textSize = 17f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setPadding(80, 40, 80, 40)
            }

            cardView.addView(textView)
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 140)
            view = cardView
            show()
        }
    }

    private fun inputFilterPassword(editText: EditText) {
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            val regex = Regex("^[a-zA-Z0-9!@#\$%^&*(),.?\":{}|<>]+$")
            source.toString().filter { char ->
                regex.matches(char.toString())
            }
        }
        editText.filters = arrayOf(inputFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        disposables.clear()
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent) {
        if (finishEvent.name.equals("ChangePassword")) {
            showMessage("更新密码成功")
            UserInfoManager.clear()
            Route.goLogin(this)
            finish()
        }
    }

}