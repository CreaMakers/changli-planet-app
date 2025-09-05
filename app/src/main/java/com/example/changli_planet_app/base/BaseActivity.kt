package com.example.changli_planet_app.base

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class BaseActivity<VB: ViewBinding> : AppCompatActivity() {

    open val TAG = javaClass.simpleName

    protected lateinit var binding: VB
        private set

    protected abstract fun createViewBinding(): VB

    protected val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        binding = createViewBinding()
        setContentView(binding.root)
        initView()
        initObserve()
        initData()
    }

    /**
     * 初始化视图
     */
    protected open fun initView() {
        // 子类实现具体的视图初始化逻辑
    }

    /**
     * 初始化数据
     */
    protected open fun initData() {
        // 子类实现具体的数据初始化逻辑
    }

    /**
     * 初始化监听器
     */
    protected open fun initObserve() {
        // 子类实现具体的监听器初始化逻辑
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}