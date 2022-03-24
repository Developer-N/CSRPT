package com.byagowi.persiancalendar

import androidx.multidex.MultiDexApplication
import com.byagowi.persiancalendar.global.initGlobal
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        initGlobal(applicationContext) // mostly used for things should be provided in locale level
    }
}
