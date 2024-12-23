package com.example.changli_planet_app.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.IMChatListAdapter
import com.example.changli_planet_app.Data.jsonbean.ChatListItem
import com.example.changli_planet_app.databinding.FragmentIMBinding

class IMFragment : Fragment() {
    private lateinit var binding: FragmentIMBinding
    private val recyclerView: RecyclerView by lazy { binding.chatListRecycler }

    private val chatList = listOf(
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Haley James",
            lastMessage = "Stand up for what you believe in",
            messageCount = 9
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Nathan Scott",
            lastMessage = "One day you're seventeen and planning for someday. And then quietly and without...",
            messageCount = 0
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Marvin McFadden",
            lastMessage = "Last night in the NBA the Charlotte Bobcats quietly made a move that most sports fans...",
            messageCount = 0
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Jamie Scott",
            lastMessage = "Some people are a little different. I think that's cool.",
            messageCount = 0
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Marvin McFadden",
            lastMessage = "Last night in the NBA the Charlotte Bobcats quietly made a move that most sports fans...",
            messageCount = 0
        ),
        ChatListItem(
            groupAvatar = "https://qiniu.dcelysia.top/articleCover/Elysia11721787964852441.jpg",
            groupName = "Antwon Taylor",
            lastMessage = "Meet me at the Rivercourt",
            messageCount = 0
        )
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIMBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = IMChatListAdapter(chatList)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            IMFragment().apply {}
    }
}