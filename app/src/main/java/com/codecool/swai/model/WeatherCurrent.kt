package com.codecool.swai.model

import com.codecool.swai.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class WeatherCurrent {

    data class Result(val weather: List<Weather>, val main: Temperature, val timezone: Long, val name: String, val cod: Int) {

        fun getCurrentHour(): Int {
            val timeZone = TimeZone.getTimeZone("UTC")
            timeZone.rawOffset = TimeUnit.SECONDS.toMillis(timezone).toInt()
            val dateFormat = SimpleDateFormat("HH", Locale.getDefault())
            dateFormat.timeZone = timeZone
            return dateFormat.format(Date()).toInt()
        }

        fun getLocationName(): String {
            return name.replace(" keruelet", " kerület", false)
        }
    }

    data class Weather(val main: String, val description: String) {

        fun getWeatherIcon(currentHour: Int): Int {
            return when(main) {
                "Thunderstorm" -> R.raw.thunder
                "Drizzle" -> R.raw.drizzle
                "Rain" -> if (currentHour in 6..17) R.raw.rainy_day else R.raw.rainy_night
                "Snow" -> if (currentHour in 6..17) R.raw.snow_day else R.raw.snow_night
                "Clear" -> if (currentHour in 6..17) R.raw.clear_day else R.raw.clear_night
                "Clouds" -> if (currentHour in 6..17) R.raw.cloudy_day else R.raw.cloudy_night
                else -> R.raw.atmosphere
            }
        }
    }

    data class Temperature(val temp: Double) {

        fun getTempCelsius(): String {
            return "${(temp - 273.15).roundToInt()}°C"
        }

        fun getTempFahrenheit(): String {
            return "${(((temp - 273.15) * 9 / 5) + 32).roundToInt()}°F"
        }
    }
}