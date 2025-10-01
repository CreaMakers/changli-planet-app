package com.example.changli_planet_app.settings.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.changli_planet_app.R
import com.example.changli_planet_app.base.FullScreenActivity
import com.example.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.example.changli_planet_app.common.redux.action.UserAction
import com.example.changli_planet_app.common.redux.store.UserStore
import com.example.changli_planet_app.core.PlanetApplication
import com.example.changli_planet_app.core.Route
import com.example.changli_planet_app.core.network.Resource
import com.example.changli_planet_app.databinding.ActivityBindingUserBinding
import com.example.changli_planet_app.feature.mooc.data.remote.repository.MoocRepository
import com.example.changli_planet_app.utils.Event.FinishEvent
import com.example.changli_planet_app.widget.View.CustomToast
import com.google.android.material.button.MaterialButton
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * 绑定用户类
 */
class BindingUserActivity : FullScreenActivity<ActivityBindingUserBinding>() {
    private val username: TextView by lazy { binding.etStudentId }
    private val password: TextView by lazy { binding.etStudentPassword }
    private val back: ImageView by lazy { binding.bindingBack }
    private val save: MaterialButton by lazy { binding.saveUser }
    private val repository by lazy { MoocRepository.instance }

    private val store = UserStore()

    override fun createViewBinding(): ActivityBindingUserBinding = ActivityBindingUserBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
        store.dispatch(UserAction.initilaize())
        observeState()
    }

    private fun observeState() {
        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    StudentInfoManager.studentId = state.userStats.studentNumber
                    username.text = state.userStats.studentNumber
                }
        )
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar){ view, windowInsets->
            val insets=windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initListener() {
        save.setOnClickListener { saveUserInfo() }
        back.setOnClickListener { finish() }
    }

    private fun saveUserInfo() {
        val studentId = username.text.toString()
        val studentPassword = password.text.toString()
        if (studentId.isEmpty() || studentPassword.isEmpty()) {
            showMessage("学号和密码不能为空")
            return
        }
        if(PlanetApplication.is_tourist){
            StudentInfoManager.studentId = studentId  //游客模式不使用网络进行储存学号
        }
        StudentInfoManager.studentPassword = studentPassword
        store.dispatch(UserAction.BindingStudentNumber(this, studentId))  //在MVI流对游客模式也进行了判断逻辑与state发布
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent) {
        if (finishEvent.name.equals("bindingUser")) {
            showMessage("学号和密码保存成功！")
            Route.goHomeForcibly(this)
            finish()
        }
    }

}