package com.codecool.swai.presenter

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.codecool.swai.R
import com.codecool.swai.contract.WeatherContract
import com.codecool.swai.model.Weather
import com.codecool.swai.BaseApp.Companion.dayBackground
import com.codecool.swai.BaseApp.Companion.nightBackground
import com.codecool.swai.model.WeatherManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*
import kotlin.collections.ArrayList

class WeatherPresenter(private val dataManager: WeatherManager) : WeatherContract.WeatherPresenter {

    private var view: WeatherContract.WeatherView? = null
    private var disposable: Disposable? = null

    @SuppressLint("InflateParams")
    override fun buildPermissionDialog(inflater: LayoutInflater, message: String, permission: String, requestCode: Int) {
        val dialogForm = inflater.inflate(R.layout.permission_dialog, null)
        val dialog = AlertDialog.Builder(inflater.context)
            .apply {
                setCancelable(false)
                setView(dialogForm)
                val dialogTitle = dialogForm.findViewById<TextView>(R.id.permissionDialogTitle)
                val dialogMessage = dialogForm.findViewById<TextView>(R.id.permissionDialogMessage)
                val positiveButton = dialogForm.findViewById<Button>(R.id.positiveButton)
                dialogTitle.text = context.getString(R.string.permission_dialog_title)
                dialogMessage.text = message
                positiveButton.text = context.getString(R.string.permission_dialog_positive_button)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    positiveButton.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.colorInformation))
                }
                positiveButton.setOnClickListener {
                    view?.requestPermission(permission, requestCode)
                }
            }.create()
        view?.showDialog(dialog)
    }

    @SuppressLint("MissingPermission")
    override fun getWeatherDataByUserLocation(locationProvider: FusedLocationProviderClient) {
        val locationRequest = LocationRequest.create().apply {
            numUpdates = 1
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 3000
        }
        locationProvider.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.let {
                    val lastLocation = it.lastLocation
                    val latitude = lastLocation.latitude
                    val longitude = lastLocation.longitude
                    dataManager.addTempUnit()
                    getWeatherData(null, latitude, longitude)
                } ?: Log.d("WeatherPresenter", "onLocationAvailability: An error occurred with the location. Please add error handling here.")
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                if(locationAvailability?.isLocationAvailable == false) {
                    Log.d("WeatherPresenter", "onLocationAvailability: The user disabled location sharing. Please add error handling here.")
                }
            }
        }, null)
    }

    override fun getTempUnit(): String {
        return dataManager.getTempUnit()
    }

    override fun saveTempUnit(unit: String) {
        dataManager.saveTempUnit(unit)
    }

    override fun startSpeechRecognition(inflater: LayoutInflater, speechRecognizer: SpeechRecognizer, packageName: String, geoCoder: Geocoder) {
        val speechDialog = buildSpeechDialog(inflater)
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                view?.showDialog(speechDialog)
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
                speechDialog.cancel()
                if (error == SpeechRecognizer.ERROR_NETWORK || error == SpeechRecognizer.ERROR_SERVER) {
                    view?.showToast(R.string.no_connection_message, Toast.LENGTH_SHORT)
                } else if(error == SpeechRecognizer.ERROR_NO_MATCH) {
                    view?.showToast(R.string.speech_no_match, Toast.LENGTH_LONG)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches: ArrayList<String>? = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val speechInput: String = matches!!.first()
                disposable = getCoordinatesBySpeech(geoCoder, speechInput)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { locationList ->
                        if (locationList.isNotEmpty()){
                            getWeatherData(
                                locationList[0] as String,
                                locationList[1] as Double,
                                locationList[2] as Double
                            )
                    } else {
                            speechDialog.cancel()
                            view?.showToast(R.string.no_results, Toast.LENGTH_SHORT)
                        }}
            }

        })
        speechRecognizer.startListening(speechIntent)
    }

    override fun onAttach(view: WeatherContract.WeatherView) {
        this.view = view
    }

    override fun onDetach() {
        disposable?.dispose()
        this.view = null
    }

    fun getWeatherData(cityName: String? = null, latitude: Double, longitude: Double) {
        disposable = dataManager.getWeatherDataByCoordinates(latitude, longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { weather ->
                            view?.hideError()
                            view?.hideLoading()
                            view?.cancelDialog()
                            processWeatherData(cityName ?: weather.current.getLocationName(), weather) },
                        { error ->
                            view?.hideLoading()
                            view?.cancelDialog()
                            view?.displayError(error) })
    }

    private fun processWeatherData(cityName: String, weather: Weather) {
        if (weather.current.cod == HttpURLConnection.HTTP_OK && weather.forecast.daily.isNotEmpty()) {
            val currentHour = weather.current.getCurrentHour()
            val currentWeatherIcon = weather.current.weather.first().getWeatherIcon(currentHour)
            if (currentHour in 6..17) {
                view?.createMainPageTheme(currentWeatherIcon, dayBackground, R.color.colorDaySky, R.color.colorDayDetails)
            } else{
                view?.createMainPageTheme(currentWeatherIcon, nightBackground, R.color.colorNightSky, R.color.colorNightDetails)
            }
            view?.displayCurrentWeatherData(cityName, weather.current)
            view?.displayForecastWeatherData(weather.forecast)
        } else {
            view?.displayError(ApiException(Status.RESULT_CANCELED))
        }
    }

    @SuppressLint("InflateParams")
    private fun buildSpeechDialog(inflater: LayoutInflater): AlertDialog {
        val dialogForm = inflater.inflate(R.layout.speech_dialog, null)
        val dialog = AlertDialog.Builder(inflater.context)
            .apply {
                setCancelable(false)
                setView(dialogForm)
                val cancelButton = dialogForm.findViewById<ImageButton>(R.id.speechCancelButton)
                cancelButton.setOnClickListener { view?.cancelSpeechRecognition() }
        }
        return dialog.create()
    }

    private fun getCoordinatesBySpeech(geoCoder: Geocoder, speechInput: String): Single<List<Any>> {
        try {
            val addresses = geoCoder.getFromLocationName(speechInput, 1)
            if (addresses.isNotEmpty()) {
                val location = addresses.first()
                if (location.hasLatitude() && location.hasLongitude()) {
                    var address = arrayOf(location.adminArea ?: location.locality, location.subLocality ?: location.locality ?: location.subAdminArea)
                        .distinct()
                        .filterNotNull()
                        .joinToString(", ")
                    if (address.isEmpty()) address = speechInput
                    return Single.just(listOf(address, location.latitude, location.longitude))
                }
            }
        } catch (e: IOException) {
            return Single.just(emptyList())
        }
        return Single.just(emptyList())
    }

}