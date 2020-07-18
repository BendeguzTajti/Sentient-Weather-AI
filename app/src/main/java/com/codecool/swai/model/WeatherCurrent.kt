package com.codecool.swai.model

class WeatherCurrent {
    data class Result(val weather: List<Weather>, val main: Temperature, val name: String, val cod: Int)
    data class Weather(val main: String, val description: String)
    data class Temperature(val temp: Double)
}