package com.example.changli_planet_app.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Data.jsonbean.Exam
import com.example.changli_planet_app.Data.jsonbean.ExamScore
import com.example.changli_planet_app.Network.Response.ExamArrangement
import com.example.changli_planet_app.R
import org.w3c.dom.Text

class ExamArrangementAdapter(var examData: MutableList<Exam>) : RecyclerView.Adapter<ExamArrangementAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val examTitle: TextView = view.findViewById(R.id.exam_title)
        val examTime: TextView = view.findViewById(R.id.exam_time)
        val examId: TextView = view.findViewById(R.id.exam_id)
        val examRoom: TextView = view.findViewById(R.id.exam_room)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exam_arrangement_item, parent, false)
        return ViewHolder(view)
    }

    fun updateData(newExamArrange: MutableList<Exam>) {
        examData = newExamArrange
    }

    override fun getItemCount() = examData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.examTitle.text = examData[position].name
        holder.examId.text = examData[position].examId
        holder.examRoom.text = examData[position].room
        holder.examTime.text = examData[position].time
    }
}