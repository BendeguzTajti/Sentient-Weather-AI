package com.codecool.swai

import com.codecool.swai.model.DataManager
import com.codecool.swai.presenter.WeatherPresenter
import org.koin.dsl.module

val appModule = module {
    single { DataManager() }
    single { WeatherPresenter(get()) }
}