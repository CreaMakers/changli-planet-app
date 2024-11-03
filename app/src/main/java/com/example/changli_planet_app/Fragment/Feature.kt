package com.example.changli_planet_app.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.example.changli_planet_app.Activity.Electronic
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.FragmentFeatureBinding

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [Feature.newInstance] factory method to
 * create an instance of this fragment.
 */
class Feature : Fragment() {
    lateinit var  binding : FragmentFeatureBinding
    private val electronic:LinearLayout by lazy { binding.nelectronic }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeatureBinding.inflate(layoutInflater)
        electronic.setOnClickListener {
            val intent = Intent(activity,Electronic::class.java)
            startActivity(intent)
        }

        // 将设置资源的代码放入 IdleHandler
        Handler(Looper.getMainLooper()).post {
            // 注册 IdleHandler
            Looper.myQueue().addIdleHandler {
                setUpImageView()
                false
            }
        }
        return binding.root
    }
    fun setUpImageView(){
        // 动态设置 ImageView 的资源
        binding.planetLogo.setImageResource(R.drawable.planet_logo)
        binding.ngrade.setIcon(R.drawable.ngrade)
        binding.ncourse.setIcon(R.drawable.ncourse)
        binding.nmap.setIcon(R.drawable.nmap)
        binding.ncet.setIcon(R.drawable.ncet)
        binding.ntest.setIcon(R.drawable.ntest)
        binding.ncalender.setIcon(R.drawable.ncalender)
        binding.nadd.setIcon(R.drawable.nadd)
        binding.nmande.setIcon(R.drawable.nmande)
        binding.nlose.setIcon(R.drawable.nlose)
        binding.nnotice.setIcon(R.drawable.nnotice)
        binding.nelectronic.setIcon(R.drawable.nelectronic)
        binding.nrank.setIcon(R.drawable.nrank)
        binding.nbalance.setIcon(R.drawable.nbalance)
        binding.nclassroom.setIcon(R.drawable.nclassroom)
    }
    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Myfragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            Feature().apply {
                arguments = Bundle().apply {
                }
            }
    }
}