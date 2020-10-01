package com.codecool.swai

import android.content.Context
import android.location.Geocoder
import com.codecool.swai.model.LocationDataManager
import com.codecool.swai.model.LocationManager
import com.codecool.swai.model.WeatherDataManager
import com.codecool.swai.model.WeatherManager
import com.codecool.swai.presenter.WeatherPresenter
import com.google.android.gms.location.LocationServices
import me.ibrahimsn.library.LiveSharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { androidContext().getSharedPreferences("userPref", Context.MODE_PRIVATE) }
    single { WeatherDataManager(get()) as WeatherManager }
    single { LocationDataManager(Geocoder(androidContext()), LocationServices.getFusedLocationProviderClient(androidContext())) as LocationManager }
    single { WeatherPresenter(get(), get()) }
    single { LiveSharedPreferences(get()) }
}