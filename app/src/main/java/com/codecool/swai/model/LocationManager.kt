package com.codecool.swai.model

interface LocationManager {
    fun getCoordinatesBySpeech(speechInput: String): List<Any>
}