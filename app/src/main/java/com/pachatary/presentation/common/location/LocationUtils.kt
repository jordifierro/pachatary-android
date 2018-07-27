package com.pachatary.presentation.common.location

import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import com.google.android.gms.location.LocationServices

class LocationUtils {

    companion object {
        fun checkLocationPermission(activity: Activity) =
            activity.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") ==
                    PackageManager.PERMISSION_GRANTED

        fun addListenerToLocation(activity: Activity, listener: (location: Location?) -> Unit) {
            if (LocationUtils.checkLocationPermission(activity)) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
                fusedLocationClient.lastLocation.addOnSuccessListener(listener)
            }
        }
    }
}