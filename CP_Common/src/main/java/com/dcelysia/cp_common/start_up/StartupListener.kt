package com.dcelysia.cp_common.start_up

import com.dcelysia.cp_common.start_up.model.CostTimesModel

interface StartupListener {

    /**
     * call when all startup completed.
     * @param totalMainThreadCostTime cost times of main thread.
     * @param costTimesModels list of cost times for every startup.
     */
    fun onCompleted(totalMainThreadCostTime: Long, costTimesModels: List<CostTimesModel>)
}