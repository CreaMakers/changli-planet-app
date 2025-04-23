package com.example.changli_planet_app.Utils.spannable

import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView

interface DslSpannableStringBuilder {
    fun addText(text: String, method: (DslSpanBuilder.() -> Unit)? = null)
}

interface DslSpanBuilder {
    fun setColor(color: String)

    fun setSize(size: Int)

    fun onClick(useUnderLine: Boolean = true, onClick: (View) -> Unit)

}

fun TextView.buildSpannableString(init: DslSpannableStringBuilder.() -> Unit) {
    // 具体实现类
    val spannableStringBuilder = DslSpannableStringBuilderImpl()
    spannableStringBuilder.init()
    movementMethod = LinkMovementMethod.getInstance()
    text = spannableStringBuilder.build()
}