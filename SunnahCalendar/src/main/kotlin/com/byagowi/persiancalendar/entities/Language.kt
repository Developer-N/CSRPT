package com.byagowi.persiancalendar.entities

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.AFGHANISTAN_TIMEZONE_ID
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.NEPAL_TIMEZONE_ID
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.listOf12Items
import com.byagowi.persiancalendar.utils.listOf7Items
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugLog
import io.github.cosinekitty.astronomy.EclipseKind
import io.github.persiancalendar.praytimes.CalculationMethod
import org.jetbrains.annotations.VisibleForTesting
import java.util.Locale
import java.util.TimeZone


enum class Language(val code: String, val nativeName: String) {
    // The following order is used for language change dialog also
    // Official languages
    FA("fa", "فارسی"),
    FA_AF("fa-AF", "دری"),
    PS("ps", "پښتو"),

    // Rest, sorted by their language code
    AR("ar", "العربية"),
    AZB("azb", "تۆرکجه"),
    CKB("ckb", "کوردی"),
    EN_IR("en", "English (Iran)"),
    EN_US("en-US", "English"),
    ES("es", "Español"),
    FR("fr", "Français"),
    GLK("glk", "گيلکي"),
    IT("it", "Italiano"),
    JA("ja", "日本語"),
    KMR("kmr", "Kurdî"),
    NE("ne", "नेपाली"),
    PT("pt", "Português"),
    RU("ru", "Русский"),
    TA("ta", "தமிழ்"),
    TG("tg", "Тоҷикӣ"),
    TR("tr", "Türkçe"),
    UR("ur", "اردو"),
    ZH_CN("zh-CN", "中文");

    val isArabic get() = this == AR
    val isDari get() = this == FA_AF
    val isPersian get() = this == FA
    val isNepali get() = this == NE

    val showNepaliCalendar get() = this == NE

    val language get() = code.replace(Regex("-(IR|AF|US|CN)"), "")

    // en-IR and fa-AF aren't recognized by system, that's handled by #language
    fun asSystemLocale() = Locale(language)

    val inParentheses: String
        get() = when (this) {
            JA, ZH_CN -> "%s（%s）"
            else -> "%s (%s)"
        }

    // Formatting "Day Month Year" considerations
    val dmy: String
        get() = when (this) {
            CKB -> "%1\$sی %2\$sی %3\$s"
            ZH_CN -> "%3\$s 年 %2\$s %1\$s 日"
            else -> "%1\$s %2\$s %3\$s"
        }
    val dm: String
        get() = when (this) {
            CKB -> "%1\$sی %2\$s"
            JA, ZH_CN -> "%2\$s %1\$s"
            else -> "%1\$s %2\$s"
        }
    val my: String
        get() = when (this) {
            CKB -> "%1\$sی %2\$s"
            ZH_CN -> "%2\$s 年 %1\$s"
            else -> "%1\$s %2\$s"
        }
    val timeAndDateFormat: String
        get() = when (this) {
            JA, ZH_CN -> "%2\$s %1\$s"
            else -> "%1\$s$spacedComma%2\$s"
        }
    val clockAmPmOrder: String
        get() = when (this) {
            ZH_CN -> "%2\$s %1\$s"
            else -> "%1\$s %2\$s"
        }

    val isLessKnownRtl: Boolean
        get() = when (this) {
            AZB, GLK -> true
            else -> false
        }

    val betterToUseShortCalendarName: Boolean
        get() = when (this) {
            EN_US, EN_IR, JA, ZH_CN, FR, ES, PT, IT, AR, TR, TG, RU, CKB -> true
            else -> false
        }

    val mightPreferUmmAlquraIslamicCalendar: Boolean
        get() = when (this) {
            FA_AF, PS, UR, AR, CKB, EN_US, JA, ZH_CN, FR, ES, PT, IT, TR, KMR, TA, TG, NE, RU -> true
            else -> false
        }

    val preferredCalculationMethod: CalculationMethod
        get() = when (this) {
            FA_AF, PS, UR, AR, CKB, TR, KMR, TG, NE, TA -> CalculationMethod.MWL
            else -> CalculationMethod.Tehran
        }

    val isHanafiMajority: Boolean
        get() = when (this) {
            TR, FA_AF, PS, TG, NE, TA -> true
            else -> false
        }

    // Based on locale, we can presume user is able to read Persian
    val isUserAbleToReadPersian: Boolean
        get() = when (this) {
            FA, GLK, AZB, FA_AF, EN_IR -> true
            else -> false
        }

