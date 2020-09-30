package com.codecool.swai.model

import io.reactivex.Single

interface WeatherManager {
    fun getWeatherDataByCoordinates(latitude: Double, longitude: Double): Single<Weather>
    fun addLatestWeatherData(weather: Weather)
    fun getLatestWeatherData(): Weather?
    fun addTempUnit()
    fun saveTempUnit(unit: String)
}