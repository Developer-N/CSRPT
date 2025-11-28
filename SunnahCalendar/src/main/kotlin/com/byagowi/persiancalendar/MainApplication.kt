package com.byagowi.persiancalendar

import androidx.compose.runtime.Composer
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.multidex.MultiDexApplication
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.utils.update
import ir.namoo.commons.koin.koinModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : MultiDexApplication() {
    @OptIn(ExperimentalComposeRuntimeApi::class)
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEVELOPMENT) Composer.setDiagnosticStackTraceEnabled(BuildConfig.DEBUG)
        startKoin {
            androidContext(this@MainApplication)
            androidLogger(level = Level.DEBUG)
            modules(koinModule)
        }
        initGlobal(applicationContext) // mostly used for things should be provided in locale level
        update(this, true)
    }
}
