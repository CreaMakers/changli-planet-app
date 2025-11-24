package com.creamaker.changli_planet_app.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    open val TAG = javaClass.simpleName

    /**
     * 对外暴露只读的binding属性
     * 使用lateinit是因为我们会在onCreateView中初始化
     */
    protected lateinit var binding: VB
        private set

    open val mDisposable = CompositeDisposable()

    /**
     * 子类必须实现此方法来创建ViewBinding实例
     */
    protected abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activeInflater = requireActivity().layoutInflater
        binding = createViewBinding(activeInflater , container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        initObserve()
    }

    /**
     * 显示加载对话框
     */
    protected open fun showLoading() {
        // TODO 可以在这里实现统一的加载对话框显示逻辑
    }

    /**
     * 隐藏加载对话框
     */
    protected open fun hideLoading() {
        // TODO 可以在这里实现统一的加载对话框隐藏逻辑
    }

    /**
     * 检查Fragment是否仍然活跃
     */
    protected fun isFragmentActive(): Boolean {
        return isAdded && !isDetached && !isRemoving && view != null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable.dispose()
    }
}