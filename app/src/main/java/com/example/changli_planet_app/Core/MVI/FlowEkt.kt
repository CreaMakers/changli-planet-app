package com.example.changli_planet_app.Core.MVI

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.reflect.KProperty1

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
