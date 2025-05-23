package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.changli_planet_app.Activity.ViewModel.AccountBookViewModel
import com.example.changli_planet_app.Adapter.SomethingItemAdapter
import com.example.changli_planet_app.Cache.Room.database.AccountBookDatabase
import com.example.changli_planet_app.Cache.Room.entity.SomethingItemEntity
import com.example.changli_planet_app.Cache.Room.entity.TopCardEntity
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.GlideUtils
import com.example.changli_planet_app.Widget.View.AddItemFloatView
import com.example.changli_planet_app.databinding.ActivityAccountBookBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccountBookActivity : FullScreenActivity() {

    private val binding by lazy { ActivityAccountBookBinding.inflate(layoutInflater) }
    private val viewModel: AccountBookViewModel by viewModels()
    private var mFloatView: AddItemFloatView? = null
    private val avatar by lazy { binding.avatar }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContentView(binding.root)
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
                        val cardData = topCard ?: TopCardEntity(UserInfoManager.username, 0, 0.0, 0.0)
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
                                SomethingItemAdapter(somethingItems, onItemDoubleClick = { item ->
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