package com.example.changli_planet_app.Core

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * 所有调度器的基类，所有Store都应该继承Store基类
 */
open abstract class Store<S,A> {
    private val eventStream = PublishSubject.create<A>()
    val _state = PublishSubject.create<S>()
    init {
        eventStream.observeOn(AndroidSchedulers.mainThread())
                    .subscribe{action->
                        handleEvent(action)
                    }
    }
    abstract fun handleEvent(action:A)
    fun dispatch(action: A){
        eventStream.onNext(action)
    }
    fun state() = _state.observeOn(AndroidSchedulers.mainThread())
}