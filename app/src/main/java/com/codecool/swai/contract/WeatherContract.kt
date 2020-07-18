package com.codecool.swai.contract

interface WeatherContract {

    interface WeatherView: BaseContract.BaseView {
    }

    interface WeatherPresenter: BaseContract.BasePresenter {
        fun getWeatherData(cityName: String)
    }
}