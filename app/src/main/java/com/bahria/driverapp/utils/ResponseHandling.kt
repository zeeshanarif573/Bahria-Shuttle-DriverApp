package com.bahria.driverapp.utils

sealed class ResponseHandling<T>(val data: T? = null, val errorMessage: String? = null) {
    class Success<T>(data: T? = null) : ResponseHandling<T>(data = data)
    class Error<T>(errorMessage: String?) : ResponseHandling<T>(errorMessage = errorMessage)
}