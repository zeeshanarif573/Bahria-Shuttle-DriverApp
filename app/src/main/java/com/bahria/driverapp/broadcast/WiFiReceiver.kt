package com.bahria.driverapp.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import com.bahria.driverapp.listeners.WifiListener

class WiFiReceiver(private var wifiListener: WifiListener) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            val wifiManager =
                context?.applicationContext?.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo.networkId != -1)
                wifiListener.onWifiFound()
        }
    }
}