package com.dcelysia.cp_common.start_up

import com.dcelysia.cp_common.start_up.dispatcher.Dispatcher
import com.dcelysia.cp_common.start_up.executor.ExecutorManager
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor


abstract class AndroidStartup<T> : Startup<T> {

    private val mWaitCountDown by lazy { CountDownLatch(getDependenciesCount()) }
    private val mObservers by lazy { mutableListOf<Dispatcher>() }

    override fun toWait() {
        try {
            mWaitCountDown.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun toNotify() {
        mWaitCountDown.countDown()
    }

    override fun createExecutor(): Executor = ExecutorManager.instance.ioExecutor

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return null
    }

    override fun dependenciesByName(): List<String>? {
        return null
    }

    override fun getDependenciesCount(): Int {
        if (dependenciesByName().isNullOrEmpty()) return dependencies()?.size ?: 0
        return dependenciesByName()?.size ?: 0
    }

    override fun onDependenciesCompleted(startup: Startup<*>, result: Any?) {}

    override fun manualDispatch(): Boolean = false

    override fun registerDispatcher(dispatcher: Dispatcher) {
        mObservers.add(dispatcher)
    }

    override fun onDispatch() {
        mObservers.forEach {
            it.toNotify()
        }
    }
}