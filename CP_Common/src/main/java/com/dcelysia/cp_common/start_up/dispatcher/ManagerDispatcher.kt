package com.dcelysia.cp_common.start_up.dispatcher

import com.dcelysia.cp_common.start_up.Startup
import com.dcelysia.cp_common.start_up.model.StartupSortStore

interface ManagerDispatcher {

    /**
     * dispatch prepare
     */
    fun prepare()

    /**
     * dispatch startup to executing.
     */
    fun dispatch(startup: Startup<*>, sortStore: StartupSortStore)

    /**
     * notify children when dependency startup completed.
     */
    fun notifyChildren(dependencyParent: Startup<*>, result: Any?, sortStore: StartupSortStore)
}