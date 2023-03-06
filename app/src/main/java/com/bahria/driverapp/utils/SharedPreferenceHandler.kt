package com.bahria.driverapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.bahria.driverapp.ui.login.model.LoginData
import com.google.gson.Gson

class SharedPreferenceHandler(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("driverApp", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPref.edit()

    fun setUpdatingPushKey(value: String?) {
        editor.putString("pushKey", value!!)
        editor.commit()
    }

    fun getUpdatingPushKey(): String? {
        return sharedPref.getString("pushKey", "")
    }

    fun setBusNo(value: String?) {
        editor.putString("busNo", value!!)
        editor.commit()
    }

    fun getBusNo(): String? {
        return sharedPref.getString("busNo", "")
    }

    fun setUser(value: LoginData) {
        val gson = Gson()
        val json = gson.toJson(value)
        editor.putString("user", json)
        editor.commit()
    }

    fun getUser(): LoginData? {
        val gson = Gson()
        val json = sharedPref.getString("user", "")
        return gson.fromJson(json, LoginData::class.java)
    }

}
