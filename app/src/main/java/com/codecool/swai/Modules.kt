package com.codecool.swai

import com.codecool.swai.model.DataManager
import com.codecool.swai.model.WeatherManager
import com.codecool.swai.presenter.WeatherPresenter
import org.koin.dsl.module

val appModule = module {
    single { DataManager() as WeatherManager }
    single { WeatherPresenter(get()) }
}