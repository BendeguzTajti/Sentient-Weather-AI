package com.codecool.swai.contract

import android.location.Geocoder
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import com.airbnb.lottie.LottieComposition
import com.codecool.swai.model.WeatherCurrent
import com.codecool.swai.model.WeatherForecast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomsheet.BottomSheetBehavior

interface WeatherContract {

    interface WeatherView: BaseContract.BaseView {
        fun requestPermission(permission: String, requestCode: Int)
        fun createMainPageTheme(weatherIcon: Int, background: LottieComposition?, colorSky: Int, colorDetailsPage: Int)
        fun displayCurrentWeatherData(city: String, currentWeather: WeatherCurrent.Result)
        fun displayForecastWeatherData(forecastWeather: WeatherForecast.Result)
        fun showToast(message: Int, toastLength: Int)
        fun showDialog(dialog: AlertDialog)
        fun cancelDialog()
        fun cancelSpeechRecognition()
    }

    interface WeatherPresenter: BaseContract.BasePresenter<WeatherView> {
        fun buildPermissionDialog(inflater: LayoutInflater, message: String, permission: String, requestCode: Int)
        fun getWeatherDataByUserLocation(locationProvider: FusedLocationProviderClient)
        fun startSpeechRecognition(inflater: LayoutInflater, speechRecognizer: SpeechRecognizer, packageName: String, geoCoder: Geocoder)
    }
}