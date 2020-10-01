package com.codecool.swai

import com.codecool.swai.model.FakeWeatherData
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class WeatherModelTest {

    private val fakeWeatherData = FakeWeatherData()
    private val weatherTest = fakeWeatherData.getWeatherSuccess()

    @ExperimentalStdlibApi
    @Test
    fun location_name() {
        assertEquals("Budapest XIV. kerület", weatherTest.current.getLocationName())
    }

    @Test
    fun temp_celsius() {
        assertEquals("29°C", weatherTest.current.main.getTemp("Celsius"))
    }

    @Test
    fun temp_fahrenheit() {
        assertEquals("85°F", weatherTest.current.main.getTemp("Fahrenheit"))
    }

}