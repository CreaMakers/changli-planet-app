package com.creamaker.changli_planet_app.widget.dialog

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.compose.ui.layout.Layout
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.mvi.observeState
import com.creamaker.changli_planet_app.databinding.DialogLevel2CommentsBinding
import com.creamaker.changli_planet_app.freshNews.contract.CommentsContract
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.CommentsResult
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level2CommentItem
import com.creamaker.changli_planet_app.freshNews.ui.adapter.Level2CommentsAdapter

import com.creamaker.changli_planet_app.freshNews.viewModel.CommentsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import kotlinx.coroutines.launch
import kotlin.jvm.java

class Level2CommentsDialog(
    val mcontext: Context,
    val maxHeight: Int,
    val level1CommentItem: Level1CommentItem,
    val commentsViewModel: CommentsViewModel,
    val onUserClick: (userId: Int) -> Unit,
    val onLevel1LikedClick: (level1Comment: Level1CommentItem) ->Unit,
    val onLevel2LikedClick: (level2Comment: Level2CommentItem) ->Unit,
) :
    BottomSheetDialogFragment() {
    private lateinit var binding: DialogLevel2CommentsBinding
    private val TAG = "Level2CommentsDialog"
    private var page = 1
    private val pageSize = 10
    private var isResponseLevel1Comment: Boolean = true
    private var responseToUserName: String = level1CommentItem.userName
    private var initialLoaded = false
    private val onResponseClick: (level2CommentItem: Level2CommentItem) -> Unit =
        { level2CommentItem ->
            binding.etComment.requestFocus()
            val imm = getSystemService(mcontext, InputMethodManager::class.java) as InputMethodManager
            imm.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT)
            binding.etComment.hint = "回复@${level2CommentItem.userName}"
            isResponseLevel1Comment = false
            responseToUserName = level2CommentItem.userName
        }
    private var hasMore: Boolean = true
    private val adapter by lazy {
        Level2CommentsAdapter(
            mcontext,
            onResponseClick,
            onUserClick,
            onLevel1LikedClick,
            onLevel2LikedClick
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogLevel2CommentsBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            rvLevel2Comments.layoutManager = LinearLayoutManager(mcontext)
            rvLevel2Comments.adapter = adapter
        }
        //加载一级评论
        if (!initialLoaded) {
            commentsViewModel.processIntent(CommentsContract.Intent.LoadLevel1Comment(level1CommentItem.commentId))
            initialLoaded = true
        }

        initListener()
        initObserve()
        registerKeyboardListener()
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { d ->
            val bottomSheet = d as BottomSheetDialog
            val sheet = bottomSheet.delegate.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            sheet?.let { view ->
                val behavior = BottomSheetBehavior.from(view)
                behavior.peekHeight = maxHeight
                // 固定高度
                view.layoutParams.height = maxHeight
                view.requestLayout()
            }
        }
    }
    private fun initObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                commentsViewModel.state.run {
                    observeState(
                        { value.level2CommentsResults },
                        {value.level2CommentPostState}
                    ) {  a, b ->
                        if (b == 2){
                            binding.rvLevel2Comments.scrollToPosition(0)
                        }
                        if (a.isNotEmpty() ) {
                            Log.d(TAG,"新的二级评论数据或一级评论数据")
                            adapter.submitList(a)
                            val count = a.size
                            val lastResult = a[count - 1]
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
    private fun initListener() {
        with(binding) {
            rvLevel2Comments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // canScrollVertically(1) 为 false 表示已经不能向下滚动（即底部）
                    val isAtBottom = !recyclerView.canScrollVertically(1)
                    Log.d(TAG, "onScrolled: isAtBottom=$isAtBottom, hasMore=$hasMore")
                    if (hasMore && isAtBottom) {
                        commentsViewModel.processIntent(
                            CommentsContract.Intent.LoadLevel2Comments(
                                level1CommentItem,
                                page++,
                                pageSize
                            )
                        )
                    }
                }
            })
            ivSend.setOnClickListener {
                if (etComment.text.isNotEmpty()){

                    if (isResponseLevel1Comment){
                        commentsViewModel.processIntent(
                            CommentsContract.Intent.SendComment(
                                level1CommentItem.freshNewsId,
                                binding.etComment.text.toString(),
                                level1CommentItem.commentId
                            )
                        )


                    }
                    else{
                        commentsViewModel.processIntent(
                            CommentsContract.Intent.SendComment(
                                level1CommentItem.freshNewsId,
                                "@${responseToUserName}  "+binding.etComment.text.toString(),
                                level1CommentItem.commentId
                            )
                        )
                    }
                }
                etComment.setText("")
                val imm = getSystemService(mcontext, InputMethodManager::class.java) as InputMethodManager
                imm.hideSoftInputFromWindow(etComment.windowToken, 0)
            }
        }

    }
    private fun registerKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { view, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            Log.d(TAG, "键盘是否可见: $imeVisible, 键盘高度: $imeHeight")
            with(binding){
                if (!imeVisible) {
                    // 当键盘不可见时，重置底部间距
                    inputLayout.setPadding(
                        inputLayout.paddingStart,
                        inputLayout.paddingTop,
                        inputLayout.paddingEnd,
                        15
                    )
                    etComment.hint = getString(R.string.comment_hint)
                    isResponseLevel1Comment = true
                    responseToUserName = level1CommentItem.userName
                }
            }


            // 返回原始的 insets，让系统继续处理
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Level2CommentsDialog 销毁")
        commentsViewModel.processIntent(CommentsContract.Intent.ResetLevel2Comments)
    }
}