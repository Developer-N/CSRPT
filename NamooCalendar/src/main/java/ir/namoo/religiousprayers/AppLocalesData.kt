package ir.namoo.religiousprayers

import ir.namoo.religiousprayers.utils.listOf12Items
import ir.namoo.religiousprayers.utils.listOf7Items

sealed interface AppLocalesData {
    val persianCalendarMonths: List<String>
    val islamicCalendarMonths: List<String>
    val gregorianCalendarMonths: List<String>
    val weekDays: List<String>

    // This is the default implementation and overridden by locales having custom initials
    val weekDaysInitials: List<String> get() = weekDays.map { it.substring(0, 1) }

    companion object {
        fun getPersianCalendarMonths(locale: String) = localeFinder(locale).persianCalendarMonths
        fun getIslamicCalendarMonths(locale: String) = localeFinder(locale).islamicCalendarMonths
        fun getGregorianCalendarMonths(locale: String, isEasternArabicMonth: Boolean) =
            when (locale) {
                LANG_AR -> {
                    if (isEasternArabicMonth) ar.easternGregorianCalendarMonths
                    else ar.gregorianCalendarMonths
                }
                else -> localeFinder(locale).gregorianCalendarMonths
            }

        fun getWeekDays(locale: String) = localeFinder(locale).weekDays
        fun getWeekDaysInitials(locale: String) = localeFinder(locale).weekDaysInitials

        private fun localeFinder(locale: String): AppLocalesData = when (locale) {
            LANG_FA_AF -> fa_af
            LANG_PS -> ps
            LANG_GLK -> glk
            LANG_AR -> ar
            LANG_CKB -> ckb
            LANG_UR -> ur
            LANG_EN_US -> en
            LANG_JA -> ja
            LANG_FR -> fr
            LANG_ES -> es
            LANG_AZB -> azb
            LANG_EN_IR, LANG_FA -> fa
            else -> fa
        }
    }

