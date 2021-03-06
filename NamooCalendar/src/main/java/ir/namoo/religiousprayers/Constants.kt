package ir.namoo.religiousprayers


const val appLink = "\r\n http://namoo.ir/pt"//namoo.ir
//const val appLink = "\r\n https://bit.ly/2JB7Rz7"//cafebazar
//const val appLink="\r\n https://bit.ly/2juUc1y"//myket
//const val appLink = "\r\n https://bit.ly/3fUiP3L"//charkhoneh

const val LOCATION_PERMISSION_REQUEST_CODE = 23
const val STORAGE_PERMISSION_REQUEST_CODE = 24
const val CALENDAR_READ_PERMISSION_REQUEST_CODE = 55

const val REQ_CODE_PICK_ATHAN_FILE = 6236
const val REQ_CODE_PICK_FAJR_FILE = 6237
const val REQ_CODE_PICK_ALARM_FILE = 6238

const val LANG_FA = "fa"
const val LANG_FA_AF = "fa-AF"
const val LANG_PS = "ps"
const val LANG_GLK = "glk"
const val LANG_AR = "ar"
const val LANG_EN_IR = "en"
const val LANG_FR = "fr"
const val LANG_ES = "es"
const val LANG_EN_US = "en-US"
const val LANG_JA = "ja"
const val LANG_AZB = "azb"
const val LANG_CKB = "ckb"
const val LANG_UR = "ur"

const val LAST_CHOSEN_TAB_KEY = "LastChosenTab"

const val PREF_MAIN_CALENDAR_KEY = "mainCalendarType"
const val PREF_OTHER_CALENDARS_KEY = "otherCalendarTypes"
const val PREF_PRAY_TIME_METHOD = "SelectedPrayTimeMethod"
const val PREF_ASR_JURISTICS = "asrJuristics"
const val PREF_NOTIFICATION_METHOD = "notification_method"
const val PREF_FULL_SCREEN_METHOD = "full_screen_method"
const val PREF_ISLAMIC_OFFSET = "islamic_offset"
const val PREF_LATITUDE = "Latitude"
const val PREF_LONGITUDE = "Longitude"
const val PREF_SELECTED_LOCATION = "Location"
const val PREF_GEOCODED_CITYNAME = "cityname"
const val PREF_ALTITUDE = "Altitude"
const val PREF_WIDGET_IN_24 = "WidgetIn24"
const val PREF_IRAN_TIME = "IranTime"
const val PREF_PERSIAN_DIGITS = "PersianDigits"
const val PREF_SHOW_DEVICE_CALENDAR_EVENTS = "showDeviceCalendarEvents"
const val PREF_WIDGET_CLOCK = "WidgetClock"
const val PREF_CENTER_ALIGN_WIDGETS = "CenterAlignWidgets"
const val PREF_WHAT_TO_SHOW_WIDGETS = "what_to_show"
const val PREF_NUMERICAL_DATE_PREFERRED = "numericalDatePreferred"
const val PREF_ASTRONOMICAL_FEATURES = "astronomicalFeatures"
const val PREF_SHOW_WEEK_OF_YEAR_NUMBER = "showWeekOfYearNumber"
const val PREF_NOTIFY_DATE = "NotifyDate"
const val PREF_NOTIFY_DATE_LOCK_SCREEN = "NotifyDateLockScreen"
const val PREF_APP_LANGUAGE = "AppLanguage"
const val PREF_EASTERN_GREGORIAN_ARABIC_MONTHS = "EasternGregorianArabicMonths"
const val PREF_SELECTED_WIDGET_TEXT_COLOR = "SelectedWidgetTextColor"
const val PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR = "SelectedWidgetNextAthanTextColor"
const val PREF_SELECTED_WIDGET_BACKGROUND_COLOR = "SelectedWidgetBackgroundColor"
const val PREF_SELECTED_DATE_AGE_WIDGET = "SelectedDateForAgeWidget"
const val PREF_TITLE_AGE_WIDGET = "TitleForAgeWidget"
const val PREF_ATHAN_GAP = "AthanGap"
const val PREF_THEME = "Theme"
const val PREF_HOLIDAY_TYPES = "holiday_types"
const val PREF_WEEK_START = "WeekStart"
const val PREF_WEEK_ENDS = "WeekEnds"
const val PREF_SHIFT_WORK_STARTING_JDN = "ShiftWorkJdn"
const val PREF_SHIFT_WORK_SETTING = "ShiftWorkSetting"
const val PREF_SHIFT_WORK_RECURS = "ShiftWorkRecurs"
const val PREF_DISABLE_OWGHAT = "DisableOwghat"
const val PREF_APP_FONT = "app_font"
const val PREF_FIRST_START = "first_start"
const val PREF_SUMMER_TIME = "summer_time"
const val PREF_ENABLE_EDIT = "enable_edit"
const val PREF_INSTALL_INFO_SENT = "install_info_sent"
const val PREF_PHONE_ID_IN_SERVER = "phone_id_in_server"
const val PREF_LAST_INSTALL_SEND_TIME = "last_install_time"
const val PREF_LAST_UPDATE_CHECK = "last_update_check"


