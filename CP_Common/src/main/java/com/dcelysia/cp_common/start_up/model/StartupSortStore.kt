package com.dcelysia.cp_common.start_up.model

import com.dcelysia.cp_common.start_up.Startup

data class StartupSortStore(
    val result: MutableList<Startup<*>>,
    val startupMap: Map<String, Startup<*>>,
    val startupChildrenMap: Map<String, MutableList<String>>
)