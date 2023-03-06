package com.bahria.driverapp.ui.driver_history.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bahria.driverapp.R
import com.bahria.driverapp.databinding.ActivityDriverHistoryBinding
import com.bahria.driverapp.ui.driver_history.model.DriverHistoryData
import com.bahria.driverapp.utils.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DriverHistoryActivity : AppCompatActivity() {

    lateinit var binding: ActivityDriverHistoryBinding
    private lateinit var driverHistoryList: MutableList<DriverHistoryData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver_history)
        getDriverHistory()
    }

    private fun getDriverHistory() {
        val gson = Gson()
        val driverInfoData = object : TypeToken<List<DriverHistoryData>>() {}.type
        driverHistoryList = gson.fromJson(
            Util.getJsonDataFromAsset(this, "driver_history.json"),
            driverInfoData
        )

//        routesAdapter = RoutesAdapter(this, driverHistoryList, this)
//        val layoutManager = LinearLayoutManager(this)
//        binding.driverHistoryView.layoutManager = layoutManager
//        binding.driverHistoryView.addItemDecoration(
//            DividerItemDecoration(
//                this,
//                layoutManager.orientation
//            )
//        )
//        binding.driverHistoryView.adapter = routesAdapter
    }
}