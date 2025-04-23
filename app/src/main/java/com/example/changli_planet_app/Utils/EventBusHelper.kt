package com.example.changli_planet_app.Utils
import org.greenrobot.eventbus.EventBus

object EventBusHelper {
        /**
         * 注册事件监听器
         */
        fun register(subscriber: Any) {
            if (!EventBus.getDefault().isRegistered(subscriber)) {
                EventBus.getDefault().register(subscriber)
            }
        }
        /**
         * 解注册事件监听器
         */
        fun unregister(subscriber: Any) {
            if (EventBus.getDefault().isRegistered(subscriber)) {
                EventBus.getDefault().unregister(subscriber)
            }
        }
        /**
         * 发送事件
         */
        fun post(event: Any) {
            EventBus.getDefault().post(event)
        }
        /**
         * 发送粘性事件
         */
        fun postSticky(event: Any) {
            EventBus.getDefault().postSticky(event)
        }
        /**
         * 移除指定的粘性事件
         */
        fun <T> removeStickyEvent(eventType: Class<T>) {
            EventBus.getDefault().removeStickyEvent(eventType)
        }
}