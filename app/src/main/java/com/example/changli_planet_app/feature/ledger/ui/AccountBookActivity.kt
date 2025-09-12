package com.example.changli_planet_app.feature.ledger.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.changli_planet_app.base.FullScreenActivity
import com.example.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.example.changli_planet_app.core.Route
import com.example.changli_planet_app.databinding.ActivityAccountBookBinding
import com.example.changli_planet_app.feature.ledger.data.local.room.entity.LedgerTopCardEntity
import com.example.changli_planet_app.feature.ledger.ui.adapter.LedgerItemAdapter
import com.example.changli_planet_app.feature.ledger.viewModel.AccountBookViewModel
import com.example.changli_planet_app.utils.GlideUtils
import com.example.changli_planet_app.widget.View.AddItemFloatView
import kotlinx.coroutines.launch

/**
 * 记账本
 */
class AccountBookActivity : FullScreenActivity<ActivityAccountBookBinding>() {

    private val viewModel: AccountBookViewModel by viewModels()
    private var mFloatView: AddItemFloatView? = null
    private val avatar by lazy { binding.avatar }

    override fun createViewBinding(): ActivityAccountBookBinding = ActivityAccountBookBinding.inflate(layoutInflater)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        GlideUtils.load(this,avatar,UserInfoManager.userAvatar,false) //加载用户头像
        handleEvents()
        showCatLoading()
        object : CountDownTimer(1 * 1000L, 12312L) {
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {
                dismissCatLoading()
            }

        }.start()
        observeViewModel()
        viewModel.checkIfNeedRefresh()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }


    private fun handleEvents() {
        binding.emptyText.setOnClickListener {
            Route.goAddSomethingAccount(this@AccountBookActivity)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.topCard.collect { topCard ->
                        val cardData = topCard ?: LedgerTopCardEntity(UserInfoManager.username, 0, 0.0, 0.0)
                        binding.allMoneyNumber.text = String.format("¥%.2f", cardData.totalMoney)
                        binding.dailyCostNumber.text = String.format("¥%.2f", cardData.dailyAverage)
                    }
                }

                launch {
                    viewModel.items.collect { somethingItems ->
                        if (somethingItems.isEmpty()) {
                            binding.allNumber.text = "0/0"
                            binding.somethingVlume.visibility = View.GONE
                            binding.emptyView.visibility = View.VISIBLE
                        } else {
                            binding.allNumber.text = "${somethingItems.size}/0"
                            binding.somethingVlume.adapter =
                                LedgerItemAdapter(somethingItems, onItemDoubleClick = { item ->
                                    Route.goFixSomethingAccount(this@AccountBookActivity, item.id)
                                })
                            binding.somethingVlume.layoutManager =
                                LinearLayoutManager(this@AccountBookActivity)
                            binding.somethingVlume.visibility = View.VISIBLE
                            binding.emptyView.visibility = View.GONE
                            addFloatView()
                        }
                    }
                }
            }

            viewModel.refreshData()
        }
    }

    private fun addFloatView() {
        if (mFloatView != null) {
            (mFloatView?.parent as? ViewGroup)?.removeView(mFloatView)
        }

        // 创建新的悬浮窗
        mFloatView = AddItemFloatView(this)

        mFloatView?.setOnFloatClickListener { view ->
            Route.goAddSomethingAccount(this@AccountBookActivity)
        }

        // 设置初始位置 (右下角)
        mFloatView?.x = resources.displayMetrics.widthPixels - 200f
        mFloatView?.y = resources.displayMetrics.heightPixels - 300f
        mFloatView?.elevation = 100f
        binding.root.addView(mFloatView)
    }
}