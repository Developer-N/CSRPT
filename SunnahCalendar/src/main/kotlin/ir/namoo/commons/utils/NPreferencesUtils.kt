package ir.namoo.commons.utils

import android.content.Context
import android.content.SharedPreferences

val Context.appPrefsLite: SharedPreferences
    get() = getSharedPreferences("lite_prefs", Context.MODE_PRIVATE)
