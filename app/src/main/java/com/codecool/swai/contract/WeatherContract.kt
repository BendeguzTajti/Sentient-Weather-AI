package com.codecool.swai.contract

import com.codecool.swai.model.WeatherCurrent
import com.codecool.swai.model.WeatherForecast
import com.google.android.gms.location.FusedLocationProviderClient

interface WeatherContract {

    interface WeatherView: BaseContract.BaseView {
        fun getLocationProvider(): FusedLocationProviderClient
        fun checkLocationPermission(): Boolean
        fun createMainPageTheme(backgroundImage: Int, colorSky: Int, colorDetailsPage: Int)
        fun displayMainPage(currentWeather: WeatherCurrent.Result)
        fun displayDetailsPage(forecastWeather: WeatherForecast.Result)
    }

    interface WeatherPresenter: BaseContract.BasePresenter {
        fun getUserCoordinates()
        fun getWeatherDataByCity(cityName: String)
        fun getWeatherDataByCoordinates(latitude: Double, longitude: Double)
    }
}