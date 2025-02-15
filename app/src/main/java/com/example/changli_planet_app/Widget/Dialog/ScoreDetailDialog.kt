package com.example.changli_planet_app.Widget.Dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.example.changli_planet_app.R

class ScoreDetailDialog(
    context: Context,
    val content: String,
    val titleContent: String,
) :
    Dialog(context) {
    private lateinit var yes: TextView
    private lateinit var no: TextView
    private lateinit var contents: TextView
    private lateinit var title: TextView

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.score_details_dialog)
        contents = findViewById(R.id.detail_score_content)
        contents.text = content
        title.text = titleContent
        title = findViewById(R.id.detail_score_title)
        yes = findViewById(R.id.chosen_yes)
        no = findViewById(R.id.chosen_no)
        yes.setOnClickListener {
            dismiss()
        }
    }
}