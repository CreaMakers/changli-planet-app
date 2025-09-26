package com.dcelysia.cp_common.start_up.executor

import java.util.concurrent.Executor

interface StartupExecutor {

    fun createExecutor(): Executor
}