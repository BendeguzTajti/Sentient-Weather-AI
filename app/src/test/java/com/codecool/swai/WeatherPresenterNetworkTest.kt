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
        val locationTest = Location(37.35, 59.61)
        given(testWeatherDataManager.getWeatherDataByCoordinates(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble()))
            .willReturn(Single.just(weatherSuccess))
        presenter.getWeatherData(locationTest)
        then(testView).should().hideError()
        then(testView).should().hideLoading()
        then(testView).should().cancelSpeechDialog()
        then(testView).should().displayWeatherData(weatherSuccess)
        verify(testView, never()).displayError(com.nhaarman.mockitokotlin2.any())
        then(testView).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getWeatherData_failed_network_call() {
        val locationTest = Location(0.0, 0.0)
        given(testWeatherDataManager.getWeatherDataByCoordinates(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble()))
            .willReturn(Single.error(Exception()))
        presenter.getWeatherData(locationTest)
        verify(testView, never()).hideError()
        then(testView).should().hideLoading()
        then(testView).should().cancelSpeechDialog()
        then(testView).should().displayError(com.nhaarman.mockitokotlin2.any())
        then(testView).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getWeatherData_wrong_weather_data() {
        val locationTest = Location(0.0, 0.0)
        given(testWeatherDataManager.getWeatherDataByCoordinates(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble()))
            .willReturn(Single.just(weatherFailure))
        presenter.getWeatherData(locationTest)
        then(testView).should().hideError()
        then(testView).should().hideLoading()
        then(testView).should().cancelSpeechDialog()
        verify(testView, never()).displayWeatherData(weatherFailure)
        then(testView).should().displayError(com.nhaarman.mockitokotlin2.any())
        then(testView).shouldHaveNoMoreInteractions()
    }

}