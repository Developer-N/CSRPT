package ir.namoo.commons

import com.byagowi.persiancalendar.BuildConfig

const val BASE_API_URL = "https://namoodev.ir/api/v1"

val APP_LINK = when (BuildConfig.FLAVOR) {
    "namooIR" -> "\r\n https://namoodev.ir/pt"//namoo.ir
    "cafebazar" -> "\r\n https://zaya.io/deft7"//cafebazar
    "myket" -> "\r\n https://zaya.io/62dnv"//myket
    else -> "\r\n https://zaya.io/jkebd"//charkhoneh
}

const val PREF_ENABLE_EDIT = "enable_edit"
const val PREF_SUMMER_TIME = "summer_time"
const val DEFAULT_SUMMER_TIME = false
const val PREF_FIRST_START = "first_start"

const val PREF_LAST_UPDATE_CHECK = "last_update_check"

const val PREF_AZKAR_REINDER = "azkar_reminder"
const val PREF_AZKAR_LANG = "azkar_lang"
const val DEFAULT_AZKAR_LANG = "fa"

const val KEY_AZKAR_EXTRA_NAME = "azkar_name"
const val KEY_AZKAR_EXTRA_TIME = "azkar_time"
const val BROADCAST_AZKAR = "BROADCAST_AZKAR"

const val REQ_CODE_PICK_ATHAN_FILE = 1
const val REQ_CODE_PICK_ALARM_FILE = 2

const val FILE_PICKER_REQUEST_CODE = "req_code"

const val PREF_APP_FONT = "app_font"
const val SYSTEM_DEFAULT_FONT = "fonts/Vazirmatn.ttf"

const val PREF_JUMMA_SILENT = "jumma_silent"
const val PREF_JUMMA_SILENT_MINUTE = "jumma_silent_minute"
const val DEFAULT_JUMMA_SILENT_MINUTE = 40

const val PREF_SHOW_SYSTEM_RINGTONES = "show_system_ringtones"

const val PREF_AUTO_SCROLL = "auto_scroll"

const val LAST_PLAYED_BEFORE_ATHAN_KEY = "last_before_athan_key"
const val LAST_PLAYED_AFTER_ATHAN_KEY = "last_after_athan_key"


const val PREF_LAST_UPDATE_PRAY_TIMES_KEY = "last_update_pray_times_key"
