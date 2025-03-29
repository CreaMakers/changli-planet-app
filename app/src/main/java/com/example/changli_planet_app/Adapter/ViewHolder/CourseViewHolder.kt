package com.example.changli_planet_app.Adapter.ViewHolder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Activity.Action.ScoreInquiryAction
import com.example.changli_planet_app.Activity.Store.ScoreInquiryStore
import com.example.changli_planet_app.Cache.ScoreCache
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Data.model.CourseScore
import com.example.changli_planet_app.Widget.Dialog.ScoreDetailDialog
import com.example.changli_planet_app.Widget.View.CustomToast
import com.example.changli_planet_app.databinding.ScoreItemCourseBinding

class CourseViewHolder(
    private val binding: ScoreItemCourseBinding,
    private val store: ScoreInquiryStore,
    private val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(courseScore: CourseScore) {
        binding.apply {
            tvCourseName.text = courseScore.name
            tvCourseName.text = courseScore.name
            tvScore.text = courseScore.score.toString()
            tvCredit.text = String.format("学分: %.1f", courseScore.credit)
            getCredit.text = String.format("绩点: %.1f", courseScore.earnedCredit)
            tvType.text = courseScore.courseType
        }

        binding.scoreItemLayout.setOnClickListener {
            if (courseScore.pscjUrl == null) {
//                ScoreDetailDialog(
//                    context = context,
//                    content = "暂无平时成绩",
//                    titleContent = courseScore.name
//                ).show()
                ScoreDetailDialog.showDialog(context,"暂无平时成绩",courseScore.name)
            } else {
                val cacheDetail = ScoreCache.getGradesDetailByUrl(courseScore.pscjUrl)
                if (cacheDetail.isEmpty()) {
                    store.dispatch(
                        ScoreInquiryAction.GetScoreDetail(
                            context,
                            courseScore.pscjUrl,
                            courseScore.name
                        )
                    )
                } else {
//                    ScoreDetailDialog(
//                        context = context,
//                        content = cacheDetail,
//                        titleContent = courseScore.name
//                    ).show()
                    ScoreDetailDialog.showDialog(context,cacheDetail,courseScore.name)
                }
            }
        }
    }
}