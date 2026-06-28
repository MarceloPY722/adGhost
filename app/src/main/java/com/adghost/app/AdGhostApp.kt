package com.adghost.app

import android.app.Application
import com.adghost.app.util.NativeAdBlockEngine
import timber.log.Timber

class AdGhostApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        NativeAdBlockEngine.init(this)
    }
}