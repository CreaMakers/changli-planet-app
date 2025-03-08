package com.example.changli_planet_app.Fragment


import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.GlideApp
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Interface.DrawerController
import com.example.changli_planet_app.Util.GlideUtils
import com.example.changli_planet_app.databinding.FragmentFeatureBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [FeatureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeatureFragment : Fragment() {
    private val TAG = "FeatureFragment"
    private lateinit var binding: FragmentFeatureBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val start = System.currentTimeMillis()
        binding = FragmentFeatureBinding.inflate(layoutInflater)
        setIcons()
        Looper.myQueue().addIdleHandler {
            setupClickListeners()
            false
        }
        lifecycleScope.launch {
            delay(1200)
            GlideUtils.load(
                this@FeatureFragment, featureAvatar, UserInfoManager.userAvatar
            )
        }
        featureAvatar.setOnClickListener { drawerController?.openDrawer() }
        Log.d(TAG, "花费时间 ${System.currentTimeMillis() - start}")
        return binding.root
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
        }

    }

    override fun onResume() {
        super.onResume()
        GlideUtils.load(
            this@FeatureFragment, featureAvatar, UserInfoManager.userAvatar
        )
    }

    private fun setIcons() {
        context?.let { ctx ->
            with(binding) {
                GlideUtils.load(
                    this@FeatureFragment, planetLogo, R.drawable.planet_logo
                )

                // 设置功能图标
                val iconIds = listOf(
                    ngrade to R.drawable.ngrade,
                    ncourse to R.drawable.ncourse,
                    nmap to R.drawable.nmap,
                    ncet to R.drawable.ncet,
                    ntest to R.drawable.ntest,
                    ncalender to R.drawable.ncalender,
                    nmande to R.drawable.nmande,
                    nlose to R.drawable.nlose,
                    nelectronic to R.drawable.nelectronic,
                    nrank to R.drawable.nrank,
                    nclassroom to R.drawable.nclassroom
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