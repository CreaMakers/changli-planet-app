package com.example.changli_planet_app.Fragment


import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Interface.DrawerController
import com.example.changli_planet_app.databinding.FragmentFeatureBinding

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [FeatureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeatureFragment : Fragment() {
    private val TAG = "FeatureFragment"
    private lateinit var binding: FragmentFeatureBinding
    private val menuButton by lazy { binding.featureMenuButton }
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
        binding = FragmentFeatureBinding.inflate(layoutInflater)
        setupClickListeners()
        setIcons()
        menuButton.setOnClickListener { drawerController?.openDrawer() }
        Looper.myQueue().addIdleHandler {
            setIcons()
            false
        }
        return binding.root
    }

    private fun setupClickListeners() {

        with(binding) {
            nelectronic.setOnClickListener { activity?.let { Route.goElectronic(it) } }
            ncourse.setOnClickListener { activity?.let { Route.goTimetable(it) } }
            ngrade.setOnClickListener { activity?.let { Route.goScoreInquiry(it) } }
            ntest.setOnClickListener { activity?.let { Route.goExamArrangement(it) } }
            ncet.setOnClickListener { activity?.let { Route.goCet(it) } }
            nmande.setOnClickListener { activity?.let { Route.goMande(it) } }
        }

    }

    private fun setIcons() {
        context?.let { ctx ->
            with(binding) {
                planetLogo.setImageResource(R.drawable.planet_logo)
                // 设置功能图标
                ngrade.setIcon(R.drawable.ngrade)
                ncourse.setIcon(R.drawable.ncourse)
                nmap.setIcon(R.drawable.nmap)
                ncet.setIcon(R.drawable.ncet)
                ntest.setIcon(R.drawable.ntest)
                ncalender.setIcon(R.drawable.ncalender)
                nmande.setIcon(R.drawable.nmande)
                nlose.setIcon(R.drawable.nlose)
                nelectronic.setIcon(R.drawable.nelectronic)
                nrank.setIcon(R.drawable.nrank)
                nclassroom.setIcon(R.drawable.nclassroom)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FeatureFragment()
    }

    // 动态计算尺寸的核心方法
    private fun calculateTargetSize(view: View): Pair<Int, Int> {
        return when {
            view.width > 0 && view.height > 0 ->
                Pair(view.width, view.height) // 已测量完成的情况

            view.isLaidOut ->
                Pair(view.width, view.height) // 已布局完成的情况

            else -> {
                // 未完成布局时使用屏幕宽高估算
                val displayMetrics = view.context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels

                // 根据布局参数动态计算（示例为宽度充满，高度按比例）
                val targetWidth = screenWidth
                val targetHeight = (screenWidth * 1f).toInt() // 假设原图是1:1比例
                Pair(targetWidth, targetHeight)
            }
        }
    }
}