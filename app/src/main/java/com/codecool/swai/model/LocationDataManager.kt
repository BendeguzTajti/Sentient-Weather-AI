package com.codecool.swai.model

import android.annotation.SuppressLint
import android.location.Geocoder
import com.google.android.gms.location.*
import io.reactivex.Single
import java.io.IOException

class LocationDataManager(
        private val geoCoder: Geocoder,
        private val fusedLocationProvider: FusedLocationProviderClient): LocationManager {

    override fun getCoordinatesBySpeech(speechInput: String): List<Any> {
        try {
            val addresses = geoCoder.getFromLocationName(speechInput, 1)
            if (addresses.isNotEmpty()) {
                val location = addresses.first()
                if (location.hasLatitude() && location.hasLongitude()) {
                    var address = arrayOf(location.adminArea ?: location.locality, location.subLocality ?: location.locality ?: location.subAdminArea)
                            .distinct()
                            .filterNotNull()
                            .joinToString(", ")
                    if (address.isEmpty()) address = speechInput
                    return listOf(address, location.latitude, location.longitude)
                }
            }
        } catch (e: IOException) {
            return emptyList()
        }
        return emptyList()
    }

    @SuppressLint("MissingPermission")
    override fun getUserLocation(): Single<Array<Double>> {
        return Single.create { emitter ->
            fusedLocationProvider.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    // add geoCoder code here if you want to access the user's country as well.
                    emitter.onSuccess(arrayOf(latitude, longitude))
                } ?: emitter.onError(Throwable("Couldn't find last location on this device. Your location settings might be turned off"))
            }
        }
    }
}