    val showIranTimeOption
        get() = when (this) {
            FA, AZB, CKB, EN_IR, EN_US, GLK -> true
            else -> false
        }

    // Whether locale uses الفبا or not
    val isArabicScript: Boolean
        get() = when (this) {
            EN_US, JA, ZH_CN, FR, ES, PT, IT, RU, TR, KMR, EN_IR, TG, NE, TA -> false
            else -> true
        }

    // Whether locale would prefer local digits like ۱۲۳ over the global ones, 123, initially at least
    val prefersLocalDigits: Boolean
        get() = when (this) {
            UR, EN_IR, EN_US, JA, ZH_CN, FR, ES, PT, IT, RU, TR, KMR, TG -> false
            else -> true
        }

    // Whether the language doesn't need " and " between date parts or not
    val languagePrefersHalfSpaceAndInDates: Boolean
        get() = when (this) {
            JA, ZH_CN -> true
            else -> false
        }

    // Local digits (۱۲۳) make sense for the locale
    val canHaveLocalDigits get() = isArabicScript || isNepali

    // Prefers ٤٥٦ over ۴۵۶
    val preferredDigits
        get() = when (this) {
            AR, CKB -> ARABIC_INDIC_DIGITS
            NE -> DEVANAGARI_DIGITS
            else -> PERSIAN_DIGITS
        }

    // We can presume user is from Afghanistan
    val isAfghanistanExclusive: Boolean
        get() = when (this) {
            FA_AF, PS -> true
            else -> false
        }

