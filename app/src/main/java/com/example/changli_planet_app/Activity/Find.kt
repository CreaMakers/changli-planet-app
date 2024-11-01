package com.example.changli_planet_app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.FragmentFindBinding

class Find : Fragment() {
    lateinit var binding: FragmentFindBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFindBinding.inflate(layoutInflater)
        return binding.root
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Find().apply {
            }
    }
}