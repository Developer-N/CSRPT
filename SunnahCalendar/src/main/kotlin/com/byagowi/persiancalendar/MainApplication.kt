package com.byagowi.persiancalendar

import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.multidex.MultiDexApplication
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.configureCalendarsAndLoadEvents
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.global.loadLanguageResources
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.update
import ir.namoo.commons.LAST_PLAYED_AFTER_ATHAN_KEY
import ir.namoo.commons.LAST_PLAYED_BEFORE_ATHAN_KEY
import ir.namoo.commons.koin.koinModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : MultiDexApplication(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            androidLogger(level = Level.DEBUG)
            modules(koinModule)
        }
        initGlobal(applicationContext) // mostly used for things should be provided in locale level
        preferences.registerOnSharedPreferenceChangeListener(this)
        update(this, true)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_TILE_STATE -> return // tile service is self contained, nothing needs to be updated
            PREF_LAST_APP_VISIT_VERSION -> return // nothing needs to be updated
            EXPANDED_TIME_STATE_KEY -> return // nothing needs to be updated
            LAST_PLAYED_ATHAN_JDN, LAST_PLAYED_ATHAN_KEY, LAST_PLAYED_BEFORE_ATHAN_KEY,
            LAST_PLAYED_AFTER_ATHAN_KEY -> return // nothing needs to be updated
            LAST_CHOSEN_TAB_KEY -> return // don't run the expensive update and etc on tab changes
            PREF_ISLAMIC_OFFSET -> {
                this.preferences.edit { putJdn(PREF_ISLAMIC_OFFSET_SET_DATE, Jdn.today()) }
            }

            PREF_PRAY_TIME_METHOD -> this.preferences.edit { remove(PREF_MIDNIGHT_METHOD) }
            PREF_NOTIFY_DATE -> {
                if (!this.preferences.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)) {
                    stopService(Intent(this, ApplicationService::class.java))
                    startWorker(applicationContext)
                }
            }
        }

        configureCalendarsAndLoadEvents(this)
        updateStoredPreference(this)

        if (key == PREF_APP_LANGUAGE) {
            applyAppLanguage(this)
            loadLanguageResources(this.resources)
        }

        if (key == PREF_EASTERN_GREGORIAN_ARABIC_MONTHS ||
            key == PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS ||
            key == PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS
        ) {
            loadLanguageResources(this.resources)
        }

        update(this, true)
    }
}