    // We can presume user is from Iran
    val isIranExclusive: Boolean
        get() = when (this) {
            AZB, GLK, FA, EN_IR -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    private val prefersGregorianCalendar: Boolean
        get() = when (this) {
            EN_US, JA, ZH_CN, FR, ES, PT, IT, RU, UR, TR, KMR, TG, TA -> true
            else -> false
        }

    private val prefersNepaliCalendar: Boolean
        get() = isNepali

    // We can presume user would prefer Gregorian calendar at least initially
    private val prefersIslamicCalendar: Boolean
        get() = when (this) {
            AR -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    private val prefersPersianCalendar: Boolean
        get() = when (this) {
            AZB, GLK, FA, FA_AF, PS, EN_IR -> true
            else -> false
        }

    val defaultMainCalendar
        get() = when {
            this == FA -> Calendar.SHAMSI.name
            prefersGregorianCalendar -> Calendar.GREGORIAN.name
            prefersIslamicCalendar -> Calendar.ISLAMIC.name
            prefersPersianCalendar -> Calendar.SHAMSI.name
            prefersNepaliCalendar -> Calendar.NEPALI.name
            else -> Calendar.SHAMSI.name
        }

    val defaultOtherCalendars
        get() = when {
            this == FA -> "${Calendar.GREGORIAN.name},${Calendar.ISLAMIC.name}"
            prefersGregorianCalendar -> "${Calendar.ISLAMIC.name},${Calendar.SHAMSI.name}"
            prefersIslamicCalendar -> "${Calendar.GREGORIAN.name},${Calendar.SHAMSI.name}"
            prefersPersianCalendar -> "${Calendar.GREGORIAN.name},${Calendar.ISLAMIC.name}"
            prefersNepaliCalendar -> Calendar.GREGORIAN.name
            else -> "${Calendar.GREGORIAN.name},${Calendar.ISLAMIC.name}"
        }

    val defaultWeekStart
        get() = when (this) {
            FA -> "0"
            TR, TG, RU -> "2" // Monday
            else -> if (prefersGregorianCalendar || isNepali) "1"/*Sunday*/ else "0"
        }

    val defaultWeekEnds
        get() = when {
            this == FA || isIranExclusive -> setOf("6")
            isAfghanistanExclusive -> setOf("6")
            isNepali -> setOf("0")
            prefersGregorianCalendar -> setOf("0", "1") // Saturday and Sunday
            else -> setOf("6") // 6 means Friday
        }

    val additionalShiftWorkTitles: List<String>
        get() = when (this) {
            FA -> listOf("مرخصی", "صبح/شب", "صبح/عصر", "عصر/شب")
            else -> emptyList()
        }

    fun getPersianMonths(resources: Resources): List<String> = when (this) {
        FA -> persianCalendarMonthsInPersian
        FA_AF -> persianCalendarMonthsInDari
        AR -> if (userTimeZoneId == IRAN_TIMEZONE_ID) persianCalendarMonthsInArabicIran
        else persianCalendarMonths.map(resources::getString)

        else -> persianCalendarMonths.map(resources::getString)
    }

    fun getIslamicMonths(resources: Resources): List<String> = when (this) {
        FA, FA_AF -> islamicCalendarMonthsInPersian
        else -> islamicCalendarMonths.map(resources::getString)
    }

    fun getGregorianMonths(resources: Resources, alternativeGregorianMonths: Boolean) =
        when (this) {
            FA -> {
                if (alternativeGregorianMonths) gregorianCalendarMonthsInPersianEnglishPronunciation
                else gregorianCalendarMonthsInPersian
            }

            FA_AF -> gregorianCalendarMonthsInDari

            AR -> {
                if (alternativeGregorianMonths) easternGregorianCalendarMonths
                else gregorianCalendarMonths.map(resources::getString)
            }

            else -> gregorianCalendarMonths.map(resources::getString)
        }

    fun getNepaliMonths(): List<String> = when (this) {
        NE -> nepaliMonths
        else -> nepaliMonthsInEnglish
    }

    fun getWeekDays(resources: Resources): List<String> = when (this) {
        FA, FA_AF -> weekDaysInPersian
        EN_IR -> weekDaysInEnglishIran
        else -> weekDays.map(resources::getString)
    }

    fun getWeekDaysInitials(resources: Resources): List<String> = when (this) {
        FA, FA_AF -> weekDaysInitialsInPersian
        EN_IR -> weekDaysInitialsInEnglishIran
        else -> weekDaysInitials.map(resources::getString)
    }

    fun getCountryName(cityItem: CityItem): String = when {
        !isArabicScript -> cityItem.countryEn
        isArabic -> cityItem.countryAr
        this == CKB -> cityItem.countryCkb
        else -> cityItem.countryFa
    }

    fun getCityName(cityItem: CityItem): String = when {
        !isArabicScript -> cityItem.en
        isArabic -> cityItem.ar
        this == CKB -> cityItem.ckb
        else -> cityItem.fa
    }

    val countriesOrder
        get() = when {
            isAfghanistanExclusive -> afCodeOrder
            isArabic -> arCodeOrder
            this == TR || this == KMR -> trCodeOrder
            else -> irCodeOrder
        }

    // Some languages don't have alphabet order matching with unicode order, this fixes them
    fun prepareForSort(text: String) = when {
        isArabicScript && !isArabic -> prepareForArabicSort(text)
        // We will need some preparation for non-English latin script
        // languages (Turkish, Spanish, French, ...) but our cities.json
        // don't have those a translation to those, so
        else -> text
    }

    // Too hard to translate and don't want to disappoint translators thus not in the common
    // i18n system yet
    fun tryTranslateEclipseType(isSolar: Boolean, type: EclipseKind) = when (this) {
        EN_US, EN_IR -> {
            when {
                isSolar && type == EclipseKind.Annular -> "Annular solar eclipse"
                isSolar && type == EclipseKind.Partial -> "Partial solar eclipse"
                !isSolar && type == EclipseKind.Partial -> "Partial lunar eclipse"
                !isSolar && type == EclipseKind.Penumbral -> "Penumbral lunar eclipse"
                isSolar && type == EclipseKind.Total -> "Total solar eclipse"
                !isSolar && type == EclipseKind.Total -> "Total lunar eclipse"
                else -> null
            }
        }

        FA, FA_AF -> {
            when {
                isSolar && type == EclipseKind.Annular -> "خورشیدگرفتگی حلقه‌ای"
                isSolar && type == EclipseKind.Partial -> "خورشیدگرفتگی جزئی"
                !isSolar && type == EclipseKind.Partial -> "ماه‌گرفتگی جزئی"
                !isSolar && type == EclipseKind.Penumbral -> "ماه‌گرفتگی نیم‌سایه‌ای"
                isSolar && type == EclipseKind.Total -> "خورشیدگرفتگی کلی"
                !isSolar && type == EclipseKind.Total -> "ماه‌گرفتگی کلی"
                else -> null
            }
        }

        else -> null
    }

    fun tryTranslateAthanVibrationSummary() = when (this) {
        EN_US, EN_IR -> "Enable vibrator in the beginning of athan"
        FA, FA_AF -> "فعال‌سازی لرزش در ابتدای پخش اذان"
        else -> null
    }

    // https://en.wikipedia.org/wiki/List_of_date_formats_by_country
    fun allNumericsDateFormat(year: Int, month: Int, dayOfMonth: Int, digits: CharArray): String {
        val sep = when (this) {
            PS, NE -> '-'
            KMR, RU, TR -> '.'
            else -> '/'
        }
        val needsZeroPad = when (this) {
            FR, IT, NE, PT, RU, TR, KMR -> true
            else -> false
        }
        val format = when (this) {
            // Year major
            FA, AZB, GLK, FA_AF, EN_IR, PS, JA, NE, ZH_CN -> "%1\$s$sep%2\$s$sep%3\$s"
            // Month major
            EN_US -> "%2\$s$sep%3\$s$sep%1\$s"
            // Day major, most likely everything else goes here but check via JS'
            // new Date().toLocaleDateString('XX')
            AR, CKB, ES, FR, IT, KMR, PT, RU, TG, TR, UR, TA -> "%3\$s$sep%2\$s$sep%1\$s"
        }
        return format.format(
            formatNumber(year, digits),
            formatNumber(
                "$month".let { if (needsZeroPad) it.padStart(2, '0') else it },
                digits
            ),
            formatNumber(
                "$dayOfMonth".let { if (needsZeroPad) it.padStart(2, '0') else it },
                digits
            )
        )
    }

    companion object {
        @SuppressLint("ConstantLocale")
        val userDeviceLanguage = Locale.getDefault().language ?: "en"

        @SuppressLint("ConstantLocale")
        private val userDeviceCountry = Locale.getDefault().country ?: "IR"

        private val userTimeZoneId = TimeZone.getDefault().id ?: IRAN_TIMEZONE_ID

        // Preferred app language for certain locale
        fun getPreferredDefaultLanguage(context: Context): Language {
            return when (userDeviceLanguage) {
                FA.code -> if (userDeviceCountry == "AF") FA_AF else FA

                "en", EN_US.code ->
                    guessLanguageFromTimezoneId() ?: guessLanguageFromKeyboards(context)

                else -> valueOfLanguageCode(userDeviceLanguage) ?: EN_US
            }
        }

        private fun guessLanguageFromTimezoneId(): Language? = when (userTimeZoneId) {
            IRAN_TIMEZONE_ID -> FA
            AFGHANISTAN_TIMEZONE_ID -> FA_AF
            NEPAL_TIMEZONE_ID -> NE
            // Other than these specific zones let's respect user device language anyway
            else -> null
        }

        // Based on https://stackoverflow.com/a/28216764 but doesn't seem to work
        private fun guessLanguageFromKeyboards(context: Context): Language = runCatching {
            val imm = context.getSystemService<InputMethodManager>() ?: return EN_US
            imm.enabledInputMethodList.forEach outer@{ method ->
                imm.getEnabledInputMethodSubtypeList(method, true).forEach { submethod ->
                    if (submethod.mode == "keyboard") {
                        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            submethod.languageTag
                        } else @Suppress("DEPRECATION") submethod.locale
                        debugLog("Language: '$locale' is available in keyboards")
                        if (locale.isEmpty()) return@forEach
                        val language = valueOfLanguageCode(locale)
                            ?: valueOfLanguageCode(locale.split("-").firstOrNull() ?: "")
                        // Use the knowledge only to detect Persian language
                        // as others might be surprising
                        if (language == FA || language == FA_AF) return language
                    }
                }
            }
            return EN_US
        }.onFailure(logException).getOrNull() ?: EN_US

        fun valueOfLanguageCode(languageCode: String) = entries.find { it.code == languageCode }

        private val arabicSortReplacements = mapOf(
            'ی' to "ي",
            'ک' to "ك",
            'گ' to "كی",
            'ژ' to "زی",
            'چ' to "جی",
            'پ' to "بی",
            'و' to "نی",
            'ڕ' to "ری",
            'ڵ' to "لی",
            'ڤ' to "فی",
            'ۆ' to "وی",
            'ێ' to "یی",
            'ھ' to "نی",
            'ە' to "هی",
        )

        @VisibleForTesting
        fun prepareForArabicSort(text: String): String =
            text.map { arabicSortReplacements[it] ?: it }.joinToString("")

        private val persianCalendarMonths = listOf12Items(
            R.string.farvardin, R.string.ordibehesht, R.string.khordad,
            R.string.tir, R.string.mordad, R.string.shahrivar,
            R.string.mehr, R.string.aban, R.string.azar, R.string.dey,
            R.string.bahman, R.string.esfand
        )
        private val islamicCalendarMonths = listOf12Items(
            R.string.muharram, R.string.safar, R.string.rabi_al_awwal,
            R.string.rabi_al_thani, R.string.jumada_al_awwal, R.string.jumada_al_thani,
            R.string.rajab, R.string.shaban, R.string.ramadan, R.string.shawwal,
            R.string.dhu_al_qidah, R.string.dhu_al_hijjah
        )
        private val gregorianCalendarMonths = listOf12Items(
            R.string.january, R.string.february, R.string.march,
            R.string.april, R.string.may, R.string.june, R.string.july,
            R.string.august, R.string.september, R.string.october,
            R.string.november, R.string.december
        )
        private val weekDays = listOf7Items(
            R.string.saturday, R.string.sunday, R.string.monday, R.string.tuesday,
            R.string.wednesday, R.string.thursday, R.string.friday
        )
        private val weekDaysInitials = listOf7Items(
            R.string.saturday_short, R.string.sunday_short, R.string.monday_short,
            R.string.tuesday_short, R.string.wednesday_short, R.string.thursday_short,
            R.string.friday_short
        )

        // These are special cases and new ones should be translated in strings.xml of the language
        private val persianCalendarMonthsInPersian = listOf12Items(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد",
            "شهریور", "مهر", "آبان", "آذر", "دی",
            "بهمن", "اسفند"
        )
        private val persianCalendarMonthsInArabicIran = listOf12Items(
            "فروردین", "أرديبهشت", "خرداد", "تير", "مرداد",
            "شهريور", "مهر", "آبان", "آذر", "دي", "بهمن", "إسفند",
        )
        private val islamicCalendarMonthsInPersian = listOf12Items(
            "مُحَرَّم", "صَفَر", "ربیع‌الاول", "ربیع‌الثانی", "جمادى‌الاولى", "جمادی‌الثانیه",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
        )
        private val gregorianCalendarMonthsInPersian = listOf12Items(
            "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
            "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
        )
        private val persianCalendarMonthsInDari = listOf12Items(
            "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله",
            "میزان", "عقرب", "قوس", "جدی", "دلو", "حوت"
        )
        private val gregorianCalendarMonthsInDari = listOf12Items(
            "جنوری", "فبروری", "مارچ", "اپریل", "می", "جون",
            "جولای", "آگست", "سپتامبر", "اکتوبر", "نوامبر", "دسامبر"
        )
        private val gregorianCalendarMonthsInPersianEnglishPronunciation = listOf12Items(
            "جنوری", "فبروری", "مارچ", "اپریل", "می", "جون",
            "جولای", "آگوست", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
        )
        private val easternGregorianCalendarMonths = listOf12Items(
            "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران", "تموز", "آب", "أيلول",
            "تشرين الأول", "تشرين الثاني", "كانون الأول"
        )
        private val weekDaysInPersian = listOf7Items(
            // https://apll.ir/wp-content/uploads/2018/10/D-1394.pdf
            // https://www.ekhtebar.ir/wp-content/uploads/2023/07/Dastour-e-Khat-17.04.1402-3.pdf
            // advices to use یکشنبه and پنجشنبه on "مرکبهایی که بسیطگونه است"
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
        )
        private val weekDaysInitialsInPersian = listOf7Items(
            "ش", "ی", "د", "س", "چ", "پ", "ج"
        )
        private val weekDaysInEnglishIran = listOf7Items(
            "Shanbe", "Yekshanbe", "Doshanbe", "Seshanbe", "Chahaarshanbe",
            "Panjshanbe", "Jom'e"
        )
        private val weekDaysInitialsInEnglishIran = listOf7Items(
            "Sh", "Ye", "Do", "Se", "Ch", "Pa", "Jo"
        )

        // https://github.com/techgaun/ad-bs-converter/blob/4731f2c/src/converter.js
        // https://en.wikipedia.org/wiki/Vikram_Samvat
        val nepaliMonths = listOf12Items(
            "बैशाख", "जेष्ठ", "आषाढ", "श्रावण", "भाद्र", "आश्विन",
            "कार्तिक", "मंसिर", "पौष", "माघ", "फाल्गुन", "चैत्र"
        )
        val nepaliMonthsInEnglish = listOf12Items(
            "Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
            "Kartik", "Mangsir", "Paush", "Mangh", "Falgun", "Chaitra"
        )

        private val irCodeOrder = listOf("zz", "ir", "tr", "af", "iq")
        private val afCodeOrder = listOf("zz", "af", "ir", "tr", "iq")
        private val arCodeOrder = listOf("zz", "iq", "tr", "ir", "af")
        private val trCodeOrder = listOf("zz", "tr", "ir", "iq", "af")

        // See the naming here, https://developer.mozilla.org/en-US/docs/Web/CSS/list-style-type
        val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val ARABIC_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val ARABIC_INDIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val DEVANAGARI_DIGITS = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
        // CJK digits: charArrayOf('０', '１', '２', '３', '４', '５', '６', '７', '８', '９')
        // but they weren't looking nice in the UI
    }
}
