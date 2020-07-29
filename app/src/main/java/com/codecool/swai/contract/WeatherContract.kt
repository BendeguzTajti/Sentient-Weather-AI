package com.codecool.swai.contract

import android.speech.SpeechRecognizer
import androidx.core.widget.NestedScrollView
import com.codecool.swai.model.WeatherCurrent
import com.codecool.swai.model.WeatherForecast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomsheet.BottomSheetBehavior

interface WeatherContract {

    interface WeatherView: BaseContract.BaseView {
        fun checkLocationPermission(): Boolean
        fun createMainPageTheme(weatherIcon: Int, backgroundImage: Int, colorSky: Int, colorDetailsPage: Int)
        fun displayCurrentWeatherData(currentWeather: WeatherCurrent.Result)
        fun displayForecastWeatherData(forecastWeather: WeatherForecast.Result)
        fun pauseBackgroundAnimations()
        fun resumeBackgroundAnimations()
        fun hideSwipeIndicator()
        fun showSwipeIndicator()
        fun changeSwipeIndicatorAnimation(newAnimation: Int)
        fun showToast(message: Int)
    }

    interface WeatherPresenter: BaseContract.BasePresenter {
        fun getUserCoordinates(locationProvider: FusedLocationProviderClient)
        fun getWeatherData(latitude: Double, longitude: Double)
        fun addBottomSheetListener(bottomSheet: BottomSheetBehavior<NestedScrollView>)
        fun startSpeechRecognition(speechRecognizer: SpeechRecognizer?, packageName: String)
    }
}