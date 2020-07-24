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

    fun getWeatherDataByCity(cityName: String): Single<Weather> {
        val currentWeather = weatherApiService.getCurrentWeatherByCity(cityName, "metric", apiKey, Locale.getDefault().language)
        val forecastWeather = weatherApiService.getWeatherForecastByCity(cityName, "metric", apiKey, Locale.getDefault().language)
        return Single.zip(currentWeather, forecastWeather, BiFunction { weather, forecast ->
            Weather(weather, forecast)
        })
    }

    fun getWeatherDataByCoordinates(latitude: Double, longitude: Double): Single<Weather> {
        val currentWeather = weatherApiService.getCurrentWeatherByCoordinates(latitude, longitude, "metric", apiKey, Locale.getDefault().language)
        val forecastWeather = weatherApiService.getWeatherForecastByCoordinates(latitude, longitude, "metric", apiKey, Locale.getDefault().language)
        return Single.zip(currentWeather, forecastWeather, BiFunction { weather, forecast ->
            Weather(weather, forecast)
        })
    }
}