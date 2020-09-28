package com.codecool.swai.model

import android.content.SharedPreferences
import com.codecool.swai.api.WeatherApiService
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.*

class DataManager(private val sharedPreferences: SharedPreferences) : WeatherManager {

    companion object {
        private const val API_KEY = "24b031540acbc5b0a68d9c8f4692734d"
        private const val DATA_TO_EXCLUDE = "current,minutely,hourly"
    }

    private val weatherApiService by lazy {
        WeatherApiService.create()
    }

    override fun getWeatherDataByCoordinates(latitude: Double, longitude: Double): Single<Weather> {
        val currentWeather = weatherApiService.getCurrentWeatherByLocation(latitude, longitude, API_KEY, Locale.getDefault().language)
        val forecastWeather = weatherApiService.getWeatherForecast(latitude, longitude, DATA_TO_EXCLUDE, API_KEY, Locale.getDefault().language)
        return Single.zip(currentWeather, forecastWeather, BiFunction { weather, forecast ->
            Weather(weather, forecast)
        })
    }

    override fun addTempUnit() {
        val unit = sharedPreferences.getString("unit", null)
        if (unit == null) {
            val metricCountries = listOf("US", "BS", "KY", "LR", "PW", "MH", "FM")
            if (metricCountries.contains(Locale.getDefault().country)) {
                sharedPreferences.edit().putString("unit", "Fahrenheit").apply()
            } else {
                sharedPreferences.edit().putString("unit", "Celsius").apply()
            }
        }
    }

    override fun saveTempUnit(unit: String) {
        sharedPreferences.edit().putString("unit", unit).apply()
    }
}