package com.example.changli_planet_app.Util
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
class EventBusLifecycleObserver(private val subscriber: Any) : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        // 注册EventBus
        EventBusHelper.register(subscriber)
    }
    override fun onStop(owner: LifecycleOwner) {
        // 解注册EventBus
        EventBusHelper.unregister(subscriber)
    }
}

