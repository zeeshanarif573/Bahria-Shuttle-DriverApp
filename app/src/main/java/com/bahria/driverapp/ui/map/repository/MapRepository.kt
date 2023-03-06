package com.bahria.driverapp.ui.map.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.bahria.driverapp.R
import com.bahria.driverapp.config.API
import com.bahria.driverapp.network.NetworkUtils
import com.bahria.driverapp.ui.login.model.LoginResponse
import com.bahria.driverapp.utils.ResponseHandling
import com.google.gson.JsonParser

class MapRepository(
    private val api: API,
    private val applicationContext: Context
) {
    private val journeyEndLiveData = MutableLiveData<ResponseHandling<LoginResponse>>()
    val journeyEndResponse: MutableLiveData<ResponseHandling<LoginResponse>>
        get() = journeyEndLiveData

    suspend fun journeyEnd(
        empID: String,
    ) {
        if (NetworkUtils.isInternetAvailable()) {
            try {
                val result = api.journeyEnd("N", empID)
                if (result.isSuccessful)
                    journeyEndLiveData.postValue(ResponseHandling.Success(result.body()))
                else {
                    val errorJsonString = result.errorBody()?.toString()
                    val message = JsonParser().parse(errorJsonString)
                        .asJsonObject["detail"]
                        .asString
                    displayErrorMsg(message)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                displayErrorMsg(e.message!!)
            }

        } else
            displayErrorMsg(applicationContext.getString(R.string.no_internet))
    }

    private fun displayErrorMsg(msg: String) {
        journeyEndLiveData.postValue(
            ResponseHandling.Error(msg)
        )
    }

}
