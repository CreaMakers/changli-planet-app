package com.dcelysia.cp_common.start_up.execption

internal class StartupException : RuntimeException {

    constructor(message: String?) : super(message)

    constructor(t: Throwable) : super(t)
}