package com.codecool.swai.model

import io.reactivex.Single

interface LocationManager {
    fun getCoordinatesBySpeech(speechInput: String): Single<List<Any>>
    fun getUserLocation(): Single<Array<Double>>
    fun getUserCityAndCountryCode(latitude: Double, longitude: Double): Single<List<Any>>
}