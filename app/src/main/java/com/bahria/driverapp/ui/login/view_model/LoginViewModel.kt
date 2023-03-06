package com.bahria.driverapp.ui.login.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahria.driverapp.ui.login.model.LoginResponse
import com.bahria.driverapp.ui.login.repository.LoginRepository
import com.bahria.driverapp.utils.ResponseHandling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {

    var empID: String? = "12037"
    var cnic: String? = "4220143712889"

//    var email: String? = null
//    var password: String? = null

    fun onLoginButtonClick() {
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