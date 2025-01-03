package com.example.changli_planet_app.Fragment


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.databinding.FragmentFeatureBinding

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [FeatureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeatureFragment : Fragment() {


    lateinit var menu: Menu


    private val timeTable by lazy { binding.ncourse }
    lateinit var  binding : FragmentFeatureBinding
    private val electronic:LinearLayout by lazy { binding.nelectronic }
    private val grade:LinearLayout by lazy { binding.ngrade }
    private val ntest:LinearLayout by lazy { binding.ntest }
    private val ncet:LinearLayout by lazy { binding.ncet }
    private val nmande:LinearLayout by lazy { binding.nmande }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeatureBinding.inflate(layoutInflater)


        electronic.setOnClickListener { activity?.let { it1 -> Route.goElectronic(it1) } }
        timeTable.setOnClickListener { activity?.let { it1 -> Route.goTimetable(it1) } }

        grade.setOnClickListener { activity?.let { it1 -> Route.goScoreInquiry(it1) } }
        ntest.setOnClickListener { activity?.let { it1 -> Route.goExamArrangement(it1) } }
        ncet.setOnClickListener { activity?.let { it1 -> Route.goCet(it1) } }
        nmande.setOnClickListener { activity?.let { it1 -> Route.goMande(it1) } }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    fun setUpImageView() {
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
            FeatureFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}