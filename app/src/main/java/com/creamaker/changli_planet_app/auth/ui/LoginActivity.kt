package com.creamaker.changli_planet_app.auth.ui

import android.Manifest
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.app.ActivityCompat
import androidx.core.content.pm.PackageInfoCompat
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.auth.data.remote.dto.UserPassword
import com.creamaker.changli_planet_app.auth.redux.action.LoginAndRegisterAction
import com.creamaker.changli_planet_app.auth.redux.store.LoginAndRegisterStore
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.common.redux.action.UserAction
import com.creamaker.changli_planet_app.common.redux.store.UserStore
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.noOpDelegate
import com.creamaker.changli_planet_app.databinding.ActivityLoginBinding
import com.creamaker.changli_planet_app.utils.Event.FinishEvent
import com.creamaker.changli_planet_app.widget.dialog.ExpiredDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class LoginActivity : FullScreenActivity<ActivityLoginBinding>() {
    private val Login: TextView by lazy { binding.login }
    private val route: TextView by lazy { binding.route }
    private val touristRoute: TextView by lazy { binding.touristRoute }
    private val touristHint: ImageView by lazy { binding.incompleteFeatureHint }
    private val account: EditText by lazy { binding.account }
    private val password: EditText by lazy { binding.password }
    private val forgetPassword:TextView by lazy { binding.forget }
    private val loginByEmail:TextView by lazy { binding.loginEmail }
    private val iVEye: ImageView by lazy { binding.ivEye }
    private val ivOx: ImageView by lazy { binding.ivOx }
    private val agreementCheckBox: CheckBox by lazy { binding.agreementCheckbox }
    val store = LoginAndRegisterStore()
    val UserStore=UserStore()

    private lateinit var accountTextWatcher: TextWatcher
    private lateinit var passwordTextWatcher: TextWatcher


    override fun createViewBinding(): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
        checkUpdate()
    }

    private fun checkUpdate(){
        // 检查版本更新
        Looper.myQueue().addIdleHandler {
            getNotificationPermissions()
            val packageManager: PackageManager = this@LoginActivity.packageManager
            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(this@LoginActivity.packageName, 0)
            UserStore.dispatch(
                UserAction.QueryIsLastedApk(
                    this@LoginActivity,
                    PackageInfoCompat.getLongVersionCode(packageInfo),
                    packageInfo.packageName
                )
            )
            false
        }
    }
    private fun initView() {
        EventBus.getDefault().register(this)
        if (intent.getBooleanExtra("from_token_expired", false)) {
            ExpiredDialog(
                this,
                "您的登录状态过期啦꒰ঌ( ⌯' '⌯)໒꒱",
                "登录提示"
            ).show()
        }
    }

    private fun initListener() {
        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    updateButtonState(state.isEnable)
                    updatePasswordVisibility(state.isVisibilityPassword)
                    updateButtonClear(state.isClearPassword)
                }
        )
        store.dispatch(LoginAndRegisterAction.initilaize)
        setUnderLine()
        accountTextWatcher = object : TextWatcher by noOpDelegate() {
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(
                    LoginAndRegisterAction.InputLogin(
                        account.text.toString(),
                        "account"
                    )
                )
            }
        }
        passwordTextWatcher = object : TextWatcher by noOpDelegate() {
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(
                    LoginAndRegisterAction.InputLogin(
                        password.text.toString(),
                        "password"
                    )
                )
            }
        }

        Login.setOnClickListener {
            store.dispatch(
                LoginAndRegisterAction.Login
                    (
                    UserPassword(account.text.toString(), password.text.toString()),
                    this
                )
            )
        }
        ivOx.setOnClickListener {
            if (store.currentState.isClearPassword) {
                clearCurPassword()
            }
        }
        iVEye.setOnClickListener {
            store.dispatch(LoginAndRegisterAction.ChangeVisibilityOfPassword)
        }
        touristHint.setOnClickListener {
            ExpiredDialog(
                this,
                "由于账号的注册与登录功能主要服务于“长理星球新鲜事”板块，但目前尚未完善，您可以点击右下角的「跳过，稍后登录」以游客身份体验应用的其他功能（除了新鲜事以外其他的基础功能都可用哦(*￣3￣)╭~)",
                "账号相关"
            ).show()
        }
        account.addTextChangedListener(accountTextWatcher)
        password.addTextChangedListener(passwordTextWatcher)
        agreementCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                store.dispatch(LoginAndRegisterAction.InputLogin("checked", "checkbox"))
            } else {
                store.dispatch(LoginAndRegisterAction.InputLogin("unchecked", "checkbox"))
            }

        }
        inputFilter(account)
        inputFilterPassword(password)
        account.setText(intent.getStringExtra("username") ?: "")
        password.setText(intent.getStringExtra("password") ?: "")
    }

    private fun inputFilter(editText: EditText) {
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            // 允许的字符是英文字母和数字
            val regex = Regex("^[a-zA-Z0-9]*$")
            // 如果输入内容符合正则表达式，则允许输入，否则返回空字符串禁止输入
            if (regex.matches(source)) source else ""
        }
        editText.filters = arrayOf(inputFilter)
    }

    private fun setUnderLine() {
        getUnderLineScope(route,6,8)
        getUnderLineScope(forgetPassword,0,4)
        getUnderLineScope(loginByEmail,0,4)
        getUnderLineScope(touristRoute,0,2)
        route.setOnClickListener {
            Route.goRegister(this)
        }
        touristRoute.setOnClickListener {
            PlanetApplication.is_tourist = true
            Route.goHome(this)
        }
        loginByEmail.setOnClickListener{
            Route.goLoginByEmailForcibly(this)
        }
        forgetPassword.setOnClickListener{
            Route.goForgetPassword(this)
        }
    }

    private fun clearCurPassword() {
        password.setText("")
    }

    private fun updateButtonState(isEnable: Boolean) {
        Login.isEnabled = isEnable
        if (isEnable) {
            Login.setBackgroundResource(R.drawable.bg_enable_button)
        } else {
            Login.setBackgroundResource(R.drawable.disable_button)
        }
    }

    private fun updateButtonClear(isClearPassword: Boolean) {
        if (!isClearPassword) {
            ivOx.visibility = View.INVISIBLE
            ivOx.setImageResource(R.drawable.dialog_login)
        } else {
            ivOx.visibility = View.VISIBLE
            ivOx.setImageResource(R.drawable.ic_login_ox_24)
        }
    }

    private fun updatePasswordVisibility(isVisible: Boolean) {
        if (isVisible) {

            password.transformationMethod = android.text.method.HideReturnsTransformationMethod.getInstance()
            iVEye.setImageResource(R.drawable.ic_login_visibility_eyes)
        } else {
            password.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            iVEye.setImageResource(R.drawable.ic_line_invisible2)
        }
        password.setSelection(password.text?.length ?: 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        account.removeTextChangedListener(accountTextWatcher)
        password.removeTextChangedListener(passwordTextWatcher)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent) {
        if (finishEvent.name.equals("Login")) {
            finish()
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

    private fun getUnderLineScope(view: TextView,start:Int,end:Int){
        var underlinetext = SpannableString(view.text.toString())
        underlinetext.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.text = underlinetext
    }

    private fun getNotificationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION
            )
        } else {
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_NOTIFICATION ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getNotificationPermissions()
                }

        }
    }
    companion object {
        private const val REQUEST_NOTIFICATION = 1002
    }


}

