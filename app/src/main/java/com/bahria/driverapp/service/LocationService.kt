package com.bahria.driverapp.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.bahria.driverapp.R
import com.bahria.driverapp.network.NetworkUtils
import com.bahria.driverapp.ui.map.MapActivity
import com.bahria.driverapp.utils.SharedPreferenceHandler
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*

class LocationService : Service() {

    private lateinit var driverInfoReference: DatabaseReference
    private lateinit var viewAllBusesReference: DatabaseReference
    var prefs: SharedPreferenceHandler? = null

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (NetworkUtils.isInternetAvailable()) {
                val locationList = locationResult.locations
                if (locationList.isNotEmpty()) {
                    val location = locationList.last()
                    val latLng = LatLng(
                        location.latitude,
                        location.longitude
                    )
                    MapActivity.marker.position = latLng
                    MapActivity.map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))

                    viewAllBusesReference =
                        FirebaseDatabase.getInstance().reference.child("All-Buses")
                    driverInfoReference =
                        FirebaseDatabase.getInstance().reference.child("Driver-Info")
                    driverInfoReference.addChildEventListener(object : ChildEventListener {
                        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                            if (dataSnapshot.child("busNo").value.toString() == prefs?.getBusNo()) {
                                val key = dataSnapshot.child("id").value.toString()
                                prefs?.setUpdatingPushKey(key)
                                val updates: MutableMap<String, Any> = HashMap()
                                updates["latitude"] = location.latitude.toString()
                                updates["longitude"] = location.longitude.toString()

                                driverInfoReference.child(key).updateChildren(updates)
                                viewAllBusesReference.child(key).updateChildren(updates)
                            }
                        }

                        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            } else
                Toast.makeText(
                    applicationContext,
                    getString(R.string.no_internet),
                    Toast.LENGTH_LONG
                )
                    .show()
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = SharedPreferenceHandler(this)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            createNotificationChanel()
        else
            startForeground(
                1,
                Notification()
            )

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(applicationContext, "Permission required", Toast.LENGTH_LONG).show()
            return
        } else {
            MapActivity.fusedLocationClient.requestLocationUpdates(
                MapActivity.locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val notificationChannelId = "Location channel id"
        val channelName = "Background Service"
        val notificationChannel = NotificationChannel(
            notificationChannelId,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(notificationChannel)

        val notificationBuilder =
            NotificationCompat.Builder(this, notificationChannelId)

        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("Driver App updates:")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        MapActivity.fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}