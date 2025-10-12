package com.creamaker.changli_planet_app.common.pool

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.view.View
import com.google.android.material.tabs.TabLayout.Tab

object TabAnimationPool {
    private val scaleDownAnimator = ObjectAnimator()
    private val scaleUpAnimator = ObjectAnimator()

    init {
        scaleDownAnimator.duration = 200
        scaleUpAnimator.duration = 200
    }

    fun animateTabSelect(tab: Tab) {
        val tabView = tab.view

        scaleDownAnimator.cancel()
        scaleUpAnimator.cancel()

        scaleDownAnimator.apply {
            target = tabView
            setPropertyName("scaleX")
            setPropertyName("scaleY")
            setFloatValues(1f, 0.8f)
            removeAllListeners()
            addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    startScaleUpAnimation(tabView)
                    (animation as ObjectAnimator).target = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    tabView.scaleX = 1f
                    tabView.scaleY = 1f
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
        }

        scaleDownAnimator.start()
    }

    private fun startScaleUpAnimation(view: View) {
        scaleUpAnimator.apply {
            target = view
            setPropertyName("scaleX")
            setPropertyName("scaleY")
            setFloatValues(0.8f, 1f)
            removeAllListeners()
            addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    (animation as ObjectAnimator).target = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    view.scaleX = 1f
                    view.scaleY = 1f
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
        }

        scaleUpAnimator.start()
    }

    fun clear() {
        scaleDownAnimator.cancel()
        scaleUpAnimator.cancel()
        scaleDownAnimator.removeAllListeners()
        scaleUpAnimator.removeAllListeners()
    }
}