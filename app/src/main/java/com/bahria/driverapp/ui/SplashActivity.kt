package com.bahria.driverapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.bahria.driverapp.BuildConfig
import com.bahria.driverapp.R
import com.bahria.driverapp.databinding.ActivityMapBinding
import com.bahria.driverapp.databinding.ActivitySplashBinding
import com.bahria.driverapp.ui.login.view.LoginActivity
import java.text.SimpleDateFormat
import java.util.*

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        binding.version.text = "Version " + BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(
                Intent(
                    this@SplashActivity,
                    LoginActivity::class.java
                )
            )
            finish()
        }, 3000)
    }
}