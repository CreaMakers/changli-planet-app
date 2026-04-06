package com.creamaker.changli_planet_app.core.main.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainTabNavigator(
    initialDestination: MainTabDestination = MainTabDestination.Overview
) {
    private var selectedDestinationState by mutableStateOf(initialDestination)

    val currentDestination: MainTabDestination
        get() = selectedDestinationState

    val displayBackStack: List<MainTabDestination>
        get() = listOf(selectedDestinationState)

    fun select(destination: MainTabDestination) {
        if (selectedDestinationState == destination) {
            return
        }
        selectedDestinationState = destination
    }

    fun select(index: Int): Boolean {
        val destination = MainTabDestination.fromIndex(index) ?: return false
        select(destination)
        return true
    }
}
