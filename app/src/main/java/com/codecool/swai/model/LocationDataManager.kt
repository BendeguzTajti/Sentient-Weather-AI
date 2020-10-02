package com.codecool.swai.model

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.*
import io.reactivex.Single
import java.io.IOException

class LocationDataManager(
        private val geoCoder: Geocoder,
        private val fusedLocationProvider: FusedLocationProviderClient): LocationManager {

    companion object {
        private const val LOCATION_REQUEST_INTERVAL = 10000L
        private const val LOCATION_REQUEST_FASTEST_INTERVAL = 5000L
    }

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = LOCATION_REQUEST_INTERVAL
        fastestInterval = LOCATION_REQUEST_FASTEST_INTERVAL
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun getCoordinatesBySpeech(speechInput: String): Single<List<Any>> {
        return Single.create { emitter ->
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
                        emitter.onSuccess(listOf(address, location.latitude, location.longitude))
                    }
                }
            } catch (e: IOException) {
                emitter.onError(Throwable("An error occurred in getUserCityAndCountryCode: $e"))
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun getUserLocation(): Single<Array<Double>> {
        return Single.create { emitter ->
            fusedLocationProvider.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    locationResult?.let {
                        val lastLocation = it.lastLocation
                        emitter.onSuccess(arrayOf(lastLocation.latitude, lastLocation.longitude))
                    } ?: emitter.onError(Throwable("Couldn't find locations. Location sharing might be disabled."))
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                    super.onLocationAvailability(locationAvailability)
                    if (locationAvailability?.isLocationAvailable == false) {
                        emitter.onError(Throwable("Location sharing is disabled."))
                    }
                }
            }, Looper.getMainLooper())
        }
    }

    override fun getUserCityAndCountryCode(latitude: Double, longitude: Double): Single<List<Any>> {
        return Single.create { emitter ->
            try {
                geoCoder.getFromLocation(latitude, longitude, 1).firstOrNull()?.let {
                    val cityName: String? = arrayOf(it.adminArea ?: it.locality, it.subLocality
                            ?: it.locality ?: it.subAdminArea)
                            .distinct()
                            .filterNotNull()
                            .joinToString(", ")
                    emitter.onSuccess(listOf(latitude, longitude, cityName ?: "", it.locale.country))
                }
            } catch (e: Exception) {
                emitter.onError(Throwable("An error occurred in getUserCityAndCountryCode: $e"))
            }
        }
    }
}