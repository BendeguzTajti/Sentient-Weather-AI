package com.codecool.swai.presenter

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import com.codecool.swai.R
import com.codecool.swai.contract.WeatherContract
import com.codecool.swai.model.DataManager
import com.codecool.swai.model.Weather
import com.codecool.swai.view.MainActivity.Companion.dayBackground
import com.codecool.swai.view.MainActivity.Companion.nightBackground
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.collections.ArrayList

class WeatherPresenter(view: WeatherContract.WeatherView) : WeatherContract.WeatherPresenter {

    private var view: WeatherContract.WeatherView? = view
    private val dataManager = DataManager()
    private var disposable: Disposable? = null


    override fun getUserCoordinates(locationProvider: FusedLocationProviderClient) {
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (view?.checkLocationPermission() == true) {
            locationProvider.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    locationProvider.removeLocationUpdates(this)
                    if (locationResult != null && locationResult.locations.size > 0) {
                        val latestLocationIndex = locationResult.locations.size - 1
                        val latitude = locationResult.locations[latestLocationIndex].latitude
                        val longitude = locationResult.locations[latestLocationIndex].longitude
                        getWeatherData(latitude, longitude)
                    }
                }
            }, Looper.getMainLooper())
        }
    }

    override fun getWeatherData(latitude: Double, longitude: Double) {
        disposable = dataManager.getWeatherDataByCoordinates(latitude, longitude)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { weather -> processWeatherData(weather) },
                { error -> view?.displayError(error) })
    }

    override fun addBottomSheetListener(bottomSheet: BottomSheetBehavior<NestedScrollView>) {
        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 1.0f && slideOffset > 0.0f) {
                    view?.hideSwipeIndicator()
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> view?.changeSwipeIndicatorAnimation(R.raw.swipe_down)
                    BottomSheetBehavior.STATE_DRAGGING -> view?.pauseBackgroundAnimations()
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        view?.resumeBackgroundAnimations()
                        view?.changeSwipeIndicatorAnimation(R.raw.swipe_up)
                    } else -> {}
                }
                view?.showSwipeIndicator()
            }
            
        })
    }

    override fun startSpeechRecognition(speechRecognizer: SpeechRecognizer?, packageName: String, geoCoder: Geocoder?) {
        if (speechRecognizer == null || geoCoder == null) {
            view?.showToast(R.string.missing_recognizer_message)
        } else {
            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            speechRecognizer.setRecognitionListener(object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {

                }

                override fun onRmsChanged(rmsdB: Float) {

                }

                override fun onBufferReceived(buffer: ByteArray?) {

                }

                override fun onPartialResults(partialResults: Bundle?) {

                }

                override fun onEvent(eventType: Int, params: Bundle?) {

                }

                override fun onBeginningOfSpeech() {

                }

                override fun onEndOfSpeech() {

                }

                override fun onError(error: Int) {
                    if (error == SpeechRecognizer.ERROR_NETWORK || error == SpeechRecognizer.ERROR_SERVER) {
                        view?.showToast(R.string.no_connection_message)
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches: ArrayList<String>? = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val speechInput: String = matches!![0]
                    disposable = getCoordinatesBySpeech(geoCoder, speechInput)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { locationList -> if (locationList.isNotEmpty()) getWeatherDataBySpeech(speechInput, locationList[0], locationList[1])}
                }

            })
            speechRecognizer.startListening(speechIntent)
        }
    }

    override fun onDetach() {
        disposable?.dispose()
        this.view = null
    }

    @SuppressLint("InflateParams")
    override fun buildPermissionDialog(inflater: LayoutInflater, message: String, permission: String, requestCode: Int): AlertDialog {
        val dialogForm = inflater.inflate(R.layout.dialog_form, null)
        val dialog = AlertDialog.Builder(inflater.context)
            .apply {
                setCancelable(false)
                setView(dialogForm)
                val dialogTitle = dialogForm.findViewById<TextView>(R.id.dialogTitle)
                val dialogMessage = dialogForm.findViewById<TextView>(R.id.dialogMessage)
                val positiveButton = dialogForm.findViewById<Button>(R.id.positiveButton)
                dialogTitle.text = context.getString(R.string.dialog_title)
                dialogMessage.text = message
                positiveButton.text = context.getString(R.string.dialog_positive_button)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    positiveButton.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.colorInformation))
                }
                positiveButton.setOnClickListener {
                    view?.requestPermission(permission, requestCode)
                }
            }
        return dialog.create()
    }

    private fun processWeatherData(weather: Weather) {
        if (weather.current.cod == HttpURLConnection.HTTP_OK && weather.forecast.daily.isNotEmpty()) {
            val currentHour = weather.current.getCurrentHour()
            val currentWeatherIcon = weather.current.weather[0].getWeatherIcon(currentHour)
            if (currentHour in 6..17) {
                view?.createMainPageTheme(currentWeatherIcon, dayBackground, R.color.colorDaySky, R.color.colorDayDetails)
            } else{
                view?.createMainPageTheme(currentWeatherIcon, nightBackground, R.color.colorNightSky, R.color.colorNightDetails)
            }
            view?.displayCurrentWeatherData(weather.current)
            view?.displayForecastWeatherData(weather.forecast)
        } else {
            view?.displayError(ApiException(Status.RESULT_CANCELED))
        }
    }

    private fun getCoordinatesBySpeech(geoCoder: Geocoder, cityName: String): Single<List<Double>> {
        val locationList = mutableListOf<Double>()
        try {
            val addresses = geoCoder.getFromLocationName(cityName, 1)
            if (addresses.isNotEmpty()) {
                val location = addresses[0]
                if (location.hasLatitude() && location.hasLongitude()) {
                    locationList.add(location.latitude)
                    locationList.add(location.longitude)
                }
            }
        } catch (e: IOException) {
            return Single.just(locationList)
        }
        return Single.just(locationList)
    }

    private fun getWeatherDataBySpeech(cityName: String, latitude: Double, longitude: Double) {
        disposable = dataManager.getWeatherDataBySpeech(cityName, latitude, longitude)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { weather -> processWeatherData(weather) },
                { error -> view?.displayError(error) })
    }

}