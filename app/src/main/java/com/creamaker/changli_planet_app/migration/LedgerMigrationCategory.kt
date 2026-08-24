package com.creamaker.changli_planet_app.migration

import com.creamaker.changli_planet_app.R

object LedgerMigrationCategory {
    const val PHONE = "PHONE"
    const val LAPTOP = "LAPTOP"
    const val TABLET = "TABLET"
    const val EARPHONES = "EARPHONES"
    const val BICYCLE = "BICYCLE"
    const val GAME = "GAME"
    const val GAME_DEVICE = "GAME_DEVICE"
    const val SMART_WATCH = "SMART_WATCH"
    const val WATCH = "WATCH"
    const val OTHER = "OTHER"

    val values = setOf(
        PHONE,
        LAPTOP,
        TABLET,
        EARPHONES,
        BICYCLE,
        GAME,
        GAME_DEVICE,
        SMART_WATCH,
        WATCH,
        OTHER
    )

    fun fromDrawable(picture: Int): String = when (picture) {
        R.drawable.ic_iphone -> PHONE
        R.drawable.laptop -> LAPTOP
        R.drawable.ic_tablet_pc -> TABLET
        R.drawable.ic_earphone -> EARPHONES
        R.drawable.ic_bicycle -> BICYCLE
        R.drawable.ic_game -> GAME
        R.drawable.ic_game_computer -> GAME_DEVICE
        R.drawable.smart_watch -> SMART_WATCH
        R.drawable.watch -> WATCH
        else -> OTHER
    }

    fun toDrawable(category: String): Int = when (category) {
        PHONE -> R.drawable.ic_iphone
        LAPTOP -> R.drawable.laptop
        TABLET -> R.drawable.ic_tablet_pc
        EARPHONES -> R.drawable.ic_earphone
        BICYCLE -> R.drawable.ic_bicycle
        GAME -> R.drawable.ic_game
        GAME_DEVICE -> R.drawable.ic_game_computer
        SMART_WATCH -> R.drawable.smart_watch
        WATCH -> R.drawable.watch
        OTHER -> R.drawable.nfeature
        else -> throw IllegalArgumentException("LEDGER_CATEGORY_INVALID")
    }
}
