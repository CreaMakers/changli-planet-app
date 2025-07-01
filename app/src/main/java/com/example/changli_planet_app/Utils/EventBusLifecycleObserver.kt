package com.example.changli_planet_app.Utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.greenrobot.eventbus.EventBus

class EventBusLifecycleObserver(private val subscriber: Any) : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        // 注册EventBus
        if (!EventBus.getDefault().isRegistered(subscriber)) {
            EventBusHelper.register(subscriber)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        // 解注册EventBus
        if (EventBus.getDefault().isRegistered(subscriber)) {
            EventBusHelper.unregister(subscriber)
        }
    }
}

