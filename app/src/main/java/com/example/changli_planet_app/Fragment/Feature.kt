package com.example.changli_planet_app.Fragment

import android.content.Intent
import android.os.Bundle
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
    private val electronic:LinearLayout by lazy { binding.electonic }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeatureBinding.inflate(layoutInflater)
        electronic.setOnClickListener {
            val intent = Intent(activity,Electronic::class.java)
            startActivity(intent)
        }
        return binding.root
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