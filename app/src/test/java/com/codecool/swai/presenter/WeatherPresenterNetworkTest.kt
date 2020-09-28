package com.codecool.swai.presenter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.codecool.swai.R
import com.codecool.swai.contract.WeatherContract
import com.codecool.swai.model.*
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.then
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.*
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class WeatherPresenterNetworkTest : KoinTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private var testView = mock(WeatherContract.WeatherView::class.java)
    private var testDataManager = mock(WeatherManager::class.java)
    private val presenter = WeatherPresenter(testDataManager)
    private val fakeWeatherData = FakeWeatherData()


    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        presenter.onAttach(testView)
    }


    @Test
    fun getWeatherData_successful_network_call() {
        val weatherData = fakeWeatherData.getWeatherSuccess()
        val latitude = 	37.35
        val longitude = 59.61
        val currentHour = weatherData.current.getCurrentHour()
        val currentWeatherIcon = weatherData.current.weather.first().getWeatherIcon(currentHour)
        given(testDataManager.getWeatherDataByCoordinates(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble()))
            .willReturn(Single.just(weatherData))
        presenter.getWeatherData(null, latitude, longitude)
        then(testView).should().hideError()
        then(testView).should().hideLoading()
        then(testView).should().cancelDialog()
        if (currentHour in 6..17) {
            then(testView).should().createMainPageTheme(currentWeatherIcon,
                null, R.color.colorDaySky, R.color.colorDayDetails)
        } else{
            then(testView).should().createMainPageTheme(currentWeatherIcon,
                null, R.color.colorNightSky, R.color.colorNightDetails)
        }
        then(testView).should()
            .displayWeatherData(weatherData.current.name, weatherData.current)
        then(testView).should()
            .displayForecastWeatherData(weatherData.forecast)
        verify(testView, never()).displayError(com.nhaarman.mockitokotlin2.any())
        then(testView).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getWeatherData_failed_network_call() {
        val latitude = 	0.0
        val longitude = 0.0
        given(testDataManager.getWeatherDataByCoordinates(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble()))
            .willReturn(Single.error(Exception()))
        presenter.getWeatherData(null, latitude, longitude)
        verify(testView, never()).hideError()
        then(testView).should().hideLoading()
        then(testView).should().cancelDialog()
        then(testView).should().displayError(com.nhaarman.mockitokotlin2.any())
        then(testView).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getWeatherData_wrong_weather_data() {
        val weatherData = fakeWeatherData.getWeatherFailure()
        val latitude = 	0.0
        val longitude = 0.0
        val currentHour = weatherData.current.getCurrentHour()
        val currentWeatherIcon = weatherData.current.weather.firstOrNull()?.getWeatherIcon(currentHour) ?: -1
        given(testDataManager.getWeatherDataByCoordinates(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble()))
            .willReturn(Single.just(weatherData))
        presenter.getWeatherData(null, latitude, longitude)
        then(testView).should().hideError()
        then(testView).should().hideLoading()
        then(testView).should().cancelDialog()
        verify(testView, never()).createMainPageTheme(currentWeatherIcon,null, R.color.colorDaySky, R.color.colorDayDetails)
        verify(testView, never()).displayWeatherData(weatherData.current.name, weatherData.current)
        verify(testView, never()).displayForecastWeatherData(weatherData.forecast)
        then(testView).should().displayError(com.nhaarman.mockitokotlin2.any())
        then(testView).shouldHaveNoMoreInteractions()
    }

}