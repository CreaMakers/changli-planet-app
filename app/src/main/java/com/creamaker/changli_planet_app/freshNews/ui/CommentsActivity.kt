package com.creamaker.changli_planet_app.freshNews.ui

import android.R.attr.bottom
import android.R.attr.lineHeight
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager.userId
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.mvi.observeState
import com.creamaker.changli_planet_app.databinding.ActivityCommentsBinding
import com.creamaker.changli_planet_app.freshNews.contract.CommentsContract
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.CommentsResult
import com.creamaker.changli_planet_app.freshNews.ui.adapter.CommentsAdapter
import com.creamaker.changli_planet_app.freshNews.viewModel.CommentsViewModel
import com.creamaker.changli_planet_app.utils.PlanetConst
import com.creamaker.changli_planet_app.utils.load
import com.creamaker.changli_planet_app.widget.dialog.ImageSliderDialog
import com.creamaker.changli_planet_app.widget.dialog.Level2CommentsDialog
import com.creamaker.changli_planet_app.widget.view.CustomToast
import com.gradle.scan.agent.serialization.scan.serializer.kryo.ge
import com.gradle.scan.agent.serialization.scan.serializer.kryo.it
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.math.max
import kotlin.math.min

class CommentsActivity : FullScreenActivity<ActivityCommentsBinding>() {
    private val freshNewsItem: FreshNewsItem? by lazy {
        EventBus.getDefault().getStickyEvent(FreshNewsItem::class.java)
    }
    private val rvParent: RecyclerView by lazy { binding.rvParent }
    private val commentsViewModel: CommentsViewModel by viewModels()
    private val etComment by lazy { binding.etComment }
    private val ivSend by lazy { binding.ivSend }
    private val inputLayout by lazy { binding.inputLayout }
    private val rootLayout by lazy { binding.rootLayout }

    private lateinit var commentsAdapter: CommentsAdapter
    private var maxInputLines = 5
    private var lineHeight = 0

    private var hasMore: Boolean = true
    private var page: Int = 1
    //如果要更改pageSize，需要修改ViewModel中判断是否还有更多数据的条件
    private val pageSize: Int = 10

