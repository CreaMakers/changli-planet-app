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
        val start = System.currentTimeMillis()
        binding = FragmentFeatureBinding.inflate(layoutInflater)
        setIcons()
        Looper.myQueue().addIdleHandler {
            setupClickListeners()
            false
        }
        menuButton.setOnClickListener { drawerController?.openDrawer() }
        Log.d(TAG, "花费时间 ${System.currentTimeMillis() - start}")
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
            nclassroom.setOnClickListener { activity?.let { Route.goClassInfo(it) } }
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

}