    private object fa : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی",
            "بهمن", "اسفند"
        )
        override val islamicCalendarMonths = listOf12Items(
            "مُحَرَّم", "صَفَر", "ربیع‌الاول", "ربیع‌الثانی", "جمادى‌الاولى", "جمادی‌الثانیه",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن", "ژوئیه", "اوت", "سپتامبر", "اکتبر",
            "نوامبر", "دسامبر"
        )
        override val weekDays = listOf7Items(
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
        )
    }

    private object fa_af : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله", "میزان", "عقرب", "قوس", "جدی", "دلو",
            "حوت"
        )
        override val islamicCalendarMonths = listOf12Items(
            "مُحَرَّم", "صَفَر", "ربیع‌الاول", "ربیع‌الثانی", "جمادى‌الاولى", "جمادی‌الثانیه",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "جنوری", "فبروری", "مارچ", "اپریل", "می", "جون", "جولای", "آگست", "سپتمبر", "اکتبر",
            "نومبر", "دیسمبر"
        )
        override val weekDays = listOf7Items(
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
        )
    }

    private object ps : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "وری", "غویی", "غبرګولی", "چنګاښ", "زمری", "وږی", "تله", "لړم", "لیندۍ", "مرغومی",
            "سلواغه", "کب"
        )
        override val islamicCalendarMonths = listOf12Items(
            "مُحَرَّم", "صَفَر", "ربیع‌الاول", "ربیع‌الثانی", "جمادى‌الاولى", "جمادی‌الثانیه",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "جنوری", "فبروری", "مارچ", "اپریل", "می", "جون", "جولای", "آگست", "سپتمبر", "اکتبر",
            "نومبر", "دیسمبر"
        )
        override val weekDays = listOf7Items(
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
        )
    }

    private object glk : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "فروردین", "اؤردیبهشت", "خؤرداد", "تیر", "مۊرداد", "شاریور", "مهر", "آبان", "آذر",
            "دی", "بهمن", "ايسفند"
        )
        override val islamicCalendarMonths = listOf12Items(
            "مُحَرَّم", "صَفَر", "ربيع الأول", "ربیع الثاني", "جمادي الأولى", "جمادي الثانية",
            "رجب", "شعبان", "رمضان", "شوال", "ذؤ القعده", "ذؤ الحجه"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژۊئن", "ژۊئیه", "اۊت", "سپتامبر", "اؤکتؤبر",
            "نؤوامبر", "دسامبر"
        )
        override val weekDays = listOf7Items(
            "شمبه", "یکشمبه", "دۊشمبه", "سه شمبه", "چارشمبه", "پئن شمبه", "جۊما"
        )
    }

    private object ar : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "الحمل", "الثور", "الجوزاء", "السرطان", "الأسد", "السنبلة", "المیزان", "العقرب",
            "القوس", "الجدي", "الدلو", "الحوت"
        )
        override val islamicCalendarMonths = listOf12Items(
            "المحرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الاولى", "جمادى الثانية",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر",
            "اكتوبر", "نوفمبر", "ديسمبر"
        )
        val easternGregorianCalendarMonths = listOf12Items(
            "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران", "تموز", "آب", "أيلول",
            "تشرين الأول", "تشرين الثاني", "كانون الأول"
        )
        override val weekDays = listOf7Items(
            "السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة"
        )
        override val weekDaysInitials = listOf7Items(
            "سب", "أح", "اث", "ثل", "أر", "خم", "جم"
        )
    }

    private object ckb : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "خاکەلێوە", "گوڵان", "جۆزەردان", "پووشپەڕ", "گەلاوێژ", "خەرمانان", "ڕەزبەر", "خەزەڵوەر",
            "سەرماوەز", "بەفرانبار", "ڕێبەندان", "ڕەشەمە"
        )
        override val islamicCalendarMonths = listOf12Items(
            "موحەڕڕەم", "سەفەر", "ڕەبیعەلئەووەڵ", "ڕەبیعەلئاخیر", "جومادەلئوولا", "جومادەلئاخیر",
            "ڕەجەب", "شەعبان", "ڕەمەزان", "شەووال", "زولقەعدە", "زولحەججە"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "جانواری", "فێبرواری", "مارچ", "ئاپریل", "مەی", "جوون", "جولای", "ئۆگست", "سێپتەمبەر",
            "ئۆکتۆبەر", "نۆڤەمبەر", "دیسەمبەر"
        )
        override val weekDays = listOf7Items(
            "شەممە", "یەکشەممە", "دووشەممە", "سێشەممە", "چوارشەممە", "پێنجشەممە", "ھەینی"
        )
    }

    private object azb : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "فروردین", "اوردیبهشت", "خورداد", "تیر", "مورداد", "شهریور", "مهر", "آبان", "آذر",
            "دی", "بهمن", "اسفند"
        )
        override val islamicCalendarMonths = listOf12Items(
            "موحررم", "صفر", "ربيع الاوول", "ربيع الآخیر", "جمادى الاوول", "جمادى الآخیر", "رجب",
            "شعبان", "رمضان", "شوال", "ذیقعده", "ذیحججه"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "ژانویه", "فوریه", "مارس", "آوریل", "مئی", "ژوئن", "ژوئیه", "آقوست", "سپتامبر",
            "اوْکتوبر", "نوْوامبر", "دسامبر"
        )
        override val weekDays = listOf7Items(
            "يئل‌گونو", "سۆدگونو", "دۇزگونو", "آراگون", "اوْدگونو", "سۇگونو", "آینی‌گون"
        )
        override val weekDaysInitials = listOf7Items(
            "یئل", "سۆد", "دۇز", "آرا", "اوْد", "سۇ", "آینی"
        )
    }

    private object ur : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "فروردی", "اردیبہشت", "خرداد", "تیر", "امرداد", "شہریور", "مہر", "آبان", "آذر",
            "دی", "بہمن", "اسفندر"
        )
        override val islamicCalendarMonths = listOf12Items(
            "محرم", "صفر", "ربيع الأول", "ربیع الثاني", "جمادى الأولى", "جمادی الثانية", "رجب",
            "شعبان", "رمضان", "شوال", "ذو القعده", "ذو الحجه"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "جنوری", "فروری", "مارچ", "اپریل", "مئی", "جون", "جولائی", "اگست", "ستمبر", "اکتوبر",
            "نومبر", "دسمبر"
        )
        override val weekDays = listOf7Items(
            "سنیچر", "اتوار", "پیر", "منگل", "بدھ", "جمعرات", "جمعہ"
        )
    }

    private object en : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar", "Mehr", "Aban",
            "Azar", "Dey", "Bahman", "Esfand"
        )
        override val islamicCalendarMonths = listOf12Items(
            "Muharram", "Safar", "Rabi' al-awwal", "Rabi' al-Thani", "Jumada al-awwal",
            "Jumada al-Thani", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qidah",
            "Dhu al-Hijjah"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "January", "February", "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December"
        )
        override val weekDays = listOf7Items(
            "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
        )
    }

    private object fr : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
            "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
        )
        override val islamicCalendarMonths = listOf12Items(
            "Mouharram", "Safar", "Rabia al awal", "Rabia ath-thani", "Joumada al oula",
            "Joumada ath-thania", "Rajab", "Chaabane", "Ramadan", "Chawwal", "Dhou al qi`da",
            "Dhou al-hijja"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet",
            "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        )
        override val weekDays = listOf7Items(
            "Samedi", "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"
        )
    }

    private object es : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "Farvardín", "Ordibehesht", "Jordad", "Tir", "Mordad", "Shahrivar",
            "Mehr", "Abán", "Azar", "Dey", "Bahmán", "Esfand"
        )
        override val islamicCalendarMonths = listOf12Items(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal",
            "Jumada al-Thani", "Rajab", "Sha'ban", "Ramadán", "Shawwal",
            "Dhu al-Qadah", "Dhu al-Hijjah"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
            "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        override val weekDays = listOf7Items(
            "Sábado", "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes"
        )
    }

    private object ja : AppLocalesData {
        override val persianCalendarMonths = listOf12Items(
            "ファルヴァルディーン", "オルディーベヘシト", "ホルダード", "ティール", "モルダード",
            "シャハリーヴァル", "メフル", "アーバーン", "アーザル", "デイ", "バフマン", "エスファンド"
        )
        override val islamicCalendarMonths = listOf12Items(
            "ムハッラム", "サファル", "ラビー・ウル・アッワル ", "ラビー・ウッサーニ", "ジュマダル・ウッラー",
            "ジュマダッサーニ", "ラジャブ", "シャバーン", "ラマダーン", "シャッワール", "ズルカーダ",
            "ズルヒッジャ"
        )
        override val gregorianCalendarMonths = listOf12Items(
            "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"
        )
        override val weekDays = listOf7Items(
            "土曜日", "日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日"
        )
    }
}
