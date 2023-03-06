package com.bahria.driverapp.ui.main.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.bahria.driverapp.R
import com.bahria.driverapp.config.API
import com.bahria.driverapp.network.NetworkUtils
import com.bahria.driverapp.ui.main.model.GeneralResponse
import com.bahria.driverapp.ui.main.model.buses.BusesResponse
import com.bahria.driverapp.ui.main.model.routes.RoutesResponse
import com.bahria.driverapp.utils.ResponseHandling
import com.google.gson.JsonParser

class MainRepository(
    private val api: API,
    private val applicationContext: Context
) {
    //Routes LiveData.............
    private val routesLiveData = MutableLiveData<ResponseHandling<RoutesResponse>>()
    val routesResponse: MutableLiveData<ResponseHandling<RoutesResponse>>
        get() = routesLiveData

    //Buses LiveData.............
    private val busesLiveData = MutableLiveData<ResponseHandling<BusesResponse>>()
    val busesResponse: MutableLiveData<ResponseHandling<BusesResponse>>
        get() = busesLiveData

    //Buses LiveData.............
    private val postDriverInformationLiveData = MutableLiveData<ResponseHandling<GeneralResponse>>()
    val postDriverInformationResponse: MutableLiveData<ResponseHandling<GeneralResponse>>
        get() = postDriverInformationLiveData

    suspend fun getRoutes() {
        if (NetworkUtils.isInternetAvailable()) {
            try {
                val result = api.getRoutes()
                if (result.isSuccessful)
                    routesLiveData.postValue(ResponseHandling.Success(result.body()))
                else {
                    val errorJsonString = result.errorBody()?.toString()
                    val message = JsonParser().parse(errorJsonString)
                        .asJsonObject["detail"]
                        .asString
                    routesLiveData.postValue(
                        ResponseHandling.Error(message)
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                routesLiveData.postValue(
                    ResponseHandling.Error(e.message)
                )
            }

        } else
            routesLiveData.postValue(
                ResponseHandling.Error(applicationContext.getString(R.string.no_internet))
            )
    }

    suspend fun getBuses(isActive: String) {
        if (NetworkUtils.isInternetAvailable()) {
            try {
                val result = api.getBuses(isActive)
                if (result.isSuccessful)
                    busesLiveData.postValue(ResponseHandling.Success(result.body()))
                else {
                    val errorJsonString = result.errorBody()?.toString()
                    val message = JsonParser().parse(errorJsonString)
                        .asJsonObject["detail"]
                        .asString
                    busesLiveData.postValue(
                        ResponseHandling.Error(message)
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                busesLiveData.postValue(
                    ResponseHandling.Error(e.message)
                )
            }

        } else
            busesLiveData.postValue(
                ResponseHandling.Error(applicationContext.getString(R.string.no_internet))
            )
    }

    suspend fun postDriverInformation(empID: String, regID: String, routeID: String) {
        if (NetworkUtils.isInternetAvailable()) {
            try {
                val result = api.postDriverInformation(empID, regID, routeID, "Y")
                if (result.isSuccessful)
                    postDriverInformationLiveData.postValue(ResponseHandling.Success(result.body()))
                else
                    postDriverInformationLiveData.postValue(
                        ResponseHandling.Error(applicationContext.getString(R.string.went_wrong))
                    )

            } catch (e: Exception) {
                e.printStackTrace()
                postDriverInformationLiveData.postValue(
                    ResponseHandling.Error(e.message)
                )
            }

        } else
            postDriverInformationLiveData.postValue(
                ResponseHandling.Error(applicationContext.getString(R.string.no_internet))
            )
    }

}
