package com.codecool.swai.model

import io.reactivex.Single

interface LocationManager {
    fun getCoordinatesBySpeech(speechInput: String): Single<Location>
    fun getUserLocation(): Single<Location>
    fun getUserCityAndCountryCode(location: Location): Single<Location>
}