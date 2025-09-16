package com.dcelysia.cp_common.start_up.model

import com.dcelysia.cp_common.start_up.StartupListener

class StartupConfig private constructor(
    val loggerLevel: LoggerLevel,
    val awaitTimeout: Long,
    val listener: StartupListener?,
    val openStatistic: Boolean? = true
) {

    class Builder {
        private var mLoggerLevel: LoggerLevel? = null
        private var mAwaitTimeout: Long? = null
        private var mListener: StartupListener? = null
        private var mOpenStatistics: Boolean? = true

        companion object {
            const val AWAIT_TIMEOUT = 10000L
        }

        fun setLoggerLevel(level: LoggerLevel) = apply {
            mLoggerLevel = level
        }

        fun setAwaitTimeout(timeoutMilliSeconds: Long) = apply {
            mAwaitTimeout = timeoutMilliSeconds
        }

        fun setListener(listener: StartupListener) = apply {
            mListener = listener
        }

        fun setOpenStatistics(openStatistic: Boolean) = apply {
            mOpenStatistics = openStatistic
        }

        fun build(): StartupConfig {
            return StartupConfig(
                mLoggerLevel ?: LoggerLevel.NONE,
                mAwaitTimeout ?: AWAIT_TIMEOUT,
                mListener,
                mOpenStatistics
            )
        }
    }

}