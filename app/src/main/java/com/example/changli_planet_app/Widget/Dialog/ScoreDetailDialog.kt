package com.example.changli_planet_app.Widget.Dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.example.changli_planet_app.Base.BaseDialog
import com.example.changli_planet_app.R

class ScoreDetailDialog(
    context: Context,
    val content: String,
    val titleContent: String,
) :
    BaseDialog(context) {
    companion object{
        private var currentDialog:ScoreDetailDialog?=null

        fun showDialog(context: Context,content: String,titleContent: String){
            if(currentDialog==null){                                  //只有当前页面没有Dialog时才创造实例，防止多个实例
                currentDialog= ScoreDetailDialog(context,content,titleContent)
                currentDialog?.show()
            }
        }
    }
    private lateinit var yes: TextView
    private lateinit var no: TextView
    private lateinit var contents: TextView
    private lateinit var title: TextView


    override fun init() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)

        contents = findViewById(R.id.detail_score_content)
        title = findViewById(R.id.detail_score_title)
        contents.text = content
        title.text = titleContent
        yes = findViewById(R.id.chosen_yes)
        yes.setOnClickListener {
            dismiss()
        }
    }

    override fun layoutId(): Int =R.layout.score_details_dialog

    override fun dismiss() {
        super.dismiss()
        currentDialog=null
    }
}