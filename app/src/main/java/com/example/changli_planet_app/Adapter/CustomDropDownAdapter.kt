package com.example.changli_planet_app.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.changli_planet_app.R

class CustomDropDownAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, R.layout.dropdown_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.dropdown_item, parent, false)
        val divider: View = view.findViewById(R.id.divider)
        divider.visibility = if (position == items.size - 1) View.GONE else View.VISIBLE
        val textView : TextView = view.findViewById(R.id.dropdown_text)
        textView.text = items[position]
        return view
    }
}