const val NAVIGATE_TO_UD = "ir.namoo.srp.ud"


//const val CHANGE_LANGUAGE_IS_PROMOTED_ONCE = "CHANGE_LANGUAGE_IS_PROMOTED_ONCE"

const val DEFAULT_CITY = "CUSTOM"
const val DEFAULT_PRAY_TIME_METHOD = "Tehran"
const val DEFAULT_ASR_JURISTICS = "Standard"
const val DEFAULT_NOTIFICATION_METHOD = 2
const val DEFAULT_FULL_SCREEN_METHOD = 1
const val DEFAULT_APP_LANGUAGE = "fa"
const val DEFAULT_SELECTED_WIDGET_TEXT_COLOR = "#ffffffff"
const val DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR = "#FF9800"
const val DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR = "#00000000"
const val DEFAULT_WIDGET_IN_24 = true
const val DEFAULT_IRAN_TIME = false
const val DEFAULT_PERSIAN_DIGITS = true
const val DEFAULT_WIDGET_CLOCK = true
const val DEFAULT_NOTIFY_DATE = true
const val DEFAULT_NOTIFY_DATE_LOCK_SCREEN = true
const val DEFAULT_WEEK_START = "0"
const val DEFAULT_ISLAMIC_OFFSET = "0"

// WeekEnds, 6 means Friday
val DEFAULT_WEEK_ENDS = setOf("6")

const val LIGHT_THEME = "LightTheme"
const val DARK_THEME = "DarkTheme"
const val CYAN_THEME = "CyanTheme"
const val PURPLE_THEME = "PurpleTheme"
const val DEEP_PURPLE_THEME = "DeepPurpleTheme"
const val INDIGO_THEME = "IndigoTheme"
const val PINK_THEME = "PinkTheme"
const val GREEN_THEME = "GreenTheme"
const val BROWN_THEME = "BrownTheme"
const val NEW_BLUE_THEME = "NewBlueTheme"
const val BLUE_THEME = "BlueTheme"
const val MODERN_THEME = "ClassicTheme" // don't change it, for legacy reasons
const val SYSTEM_DEFAULT_THEME = "SystemDefault"

const val LOAD_APP_ID = 1000
const val THREE_HOURS_APP_ID = 1010
const val ALARMS_BASE_ID = 2000

//const val OFFSET_ARGUMENT = "OFFSET_ARGUMENT"
const val BROADCAST_ALARM = "BROADCAST_ALARM"
const val BROADCAST_RESTART_APP = "BROADCAST_RESTART_APP"
const val BROADCAST_UPDATE_APP = "BROADCAST_UPDATE_APP"
const val KEY_EXTRA_PRAYER_KEY = "prayer_name"
const val KEY_EXTRA_PRAYER_TIME = "prayer_time"
const val SYSTEM_DEFAULT_FONT = "fonts/Vazir.ttf"

const val RLM = '\u200F'
const val ZWJ = "\u200D"

const val DEFAULT_AM = "ق.ظ"
const val DEFAULT_PM = "ب.ظ"

