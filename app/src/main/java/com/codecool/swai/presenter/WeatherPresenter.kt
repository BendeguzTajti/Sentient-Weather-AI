package com.codecool.swai.presenter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
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
import com.codecool.swai.model.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection
import kotlin.collections.ArrayList

class WeatherPresenter(
        private val weatherDataManager: WeatherManager,
        private val locationDataManager: LocationManager) : WeatherContract.WeatherPresenter {

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

    override fun getWeatherDataByUserLocation() {
        view?.showLoading()
        weatherDataManager.addTempUnit()
        disposable = locationDataManager.getUserLocation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { locationList -> getWeatherData(null, locationList.first(), locationList.last()) },
                        { error -> Log.d(".WeatherPresenter", "getWeatherDataByUserLocation: ${error.message}") }
                )
    }

    override fun getLatestWeatherData(): Weather? {
        return weatherDataManager.getLatestWeatherData()
    }

    override fun saveTempUnit(unit: String) {
        weatherDataManager.saveTempUnit(unit)
    }

    override fun registerSpeechListener(inflater: LayoutInflater, speechRecognizer: SpeechRecognizer) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            val speechDialog = buildSpeechDialog(inflater)

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
                disposable = Single.fromCallable { locationDataManager.getCoordinatesBySpeech(speechInput) }
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
    }

    override fun onAttach(view: WeatherContract.WeatherView) {
        this.view = view
    }

    override fun onDetach() {
        disposable?.dispose()
        this.view = null
    }

    fun getWeatherData(cityName: String? = null, latitude: Double, longitude: Double) {
        disposable = weatherDataManager.getWeatherDataByCoordinates(latitude, longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { weather ->
                            weatherDataManager.addLatestWeatherData(weather)
                            view?.hideError()
                            view?.hideLoading()
                            view?.cancelDialog()
                            cityName?.let { weather.current.name = it }
                            processWeatherData(weather) },
                        { error ->
                            view?.hideLoading()
                            view?.cancelDialog()
                            view?.displayError(error) })
    }

    private fun processWeatherData(weather: Weather) {
        if (weather.current.cod == HttpURLConnection.HTTP_OK && weather.forecast.daily.isNotEmpty()) {
            view?.displayWeatherData(weather)
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

}