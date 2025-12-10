package com.creamaker.changli_planet_app.feature.common.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.databinding.ActivityExamArrangementBinding
import com.creamaker.changli_planet_app.feature.common.contract.ExamArrangementContract
import com.creamaker.changli_planet_app.feature.common.ui.adapter.ExamArrangementAdapter
import com.creamaker.changli_planet_app.feature.common.ui.adapter.model.Exam
import com.creamaker.changli_planet_app.feature.common.viewModel.ExamArrangementViewModel
import com.creamaker.changli_planet_app.widget.dialog.ErrorStuPasswordResponseDialog
import com.creamaker.changli_planet_app.widget.view.CustomToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 考试安排查询
 */
class ExamArrangementActivity : FullScreenActivity<ActivityExamArrangementBinding>() {

    private val viewModel: ExamArrangementViewModel by viewModels()

    private val examRecyclerView: RecyclerView by lazy { binding.recyclerView }
    private val back: ImageView by lazy { binding.bindingBack }
    private val refresh: ImageView by lazy { binding.refresh }
    private val studentId by lazy { StudentInfoManager.studentId }
    private val studentPassword by lazy { StudentInfoManager.studentPassword }
    private var adapter: ExamArrangementAdapter? = null

    private fun showLoading() {
        binding.loadingLayout.visibility = View.VISIBLE
        examRecyclerView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.loadingLayout.visibility = View.GONE
        examRecyclerView.visibility = View.VISIBLE
    }

    override fun createViewBinding(): ActivityExamArrangementBinding =
        ActivityExamArrangementBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )
            WindowInsetsCompat.CONSUMED
        }

        examRecyclerView.layoutManager = LinearLayoutManager(this)
        // Initialize adapter with empty list
        adapter = ExamArrangementAdapter(mutableListOf())
        examRecyclerView.adapter = adapter
        back.setOnClickListener { finish() }
        refresh.setOnClickListener { refreshData() }
        refreshData()
        initObserve()
    }

    private fun initObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collectLatest { state ->
                        if (state.isLoading) {
                            showLoading()
                        } else {
                            hideLoading()
                            if (state.exams.isNotEmpty()) {
                                updateExamList(state.exams)
                            }
                        }
                    }
                }

                launch {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is ExamArrangementContract.Effect.ShowToast -> {
                                CustomToast.showMessage(
                                    this@ExamArrangementActivity,
                                    effect.message
                                )
                            }

                            is ExamArrangementContract.Effect.ShowErrorDialog -> {
                                ErrorStuPasswordResponseDialog(
                                    this@ExamArrangementActivity,
                                    effect.message,
                                    "查询失败"
                                ) { refreshData() }.show()
                            }
                        }
                    }
                }
            }
        }
    }



    private fun refreshData() {
        if (studentId.isNotEmpty() && studentPassword.isNotEmpty()) {
            viewModel.processIntent(
                ExamArrangementContract.Intent.LoadExamArrangement(
                    getCurrentTerm()
                )
            )
        } else {
            showMessage("请先绑定学号和密码")
            Route.goBindingUser(this@ExamArrangementActivity)
            finish()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateExamList(exams: List<Exam>) {
        // Update adapter
        adapter?.updateData(exams.toMutableList())
        adapter?.notifyDataSetChanged()
    }

    private fun getCurrentTerm(): String {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        return when {
            currentMonth >= 9 -> "$currentYear-${currentYear + 1}-1"  // 第一学期
            currentMonth >= 2 -> "${currentYear - 1}-${currentYear}-2"  // 第二学期
            else -> "${currentYear - 1}-${currentYear}-1"  // 上学年第一学期
        }
    }

    private fun showMessage(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).apply {
            val cardView = CardView(applicationContext).apply {
                radius = 25f
                cardElevation = 8f
                setCardBackgroundColor(getColor(R.color.color_bg_secondary))
                useCompatPadding = true
            }

            val textView = TextView(applicationContext).apply {
                text = msg
                textSize = 17f
                setTextColor(getColor(R.color.color_text_primary))
                gravity = Gravity.CENTER
                setPadding(80, 40, 80, 40)
            }
            cardView.addView(textView)
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 140)
            view = cardView
            show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}