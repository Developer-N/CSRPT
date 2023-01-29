package com.byagowi.persiancalendar

import androidx.multidex.MultiDexApplication
import com.byagowi.persiancalendar.global.initGlobal
import ir.namoo.commons.koin.koinModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        initGlobal(applicationContext) // mostly used for things should be provided in locale level
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(koinModule)
        }
    }
}
