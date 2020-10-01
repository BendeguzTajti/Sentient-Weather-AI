package com.codecool.swai.model

import io.reactivex.Single

interface LocationManager {
    fun getCoordinatesBySpeech(speechInput: String): List<Any>
    fun getUserLocation(): Single<Array<Double>>
}