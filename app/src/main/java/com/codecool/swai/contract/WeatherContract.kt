package com.codecool.swai.contract

import android.location.Geocoder
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.codecool.swai.model.Weather
import com.google.android.gms.location.FusedLocationProviderClient

interface WeatherContract {

    interface WeatherView: BaseContract.BaseView {
        fun requestPermission(permission: String, requestCode: Int)
        fun displayWeatherData(weather: Weather)
        fun showToast(message: Int, toastLength: Int)
        fun showDialog(dialog: AlertDialog)
        fun cancelDialog()
        fun cancelSpeechRecognition()
    }

    interface WeatherPresenter: BaseContract.BasePresenter<WeatherView> {
        fun buildPermissionDialog(inflater: LayoutInflater, message: String, permission: String, requestCode: Int)
        fun getWeatherDataByUserLocation(locationProvider: FusedLocationProviderClient)
        fun getLatestWeatherData(): Weather?
        fun saveTempUnit(unit: String)
        fun registerSpeechListener(inflater: LayoutInflater, speechRecognizer: SpeechRecognizer, geoCoder: Geocoder)
    }
}