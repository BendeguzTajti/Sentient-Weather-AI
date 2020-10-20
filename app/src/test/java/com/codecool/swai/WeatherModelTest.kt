package com.codecool.swai

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class WeatherModelTest {

    private val fakeWeatherData = weatherSuccess

    @ExperimentalStdlibApi
    @Test
    fun location_name() {
        assertEquals("Budapest", fakeWeatherData.current.getLocationName())
    }

    @Test
    fun temp_celsius() {
        assertEquals("29°C", fakeWeatherData.current.main.getTemp("Celsius"))
    }

    @Test
    fun temp_fahrenheit() {
        assertEquals("85°F", fakeWeatherData.current.main.getTemp("Fahrenheit"))
    }

}