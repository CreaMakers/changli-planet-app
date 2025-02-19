package com.example.changli_planet_app.Fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.IMChatListAdapter
import com.example.changli_planet_app.Data.jsonbean.ChatListItem
import com.example.changli_planet_app.Interface.DrawerController
import com.example.changli_planet_app.databinding.FragmentIMBinding
import com.google.android.material.imageview.ShapeableImageView

class IMFragment : Fragment() {
    private lateinit var binding: FragmentIMBinding
    private val TAG = "IMFragment"
    private val recyclerView: RecyclerView by lazy { binding.chatListRecycler }
    private val imAvatar: ShapeableImageView by lazy { binding.imAvatar }
    private var drawerController: DrawerController? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DrawerController) {
            drawerController = context
        } else {
            Log.d(TAG, "DrawerControl,宿主Activity未实现接口")
        }
    }

    override fun onDetach() {
        drawerController = null
        super.onDetach()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIMBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imAvatar.setOnClickListener { drawerController?.openDrawer() }
        recyclerView.adapter = IMChatListAdapter(chatList)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private val chatList = listOf(
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Haley James",
            lastMessage = "Stand up for what you believe in",
            messageCount = 9
        ),
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Nathan Scott",
            lastMessage = "One day you're seventeen and planning for someday. And then quietly and without...",
            messageCount = 0
        ),
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Marvin McFadden",
            lastMessage = "Last night in the NBA the Charlotte Bobcats quietly made a move that most sports fans...",
            messageCount = 0
        ),
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://csustplant.obs.cn-south-1.myhuaweicloud.com:443/userAvatar/creamaker.png?AccessKeyId=SE9UPDVWGDCZ18BGAEZN&Expires=1739888760&Signature=z6hYk%2F9ek9i4ZqO9CX74GOAZgus%3D",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        )
    )

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            IMFragment().apply {}
    }
}