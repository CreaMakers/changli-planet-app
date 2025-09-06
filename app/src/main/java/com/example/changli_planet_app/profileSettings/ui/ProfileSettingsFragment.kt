package com.example.changli_planet_app.profileSettings.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.changli_planet_app.R
import com.example.changli_planet_app.base.BaseFragment
import com.example.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.example.changli_planet_app.core.PlanetApplication
import com.example.changli_planet_app.core.Route
import com.example.changli_planet_app.databinding.FragmentProfileSettingsBinding
import com.example.changli_planet_app.profileSettings.ui.adapter.SettingAdapter
import com.example.changli_planet_app.profileSettings.ui.adapter.model.SettingItem
import com.example.changli_planet_app.utils.NetworkUtil
import com.example.changli_planet_app.utils.load
import com.example.changli_planet_app.widget.Dialog.NormalChosenDialog
import com.example.changli_planet_app.widget.View.CustomToast

class ProfileSettingsFragment : BaseFragment<FragmentProfileSettingsBinding>() {
    private lateinit var settingAdapter: SettingAdapter
    companion object Companion {
        private const val REQUEST_READ_TELEPHONE = 1001
        private const val REQUEST_NOTIFICATION = 1002
        private const val FEI_SHU_URL =
            "https://creamaker.feishu.cn/share/base/form/shrcn6LjBK78JLJfLeKDMe3hczd?chunked=false"
        @JvmStatic
        fun newInstance() =
            ProfileSettingsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileSettingsBinding {
        return FragmentProfileSettingsBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        setupViews()
        initRecyclerView()
        loadUserData()
    }

    private fun setupViews() {
        binding.ivBackground.load(R.drawable.ic_profile_home_bg)
        binding.mbLogout.setOnClickListener {
            NormalChosenDialog(
                requireContext(),
                "将清除本地所有缓存",
                "是否登出",
                onConfirm = {
                    PlanetApplication.Companion.clearCacheAll()
                    Route.goLoginForcibly(requireContext())
                }
            ).show()
        }

        binding.ivEdit.setOnClickListener {
            getNetPermissions()
            if (NetworkUtil.Companion.getNetworkType(requireContext()) != NetworkUtil.NetworkType.None) { //检查网络是否连接
                Route.goUserProfile(requireContext())
            } else {
                CustomToast.Companion.showMessage(requireContext(), "网络未连接")
            }
        }
    }

    private fun getNetPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_READ_TELEPHONE
            )
        } else {
            return
        }
    }
    private fun initRecyclerView() {
        val settingItems = createSettingItems()
        settingAdapter = SettingAdapter(settingItems).apply {
            setOnSettingItemClickListener { item ->
                if (!item.isHeader) {
                    handleSettingItemClick(item)
                }
            }
        }

        binding.rvSettings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingAdapter
        }
    }

    private fun createSettingItems(): List<SettingItem> {
        return listOf(
            SettingItem("主要设置", isHeader = true),
//            SettingItem("通知设置", R.drawable.notification2, actionType = 1),
            SettingItem("隐私设置", R.drawable.yingsi, actionType = 2),
            SettingItem("账号安全", R.drawable.zhanghao, actionType = 3),

            SettingItem("常用功能", isHeader = true),
            SettingItem("清除缓存", R.drawable.qingchu, actionType = 4),
            SettingItem("绑定学号", R.drawable.bianji_, actionType = 5),
//            SettingItem("主题设置", R.drawable.zhuti_tiaosepan, actionType = 6),
//            SettingItem("消息中心", R.drawable.xiaoxi, actionType = 7),

            SettingItem("帮助与支持", isHeader = true),
            SettingItem("帮助中心", R.drawable.bangzhu, actionType = 8),
            SettingItem("关于我们", R.drawable.guanyuwomen, actionType = 9),
            SettingItem("意见反馈", R.drawable.yijianfankui, actionType = 10)
        )
    }

    private fun handleSettingItemClick(item: SettingItem) {
        when (item.actionType) {
//            1 -> {
//                // 通知设置
//                // TODO: 实现通知设置跳转
//            }

            2 -> {
                // 隐私设置
                // TODO: 实现隐私设置跳转
            }

            3 -> {
                // 账号安全
                activity?.let { Route.goAccountSecurity(it) }
            }

            4 -> {
                // 清除缓存
                showClearCacheDialog()
            }

            5 -> {
                // 绑定学号
                activity?.let { Route.goBindingUser(it) }
            }

//            6 -> {
//                // 主题设置
//                // TODO: 实现主题设置跳转
//            }
//
//            7 -> {
//                // 消息中心
//                // TODO: 实现消息中心跳转
//            }

            8 -> {
                // 帮助中心
                // TODO: 实现帮助中心跳转
            }

            9 -> {
                // 关于我们
                Route.goAbout(requireContext())
            }

            10 -> {
                // 意见反馈
                Route.goWebView(requireContext(), FEI_SHU_URL)
            }
        }
    }

    private fun showClearCacheDialog() {
        activity?.let { activity ->
            NormalChosenDialog(
                activity,
                "将清除实用工具的所有缓存",
                "确定要清除缓存嘛₍ᐢ.ˬ.⑅ᐢ₎"
            ) {
                PlanetApplication.clearContentCache()
                CustomToast.showMessage(activity, "缓存已清除")
            }.show()
        }
    }
    private fun loadUserData() {
        binding.tvUsername.text = UserInfoManager.account
        binding.ivAvatar.load(UserInfoManager.userAvatar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}