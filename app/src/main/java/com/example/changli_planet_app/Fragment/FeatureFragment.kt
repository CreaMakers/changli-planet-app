package com.example.changli_planet_app.Fragment


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Transition
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.UI.FunctionItem
import com.example.changli_planet_app.databinding.FragmentFeatureBinding

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [FeatureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeatureFragment : Fragment() {
    private lateinit var binding: FragmentFeatureBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeatureBinding.inflate(layoutInflater)
        setupClickListeners()
        setIcons()
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
                // 设置 logo
                Glide.with(ctx)
                    .load(R.drawable.planet_logo)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(planetLogo)

                // 设置功能图标
                ngrade.setIcon(R.drawable.ngrade)
                ncourse.setIcon(R.drawable.ncourse)
                nmap.setIcon(R.drawable.nmap)
                ncet.setIcon(R.drawable.ncet)
                ntest.setIcon(R.drawable.ntest)
                ncalender.setIcon(R.drawable.ncalender)
                nadd.setIcon(R.drawable.nadd)
                nmande.setIcon(R.drawable.nmande)
                nlose.setIcon(R.drawable.nlose)
                nnotice.setIcon(R.drawable.nnotice)
                nelectronic.setIcon(R.drawable.nelectronic)
                nrank.setIcon(R.drawable.nrank)
                nbalance.setIcon(R.drawable.nbalance)
                nclassroom.setIcon(R.drawable.nclassroom)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FeatureFragment()
    }
}