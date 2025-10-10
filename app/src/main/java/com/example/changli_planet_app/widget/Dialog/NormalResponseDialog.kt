package com.example.changli_planet_app.widget.Dialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import com.example.changli_planet_app.R
import com.example.changli_planet_app.base.BaseDialog

class NormalResponseDialog(context: Context, val content: String, val type: String) :
    BaseDialog(context) {
    private lateinit var yes: TextView
    private lateinit var contents: TextView
    private lateinit var fade: TextView

    override fun init() {
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setWindowAnimations(R.style.DialogAnimation)
        yes = findViewById(R.id.yes)
        yes.setOnClickListener {
            dismiss()
        }
        contents = findViewById(R.id.content)
        contents.text = content
        contents.setTextIsSelectable(true)

        // 拦截选择模式中的复制操作，显示 Toast（保留系统的选择菜单）
        contents.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                if (item?.itemId == android.R.id.copy) {
                    val start = contents.selectionStart
                    val end = contents.selectionEnd
                    if (start >= 0 && end > start) {
                        val selected = contents.text.subSequence(start, end).toString()
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("dialog_content", selected))
                        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    }
                    mode?.finish()
                    return true
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {}
        }

        fade = findViewById(R.id.fade)
        fade.text = type
    }

    override fun layoutId(): Int =R.layout.login_dialog
}