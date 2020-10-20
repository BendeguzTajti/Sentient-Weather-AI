package com.codecool.swai.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecool.swai.R
import com.codecool.swai.adapter.ForecastAdapter
import com.codecool.swai.contract.WeatherContract
import com.codecool.swai.dialog.PermissionDialog
import com.codecool.swai.dialog.SpeechDialog
import com.codecool.swai.listener.LocationListenerCallback
import com.codecool.swai.listener.LocationSpeechListener
import com.codecool.swai.model.Weather
import com.codecool.swai.presenter.WeatherPresenter
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import me.ibrahimsn.library.LiveSharedPreferences
import org.koin.android.ext.android.inject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import javax.net.ssl.SSLHandshakeException


class MainActivity : AppCompatActivity(),
        WeatherContract.WeatherView,
        PermissionDialog.OnPermissionDialogClosedListener,
        SpeechDialog.OnSpeechDialogCanceledListener,
        LocationListenerCallback{

    companion object {
        private const val FINE_LOCATION_RQ = 101
        private const val RECORD_AUDIO_RQ = 102
    }

    private val presenter: WeatherPresenter by inject()
    private val liveSharedPref: LiveSharedPreferences by inject()
    private var tempUnit: String? = null
    private val forecastAdapter = ForecastAdapter()
    private val permissionDialog = PermissionDialog()
    private val speechDialog = SpeechDialog()
    private val locationSpeechListener = LocationSpeechListener
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var bottomSheet: BottomSheetBehavior<NestedScrollView>
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        presenter.onAttach(this)
        bottomSheet = BottomSheetBehavior.from(bottomSheetPage)
        recyclerView.adapter = forecastAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        if(SpeechRecognizer.isRecognitionAvailable(this) && Geocoder.isPresent()) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            locationSpeechListener.onAttach(this)
            speechRecognizer.setRecognitionListener(locationSpeechListener)
            speechButtonContainer.visibility = View.VISIBLE
        }
        liveSharedPref.getString("unit", "Celsius").observe(this, Observer<String> { value ->
            updateWeatherTemp(value)  })
        checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_RQ, getString(R.string.location_dialog_message))
        addPopupMenuListener()
        addBottomSheetListener()
        speechButton.setOnClickListener { checkForPermission(Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_RQ, getString(R.string.record_audio_dialog_message)) }
    }

    override fun onPause() {
        cancelToast()
        super.onPause()
    }

    override fun onDestroy() {
        presenter.onDetach()
        locationSpeechListener.onDetach()
        if (this::speechRecognizer.isInitialized) speechRecognizer.destroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults.first() != PackageManager.PERMISSION_GRANTED) {
            if (requestCode == FINE_LOCATION_RQ) {
                finish()
            }
        } else {
            when(requestCode) {
                FINE_LOCATION_RQ -> presenter.getWeatherDataByUserLocation()
                RECORD_AUDIO_RQ -> {
                    cancelToast()
                    startSpeechListener()
                }
            }
        }
    }

    @ExperimentalStdlibApi
    override fun displayWeatherData(weather: Weather) {
        createMainPageTheme(weather)
        mainTemp.text = weather.current.main.getTemp(tempUnit!!)
        cityName.text = weather.current.getLocationName()
        description.text = weather.current.getDescription()
        forecastAdapter.setForecastData(tempUnit!!, weather.forecast.daily.subList(0, 3))
        bottomSheet.isDraggable = true
    }

    override fun cancelSpeechDialog() {
        if (speechDialog.isVisible) speechDialog.dismiss()
    }

    override fun showLoading() {
        if (errorContainer.visibility == View.GONE) {
            loadingScreen.visibility = View.VISIBLE
        }
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
            is NullPointerException -> {
                cancelSpeechDialog()
                showToast(R.string.no_results, Toast.LENGTH_SHORT)
            }
            is SocketTimeoutException -> displayInternetError()
            is SSLHandshakeException -> displayInternetError()
            is UnknownHostException -> displayInternetError()
            is ConnectException -> displayInternetError()
            else -> Log.d(".MainActivity", ".displayError: $exception")
        }
    }

    override fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
    }

    override fun cancelRecognition() {
        speechRecognizer.cancel()
    }

    override fun onRecognitionStarted() {
        speechDialog.show(supportFragmentManager, speechDialog.tag)
    }

    override fun onRecognitionResult(speechInput: String) {
        presenter.getWeatherDataBySpeech(speechInput)
    }

    override fun onRecognitionError(error: Int) {
        speechDialog.dismiss()
        if (error == SpeechRecognizer.ERROR_NETWORK || error == SpeechRecognizer.ERROR_SERVER) {
            showToast(R.string.no_connection_message, Toast.LENGTH_LONG)
        } else if(error == SpeechRecognizer.ERROR_NO_MATCH) {
            showToast(R.string.speech_no_match, Toast.LENGTH_SHORT)
        }
    }

    private fun checkForPermission(permission: String, requestCode: Int, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                    when(permission) {
                        Manifest.permission.ACCESS_FINE_LOCATION -> presenter.getWeatherDataByUserLocation()
                        Manifest.permission.RECORD_AUDIO -> {
                            cancelToast()
                            startSpeechListener()
                        }
                    }
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    val bundle = Bundle()
                    bundle.putString(PermissionDialog.PERMISSION_KEY, permission)
                    bundle.putInt(PermissionDialog.REQUEST_CODE_KEY, requestCode)
                    bundle.putString(PermissionDialog.DIALOG_MESSAGE_KEY, message)
                    permissionDialog.arguments = bundle
                    permissionDialog.show(supportFragmentManager, permissionDialog.tag)
                }
                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    private fun showToast(message: Int, toastLength: Int) {
        if (this::toast.isInitialized) {
            toast.cancel()
        }
        toast = Toast.makeText(this, getString(message), toastLength)
        toast.show()
    }

    private fun createMainPageTheme(weather: Weather) {
        val weatherIcon = weather.current.getWeatherIcon()
        val background = weather.current.getBackground()
        mainWeatherIcon.setAnimation(weatherIcon)
        if (mainBackground.composition != background) {
            rootLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.background_fade_out))
            val skyAndGroundColor: Array<Int> = weather.current.getSkyAndGroundColors()
            val colorSky = ContextCompat.getColor(this, skyAndGroundColor.first())
            val colorGround = ContextCompat.getColor(this, skyAndGroundColor.last())
            mainBackground.setComposition(background)
            mainBackground.setBackgroundColor(colorSky)
            mainTempContainer.setBackgroundColor(colorSky)
            description.setBackgroundColor(colorSky)
            mainWeatherIcon.setBackgroundColor(colorSky)
            rootLayout.setBackgroundColor(colorGround)
        }
        mainPage.visibility = View.VISIBLE
        swipeIndicator.visibility = View.VISIBLE
        bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun addPopupMenuListener() {
        tempOptions.setOnClickListener { view ->
            pauseMainPageAnimations()
            PopupMenu(this, view).apply {
                setOnMenuItemClickListener { item: MenuItem? ->
                    when(item?.itemId) {
                        R.id.celsius -> {
                            if (tempUnit != "Celsius") presenter.saveTempUnit("Celsius")
                            true
                        }
                        R.id.fahrenheit -> {
                            if (tempUnit != "Fahrenheit") presenter.saveTempUnit("Fahrenheit")
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }
                setOnDismissListener {
                    resumeMainPageAnimations()
                }
                inflate(R.menu.popup_menu)
                show()
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
                    BottomSheetBehavior.STATE_DRAGGING -> pauseMainPageAnimations()
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        resumeMainPageAnimations()
                        swipeIndicator.setAnimation(R.raw.swipe_up)
                        swipeIndicator.progress = 0.0f
                        swipeIndicator.playAnimation()
                    } else -> {}
                }
                swipeIndicator.visibility = View.VISIBLE
            }

        })
    }

    private fun startSpeechListener() {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        speechRecognizer.startListening(speechIntent)
    }

    private fun updateWeatherTemp(tempUnit: String) {
        if (this.tempUnit != tempUnit) {
            this.tempUnit = tempUnit
            val weatherData = presenter.getLatestWeatherData()
            weatherData?.let {
                val fadeAnim = AnimationUtils.loadAnimation(this, R.anim.text_fade_in_out)
                fadeAnim.setAnimationListener(object :Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                        mainTemp.text = it.current.main.getTemp(tempUnit)
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        resumeMainPageAnimations()
                    }

                    override fun onAnimationStart(animation: Animation?) {
                        pauseMainPageAnimations()
                    }
                })
                mainTemp.startAnimation(fadeAnim)
                forecastAdapter.updateForecastTemp(tempUnit)
            }
        }
    }

    private fun cancelToast() {
        if (this::toast.isInitialized) {
            toast.cancel()
        }
    }

    private fun pauseMainPageAnimations() {
        if (mainBackground.isAnimating) {
            mainBackground.pauseAnimation()
        }
        mainWeatherIcon.pauseAnimation()
    }

    private fun resumeMainPageAnimations() {
        if (mainBackground.progress < 0.99f) {
            mainBackground.resumeAnimation()
        }
        mainWeatherIcon.resumeAnimation()
    }

    private fun displayInternetError() {
        retryButton.visibility = View.VISIBLE
        retryLoading.visibility = View.INVISIBLE
        errorContainer.visibility = View.VISIBLE
        retryButton.setOnClickListener {
            retryButton.visibility = View.INVISIBLE
            retryLoading.visibility = View.VISIBLE
            presenter.getWeatherDataByUserLocation()
        }
    }
}