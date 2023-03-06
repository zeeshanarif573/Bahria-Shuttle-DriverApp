package com.bahria.driverapp.utils

import android.app.Application
import android.content.Context
import com.bahria.driverapp.config.API
import com.bahria.driverapp.config.RetrofitHelper
import com.bahria.driverapp.ui.login.repository.LoginRepository
import com.bahria.driverapp.ui.main.repository.MainRepository

class DriverAppApplication : Application() {

    lateinit var loginRepository: LoginRepository
    lateinit var mainRepository: MainRepository

    companion object {
        private lateinit var appContext: Context
        fun getCtx(): Context {
            return appContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext;
        init()
    }

    private fun init() {
        val apiService = RetrofitHelper.invoke().create(API::class.java)
//        val database = VisitorDatabase.getDatabase(applicationContext)

        loginRepository = LoginRepository(apiService, applicationContext)
        mainRepository = MainRepository(apiService, applicationContext)
    }
}