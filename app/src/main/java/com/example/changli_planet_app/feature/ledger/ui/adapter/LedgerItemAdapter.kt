package com.example.changli_planet_app.feature.ledger.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.R
import com.example.changli_planet_app.feature.ledger.data.local.room.entity.LedgerItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LedgerItemAdapter(
    val data: List<LedgerItemEntity>,
    val onItemDoubleClick: (LedgerItemEntity) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class SomethingItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picture: ImageView = view.findViewById(R.id.picture)
        val name: TextView = view.findViewById(R.id.name)
        val allMoney: TextView = view.findViewById(R.id.all_money)
        val dailyCost: TextView = view.findViewById(R.id.daily_cost)
        val days: TextView = view.findViewById(R.id.days)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_something_value, parent, false)
        return SomethingItemViewHolder(view)
    }

    private var lastClickedPosition = -1
    private var lastClickTime = 0L

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val somethingItemViewHolder = holder as SomethingItemViewHolder
        val dsf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val lastTime = dsf.parse(data[position].startTime)
        val now = Date()
        val days = Math.max(1, (now.time - lastTime.time) / 1000 / 60 / 60 / 24)
        somethingItemViewHolder.picture.setImageResource(data[position].picture)
        somethingItemViewHolder.name.text = data[position].name
        somethingItemViewHolder.allMoney.text = String.format("¥%.2f", data[position].totalMoney)
        somethingItemViewHolder.dailyCost.text =
            String.format("¥%.2f/天", data[position].totalMoney / days)
        somethingItemViewHolder.days.text = days.toString()

        somethingItemViewHolder.itemView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val currentPosition = holder.bindingAdapterPosition  // 使用新方法

            if (currentPosition != RecyclerView.NO_POSITION) {  // 检查位置有效性
                if (currentPosition == lastClickedPosition &&
                    currentTime - lastClickTime < 300
                ) {
                    onItemDoubleClick(data[currentPosition])
                }

                lastClickedPosition = currentPosition
                lastClickTime = currentTime
            }
        }
    }

    override fun getItemCount(): Int = data.size
}