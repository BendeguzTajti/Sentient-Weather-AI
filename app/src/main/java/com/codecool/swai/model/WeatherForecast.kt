package com.codecool.swai.model

class WeatherForecast {
    data class Result(val cod: Int, val list: List<Forecast>, val city: CityName)
    data class Forecast(val main: Temperature, val weather: List<Weather>, val dt_txt: String)
    data class Temperature(val temp: Double)
    data class Weather(val main: String, val description: String)
    data class CityName(val name: String)
}