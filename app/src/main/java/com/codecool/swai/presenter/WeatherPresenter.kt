package com.codecool.swai.presenter

import android.os.Looper
import android.view.View
import androidx.core.widget.NestedScrollView
import com.codecool.swai.R
import com.codecool.swai.contract.WeatherContract
import com.codecool.swai.model.DataManager
import com.codecool.swai.model.Weather
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection

class WeatherPresenter(view: WeatherContract.WeatherView) : WeatherContract.WeatherPresenter {

    private var view: WeatherContract.WeatherView? = view
    private val dataManager = DataManager()


    override fun getUserCoordinates() {
        val locationProvider = view?.getLocationProvider()
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (view?.checkLocationPermission() == true) {
            locationProvider?.requestLocationUpdates(locationRequest, object : LocationCallback() {
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
        dataManager.getWeatherDataByCoordinates(latitude, longitude)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { weather -> processWeatherData(weather) },
                { view?.displayError() })
    }

    override fun addBottomSheetListener(bottomSheet: BottomSheetBehavior<NestedScrollView>) {
        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (view!!.isBackgroundAnimating() && slideOffset < 1.0f && slideOffset > 0.0f) {
                    view?.pauseBackgroundAnimation()
                }
                if (slideOffset < 1.0f && slideOffset > 0.0f) {
                    view?.hideSwipeIndicator()
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (view!!.getBackgroundAnimationProgress() > 0.0f) {
                    view?.resumeBackgroundAnimation()
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    view?.changeSwipeIndicatorAnimation(R.raw.swipe_down)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    view?.changeSwipeIndicatorAnimation(R.raw.swipe_up)
                }
                view?.showSwipeIndicator()
            }
            
        })
    }

    override fun onDetach() {
        this.view = null
    }

    private fun processWeatherData(weather: Weather) {
        if (weather.current.cod == HttpURLConnection.HTTP_OK && weather.forecast.daily.isNotEmpty()) {
            val currentHour = weather.current.getCurrentHour()
            val currentWeatherIcon = weather.current.weather[0].getWeatherIcon(currentHour)
            if (currentHour in 6..17) {
                view?.createMainPageTheme(currentWeatherIcon, R.raw.day_background, R.color.colorDaySky, R.color.colorDayDetails)
            } else{
                view?.createMainPageTheme(currentWeatherIcon, R.raw.night_background, R.color.colorNightSky, R.color.colorNightDetails)
            }
            view?.displayCurrentWeatherData(weather.current)
            view?.displayForecastWeatherData(weather.forecast)
        } else {
            view?.displayError()
        }
    }

}