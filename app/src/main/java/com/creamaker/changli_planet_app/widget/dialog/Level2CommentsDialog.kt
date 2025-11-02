package com.creamaker.changli_planet_app.widget.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.layout.Layout
import com.creamaker.changli_planet_app.databinding.DialogLevel2CommentsBinding
import com.creamaker.changli_planet_app.freshNews.contract.CommentsContract
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.freshNews.viewModel.CommentsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class Level2CommentsDialog(
    val mcontext: Context,
    val level1CommentItem: Level1CommentItem,
    val commentsViewModel: CommentsViewModel,
    val onUserClick: (userId: Int) -> Unit,
    val onLevel1LikedClick: (commentId:Int,userId:Int,isParent:Int) ->Unit,
    val onLevel2LikedClick: (commentId:Int,userId:Int,isParent:Int) ->Unit,
) :
    BottomSheetDialogFragment() {
    private val TAG = "Level2CommentsDialog"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogLevel2CommentsBinding.inflate(inflater, container, false)
        with(binding){
        }
        return binding.root
    }

}