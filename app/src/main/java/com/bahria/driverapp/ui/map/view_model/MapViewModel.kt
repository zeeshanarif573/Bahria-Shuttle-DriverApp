package com.bahria.driverapp.ui.map.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahria.driverapp.ui.login.model.LoginResponse
import com.bahria.driverapp.ui.login.repository.LoginRepository
import com.bahria.driverapp.ui.map.repository.MapRepository
import com.bahria.driverapp.utils.ResponseHandling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapViewModel(private val repository: MapRepository) : ViewModel() {

    fun journeyEnd() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.doLogin(
                empID!!,
                cnic!!
            )
        }
    }

    val loginResponse: MutableLiveData<ResponseHandling<LoginResponse>>
        get() = repository.loginResponse

    fun flushVariables() {
        loginResponse.value = null
    }

}