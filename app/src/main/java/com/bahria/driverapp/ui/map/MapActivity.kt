package com.bahria.driverapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bahria.driverapp.R
import com.bahria.driverapp.broadcast.WiFiReceiver
import com.bahria.driverapp.listeners.WifiListener
import com.bahria.driverapp.network.NetworkUtils
import com.bahria.driverapp.service.LocationService
import com.bahria.driverapp.utils.SharedPreferenceHandler
import com.bahria.driverapp.utils.Util
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

open class MapActivity : AppCompatActivity(), OnMapReadyCallback, WifiListener {

    lateinit var binding: ActivityMapBinding
    private var isOffline = true
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var mLocationService: LocationService = LocationService()
    private lateinit var mServiceIntent: Intent
    private lateinit var wifiReceiver: BroadcastReceiver
    lateinit var progressDialog: ProgressDialog
    private var prefs: SharedPreferenceHandler? = null
    private val sdf = SimpleDateFormat("dd MMM,yyyy HH:mm:ss a")
    private var latitude = 0.0
    private var longitude = 0.0
    private var driverInfo = "Driver-Info"
    private var allBuses = "All-Buses"

    private val getLocationResult: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK)
                if (NetworkUtils.isInternetAvailable())
                    getCurrentLatLng()
                else
                    Toast.makeText(
                        this@MapActivity,
                        getString(R.string.no_internet),
                        Toast.LENGTH_LONG
                    ).show()
        }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var fusedLocationClient: FusedLocationProviderClient
        lateinit var map: GoogleMap
        lateinit var marker: Marker
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 3000
        }
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (NetworkUtils.isInternetAvailable()) {
                val locationList = locationResult.locations
                if (locationList.isNotEmpty()) {
                    val location = locationList.last()
                    latitude = location.latitude
                    longitude = location.longitude
                    getInitialLocationOnMap()
                }
            } else
                Toast.makeText(this@MapActivity, getString(R.string.no_internet), Toast.LENGTH_LONG)
                    .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        initialization()

        binding.topLayout.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.endUpdates.setOnClickListener {
            isOffline = true
            binding.endUpdates.visibility = GONE
            stopLocationService()
            finish()

//            if (isOffline) {
//                isOffline = false
//                checkAccessFineLocation()
//                setButtonDimensions(
//                    getString(R.string.end_trip), ResourcesCompat.getColor(
//                        resources,
//                        R.color.red,
//                        null
//                    )
//                )
//            } else {
//                isOffline = true
//                stopLocationService()
//                setButtonDimensions(
//                    getString(R.string.start_trip), ResourcesCompat.getColor(
//                        resources,
//                        R.color.green,
//                        null
//                    )
//                )
//            }
        }
    }

    private fun initialization() {
        prefs = SharedPreferenceHandler(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        progressDialog = Util.initProgressDialog(this)
        binding.topLayout.backBtn.visibility = VISIBLE
        binding.name.text = "Bus Driver: " + prefs?.getUser()?.DRIVER_NAME
        binding.busNo.text = "Bus No: " + intent.getStringExtra("busNo")
        binding.startingTime.text = sdf.format(Calendar.getInstance().time)

        wifiReceiver = WiFiReceiver(this)
        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        registerReceiver(wifiReceiver, filter)

        checkAccessFineLocation()
    }

    private fun startTravelTime() {
        binding.travelTime.setOnChronometerTickListener { chronometer ->
            val time: Long = SystemClock.elapsedRealtime() - chronometer.base
            val h = (time / 3600000).toInt()
            val m = (time - h * 3600000).toInt() / 60000
            val s = (time - h * 3600000 - m * 60000).toInt() / 1000
            val t =
                (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
            chronometer.text = t
        }

        binding.travelTime.base = SystemClock.elapsedRealtime()
        binding.travelTime.start()
    }

    private fun setButtonDimensions(text: String, color: Int) {
        binding.endUpdates.text = text
        binding.endUpdates.setBackgroundColor(color)
    }

    private fun checkAccessFineLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            )
                checkGPSLocation()
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    requestBackgroundLocationPermission()
                else
                    checkGPSLocation()
            }
        } else
            requestFineLocationPermission()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setTitle("Background permission")
            setMessage(R.string.background_location_permission_message)
            setPositiveButton(
                "Grant background Permission"
            ) { dialog, id ->
                ActivityCompat.requestPermissions(
                    this@MapActivity,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 101
                )
            }
        }.create().show()
    }

    private fun requestFineLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            100
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            requestBackgroundLocationPermission()
                        } else
                            checkGPSLocation()
                    }

                } else
                    requestFineLocationPermission()

                return
            }

            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkGPSLocation()
                } else
                    requestBackgroundLocationPermission()

                return
            }
        }
    }

    private fun checkGPSLocation() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                if (response!!.locationSettingsStates?.isGpsPresent!! && response.locationSettingsStates?.isGpsUsable!!)
                    if (NetworkUtils.isInternetAvailable())
                        getCurrentLatLng()
                    else
                        Toast.makeText(
                            this@MapActivity,
                            getString(R.string.no_internet),
                            Toast.LENGTH_LONG
                        ).show()

            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            if (exception is ResolvableApiException) {
                                val request: IntentSenderRequest = IntentSenderRequest.Builder(
                                    exception.resolution.intentSender
                                ).setFillInIntent(Intent())
                                    .build()
                                getLocationResult.launch(request)
                            }

                        } catch (e: IntentSender.SendIntentException) {
                            e.printStackTrace()
                        } catch (e: ClassCastException) {
                            e.printStackTrace()
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }

    private fun getInitialLocationOnMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun checkIfServiceAlreadyRunning(): Boolean {
        mLocationService = LocationService()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        return Util.isMyServiceRunning(mLocationService.javaClass, this)
    }

    private fun startLocationService() {
        if (NetworkUtils.isInternetAvailable()) {
            if (!checkIfServiceAlreadyRunning()) {
                startTravelTime()
                startService(mServiceIntent)
            }
        } else
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
    }

    private fun stopLocationService() {
        if (checkIfServiceAlreadyRunning())
            stopService(mServiceIntent)
        binding.travelTime.stop()
        driverOffline()
    }

    private fun driverOffline() {
        setResetLatLngValues(driverInfo, allBuses, "latitude")
        setResetLatLngValues(driverInfo, allBuses, "longitude")
    }

    private fun setResetLatLngValues(driverInfo: String, allBuses: String, child: String) {
        if (NetworkUtils.isInternetAvailable()) {
            database.reference.child(driverInfo).child(prefs?.getUpdatingPushKey()!!)
                .child(child)
                .setValue("0.0")

            database.reference.child(allBuses).child(prefs?.getUpdatingPushKey()!!)
                .child(child)
                .setValue("0.0")
        } else
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        try {
            if (isOffline) {
                super.onBackPressed()
                stopLocationService()
                unregisterReceiver(wifiReceiver)
            } else
                Util.showSnackBar(binding.mapContainer, getString(R.string.already_online))

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        stopLocationService()
        super.onDestroy()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.clear()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        progressDialog.dismiss()

        val latLng = LatLng(latitude, longitude)
        marker = map.addMarker(
            MarkerOptions().position(latLng)
                .icon(
                    Util.bitmapFromVector(
                        applicationContext,
                        R.drawable.ic_bus_round
                    )
                )
        )!!
        val update = CameraUpdateFactory.newLatLngZoom(latLng, 18.0f)
        map.animateCamera(update)
        map.isTrafficEnabled = true
        binding.endUpdates.visibility = VISIBLE
        isOffline = false

        if (!isOffline)
            startLocationService()
    }

    private fun getCurrentLatLng() {
        progressDialog.show()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestFineLocationPermission()
            return
        }

        if (NetworkUtils.isInternetAvailable()) {
            fusedLocationClient.lastLocation
                .addOnCompleteListener {
                    val location = it.result
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        getInitialLocationOnMap()

                    } else
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                }
        } else
            Toast.makeText(this@MapActivity, getString(R.string.no_internet), Toast.LENGTH_LONG)
                .show()
    }

    override fun onWifiFound() {
        getCurrentLatLng()
    }
}