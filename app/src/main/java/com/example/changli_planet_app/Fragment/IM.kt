package com.example.changli_planet_app.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.FragmentFindBinding
import com.example.changli_planet_app.databinding.FragmentIMBinding

class IM : Fragment() {
    private lateinit var binding: FragmentIMBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIMBinding.inflate(layoutInflater)
        return binding.root
    }
    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            IM().apply {}
    }
}