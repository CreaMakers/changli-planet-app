package com.example.changli_planet_app.Fragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.changli_planet_app.databinding.FragmentFindBinding
class FindFragment : Fragment() {
    private lateinit var binding: FragmentFindBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFindBinding.inflate(layoutInflater)
        return binding.root
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            FindFragment().apply {
            }
    }
}