package com.codecool.swai.model

import android.location.Geocoder
import java.io.IOException

class LocationDataManager(private val geoCoder: Geocoder): LocationManager {

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
}