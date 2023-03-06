package com.bahria.driverapp.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.bahria.driverapp.utils.DriverAppApplication

class NetworkUtils {

    companion object {
        fun isInternetAvailable(): Boolean {
            (DriverAppApplication.getCtx()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
                return this.getNetworkCapabilities(this.activeNetwork)?.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) ?: false
            }
        }
    }
}