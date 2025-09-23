package com.dcelysia.cp_common.start_up.annotation

import android.os.Process

@MustBeDocumented
@Retention
@Target(AnnotationTarget.CLASS)
annotation class ThreadPriority(val priority: Int = Process.THREAD_PRIORITY_DEFAULT)