// ======== 颜色定义（模拟你的 @color/xxx）===========
private val ColorTextPrimary = Color(0xFF333333)
private val ColorTextSecondary = Color(0xFF666666)
private val ColorTextFunctional = Color(0xFF007AFF)
private val ColorTextHighlight = Color(0xFF1A1A1A)
private val ColorTextGrey = Color(0xFF999999)
private val ColorIconPrimary = Color(0xFF555555)
private val ColorBaseWhite = Color.White
private val ButtonEnabledBackground = Color(0xFF007AFF) // 模拟 @drawable/enable_button

@Composable
@Preview(showBackground = true)
fun LoginScreen() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            // 背景图：用 Image + contentScale = ContentScale.FillBounds 模拟 android:background="@drawable/login_background"
            Image(
                painter = painterResource(id = R.drawable.login_background),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize(),
                contentScale = ContentScale.FillBounds
            )

            // 主内容：ConstraintLayout
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp) // 可选：微调边距使更自然
            ) {
                val (spaceRef, imageViewRef, textViewRef, hintRef,
                    accountRef, passwordContentRef, agreementRef, loginRef,
                    forgetRef, emailRef, creMakerRef, routeRef, touristRef) = createRefs()

                // Space (占 10% 高度)
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(spaceRef) {
                            top.linkTo(parent.top)
                            height = Dimension.percent(0.10f)
                        }
                )

                // 左上角图标 @drawable/turn
                Image(
                    painter = painterResource(id = R.drawable.turn),
                    contentDescription = "Back",
                    modifier = Modifier
                        .constrainAs(imageViewRef) {
                            top.linkTo(spaceRef.bottom)
                            start.linkTo(parent.start)
                            width = Dimension.percent(0.28f)
                            height = Dimension.percent(0.10f)
                        }
                        .padding(start = 10.dp)
                )

                // 标题“账户登录”
                Text(
                    text = "账户登录",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextHighlight,
                    modifier = Modifier
                        .constrainAs(textViewRef) {
                            top.linkTo(imageViewRef.bottom)
                            start.linkTo(parent.start)
                            width = Dimension.percent(0.30f)
                        }
                        .padding(start = 15.dp)
                )

                // 右上角提示图标（incomplete_feature_hint）
                Image(
                    painter = painterResource(id = R.drawable.ic_electricity_default),
                    contentDescription = "Feature hint",
                    colorFilter = ColorFilter.tint(ColorIconPrimary),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp)
                        .constrainAs(hintRef) {
                            top.linkTo(spaceRef.bottom)
                            start.linkTo(imageViewRef.end)
                            width = Dimension.percent(0.28f)
                            height = Dimension.percent(0.10f)
                        }
                )

                // 账号输入框
                OutlinedTextField(
                    value = "", // 绑定状态变量更佳（后文说明）
                    onValueChange = {},
                    placeholder = { Text("请输入账号", color = ColorTextGrey) },
                    singleLine = true,
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = Color.Gray,
//                        unfocusedBorderColor = Color.Gray,
//                        cursorColor = ColorTextPrimary,
//                        textColor = ColorTextPrimary
//                    ),
                    modifier = Modifier
                        .constrainAs(accountRef) {
                            top.linkTo(textViewRef.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.percent(0.8f)
                            verticalBias = 0.1f
                        }
                        .height(40.dp)
                )

                // 密码区域（LinearLayout → Row）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .constrainAs(passwordContentRef) {
                            top.linkTo(accountRef.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.percent(0.8f)
                            verticalBias = 0.1f
                        }
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp)) // 模拟 @drawable/edit_text_background
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp)
                ) {
                    var passwordVisible by remember { mutableStateOf(false) }

                    // 左侧“眼睛”图标
                    Image(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_login_visibility_eyes else R.drawable.ic_line_invisible2
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        colorFilter = ColorFilter.tint(ColorIconPrimary),
                        modifier = Modifier
                            .size(25.dp)
                            .clickable { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 密码输入框（背景 null → 透明）
                    BasicTextField(
                        value = "",
                        onValueChange = {},
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.None
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            color = ColorTextPrimary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.weight(10f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

//                    // 右侧图标（默认 invisible）
//                    Image(
//                        painter = painterResource(id = R.drawable.dialog_login),
//                        contentDescription = "Clear",
//                        colorFilter = ColorFilter.tint(ColorIconPrimary),
//                        modifier = Modifier
//                            .size(25.dp)
//                            .clickable { }
//                    )
                }

                // 协议区域（CheckBox + 文字）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .constrainAs(agreementRef) {
                            top.linkTo(passwordContentRef.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.percent(0.8f)
                            verticalBias = 0f
                        }
                ) {
                    var checked by remember { mutableStateOf(false) }
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ColorIconPrimary,
                            uncheckedColor = Color.Gray
                        )
                    )
                    Text("我已阅读并同意", fontSize = 14.sp, color = ColorTextSecondary)
                    Text(
                        "《长理星球用户协议》",
                        fontSize = 14.sp,
                        color = ColorTextFunctional,
                        modifier = Modifier.clickable {
                            // TODO: 跳转协议页
                        }
                    )
                }

                // 登录按钮
                Button(
                    onClick = { /* 登录逻辑 */ },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonEnabledBackground),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .constrainAs(loginRef) {
                            top.linkTo(passwordContentRef.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.percent(0.8f)
                            verticalBias = 0.162f
                        }
                        .height(40.dp)
                ) {
                    Text("登录", color = ColorBaseWhite, fontSize = 16.sp)
                }

                // 忘记密码 & 邮箱登录（左右分布）
                Row(
                    modifier = Modifier
                        .constrainAs(forgetRef) { // 复用一个 ref 即可，或拆成两个
                            top.linkTo(loginRef.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .padding(top = 20.dp)
                ) {
                    Text(
                        "忘记密码",
                        color = ColorTextFunctional,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 100.dp)
                            .clickable { /* 忘记密码 */ }
                    )
                    Text(
                        "邮箱登录",
                        color = ColorTextFunctional,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 100.dp, start = 8.dp)
                            .clickable { /* 邮箱登录 */ }
                    )
                }

                // Creamaker 图标
                Image(
                    painter = painterResource(id = R.drawable.creamaker),
                    contentDescription = "Creamaker",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .constrainAs(creMakerRef) {
                            top.linkTo(loginRef.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.percent(0.8f)
                            height = Dimension.percent(0.30f)
                        }
                        .padding(top = 25.dp)
                )

                // 底部两个文字（注册 / 游客）
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .constrainAs(routeRef) { // 同样复用 ref
                            top.linkTo(creMakerRef.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                        .padding(horizontal = 18.dp)
                ) {
                    Text(
                        "没有账号？去注册",
                        color = ColorTextFunctional,
                        fontSize = 15.sp,
                        modifier = Modifier.clickable { /* 跳转注册 */ }
                    )
                    Text(
                        "跳过，稍后登录",
                        color = ColorTextFunctional,
                        fontSize = 15.sp,
                        modifier = Modifier.clickable { /* 游客登录 */ }
                    )
                }
            }
        }
    }
}