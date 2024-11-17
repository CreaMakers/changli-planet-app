package com.example.changli_planet_app

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.example.changli_planet_app.Activity.Electronic

import com.example.changli_planet_app.Activity.TimeTable
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.FragmentFeatureBinding

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [Feature.newInstance] factory method to
 * create an instance of this fragment.
 */
class Feature : Fragment() {
    lateinit var binding: FragmentFeatureBinding
    private val electronic: LinearLayout by lazy { binding.electonic }
    private val coursesLayout: LinearLayout by lazy { binding.coursesLayout }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeatureBinding.inflate(layoutInflater)
        electronic.setOnClickListener {
            val intent = Intent(activity, Electronic::class.java)
            startActivity(intent)
        }
        coursesLayout.setOnClickListener {
            val intent = Intent(activity, TimeTable::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btn = binding.drawBtn
        btn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }


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