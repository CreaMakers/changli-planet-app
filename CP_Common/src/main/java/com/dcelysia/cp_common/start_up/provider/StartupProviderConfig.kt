package com.dcelysia.cp_common.start_up.provider

import com.dcelysia.cp_common.start_up.model.StartupConfig

interface StartupProviderConfig {

    fun getConfig(): StartupConfig
}