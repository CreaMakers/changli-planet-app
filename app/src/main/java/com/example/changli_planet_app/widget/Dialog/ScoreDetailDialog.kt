package com.example.changli_planet_app.widget.Dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import com.example.changli_planet_app.R
import com.example.changli_planet_app.base.BaseDialog
import java.lang.ref.WeakReference

class ScoreDetailDialog(
    context: Context,
    val content: String,
    val titleContent: String,
) :
    BaseDialog(context) {
    companion object {
        private fun getDialog(context: Context, content: String, titleContent: String) =
            ScoreDetailDialog(
                context,
                content,
                titleContent
            )

        fun showDialog(context: Context, content: String, titleContent: String) {
            val dialog = WeakReference(getDialog(context, content, titleContent))
            dialog.get()?.show()
        }
    }

    private lateinit var yes: TextView
    private lateinit var no: TextView
    private lateinit var contents: TextView
    private lateinit var title: TextView

    private fun getDialog(context: Context, content: String, titleContent: String) =
        ScoreDetailDialog(
            context,
            content,
            titleContent
        )

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

    override fun layoutId(): Int = R.layout.score_details_dialog
}