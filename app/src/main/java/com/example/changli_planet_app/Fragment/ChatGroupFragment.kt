package com.example.changli_planet_app.Fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.ChatGroupLeftItemAdapter
import com.example.changli_planet_app.Adapter.ChatGroupRightAdapter
import com.example.changli_planet_app.Data.jsonbean.ChatGroupItem
import com.example.changli_planet_app.Data.jsonbean.ChatGroupLeftItem
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.FragmentChatGroupBinding

class ChatGroupFragment : Fragment() {

    private lateinit var binding: FragmentChatGroupBinding
    private val leftRecyclerView: RecyclerView by lazy { binding.leftRecyclerView }
    private val rightRecyclerView: RecyclerView by lazy { binding.rightRecyclerView }

    private val leftRecyclerData: List<ChatGroupLeftItem> = listOf(
        ChatGroupLeftItem("学习", R.drawable.chat_study),
        ChatGroupLeftItem("生活", R.drawable.chat_live),
        ChatGroupLeftItem("工具", R.drawable.chat_tool),
        ChatGroupLeftItem("问题\n反馈", R.drawable.chat_question),
        ChatGroupLeftItem("社团", R.drawable.chat_club),
        ChatGroupLeftItem("比赛", R.drawable.chat_contest),
    )

    private val rightRecyclerData = listOf(
        ChatGroupItem("芙莉莲同人群", "https://qiniu.dcelysia.top/articleCover/Elysia11721746869561904.png", "最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了"),
        ChatGroupItem("芙莉莲同人群", "https://qiniu.dcelysia.top/articleCover/Elysia11721746869561904.png", "最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了"),
        ChatGroupItem("芙莉莲同人群", "https://qiniu.dcelysia.top/articleCover/Elysia11721746869561904.png", "最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了"),
        ChatGroupItem("芙莉莲同人群", "https://qiniu.dcelysia.top/articleCover/Elysia11721746869561904.png", "最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了"),
        ChatGroupItem("芙莉莲同人群", "https://qiniu.dcelysia.top/articleCover/Elysia11721746869561904.png", "最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了"),
        ChatGroupItem("芙莉莲同人群", "https://qiniu.dcelysia.top/articleCover/Elysia11721746869561904.png", "最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了"),
        ChatGroupItem("芙莉莲同人群", "https://qiniu.dcelysia.top/articleCover/Elysia11721746869561904.png", "最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了最喜欢爱莉希雅了"),

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.underConstructionView.setOnClickListener {
            it.visibility = View.GONE
        }
        leftRecyclerView.adapter = ChatGroupLeftItemAdapter(leftRecyclerData)
        leftRecyclerView.layoutManager = LinearLayoutManager(context)
        rightRecyclerView.adapter = ChatGroupRightAdapter(rightRecyclerData)
        rightRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ChatGroupFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}