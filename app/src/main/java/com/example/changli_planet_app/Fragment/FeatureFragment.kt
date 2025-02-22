package com.example.changli_planet_app.Fragment


import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.changli_planet_app.Activity.Action.UserAction
import com.example.changli_planet_app.Activity.Store.UserStore
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Interface.DrawerController
import com.example.changli_planet_app.databinding.FragmentFeatureBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [FeatureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeatureFragment : Fragment() {
    private val TAG = "FeatureFragment"
    private lateinit var binding: FragmentFeatureBinding
    private var drawerController: DrawerController? = null

    private val featureAvatar by lazy { binding.featureAvatar }

    private val disposables by lazy { CompositeDisposable() }
    private val store by lazy { UserStore() }

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
        observeState()
        Looper.myQueue().addIdleHandler {
            setupClickListeners()
            false
        }
        store.dispatch(UserAction.GetCurrentUserProfile(requireContext()))
        Log.d(TAG, "花费时间 ${System.currentTimeMillis() - start}")
        return binding.root
    }

    private fun observeState() {
        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    Glide.with(this)
                        .load(state.userProfile.avatarUrl)
                        .into(featureAvatar)
                }
        )
    }

    private fun setupClickListeners() {

        with(binding) {
            featureAvatar.setOnClickListener { drawerController?.openDrawer() }
            nelectronic.setOnClickListener { activity?.let { Route.goElectronic(it) } }
            featureTimetable.setOnClickListener { activity?.let { Route.goTimetable(it) } }
            featureGrades.setOnClickListener { activity?.let { Route.goScoreInquiry(it) } }
            ntest.setOnClickListener { activity?.let { Route.goExamArrangement(it) } }
            ncet.setOnClickListener { activity?.let { Route.goCet(it) } }
            nmande.setOnClickListener { activity?.let { Route.goMande(it) } }
            nclassroom.setOnClickListener { activity?.let { Route.goClassInfo(it) } }
        }

    }

    private fun setIcons() {
        context?.let {
            with(binding) {
                // 设置功能图标
                val iconIds = listOf(
                    nmap to R.drawable.nmap,
                    ncet to R.drawable.ncet,
                    ntest to R.drawable.ntest,
                    ncalender to R.drawable.ncalender,
                    nmande to R.drawable.nmande,
                    nlose to R.drawable.nlose,
                    nelectronic to R.drawable.nelectronic,
                    nrank to R.drawable.nrank,
                    nclassroom to R.drawable.nclassroom
                )

                iconIds.forEach { (item, resId) ->
                    item.setIconWithGlide(resId)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FeatureFragment()


    }

}