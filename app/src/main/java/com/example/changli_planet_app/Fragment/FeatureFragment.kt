package com.example.changli_planet_app.Fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.databinding.FragmentFeatureBinding
//import com.example.changli_planet_app.databinding.NavHeaderBinding

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [FeatureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeatureFragment : Fragment() {
    lateinit var binding: FragmentFeatureBinding
//    lateinit var navHeaderBinding: NavHeaderBinding
    lateinit var menu: Menu

    private val electronic: LinearLayout by lazy { binding.nelectronic }
    private val timeTable by lazy { binding.ncourse }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeatureBinding.inflate(layoutInflater)

//        val headerLayout = binding.navigation.getHeaderView(0)
//        navHeaderBinding = NavHeaderBinding.bind(headerLayout)

//        menu = binding.navigation.menu

        electronic.setOnClickListener { activity?.let { it1 -> Route.goElectronic(it1) } }
        timeTable.setOnClickListener { activity?.let { it1 -> Route.goTimetable(it1) } }

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
//        binding.drawerBtn.setOnClickListener {
//            binding.drawerLayout.openDrawer(GravityCompat.START)
//        }
//        binding.drawerLayout.setScrimColor(Color.TRANSPARENT)  // 禁止侧滑阴影效果
//        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) // 解锁滑动模式
//
//        navHeaderBinding.editName.setOnClickListener {
//
//        }
//        menu.findItem(R.id.menu_unbind).setOnMenuItemClickListener {
//            Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
//            true
//        }
//        menu.findItem(R.id.menu_clear_cache).setOnMenuItemClickListener {
//            Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
//            true
//        }
//        menu.findItem(R.id.menu_contact_info).setOnMenuItemClickListener {
//            Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
//            true
//        }
//        menu.findItem(R.id.menu_tomorrow_courses).setOnMenuItemClickListener {
//            Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
//            true
//        }
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
//        binding.drawerBtn.setImageResource(R.drawable.prehead)
//        navHeaderBinding.iconImage.setImageResource(R.drawable.prehead)
//        navHeaderBinding.navRootLayout.setBackgroundResource(R.drawable.personbackground)
//        navHeaderBinding.editName.setImageResource(R.drawable.nav_edit)
//        menu.findItem(R.id.menu_unbind).setIcon(R.drawable.menu_unbind)
//        menu.findItem(R.id.menu_clear_cache).setIcon(R.drawable.menu_clear_cache)
//        menu.findItem(R.id.menu_contact_info).setIcon(R.drawable.menu_contact)
//        menu.findItem(R.id.menu_tomorrow_courses).setIcon(R.drawable.menu_tomorrow_courses)
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