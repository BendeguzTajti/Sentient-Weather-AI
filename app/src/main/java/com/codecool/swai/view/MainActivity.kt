package com.codecool.swai.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieComposition
import com.codecool.swai.R
import com.codecool.swai.adapter.ForecastAdapter
import com.codecool.swai.contract.WeatherContract
import com.codecool.swai.model.WeatherCurrent
import com.codecool.swai.model.WeatherForecast
import com.codecool.swai.presenter.WeatherPresenter
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import java.net.UnknownHostException
import java.util.*


class MainActivity : AppCompatActivity(), WeatherContract.WeatherView {

    companion object {
        private const val FINE_LOCATION_RQ = 101
        private const val RECORD_AUDIO_RQ = 102
    }

    private val presenter: WeatherPresenter by inject()
    private val forecastAdapter = ForecastAdapter()
    private var speechRecognizer: SpeechRecognizer? = null
    private var geoCoder: Geocoder? = null
    private lateinit var bottomSheet: BottomSheetBehavior<NestedScrollView>
    private lateinit var locationProvider: FusedLocationProviderClient
    private lateinit var toast: Toast
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        retryLoading.indeterminateDrawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorRetryButton), PorterDuff.Mode.SRC_IN)
        presenter.onAttach(this)
        recyclerView.adapter = forecastAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        bottomSheet = BottomSheetBehavior.from(bottomSheetPage)
    }

    override fun onStart() {
        super.onStart()
        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        speechRecognizer = if (SpeechRecognizer.isRecognitionAvailable(this)) SpeechRecognizer.createSpeechRecognizer(this) else null
        geoCoder = if (Geocoder.isPresent()) Geocoder(this, Locale.getDefault()) else null
        if(speechRecognizer != null && geoCoder != null) speechButtonContainer.visibility = View.VISIBLE
        checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_RQ, getString(R.string.location_dialog_message))
    }

    override fun onPause() {
        cancelToast()
        super.onPause()
    }

    override fun onDestroy() {
        cancelDialog()
        presenter.onDetach()
        speechRecognizer?.destroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults.first() != PackageManager.PERMISSION_GRANTED) {
            if (requestCode == FINE_LOCATION_RQ) {
                finish()
            }
        } else {
            when(requestCode) {
                FINE_LOCATION_RQ -> presenter.getWeatherDataByUserLocation(locationProvider)
                RECORD_AUDIO_RQ -> {
                    cancelToast()
                    presenter.startSpeechRecognition(layoutInflater, speechRecognizer!!, application.packageName, geoCoder!!)
                }
            }
        }
    }

    override fun requestPermission(permission: String, requestCode: Int) {
        dialog.dismiss()
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
    }

    override fun createMainPageTheme(weatherIcon: Int, background: LottieComposition?, colorSky: Int, colorDetailsPage: Int) {
        mainWeatherIcon.setAnimation(weatherIcon)
        val currentRootColor = rootLayout.background as ColorDrawable?
        if (currentRootColor?.color != ContextCompat.getColor(this, colorSky)) {
            mainBackground.setComposition(background!!)
            mainBackground.setBackgroundColor(ContextCompat.getColor(this, colorSky))
            mainTemp.setBackgroundColor(ContextCompat.getColor(this, colorSky))
            description.setBackgroundColor(ContextCompat.getColor(this, colorSky))
            mainWeatherIcon.setBackgroundColor(ContextCompat.getColor(this, colorSky))
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, colorDetailsPage))
        }
        bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        mainPageData.visibility = View.VISIBLE
        if (mainBackground.progress < 0.99f) {
            mainBackground.resumeAnimation()
        }
    }

    @ExperimentalStdlibApi
    override fun displayCurrentWeatherData(city:String, currentWeather: WeatherCurrent.Result) {
        mainTemp.text = currentWeather.main.getTempCelsius()
        cityName.text = city.capitalize(Locale.getDefault())
        description.text = currentWeather.weather.first().description
    }

    override fun displayForecastWeatherData(forecastWeather: WeatherForecast.Result) {
        addBottomSheetListener()
        speechButton.setOnClickListener { checkForPermission(Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_RQ, getString(R.string.record_audio_dialog_message)) }
        forecastAdapter.setForecastData(forecastWeather.daily.subList(0, 3))
        bottomSheet.isDraggable = true
    }

    override fun showToast(message: Int, toastLength: Int) {
        if (this::toast.isInitialized) {
            toast.cancel()
        }
        toast = Toast.makeText(this, getString(message), toastLength)
        toast.show()
    }

    override fun showDialog(dialog: AlertDialog) {
        this.dialog = dialog
        dialog.window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.colorTransparent)))
        dialog.show()
    }

    override fun cancelDialog() {
        if (this::dialog.isInitialized) {
            dialog.dismiss()
        }
    }

    override fun cancelSpeechRecognition() {
        speechRecognizer?.cancel()
        cancelDialog()
    }

    override fun hideLoading() {
        loadingScreen.visibility = View.GONE
    }

    override fun hideError() {
        errorContainer.visibility = View.GONE
    }

    override fun displayError(exception: Throwable) {
        when(exception) {
            is ApiException -> showToast(R.string.server_error_message, Toast.LENGTH_LONG)
            is UnknownHostException -> {
                retryButton.visibility = View.VISIBLE
                retryLoading.visibility = View.INVISIBLE
                errorContainer.visibility = View.VISIBLE
                retryButton.setOnClickListener {
                    retryButton.visibility = View.INVISIBLE
                    retryLoading.visibility = View.VISIBLE
                    presenter.getWeatherDataByUserLocation(locationProvider)
                }
            }
            else -> Log.d(".displayError", "$exception")
        }
    }

    private fun checkForPermission(permission: String, requestCode: Int, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                    when(permission) {
                        Manifest.permission.ACCESS_FINE_LOCATION -> presenter.getWeatherDataByUserLocation(locationProvider)
                        Manifest.permission.RECORD_AUDIO -> {
                            cancelToast()
                            presenter.startSpeechRecognition(layoutInflater, speechRecognizer!!, application.packageName, geoCoder!!)
                        }
                    }
                }
                shouldShowRequestPermissionRationale(permission) -> presenter.buildPermissionDialog(layoutInflater, message, permission, requestCode)
                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    private fun addBottomSheetListener() {
        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 1.0f && slideOffset > 0.0f) {
                    swipeIndicator.visibility = View.INVISIBLE
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        swipeIndicator.setAnimation(R.raw.swipe_down)
                        swipeIndicator.progress = 0.0f
                        swipeIndicator.playAnimation()
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        if (mainBackground.isAnimating) {
                            mainBackground.pauseAnimation()
                        }
                        mainWeatherIcon.pauseAnimation()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        if (mainBackground.progress < 0.99f) {
                            mainBackground.resumeAnimation()
                        }
                        mainWeatherIcon.resumeAnimation()
                        swipeIndicator.setAnimation(R.raw.swipe_up)
                        swipeIndicator.progress = 0.0f
                        swipeIndicator.playAnimation()
                    } else -> {}
                }
                swipeIndicator.visibility = View.VISIBLE
            }

        })
    }

    private fun cancelToast() {
        if (this::toast.isInitialized) {
            toast.cancel()
        }
    }
}