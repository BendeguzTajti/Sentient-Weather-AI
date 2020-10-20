package com.codecool.swai.presenter

import com.codecool.swai.contract.WeatherContract
import com.codecool.swai.model.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection

class WeatherPresenter(
        private val weatherDataManager: WeatherManager,
        private val locationDataManager: LocationManager) : WeatherContract.WeatherPresenter {

    private var view: WeatherContract.WeatherView? = null
    private var disposable: Disposable? = null

    override fun getWeatherDataByUserLocation() {
        view?.showLoading()
        disposable = locationDataManager.getUserLocation()
                .flatMap { location -> locationDataManager.getUserCityAndCountryCode(location)
                        .subscribeOn(Schedulers.io()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { location ->
                            weatherDataManager.addTempUnit(location.countryCode)
                            getWeatherData(location) },
                        { error ->
                            view?.hideLoading()
                            view?.displayError(error) }
                )
    }

    override fun getWeatherDataBySpeech(speechInput: String) {
        disposable = locationDataManager.getCoordinatesBySpeech(speechInput)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { location -> getWeatherData(location) },
                        { error -> view?.displayError(error)  })
    }

    override fun getLatestWeatherData(): Weather? {
        return weatherDataManager.getLatestWeatherData()
    }

    override fun saveTempUnit(unit: String) {
        weatherDataManager.saveTempUnit(unit)
    }

    override fun onAttach(view: WeatherContract.WeatherView) {
        this.view = view
    }

    override fun onDetach() {
        disposable?.dispose()
        this.view = null
    }

    fun getWeatherData(location: Location) {
        disposable = weatherDataManager.getWeatherDataByCoordinates(location.latitude, location.longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { weather ->
                            weatherDataManager.addLatestWeatherData(weather)
                            view?.hideError()
                            view?.hideLoading()
                            view?.cancelSpeechDialog()
                            if (location.cityName.isNotEmpty()) weather.current.name = location.cityName
                            processWeatherData(weather) },
                        { error ->
                            view?.hideLoading()
                            view?.cancelSpeechDialog()
                            view?.displayError(error) })
    }

    private fun processWeatherData(weather: Weather) {
        if (weather.current.cod == HttpURLConnection.HTTP_OK && weather.forecast.daily.isNotEmpty()) {
            view?.displayWeatherData(weather)
        } else {
            view?.displayError(ApiException(Status.RESULT_CANCELED))
        }
    }
}