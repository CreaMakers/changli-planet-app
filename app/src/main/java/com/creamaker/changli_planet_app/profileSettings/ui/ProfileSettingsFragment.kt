package com.creamaker.changli_planet_app.profileSettings.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.BaseFragment
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.databinding.FragmentProfileSettingsBinding
import com.creamaker.changli_planet_app.profileSettings.ui.adapter.SettingAdapter
import com.creamaker.changli_planet_app.profileSettings.ui.adapter.model.SettingItem
import com.creamaker.changli_planet_app.utils.Event.SelectEvent
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.utils.NetworkUtil
import com.creamaker.changli_planet_app.utils.load
import com.creamaker.changli_planet_app.widget.dialog.NormalChosenDialog
import com.creamaker.changli_planet_app.widget.view.CustomToast

class ProfileSettingsFragment() : BaseFragment<FragmentProfileSettingsBinding>() {
    private var backCallback: OnBackPressedCallback? = null

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

        if (PlanetApplication.is_tourist) {
            setupTouristViews()
        } else {
            setupViews()
        }
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

    private fun setupTouristViews() {
        binding.ivBackground.load(R.drawable.ic_profile_home_bg)
        binding.mbLogout.setOnClickListener {
            NormalChosenDialog(
                requireContext(),
                "将清除本地所有缓存哦~",
                "现在进行登录吗",
                onConfirm = {
                    PlanetApplication.Companion.clearCacheAll()
                    Route.goLoginForcibly(requireContext())
                }

            ).show()
        }
        binding.ivAvatar.setOnClickListener {
            NormalChosenDialog(
                requireContext(),
                "将清除本地所有缓存哦~",
                "现在进行登录吗",
                onConfirm = {
                    PlanetApplication.Companion.clearCacheAll()
                    Route.goLoginForcibly(requireContext())
                }

            ).show()
        }
        binding.mbLogout.text = "登录账号"
        binding.ivEdit.visibility = View.GONE
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
            SettingItem("绑定学号", R.drawable.ic_bianji, actionType = 5),
            SettingItem("主题设置", R.drawable.zhuti_tiaosepan, actionType = 6),
//            SettingItem("消息中心", R.drawable.xiaoxi, actionType = 7),

            SettingItem("帮助与支持", isHeader = true),
            SettingItem("帮助中心", R.drawable.ic_help, actionType = 8),
            SettingItem("关于我们", R.drawable.ic_guanyuwomen, actionType = 9),
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
                if (PlanetApplication.is_tourist) {
                    CustomToast.showMessage(requireContext(), "游客账号无法进行此操作哦~")
                    return
                }
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

            6 -> {
                activity?.let { Route.goSkinSecletion(it) }
            }
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
        if (PlanetApplication.is_tourist) { //对游客账号进行特殊处理，为避免与网络逻辑嵌套混淆，没有走MVI流
            binding.tvUsername.text = "长理学子~"
            binding.ivAvatar.load(UserInfoManager.userAvatar)
        } else {
            binding.tvUsername.text = UserInfoManager.account
            binding.ivAvatar.load(UserInfoManager.userAvatar)
        }
    }


    override fun onResume() {
        super.onResume()
        if (backCallback == null) {

            backCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    EventBusHelper.post(SelectEvent(0))
                    isEnabled = false
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                backCallback!!
            )
        }
    }

    override fun onPause() {
        super.onPause()
        backCallback?.remove()
        backCallback = null
    }
}