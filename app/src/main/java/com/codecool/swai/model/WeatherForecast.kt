package com.codecool.swai.model

import com.codecool.swai.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class WeatherForecast {
    data class Result(val daily: List<Forecast>)
    data class Forecast(val dt: Long, val temp: Temperature, val weather: List<Weather>) {

        @ExperimentalStdlibApi
        fun getDay(): String {
            val sdf = SimpleDateFormat("EEE", Locale.getDefault())
            val date = Date(dt * 1000)
            return sdf.format(date).capitalize(Locale.getDefault())
        }

    }
    data class Temperature(val min: Double, val max: Double) {

        fun getMinMaxTempCelsius(): String {
            val maxCelsius = (max - 273.15).roundToInt().toString() + "°"
            val minCelsius = (min - 273.15).roundToInt().toString() + "°"
            return "$maxCelsius / $minCelsius"
        }

        fun getMinMaxTempFahrenheit(): String {
            val maxFahrenheit = (((max - 273.15) * 9 / 5) + 32).roundToInt().toString() + "°"
            val minFahrenheit = (((min - 273.15) * 9 / 5) + 32).roundToInt().toString() + "°"
            return "$maxFahrenheit / $minFahrenheit"
        }

    }
    data class Weather(val main: String) {

        fun getWeatherIcon(): Int {
            return when(main) {
                "Thunderstorm" -> R.raw.thunder
                "Drizzle" -> R.raw.drizzle
                "Rain" -> R.raw.rainy_day
                "Snow" -> R.raw.snow_day
                "Clear" -> R.raw.clear_day
                "Clouds" -> R.raw.cloudy_day
                else -> R.raw.atmosphere
            }
        }

        fun getWeatherType(): Int {
            return when(main) {
                "Thunderstorm" -> R.string.type_thunder
                "Drizzle" -> R.string.type_drizzle
                "Rain" -> R.string.type_rain
                "Snow" -> R.string.type_snow
                "Clear" -> R.string.type_clear
                "Clouds" -> R.string.type_clouds
                else -> R.string.type_mist
            }
        }

    }

}