package com.dcelysia.cp_common.start_up.annotation

@MustBeDocumented
@Retention
@Target(AnnotationTarget.CLASS)
annotation class MultipleProcess(vararg val process: String)