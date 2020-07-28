package com.codecool.swai.contract

import androidx.core.widget.NestedScrollView
import com.codecool.swai.model.WeatherCurrent
import com.codecool.swai.model.WeatherForecast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomsheet.BottomSheetBehavior

interface WeatherContract {

    interface WeatherView: BaseContract.BaseView {
        fun getLocationProvider(): FusedLocationProviderClient
        fun checkLocationPermission(): Boolean
        fun createMainPageTheme(weatherIcon: Int, backgroundImage: Int, colorSky: Int, colorDetailsPage: Int)
        fun displayCurrentWeatherData(currentWeather: WeatherCurrent.Result)
        fun displayForecastWeatherData(forecastWeather: WeatherForecast.Result)
        fun isBackgroundAnimating(): Boolean
        fun pauseBackgroundAnimation()
        fun resumeBackgroundAnimation()
        fun getBackgroundAnimationProgress(): Float
        fun hideSwipeIndicator()
        fun showSwipeIndicator()
        fun changeSwipeIndicatorAnimation(newAnimation: Int)
    }

    interface WeatherPresenter: BaseContract.BasePresenter {
        fun getUserCoordinates()
        fun getWeatherData(latitude: Double, longitude: Double)
        fun addBottomSheetListener(bottomSheet: BottomSheetBehavior<NestedScrollView>)
    }
}