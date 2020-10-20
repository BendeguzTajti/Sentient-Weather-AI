package com.codecool.swai.contract

import com.codecool.swai.model.Weather

interface WeatherContract {

    interface WeatherView: BaseContract.BaseView {
        fun displayWeatherData(weather: Weather)
        fun cancelSpeechDialog()
    }

    interface WeatherPresenter: BaseContract.BasePresenter<WeatherView> {
        fun getWeatherDataByUserLocation()
        fun getWeatherDataBySpeech(speechInput: String)
        fun getLatestWeatherData(): Weather?
        fun saveTempUnit(unit: String)
    }
}