package com.codecool.swai

import com.codecool.swai.model.Weather
import com.codecool.swai.model.WeatherCurrent
import com.codecool.swai.model.WeatherForecast


val weatherSuccess = Weather(WeatherCurrent.Result(
        listOf(WeatherCurrent.Weather("Clear", "clear sky")),
        WeatherCurrent.Temperature(	302.54),
        7200,
        "budapest",
        200), WeatherForecast.Result(listOf(
        WeatherForecast.Forecast(1599991200, WeatherForecast.Temperature(294.83, 302.99), listOf(
                WeatherForecast.Weather("Clear"))),
        WeatherForecast.Forecast(1600077600, WeatherForecast.Temperature(294.91, 303.29), listOf(
                WeatherForecast.Weather("Clouds"))),
        WeatherForecast.Forecast(1600164000, WeatherForecast.Temperature(295.09, 303.79), listOf(
                WeatherForecast.Weather("Clouds"))))))

val weatherFailure = Weather(WeatherCurrent.Result(
        emptyList(),
        WeatherCurrent.Temperature(	0.0),
        0,
        "",
        404), WeatherForecast.Result(emptyList()))