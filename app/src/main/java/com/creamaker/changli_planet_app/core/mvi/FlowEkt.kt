package com.creamaker.changli_planet_app.core.mvi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

suspend fun <T, A> StateFlow<T>.observeState(
    prop1: (T) -> A,
    action: (a: A) -> Unit
) {
    this.map { StateTuple1(prop1.invoke(it)) }.distinctUntilChanged().collect {
        action(it.a)
    }
}

suspend fun <T, A, B> StateFlow<T>.observeState(
    prop1: (T) -> A,
    prop2: (T) -> B,
    action: (a: A, b: B) -> Unit
) {
    this.map { StateTuple2(prop1.invoke(it) to prop2.invoke(it)) }.distinctUntilChanged().collect {
        action(it.a, it.b)
    }
}

suspend fun <T, A, B, C> StateFlow<T>.observeState(
    prop1: (T) -> A,
    prop2: (T) -> B,
    prop3: (T) -> C,
    action: (a: A, b: B, c: C) -> Unit
) {
    this.map { StateTuple3(prop1(it), prop2(it), prop3(it)) }
        .distinctUntilChanged()
        .collect { action(it.a, it.b, it.c) }
}

fun <T> MutableStateFlow<T>.setValue(newValue: T) {
    this.value = newValue
}


fun <T> MutableStateFlow<T>.updateState(reducer: T.() -> T) {
    update { value.reducer() }
}
@JvmInline
value class StateTuple1<A>(val a: A)
@JvmInline
value class StateTuple2<A, B>(private val data: Pair<A, B>) {
    val a: A get() = data.first
    val b: B get() = data.second
}
@JvmInline
value class StateTuple3<A, B, C>(private val data: Triple<A, B, C>) {
    constructor(a: A, b: B, c: C) : this(Triple(a, b, c))
    val a: A get() = data.first
    val b: B get() = data.second
    val c: C get() = data.third
}
