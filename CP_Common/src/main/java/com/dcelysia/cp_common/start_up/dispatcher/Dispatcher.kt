package com.dcelysia.cp_common.start_up.dispatcher

interface Dispatcher {

    /**
     * Return true call the create function on main thread otherwise false.
     */
    fun callCreateOnMainThread(): Boolean

    /**
     * Return true block the main thread until the startup completed otherwise false.
     *
     * Note: If the function [callCreateOnMainThread] return true, main thread default block.
     */
    fun waitOnMainThread(): Boolean

    /**
     * To wait dependencies startup completed.
     */
    fun toWait()

    /**
     * To notify the startup when dependencies startup completed.
     */
    fun toNotify()
}