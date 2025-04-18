package com.example.changli_planet_app.Widget.Dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.example.changli_planet_app.Base.BaseDialog
import com.example.changli_planet_app.R

class LoginInformationDialog(context: Context, val content: String, val type: String) : BaseDialog(context) {
    lateinit var yes: TextView
    lateinit var contents: TextView
    lateinit var fade : TextView

    companion object{
        private var currentDialog:LoginInformationDialog?=null

        fun showDialog(context: Context,content: String,type: String){
            if(currentDialog==null){                                  //只有当前页面没有Dialog时才创造实例，防止多个实例
                currentDialog= LoginInformationDialog(context,content,type)
                currentDialog?.show()
            }
        }
    }
    override fun init() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)

        contents = findViewById(R.id.content)
        if (content.equals("无效的凭证")) {
            contents.text = "账号或密码错误"
        } else {
            contents.text = content
        }
        yes = findViewById(R.id.yes)
        yes.setOnClickListener {
            dismiss()
        }
        fade = findViewById(R.id.fade)
        fade.text = type
    }

    override fun layoutId(): Int =R.layout.login_dialog

    override fun dismiss() {
        super.dismiss()
        currentDialog=null
    }
}