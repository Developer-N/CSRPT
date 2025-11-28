package ir.namoo.quran.utils

//const val DB_LINK = "https://namoodev.ir/storage/quran_v3.zip"
const val DB_LINK =
    "https://raw.githubusercontent.com/Developer-N/QuranProject/main/AndroidDB/quran_v3.zip"

const val PREF_QURAN_FONT = "key_quran_font"
const val DEFAULT_QURAN_FONT = "fonts/Quran_Bahij_Regular.ttf"

const val PREF_QURAN_FONT_SIZE = "key_quran_font_size"
const val DEFAULT_QURAN_FONT_SIZE = 24f

const val PREF_ENGLISH_FONT = "key_english_font"
const val DEFAULT_ENGLISH_FONT = "fonts/english_segoeui.ttf"

const val PREF_ENGLISH_FONT_SIZE = "key_english_font_size"
const val DEFAULT_ENGLISH_FONT_SIZE = 16f

const val PREF_KURDISH_FONT = "key_kurdish_font"
const val DEFAULT_KURDISH_FONT = "fonts/Vazirmatn.ttf"

const val PREF_KURDISH_FONT_SIZE = "key_kurdish_font_size"
const val DEFAULT_KURDISH_FONT_SIZE = 16f

const val PREF_FARSI_FONT = "key_farsi_font"
const val DEFAULT_FARSI_FONT = "fonts/Vazirmatn.ttf"

const val PREF_FARSI_FONT_SIZE = "key_farsi_font_size"
const val DEFAULT_FARSI_FONT_SIZE = 16f

const val PREF_FARSI_FULL_TRANSLATE = "key_farsi_translate_full"

const val PREF_PLAY_TYPE = "play_type"
const val DEFAULT_PLAY_TYPE = 1 // 1:Quran and Translate 2:Quran 3:Translate

const val PREF_STORAGE_PATH = "storage_path"

const val PREF_SELECTED_QARI = "key_selected_qari"
const val DEFAULT_SELECTED_QARI = "Alafasi"

const val PREF_TRANSLATE_TO_PLAY = "key_translate_to_play"
const val DEFAULT_TRANSLATE_TO_PLAY = "Khorramdel"

val quranFonts = listOf(
    "fonts/Quran_UthmanTahaN1B.ttf",
    "fonts/Quran_Al_Majeed.ttf",
    "fonts/Quran_Al_Mushaf.ttf",
    "fonts/Quran_Al_Qalam.ttf",
    "fonts/Quran_Hafs.ttf",
    "fonts/Quran_Me_Quran.ttf",
    "fonts/Quran_MUHAMMADI.ttf",
    "fonts/Quran_Nabi.ttf",
    "fonts/Quran_Neirizi.ttf",
    "fonts/Quran_PDMS_Saleem.ttf",
    "fonts/Quran_Bahij_Bold.ttf",
    "fonts/Quran_Bahij_Regular.ttf",
    "fonts/Quran_Taha.ttf",
    "fonts/Vazirmatn.ttf"
)
val quranFontNames = listOf(
    "Quran_UthmanTahaN1B",
    "Quran_Al_Majeed",
    "Quran_Al_Mushaf",
    "Quran_Al_Qalam",
    "Quran_Hafs",
    "Quran_Me_Quran",
    "Quran_MUHAMMADI",
    "Quran_Nabi",
    "Quran_Neirizi",
    "Quran_PDMS_Saleem",
    "Quran_Bahij_Bold",
    "Quran_Bahij_Regular",
    "Quran_Taha",
    "Vazirmatn"
)

val farsiFonts = listOf(
    "fonts/Vazirmatn.ttf", "fonts/Vazirmatn-Light.ttf"
)
val farsiFontNames = listOf(
    "Vazirmatn", "Vazirmatn-Light"
)

val kurdishFonts = listOf(
    "fonts/kurdish_KMestan.ttf",
    "fonts/kurdish_KTishk.ttf",
    "fonts/kurdish_KPenos.ttf",
    "fonts/kurdish_KChimen.ttf",
    "fonts/kurdish_KudrLav.ttf",
    "fonts/Vazirmatn.ttf",
    "fonts/Vazirmatn-Light.ttf"
)
val kurdishFontNames = listOf(
    "Mestan", "Tishk", "Penos", "Chimen", "KudrLav", "Vazirmatn", "Vazirmatn-Light"
)

val englishFonts = listOf(
    "fonts/english_segoeui.ttf"
)
val englishFontNames = listOf(
    "Segoe UI"
)

const val PREF_SEARCH_IN_TRANSLATES = "search_in_translates"
const val DEFAULT_SEARCH_IN_TRANSLATES = false

const val EXTRA_SURA = "extra_sura"
const val EXTRA_AYA = "extra_aya"

const val PREF_PLAY_NEXT_SURA = "play_next_sura"
const val DEFAULT_PLAY_NEXT_SURA = false

const val PREF_IS_SURA_VIEW_IS_OPEN = "is_sura_view_is_open"

const val PREF_PLAYER_SPEED = "player_speed"
const val DEFAULT_PLAYER_SPEED = 1f

const val PREF_BOOKMARK_VERSE = "pref_bookmark_verse"

const val PREF_PAGE_TYPE = "pref_page_type"
const val DEFAULT_PAGE_TYPE = 0

const val PREF_HIDE_TOOLBAR_ON_SCROLL = "hide_toolbar_on_scroll"

const val PREF_MUSHAF_TEXT_SIZE ="pref_mushaf_text_size"
const val DEFAULT_MUSHAF_TEXT_SIZE = 24f
