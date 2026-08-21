package com.zhelearn.CSUSTPlanet.core.network.listener

import okhttp3.Response

interface RequestCallback {
    fun onSuccess(response: Response)
    fun onFailure(error: String)
}
