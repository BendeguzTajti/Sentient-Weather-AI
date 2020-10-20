package com.codecool.swai.model

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.*
import io.reactivex.Single
import java.io.IOException
import java.net.ConnectException

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

    override fun getCoordinatesBySpeech(speechInput: String): Single<Location> {
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
                        emitter.onSuccess(Location(location.latitude, location.longitude, address))
                    }
                }
            } catch (e: IOException) {
                emitter.onError(ConnectException("Internet connection lost"))
            }
            if (!emitter.isDisposed) emitter.onError(NullPointerException("No location found."))
        }
    }

    @SuppressLint("MissingPermission")
    override fun getUserLocation(): Single<Location> {
        return Single.create { emitter ->
            fusedLocationProvider.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    locationResult?.let {
                        val lastLocation = it.lastLocation
                        emitter.onSuccess(Location(lastLocation.latitude, lastLocation.longitude))
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

    override fun getUserCityAndCountryCode(location: Location): Single<Location> {
        return Single.create { emitter ->
            try {
                geoCoder.getFromLocation(location.latitude, location.longitude, 1).firstOrNull()?.let {
                    val cityName: String? = arrayOf(it.adminArea ?: it.locality, it.subLocality
                            ?: it.locality ?: it.subAdminArea)
                            .distinct()
                            .filterNotNull()
                            .joinToString(", ")
                    location.cityName = cityName ?: ""
                    location.countryCode = it.locale.country
                    emitter.onSuccess(location)
                }
            } catch (e: IOException) {
                emitter.onError(ConnectException("Internet connection lost."))
            }
        }
    }
}