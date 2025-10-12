package com.creamaker.changli_planet_app.core.task


import android.content.Context
import com.dcelysia.cp_common.start_up.AndroidStartup
import com.tencent.mmkv.MMKV

class MMKVStartup : AndroidStartup<Unit>() {

    override fun create(context: Context): Unit {
        MMKV.initialize(context)
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = true
}