    private var parentCommentId: Int = 0
    private var isLevel1CommentsCountChanged = false
    override fun createViewBinding(): ActivityCommentsBinding {
        return ActivityCommentsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (freshNewsItem == null) {
            Log.e(TAG, "onCreate: freshNewsItem is null, finish activity")
            finish()
            CustomToast.showMessage(PlanetApplication.appContext, "无法加载评论，数据异常")
            return
        }
        Log.d(TAG, "onCreate: freshNewsItem=$freshNewsItem")
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isLevel1CommentsCountChanged) setResult(PlanetConst.RESULT_OK)
                finish()
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        iniFreshNews()
        initializeViews()
        setupInputField()
        initObserve()
    }

    override fun onDestroy() {
        rvParent.clearOnScrollListeners()
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, null)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        // 防止输入法内存泄漏
        currentFocus?.clearFocus()
        try {
            EventBus.getDefault().removeStickyEvent(FreshNewsItem::class.java)
        } catch (e: Exception) {
            Log.w(TAG, "onDestroy: removeStickyEvent failed", e)
        }
        super.onDestroy()
    }

    private fun initializeViews() {
        binding.ivBack.apply {
            load(R.drawable.ic_back)
            setOnClickListener {
                if (isLevel1CommentsCountChanged) setResult(PlanetConst.RESULT_OK)
                finish()
            }
        }
        ivSend.setOnClickListener {
            isLevel1CommentsCountChanged = true
            sendComment()
            val imm  = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etComment.windowToken, 0)
        }
        //键盘窗口监听
        registerKeyboardListener()
        //初始化adapter
        commentsAdapter = CommentsAdapter(
            this,
            onImageClick = { images, position ->
                ImageSliderDialog(this, images, position).show()
            },
            onUserClick = { userId ->
                startActivity(Intent(this, UserHomeActivity::class.java).apply {
                    putExtra("userId", userId)
                })
            },
            onPostLevel2CommentClick = { commentText, level1CommentItem ->
                etComment.requestFocus()
                val imm  = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT)
                etComment.hint = "回复 ${level1CommentItem.userName} :"
                parentCommentId = level1CommentItem.commentId
            },
            onLevel1CommentLikeClick = { level1CommentItem ->
                commentsViewModel.processIntent(
                    CommentsContract.Intent.Level1CommentLikedClick(
                        level1CommentItem
                    )
                )
            },
            onCommentResponseCountClick = { level1CommentItem ->
                Log.d(TAG,"level1CommentId: ${level1CommentItem.commentId} response count click")
                val dialog = Level2CommentsDialog(
                     this,
                    binding.root.height * 9 / 10,
                    level1CommentItem,
                    commentsViewModel,
                    onUserClick = { userId ->
                        startActivity(Intent(this, UserHomeActivity::class.java).apply {
                            putExtra("userId", userId)
                        })
                    },
                    onLevel1LikedClick = { level1CommentItem ->
                        commentsViewModel.processIntent(
                            CommentsContract.Intent.Level1CommentLikedClick(
                                level1CommentItem, true
                            )
                        )
                    },
                    onLevel2LikedClick = { level2CommentItem ->
                        commentsViewModel.processIntent(
                            CommentsContract.Intent.Level2CommentLikedClick(
                                level2CommentItem
                            )
                        )
                    }
                )
                dialog.show(supportFragmentManager, "Level2CommentsDialog")
            },
            onPostLevel1CommentClick = {
                etComment.requestFocus()
                val imm  = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT)
                etComment.hint = getString(R.string.comment_hint)
                parentCommentId = 0
            }
        )
        val layoutManager = LinearLayoutManager(this)
        rvParent.layoutManager = layoutManager
        rvParent.adapter = commentsAdapter
        rvParent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // canScrollVertically(1) 为 false 表示已经不能向下滚动（即底部）
                val isAtBottom = !recyclerView.canScrollVertically(1)

                if (hasMore && isAtBottom) {
                    commentsViewModel.processIntent(
                        CommentsContract.Intent.LoadLevel1Comments(
                            freshNewsItem!!,
                            page++,
                            pageSize
                        )
                    )
                }
            }
        })
    }

    private fun registerKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            Log.d(TAG, "键盘是否可见: $imeVisible, 键盘高度: $imeHeight")

            if (imeVisible) {
                // 当键盘可见时，调整输入布局的底部间距
                inputLayout.setPadding(
                    inputLayout.paddingStart,
                    inputLayout.paddingTop,
                    inputLayout.paddingEnd,
                    imeHeight + 15
                )
            } else {
                // 当键盘不可见时，重置底部间距
                inputLayout.setPadding(
                    inputLayout.paddingStart,
                    inputLayout.paddingTop,
                    inputLayout.paddingEnd,
                    15
                )
                etComment.hint = getString(R.string.comment_hint)
                parentCommentId = 0
            }

            // 返回原始的 insets，让系统继续处理
            insets
        }
    }

    private fun setupInputField() {
        etComment.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (lineHeight == 0) {
                    lineHeight = etComment.lineHeight
                }
                etComment.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        etComment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                adjustInputFieldHeight()
            }
        })
    }

    private fun adjustInputFieldHeight() {
        val lineCount = etComment.lineCount
        val desiredHeight = if (lineCount <= maxInputLines) {
            lineHeight * min(lineCount, maxInputLines)
        } else {
            lineHeight * maxInputLines
        }

        val minHeight = lineHeight * 1
        val newHeight = max(minHeight, desiredHeight)

        val layoutParams = etComment.layoutParams
        layoutParams.height = newHeight
        etComment.layoutParams = layoutParams

        if (lineCount > 1) {
            etComment.setSelection(etComment.text.length)
        }
    }

    private fun sendComment() {
        val commentText = etComment.text.toString().trim()
        if (commentText.isNotEmpty()) {
            commentsViewModel.processIntent(
                CommentsContract.Intent.SendComment(
                    freshNewsItem!!.freshNewsId,
                    commentText,
                    parentCommentId
                )
            )
            etComment.setText("")
        }
    }

    private fun iniFreshNews() {
        commentsViewModel.processIntent(
            CommentsContract.Intent.LoadFreshNews(
                freshNewsItem!!
            )
        )
    }

    fun initObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                commentsViewModel.state.run {
                    observeState(
                        { value.freshNewsItem },
                        { value.level1CommentsResults },
                        {value.level1CommentPostState}) { a, b, c ->
                        if (c == 2){
                            rvParent.scrollToPosition(0)
                        }
                        if (a.freshNewsId != -1) {
                            commentsAdapter.submitFreshNews(a)
                        }
                        if (b.isNotEmpty()) {
                            commentsAdapter.submitLevel1Comments(b)
                            val count = b.size
                            val lastResult = b[count - 1]
                            hasMore = {
                                lastResult !is CommentsResult.noMore &&
                                        lastResult !is CommentsResult.Empty &&
                                        lastResult !is CommentsResult.Error
                            }()

                        }
                    }
                }
            }
        }


    }
}