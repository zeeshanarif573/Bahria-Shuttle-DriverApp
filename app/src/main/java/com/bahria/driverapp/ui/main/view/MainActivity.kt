package com.bahria.driverapp.ui.main.view

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bahria.driverapp.R
import com.bahria.driverapp.databinding.ActivityMainBinding
import com.bahria.driverapp.model.DriverInfo
import com.bahria.driverapp.network.NetworkUtils
import com.bahria.driverapp.ui.map.MapActivity
import com.bahria.driverapp.ui.main.model.buses.BusesData
import com.bahria.driverapp.ui.main.model.routes.RoutesData
import com.bahria.driverapp.ui.main.view_model.MainViewModel
import com.bahria.driverapp.ui.main.view_model.MainViewModelFactory
import com.bahria.driverapp.utils.DriverAppApplication
import com.bahria.driverapp.utils.ResponseHandling
import com.bahria.driverapp.utils.SharedPreferenceHandler
import com.bahria.driverapp.utils.Util
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private var routeList: MutableList<RoutesData> = ArrayList()
    private var busesList: MutableList<BusesData> = ArrayList()
    lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var databaseReference: DatabaseReference
    private var driverInfo = "Driver-Info"
    private var allBuses = "All-Buses"
    lateinit var progressDialog: ProgressDialog
    private var prefs: SharedPreferenceHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val repository = (application as DriverAppApplication).mainRepository
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(repository))[MainViewModel::class.java]
        binding.mainViewModel = mainViewModel
        initialization()
        routesObserver()
        busesObserver()

        binding.startRide.setOnClickListener {
            if (binding.route.selectedItem.toString() == "Select Route" || binding.busNo.selectedItem.toString() == "Select Bus-No")
                Snackbar.make(
                    binding.mainContainer,
                    "Please Select Route and Bus No",
                    Snackbar.LENGTH_SHORT
                ).show()
            else
                updateFirebaseDatabase()
        }

        binding.busNo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val bus = busesList[position]
                prefs?.setBusNo(bus.REG_NO)
            }
        }
    }

    private fun initialization() {
        prefs = SharedPreferenceHandler(applicationContext)
        progressDialog = Util.initProgressDialog(this)
        progressDialog.show()
        binding.topLayout.logout.visibility = VISIBLE
//        Util.initializeDropdown(this, routeList, binding.route)
//        Util.initializeDropdown(this, busNoList, binding.busNo)
    }

    private fun routesObserver() {
        mainViewModel.routesResponse.observe(this) {
            when (it) {
                is ResponseHandling.Success -> {
                    if (it.data?.Success == "true") {
                        routeList.clear()
                        routeList.add(RoutesData("", "", "N", 0, "Select Route"))
                        routeList.addAll(it.data.Result)
                        val arrayAdapter =
                            ArrayAdapter(
                                this@MainActivity,
                                R.layout.spinner_item,
                                routeList
                            )

                        binding.route.adapter = arrayAdapter

                    } else {
                        progressDialog.dismiss()
                        mainViewModel.flushVariables()
                        Util.showSnackBar(
                            binding.mainContainer, getString(R.string.went_wrong)
                        )
                    }
                }

                is ResponseHandling.Error -> {
                    progressDialog.dismiss()
                    mainViewModel.flushVariables()
                    Util.showSnackBar(binding.mainContainer, it.errorMessage!!)
                }
            }
        }
    }

    private fun busesObserver() {
        mainViewModel.busesResponse.observe(this) {
            when (it) {
                is ResponseHandling.Success -> {
                    if (it.data?.Success == "true") {
                        busesList.clear()
                        busesList.add(BusesData("", "", "N", 0.0, "Select Bus-No"))
                        busesList.addAll(it.data.Result)
                        val arrayAdapter =
                            ArrayAdapter(
                                this@MainActivity,
                                R.layout.spinner_item,
                                busesList
                            )

                        binding.busNo.adapter = arrayAdapter

                    } else {
                        mainViewModel.flushVariables()
                        Util.showSnackBar(
                            binding.mainContainer, getString(R.string.went_wrong)
                        )
                    }
                    progressDialog.dismiss()
                }

                is ResponseHandling.Error -> {
                    progressDialog.dismiss()
                    mainViewModel.flushVariables()
                    Util.showSnackBar(binding.mainContainer, it.errorMessage!!)
                }
            }
        }
    }

    private fun postDriverInfoObserver() {
        mainViewModel.postDriverInformationResponse.observe(this) {
            when (it) {
                is ResponseHandling.Success -> {
                    if (it.data?.Success == "Route Assign Succesfully") {
                        val intent = Intent(this, MapActivity::class.java)
                        intent.putExtra("busNo", binding.busNo.selectedItem.toString())
                        startActivity(intent)

                    } else {
                        mainViewModel.flushPostDriverVariable()
                        Util.showSnackBar(
                            binding.mainContainer, getString(R.string.went_wrong)
                        )
                    }
                    progressDialog.dismiss()
                }

                is ResponseHandling.Error -> {
                    progressDialog.dismiss()
                    mainViewModel.flushPostDriverVariable()
                    Util.showSnackBar(binding.mainContainer, it.errorMessage!!)
                }
            }
        }
    }

    private fun updateFirebaseDatabase() {
        if (NetworkUtils.isInternetAvailable()) {
            progressDialog.show()
            databaseReference = FirebaseDatabase.getInstance().reference
            val query: Query = databaseReference.child(driverInfo).orderByChild("cnic")
                .equalTo(prefs?.getUser()?.DRIVER_CNIC)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (item in snapshot.children) {
                            val key = item.child("id").value.toString()
                            updateDriverRecordIntoFirebase(key)
                        }
                    } else
                        insertDriverRecordIntoFirebase()
                }

                override fun onCancelled(error: DatabaseError) {
                    progressDialog.dismiss()
                    Log.e("error", error.message)
                }
            })
        } else
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
    }

    private fun returnCompleteDriverRecord(key: String): DriverInfo {
        return DriverInfo(
            key,
            prefs?.getUser()?.DRIVER_NAME!!,
            prefs?.getUser()?.DRIVER_CNIC!!,
            prefs?.getUser()?.DRIVER_CELLNO!!,
            binding.route.selectedItem.toString(),
            binding.busNo.selectedItem.toString(),
            "0.0",
            "0.0"
        )
    }

    private fun insertDriverRecordIntoFirebase() {
        if (NetworkUtils.isInternetAvailable()) {
            val key = databaseReference.child(driverInfo).push().key
            databaseReference.child(driverInfo).child(key!!)
                .setValue(returnCompleteDriverRecord(key))
            databaseReference.child(allBuses).child(key).setValue(returnCompleteDriverRecord(key))
            progressDialog.dismiss()
            startActivity(
                Intent(
                    this,
                    MapActivity::class.java
                )
            )
        } else
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
    }

    private fun updateDriverRecordIntoFirebase(key: String) {
        if (NetworkUtils.isInternetAvailable()) {
            databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference.child(driverInfo).child(key).setValue(returnCompleteDriverRecord(key))
            databaseReference.child(allBuses).child(key).setValue(returnCompleteDriverRecord(key))

            mainViewModel.postDriverInformation(
                prefs?.getUser()?.DRIVER_EMPID!!,
                binding.busNo.selectedItem.toString(),
                binding.route.selectedItem.toString()
            )
            postDriverInfoObserver()

        } else
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
    }
}