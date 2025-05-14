package com.example.changli_planet_app.Activity

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.changli_planet_app.Activity.ViewModel.UserHomeViewModel
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.GlideUtils
import com.example.changli_planet_app.Widget.View.NestCollapsingToolbarLayout
import com.example.changli_planet_app.databinding.ActivityUserHomeBinding
import kotlinx.coroutines.launch

class UserHomeActivity : FullScreenActivity() {
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityUserHomeBinding.inflate(layoutInflater)
    }

    private val appBarLayout: NestCollapsingToolbarLayout by lazy(LazyThreadSafetyMode.NONE) { binding.toolbarLayout }
    private val llSmallAuthor by lazy(LazyThreadSafetyMode.NONE) { binding.llSmallAuthor }
    private val rlTitle by lazy(LazyThreadSafetyMode.NONE) { binding.rlTitle }
    private val ivBack by lazy(LazyThreadSafetyMode.NONE) { binding.ivBack }
    private val back by lazy(LazyThreadSafetyMode.NONE) { binding.ivBack }

    private val stateLayout by lazy(LazyThreadSafetyMode.NONE) { binding.stateLayout }

    private lateinit var objectAnimator: ObjectAnimator

    private val viewModel: UserHomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initData()
        initView()
        initAnimator()
        initListener()
    }

    private fun initData() {
        val userId = intent.getIntExtra("userId", -1)
        if (userId != -1) {
            viewModel.getUserProfile(userId)
        }
    }

    private fun initView() {
        stateLayout.apply {
            emptyLayout = R.layout.layout_empty
            errorLayout = R.layout.layout_error
        }
        stateLayout.showEmpty()

    }

    private fun initAnimator() {
        objectAnimator = ObjectAnimator.ofFloat(llSmallAuthor, "translationY", 120f, 0f)
        objectAnimator.interpolator = AccelerateInterpolator()
        objectAnimator.setDuration(300)
    }

    private fun initListener() {
        back.setOnClickListener { finish() }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { result ->
                        with(binding) {
                            when (result) {
                                is Resource.Error -> {}
                                is Resource.Loading -> {}
                                is Resource.Success -> {
                                    val userProfile = result.data
                                    GlideUtils.load(
                                        this@UserHomeActivity,
                                        ivHead,
                                        userProfile.avatarUrl,
                                    )
                                    GlideUtils.load(
                                        this@UserHomeActivity,
                                        igSmallAvatar,
                                        userProfile.avatarUrl,
                                    )
                                    authorName.text = userProfile.account
                                    tvSmallName.text = userProfile.account
                                    tvDescription.text = userProfile.bio

                                }
                            }
                        }
                    }
                }
            }
        }
        appBarLayout.setScrimsShowListener(object :
            NestCollapsingToolbarLayout.OnScrimsShowListener {
            override fun onScrimsShowChange(
                nestCollapsingToolbarLayout: NestCollapsingToolbarLayout,
                isScrimesShow: Boolean
            ) {
                if (isScrimesShow) {
                    rlTitle.setBackgroundColor(Color.WHITE)
                    ivBack.setImageResource(R.drawable.ic_back_blue)
                    showSmallAuthor()
                } else {
                    rlTitle.setBackgroundColor(Color.TRANSPARENT)
                    ivBack.setImageResource(R.drawable.ic_back)

                    if (objectAnimator.isRunning) {
                        objectAnimator.cancel()
                    }
                    llSmallAuthor.visibility = View.INVISIBLE
                }
            }

        })
    }

    private fun showSmallAuthor() {
        llSmallAuthor.visibility = View.VISIBLE
        objectAnimator.start()
    }
}