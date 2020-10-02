package com.codecool.swai.model

import com.airbnb.lottie.LottieComposition
import com.codecool.swai.BaseApp
import com.codecool.swai.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class WeatherCurrent {

    data class Result(val weather: List<Weather>, val main: Temperature, val timezone: Long, var name: String, val cod: Int) {

        private fun getCurrentHour(): Int {
            val timeZone = TimeZone.getTimeZone("UTC")
            timeZone.rawOffset = TimeUnit.SECONDS.toMillis(timezone).toInt()
            val dateFormat = SimpleDateFormat("HH", Locale.getDefault())
            dateFormat.timeZone = timeZone
            return dateFormat.format(Date()).toInt()
        }

        @ExperimentalStdlibApi
        fun getLocationName(): String {
            return name.capitalize(Locale.getDefault())
        }

        fun getDescription(): String {
            return weather.first().description
        }

        fun getWeatherIcon(): Int {
            val currentHour = getCurrentHour()
            return when(weather.first().main) {
                "Thunderstorm" -> R.raw.thunder
                "Drizzle" -> R.raw.drizzle
                "Rain" -> if (currentHour in 6..17) R.raw.rainy_day else R.raw.rainy_night
                "Snow" -> if (currentHour in 6..17) R.raw.snow_day else R.raw.snow_night
                "Clear" -> if (currentHour in 6..17) R.raw.clear_day else R.raw.clear_night
                "Clouds" -> if (currentHour in 6..17) R.raw.cloudy_day else R.raw.cloudy_night
                else -> R.raw.atmosphere
            }
        }

        fun getBackground(): LottieComposition {
            val currentHour = getCurrentHour()
            return if (currentHour in 6..17) BaseApp.dayBackground else BaseApp.nightBackground
        }

        fun getSkyAndGroundColors(): Array<Int> {
            val currentHour = getCurrentHour()
            return if (currentHour in 6..17) arrayOf(R.color.colorDaySky, R.color.colorDayDetails) else arrayOf(R.color.colorNightSky, R.color.colorNightDetails)
        }
    }

    data class Weather(val main: String, val description: String)

    data class Temperature(val temp: Double, var isAnimating: Boolean = false) {

        fun getTemp(tempUnit: String): String {
            return if (tempUnit == "Celsius") getTempCelsius() else getTempFahrenheit()
        }

        private fun getTempCelsius(): String {
            return "${(temp - 273.15).roundToInt()}°C"
        }

        private fun getTempFahrenheit(): String {
            return "${(((temp - 273.15) * 9 / 5) + 32).roundToInt()}°F"
        }
    }
}