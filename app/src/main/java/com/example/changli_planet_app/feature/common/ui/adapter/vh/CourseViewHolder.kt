package com.example.changli_planet_app.feature.common.ui.adapter.vh

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.databinding.ScoreItemCourseBinding
import com.example.changli_planet_app.feature.common.data.local.mmkv.ScoreCache
import com.example.changli_planet_app.feature.common.redux.action.ScoreInquiryAction
import com.example.changli_planet_app.feature.common.redux.store.ScoreInquiryStore
import com.example.changli_planet_app.feature.common.ui.adapter.model.CourseScore
import com.example.changli_planet_app.widget.Dialog.ScoreDetailDialog

class CourseViewHolder(
    private val binding: ScoreItemCourseBinding,
    private val store: ScoreInquiryStore,
    private val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("DefaultLocale")
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