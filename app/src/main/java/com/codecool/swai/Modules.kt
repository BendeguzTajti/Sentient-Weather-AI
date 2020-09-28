package com.codecool.swai

import android.content.Context
import com.codecool.swai.model.DataManager
import com.codecool.swai.model.WeatherManager
import com.codecool.swai.presenter.WeatherPresenter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { DataManager(androidContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)) as WeatherManager }
    single { WeatherPresenter(get()) }
}