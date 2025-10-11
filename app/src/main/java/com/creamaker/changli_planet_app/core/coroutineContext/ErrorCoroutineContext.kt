package com.creamaker.changli_planet_app.core.coroutineContext

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class ErrorCoroutineContext : AbstractCoroutineContextElement(CoroutineExceptionHandler),
    CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        Log.d("CoroutineError", "Caught an exception ${exception.message.toString()}")
    }
}