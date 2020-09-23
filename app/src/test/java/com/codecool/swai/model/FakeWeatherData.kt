package com.codecool.swai.model

class FakeWeatherData {

    private val weatherCurrentSuccess = WeatherCurrent.Result(
        listOf(WeatherCurrent.Weather("Clear", "clear sky")),
        WeatherCurrent.Temperature(	302.54),
        7200,
        "Budapest",
        200)

    private val weatherForecastSuccess = WeatherForecast.Result(listOf(
        WeatherForecast.Forecast(1599991200, WeatherForecast.Temperature(294.83, 302.99), listOf(
            WeatherForecast.Weather("Clear"))),
        WeatherForecast.Forecast(1600077600, WeatherForecast.Temperature(294.91, 303.29), listOf(
            WeatherForecast.Weather("Clouds"))),
        WeatherForecast.Forecast(1600164000, WeatherForecast.Temperature(295.09, 303.79), listOf(
            WeatherForecast.Weather("Clouds")))))

    private val weatherCurrentFailure = WeatherCurrent.Result(
        emptyList(),
        WeatherCurrent.Temperature(	0.0),
        0,
        "",
        404)

    private val weatherForecastFailure = WeatherForecast.Result(emptyList())

    fun getWeatherSuccess(): Weather {
        return Weather(weatherCurrentSuccess, weatherForecastSuccess)
    }

    fun getWeatherFailure(): Weather {
        return Weather(weatherCurrentFailure, weatherForecastFailure)
    }
}