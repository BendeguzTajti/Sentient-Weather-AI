package com.codecool.swai.contract

import android.location.Geocoder
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieComposition
import com.codecool.swai.model.Weather
import com.google.android.gms.location.FusedLocationProviderClient

interface WeatherContract {

    interface WeatherView: BaseContract.BaseView {
        fun requestPermission(permission: String, requestCode: Int)
        fun createMainPageTheme(weatherIcon: Int, background: LottieComposition?, colorSky: Int, colorDetailsPage: Int)
        fun displayWeatherData(city: String, weather: Weather)
        fun showToast(message: Int, toastLength: Int)
        fun showDialog(dialog: AlertDialog)
        fun cancelDialog()
        fun cancelSpeechRecognition()
    }

    interface WeatherPresenter: BaseContract.BasePresenter<WeatherView> {
        fun buildPermissionDialog(inflater: LayoutInflater, message: String, permission: String, requestCode: Int)
        fun getWeatherDataByUserLocation(locationProvider: FusedLocationProviderClient)
        fun saveTempUnit(unit: String)
        fun startSpeechRecognition(inflater: LayoutInflater, speechRecognizer: SpeechRecognizer, packageName: String, geoCoder: Geocoder)
    }
}