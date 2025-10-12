package com.creamaker.changli_planet_app.im.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.common.api.DrawerController
import com.creamaker.changli_planet_app.databinding.FragmentIMBinding
import com.creamaker.changli_planet_app.im.data.ChatListItem
import com.creamaker.changli_planet_app.utils.load

class IMFragment : Fragment() {
    private lateinit var binding: FragmentIMBinding
    private val TAG = "IMFragment"

//    private val recyclerView: RecyclerView by lazy { binding.chatListRecycler }
//    private val imAvatar: ShapeableImageView by lazy { binding.imAvatar }
//    private val imName by lazy { binding.imName }

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

//    override fun onResume() {
//        super.onResume()
//        imName.text = UserInfoManager.account
//        GlideUtils.loadWithThumbnail(
//            this,
//            imAvatar,
//            UserInfoManager.userAvatar
//        )
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIMBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivUnderConstruction.load(R.drawable.under_construction)
//        imAvatar.setOnClickListener { drawerController?.openDrawer() }
//        recyclerView.adapter = IMChatListAdapter(chatList)
//        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private val chatList = listOf(
        ChatListItem(
            groupAvatar = "https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg",
            groupName = "Brooke Davis",
            lastMessage = "I am who I am. No excuses.",
            messageCount = 2
        ),
        ChatListItem(
            groupAvatar = "https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg",
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