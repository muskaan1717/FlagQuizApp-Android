package com.flagquiz.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class FlagQuizApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Turn on logging for testing
        Timber.plant(Timber.DebugTree())
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                // Allows Coil to download and paint vector SVG flags cleanly
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}