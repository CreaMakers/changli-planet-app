package com.dcelysia.cp_common.start_up.model

import com.dcelysia.cp_common.start_up.AndroidStartup
import com.dcelysia.cp_common.start_up.provider.StartupProviderConfig

data class StartupProviderStore(
    val result: List<AndroidStartup<*>>,
    val config: StartupProviderConfig?
)