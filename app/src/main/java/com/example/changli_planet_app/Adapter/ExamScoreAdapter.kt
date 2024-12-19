package com.example.changli_planet_app.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Data.jsonbean.ExamScore
import com.example.changli_planet_app.R

class ExamScoreAdapter (var examScoreData: MutableList<ExamScore>): RecyclerView.Adapter<ExamScoreAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val lessonName: TextView = view.findViewById(R.id.lesson_name)
        val lessonType: TextView = view.findViewById(R.id.lesson_type)
        val lessonCredit: TextView = view.findViewById(R.id.lesson_credit)
        val lessonScore: TextView = view.findViewById(R.id.lesson_score)
    }

    fun updateData(newScores: MutableList<ExamScore>) {
        examScoreData = newScores
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.score_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = examScoreData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val examScore = examScoreData[position]
        holder.lessonName.text = examScore.name
        holder.lessonType.text = examScore.attribute
        holder.lessonCredit.text = examScore.point
        holder.lessonScore.text = examScore.grade
    }

}