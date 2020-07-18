package com.codecool.swai.presenter

import android.util.Log
import com.codecool.swai.contract.WeatherContract

class WeatherPresenter(view: WeatherContract.WeatherView): WeatherContract.WeatherPresenter {

    private var view: WeatherContract.WeatherView? = view

    override fun getWeatherData(cityName: String) {
        Log.d("Presenter", "getWeatherData: $cityName")
    }

    override fun onDetach() {
        this.view = null
    }
}