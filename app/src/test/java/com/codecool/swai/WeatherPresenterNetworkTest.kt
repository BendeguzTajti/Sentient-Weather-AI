package com.codecool.swai

import com.codecool.swai.contract.WeatherContract
import com.codecool.swai.model.*
import com.codecool.swai.presenter.WeatherPresenter
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

    private val testView = mock(WeatherContract.WeatherView::class.java)
    private val testWeatherDataManager = mock(WeatherManager::class.java)
    private val testLocationDataManager = mock(LocationManager::class.java)
    private val presenter = WeatherPresenter(testWeatherDataManager, testLocationDataManager)
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
        given(testWeatherDataManager.getWeatherDataByCoordinates(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble()))
            .willReturn(Single.just(weatherData))
        presenter.getWeatherData(null, latitude, longitude)
        then(testView).should().hideError()
        then(testView).should().hideLoading()
        then(testView).should().cancelDialog()
        then(testView).should().displayWeatherData(weatherData)
        verify(testView, never()).displayError(com.nhaarman.mockitokotlin2.any())
        then(testView).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getWeatherData_failed_network_call() {
        val latitude = 	0.0
        val longitude = 0.0
        given(testWeatherDataManager.getWeatherDataByCoordinates(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble()))
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
        given(testWeatherDataManager.getWeatherDataByCoordinates(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble()))
            .willReturn(Single.just(weatherData))
        presenter.getWeatherData(null, latitude, longitude)
        then(testView).should().hideError()
        then(testView).should().hideLoading()
        then(testView).should().cancelDialog()
        verify(testView, never()).displayWeatherData(weatherData)
        then(testView).should().displayError(com.nhaarman.mockitokotlin2.any())
        then(testView).shouldHaveNoMoreInteractions()
    }

}