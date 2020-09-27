package com.codecool.swai

import android.app.Application
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BaseApp : Application() {

    companion object {
        var dayBackground: LottieComposition? = null
        var nightBackground: LottieComposition? = null
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BaseApp)
            modules(appModule)
        }
        preLoadBackgrounds()
    }

    private fun preLoadBackgrounds() {
        LottieCompositionFactory.fromAssetSync(this, "day_background.json").value?.let { dayBackground = it }
        LottieCompositionFactory.fromAssetSync(this, "night_background.json").value?.let { nightBackground = it }
    }

}