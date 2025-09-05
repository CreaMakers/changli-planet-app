package com.example.changli_planet_app.feature.common.ui

import android.content.Context
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.changli_planet_app.R
import com.example.changli_planet_app.base.BaseFragment
import com.example.changli_planet_app.common.api.DrawerController
import com.example.changli_planet_app.common.cache.CommonInfo
import com.example.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.example.changli_planet_app.core.Route
import com.example.changli_planet_app.databinding.FragmentFeatureBinding
import com.example.changli_planet_app.utils.load
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeatureFragment : BaseFragment<FragmentFeatureBinding>() {
    private val featureAvatar by lazy { binding.featureAvatar }
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

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFeatureBinding {
        return FragmentFeatureBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setIcons()

        Looper.myQueue().addIdleHandler {
            setupClickListeners()
            false
        }

        featureAvatar.setOnClickListener { drawerController?.openDrawer() }
        Log.d(TAG, "花费时间 ${System.currentTimeMillis() - CommonInfo.startTime}")
    }

    override fun initData() {
        // 保持原有的延迟加载头像逻辑
        lifecycleScope.launch {
            delay(1200)
            featureAvatar.load(UserInfoManager.userAvatar)
        }
    }

    override fun onResume() {
        super.onResume()
        featureAvatar.load(UserInfoManager.userAvatar)
    }

    private fun setupClickListeners() {
        with(binding) {
            nmap.setOnClickListener { activity?.let { Route.goCampusMap(it) } }
            nelectronic.setOnClickListener { activity?.let { Route.goElectronic(it) } }
            ncourse.setOnClickListener { activity?.let { Route.goTimetable(it) } }
            ngrade.setOnClickListener { activity?.let { Route.goScoreInquiry(it) } }
            ntest.setOnClickListener { activity?.let { Route.goExamArrangement(it) } }
            ncet.setOnClickListener { activity?.let { Route.goCet(it) } }
            nmande.setOnClickListener { activity?.let { Route.goMande(it) } }
            nclassroom.setOnClickListener { activity?.let { Route.goClassInfo(it) } }
            accountbook.setOnClickListener { activity?.let { Route.goAccountBook(it) } }
            ndocument.setOnClickListener { activity?.let { Route.goContract(it) } }
        }
    }

    private fun setIcons() {
        context?.let { ctx ->
            with(binding) {
                planetLogo.load(R.drawable.planet_logo)

                // 设置功能图标
                val iconIds = listOf(
                    ngrade to R.drawable.ic_exam,
                    ncourse to R.drawable.ic_timetable,
                    nmap to R.drawable.ic_map,
                    ncet to R.drawable.ic_essay,
                    ntest to R.drawable.ic_schedule,
                    ncalender to R.drawable.ic_calendar,
                    nmande to R.drawable.ic_talking,
                    nlose to R.drawable.ic_lost_and_found,
                    nelectronic to R.drawable.ic_bill,
                    nrank to R.drawable.ic_rank,
                    nclassroom to R.drawable.ic_classroom,
                    accountbook to R.drawable.account_book,
                    ndocument to R.drawable.ic_document
                )

                iconIds.forEach { (item, resId) ->
                    item.setIconWithGlide(resId)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FeatureFragment()
    }
}