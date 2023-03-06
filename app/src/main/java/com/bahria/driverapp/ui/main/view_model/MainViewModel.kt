package com.bahria.driverapp.ui.main.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahria.driverapp.ui.main.model.GeneralResponse
import com.bahria.driverapp.ui.main.model.buses.BusesResponse
import com.bahria.driverapp.ui.main.model.routes.RoutesResponse
import com.bahria.driverapp.ui.main.repository.MainRepository
import com.bahria.driverapp.utils.ResponseHandling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getRoutes()
            repository.getBuses("N")
        }
    }

    fun postDriverInformation(empID: String, regID: String, routeID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.postDriverInformation(
                empID,
                regID,
                routeID
            )
        }
    }

    val routesResponse: MutableLiveData<ResponseHandling<RoutesResponse>>
        get() = repository.routesResponse

    val busesResponse: MutableLiveData<ResponseHandling<BusesResponse>>
        get() = repository.busesResponse

    val postDriverInformationResponse: MutableLiveData<ResponseHandling<GeneralResponse>>
        get() = repository.postDriverInformationResponse

    fun flushVariables() {
        postDriverInformationResponse.value = null
    }

    fun flushPostDriverVariable() {
        postDriverInformationResponse.value = null
    }

}