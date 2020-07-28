package com.codecool.swai.api

import com.codecool.swai.model.WeatherCurrent
import com.codecool.swai.model.WeatherForecast
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather")
    fun getCurrentWeather(@Query("lat") lat: Double,
                          @Query("lon") lon: Double,
                          @Query("APPID") APPID: String,
                          @Query("lang") lang: String): Single<WeatherCurrent.Result>

    @GET("onecall")
    fun getWeatherForecast(@Query("lat") lat: Double,
                           @Query("lon") lon: Double,
                           @Query("exclude") exclude: String,
                           @Query("APPID") APPID: String,
                           @Query("lang") lang: String): Single<WeatherForecast.Result>

    companion object {

        private const val BASE_URL = "http://api.openweathermap.org/data/2.5/"

        fun create(): WeatherApiService {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(WeatherApiService::class.java)
        }
    }
}