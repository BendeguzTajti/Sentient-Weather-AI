package com.codecool.swai.model

import com.codecool.swai.BuildConfig
import com.codecool.swai.api.WeatherApiService
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.*

class DataManager {

    private val apiKey = BuildConfig.API_KEY

    private val weatherApiService by lazy {
        WeatherApiService.create()
    }

    fun getWeatherDataByCoordinates(latitude: Double, longitude: Double): Single<Weather> {
        val currentWeather = weatherApiService.getCurrentWeather(latitude, longitude, apiKey, Locale.getDefault().language)
        val forecastWeather = weatherApiService.getWeatherForecast(latitude, longitude, "current,minutely,hourly", apiKey, Locale.getDefault().language)
        return Single.zip(currentWeather, forecastWeather, BiFunction { weather, forecast ->
            Weather(weather, forecast)
        })
    }
}