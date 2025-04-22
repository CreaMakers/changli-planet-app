package com.example.changli_planet_app.Activity

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Activity.Action.UserAction
import com.example.changli_planet_app.Activity.Store.UserStore
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.Event.FinishEvent
import com.example.changli_planet_app.databinding.ActivityBindingUserBinding
import com.google.android.material.button.MaterialButton
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class BindingUserActivity : FullScreenActivity() {
    private lateinit var binding: ActivityBindingUserBinding
    private val username: TextView by lazy { binding.etStudentId }
    private val password: TextView by lazy { binding.etStudentPassword }
    private val back: ImageView by lazy { binding.bindingBack }
    private val save: MaterialButton by lazy { binding.saveUser }

    private val disposables by lazy { CompositeDisposable() }
    private val store = UserStore()

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
        binding = ActivityBindingUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EventBus.getDefault().register(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar){view,windowInsets->
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
        StudentInfoManager.studentPassword = studentPassword
        store.dispatch(UserAction.BindingStudentNumber(this, studentId))
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