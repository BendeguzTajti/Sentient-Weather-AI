package com.codecool.swai.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecool.swai.R
import com.codecool.swai.adapter.ForecastAdapter
import com.codecool.swai.contract.WeatherContract
import com.codecool.swai.model.WeatherCurrent
import com.codecool.swai.model.WeatherForecast
import com.codecool.swai.presenter.WeatherPresenter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), WeatherContract.WeatherView {

    private val fineLocationRQ = 101
    private val recordAudioRQ = 102
    private val presenter: WeatherContract.WeatherPresenter = WeatherPresenter(this)
    private val forecastAdapter = ForecastAdapter()
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var bottomSheet: BottomSheetBehavior<NestedScrollView>
    private lateinit var locationProvider: FusedLocationProviderClient
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        recyclerView.adapter = forecastAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        speechRecognizer = if (SpeechRecognizer.isRecognitionAvailable(this)) SpeechRecognizer.createSpeechRecognizer(this) else null
        bottomSheet = BottomSheetBehavior.from(detailsPage)
        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION, fineLocationRQ, getString(R.string.location_dialog_message))
        speechButton.setOnClickListener { checkForPermission(Manifest.permission.RECORD_AUDIO, recordAudioRQ, getString(R.string.record_audio_dialog_message)) }
    }

    override fun onPause() {
        if (this::toast.isInitialized) {
            toast.cancel()
        }
        super.onPause()
    }

    override fun onDestroy() {
        presenter.onDetach()
        speechRecognizer?.destroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (requestCode == fineLocationRQ) {
                finish()
            }
        } else {
            when(requestCode) {
                fineLocationRQ -> presenter.getUserCoordinates(locationProvider)
                recordAudioRQ -> presenter.startSpeechRecognition(speechRecognizer, application.packageName)
            }
        }
    }

    override fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    override fun createMainPageTheme(weatherIcon: Int, backgroundImage: Int, colorSky: Int, colorDetailsPage: Int) {
        mainBackground.setAnimation(backgroundImage)
        mainBackground.addLottieOnCompositionLoadedListener {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, colorSky))
            mainTemp.setBackgroundColor(ContextCompat.getColor(this, colorSky))
            cityName.setBackgroundColor(ContextCompat.getColor(this, colorSky))
            description.setBackgroundColor(ContextCompat.getColor(this, colorSky))
            detailsPage.setBackgroundColor(ContextCompat.getColor(this, colorDetailsPage))
            mainWeatherIcon.setAnimation(weatherIcon)
            mainWeatherIcon.addLottieOnCompositionLoadedListener{ mainPageData.visibility = View.VISIBLE }
        }
    }

    override fun displayCurrentWeatherData(currentWeather: WeatherCurrent.Result) {
        mainTemp.text = currentWeather.main.getTempCelsius()
        cityName.text = currentWeather.getLocationName()
        description.text = currentWeather.weather[0].description
    }

    override fun displayForecastWeatherData(forecastWeather: WeatherForecast.Result) {
        forecastAdapter.setForecastData(forecastWeather.daily.subList(0, 3))
        presenter.addBottomSheetListener(bottomSheet)
    }

    override fun pauseBackgroundAnimations() {
        if (mainBackground.isAnimating) {
            mainBackground.pauseAnimation()
        }
        mainWeatherIcon.pauseAnimation()
    }

    override fun resumeBackgroundAnimations() {
        if (mainBackground.progress < 0.99f) {
            mainBackground.resumeAnimation()
        }
        mainWeatherIcon.resumeAnimation()
    }

    override fun hideSwipeIndicator() {
        swipeIndicator.visibility = View.INVISIBLE
    }

    override fun showSwipeIndicator() {
        swipeIndicator.visibility = View.VISIBLE
    }

    override fun changeSwipeIndicatorAnimation(newAnimation: Int) {
        swipeIndicator.setAnimation(newAnimation)
        swipeIndicator.progress = 0.0f
        swipeIndicator.playAnimation()
    }

    override fun showToast(message: Int) {
        if (this::toast.isInitialized) {
            toast.cancel()
        }
        toast = Toast.makeText(this, getString(message), Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun displayError() {
        TODO("Not yet implemented")
    }

    private fun checkForPermission(permission: String, requestCode: Int, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                    when(permission) {
                        Manifest.permission.ACCESS_FINE_LOCATION -> presenter.getUserCoordinates(locationProvider)
                        Manifest.permission.RECORD_AUDIO -> presenter.startSpeechRecognition(speechRecognizer, application.packageName)
                    }
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(message, permission, requestCode)
                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    private fun showDialog(message: String, permission: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(getString(R.string.dialog_title))
            setMessage(message)
            setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}