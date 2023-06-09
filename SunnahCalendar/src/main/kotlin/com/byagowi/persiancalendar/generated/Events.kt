package com.byagowi.persiancalendar.generated

import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public enum class EventType(
    public val source: String
) {
    Afghanistan("https://w.mudl.gov.af/sites/default/files/2020-03/Calendar%202020%20Website.pdf"), Iran(
        "https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201402.pdf"
    ),
    AncientIran("~https://raw.githubusercontent.com/ilius/starcal/master/plugins/iran-ancient-data.txt"), International(
        "~http://www.un.org/en/sections/observances/international-days/"
    ),
    Nepal(""), ;
}

public class CalendarRecord(
    public val title: String,
    public val type: EventType,
    public val isHoliday: Boolean,
    public val month: Int,
    public val day: Int
)

public val persianEvents: List<CalendarRecord> = listOf(
    CalendarRecord(
        title = "جشن نوروز", type = EventType.Afghanistan, isHoliday = true, month = 1, day = 1
    ),
    CalendarRecord(
        title = "جشن دهقان", type = EventType.Afghanistan, isHoliday = true, month = 1, day = 2
    ),
    CalendarRecord(
        title = "روز زنگ مکتب", type = EventType.Afghanistan, isHoliday = false, month = 1, day = 3
    ),
    CalendarRecord(
        title = "روز مطبوعات", type = EventType.Afghanistan, isHoliday = false, month = 2, day = 5
    ),
    CalendarRecord(
        title = "کودتای حزب دموکراتیک خلق افغانستان (سال ۱۳۵۷)",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 2,
        day = 7
    ),
    CalendarRecord(
        title = "پیروزی جهاد مقدس افغانستان (سال ۱۳۷۱)",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 2,
        day = 8
    ),
    CalendarRecord(
        title = "روز مادر", type = EventType.Afghanistan, isHoliday = false, month = 3, day = 24
    ),
    CalendarRecord(
        title = "روز استرداد استقلال کشور (سال ۱۲۹۸ مصادف با ۱۹۱۹ میلادی)",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 5,
        day = 28
    ),
    CalendarRecord(
        title = "روز همبستگی با برادران پشتون و بلوچ",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 6,
        day = 9
    ),
    CalendarRecord(
        title = "روز شهادت قهرمان ملی افغانستان (آغاز هفته شهید)",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 6,
        day = 18
    ),
    CalendarRecord(
        title = "روز هنر", type = EventType.Afghanistan, isHoliday = false, month = 7, day = 16
    ),
    CalendarRecord(
        title = "روز جیودیزیست‌های کشور",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 7,
        day = 21
    ),
    CalendarRecord(
        title = "آغاز هفته مخصوص سره میاشت",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 7,
        day = 24
    ),
    CalendarRecord(
        title = "روز ملی زبان اوزبیکی",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 7,
        day = 29
    ),
    CalendarRecord(
        title = "روز وحدت ملی و شهادت شش عضو ولسی جرگه شورای ملی در بغلان (سال ۱۳۸۶)",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 8,
        day = 15
    ),
    CalendarRecord(
        title = "روز تحقیق و پژوهش",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 9,
        day = 13
    ),
    CalendarRecord(
        title = "روز تجاوز ارتش اتحاد شوروی سابق بر حریم کشور (سال ۱۳۵۸)",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 10,
        day = 6
    ),
    CalendarRecord(
        title = "هفته قانون اساسی افغانستان",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 10,
        day = 14
    ),
    CalendarRecord(
        title = "روز متعاقدین",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 11,
        day = 10
    ),
    CalendarRecord(
        title = "روز شکست و خروج ارتش اتحاد شوروی سابق از افغانستان",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 11,
        day = 26
    ),
    CalendarRecord(
        title = "روز تکبیر و قیام مردم کابل در برابر تجاوز ارتش اتحاد شوروی سابق",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 12,
        day = 3
    ),
    CalendarRecord(
        title = "روز ملی حمایت از نیروهای دفاعی و امنیتی کشور",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 12,
        day = 9
    ),
    CalendarRecord(
        title = "روز حفاظت از میراث‌های فرهنگی کشور",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 12,
        day = 20
    ),
    CalendarRecord(
        title = "روز شهید وحدت ملی استاد عبدالعلی مزاری (۱۳۷۳)",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 12,
        day = 22
    ),
    CalendarRecord(
        title = "روز قیام مردم هرات بر علیه جمهوری دموکراتیک خلق افغانستان",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 12,
        day = 24
    ),
    CalendarRecord(
        title = "روز ملی خبرنگار",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 12,
        day = 27
    ),
    CalendarRecord(
        title = "آغاز نوروز", type = EventType.Iran, isHoliday = true, month = 1, day = 1
    ),
    CalendarRecord(
        title = "نوروز", type = EventType.Iran, isHoliday = true, month = 1, day = 2
    ),
    CalendarRecord(
        title = "هجوم مأموران ستم‌شاهی پهلوی به مدرسهٔ فیضیهٔ قم (۱۳۴۲ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 2
    ),
    CalendarRecord(
        title = "آغاز عملیات فتح‌المبین (۱۳۶۱ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 2
    ),
    CalendarRecord(
        title = "نوروز", type = EventType.Iran, isHoliday = true, month = 1, day = 3
    ),
    CalendarRecord(
        title = "نوروز", type = EventType.Iran, isHoliday = true, month = 1, day = 4
    ),
    CalendarRecord(
        title = "زادروز زرتشت پیامبر", type = EventType.Iran, isHoliday = false, month = 1, day = 6
    ),
    CalendarRecord(
        title = "روز هنرهای نمایشی", type = EventType.Iran, isHoliday = false, month = 1, day = 7
    ),
    CalendarRecord(
        title = "روز جمهوری اسلامی ایران",
        type = EventType.Iran,
        isHoliday = true,
        month = 1,
        day = 12
    ),
    CalendarRecord(
        title = "روز طبیعت", type = EventType.Iran, isHoliday = true, month = 1, day = 13
    ),
    CalendarRecord(
        title = "روز ذخایر ژنتیکی و زیستی",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 15
    ),
    CalendarRecord(
        title = "روز سلامتی", type = EventType.Iran, isHoliday = false, month = 1, day = 18
    ),
    CalendarRecord(
        title = "شهادت آیت‌اللّه سید محمدباقر صدر و خواهر ایشان بنت‌الهدی به دست حکومت بعث عراق (۱۳۵۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 19
    ),
    CalendarRecord(
        title = "روز ملّی فنّاوری هسته‌ای",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 20
    ),
    CalendarRecord(
        title = "روز هنر انقلاب اسلامی (سالروز شهادت سید مرتضی آوینی) (۱۳۷۲ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 20
    ),
    CalendarRecord(
        title = "شهادت امیر سپهبد علی صیاد شیرازی (۱۳۷۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 21
    ),
    CalendarRecord(
        title = "سالروز افتتاح حساب شمارهٔ ۱۰۰ به فرمان حضرت امام خمینی (ره) و تأسیس بنیاد مسکن انقلاب اسلامی (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 21
    ),
    CalendarRecord(
        title = "روز بزرگداشت عطّار نیشابوری",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 25
    ),
    CalendarRecord(
        title = "روز ارتش جمهوری اسلامی و نیروی زمینی",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 29
    ),
    CalendarRecord(
        title = "روز بزرگداشت سعدی", type = EventType.Iran, isHoliday = false, month = 2, day = 1
    ),
    CalendarRecord(
        title = "تأسیس سپاه پاسداران انقلاب اسلامی (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 2
    ),
    CalendarRecord(
        title = "سالروز اعلام انقلاب فرهنگی (۱۳۵۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 2
    ),
    CalendarRecord(
        title = "روز زمین پاک", type = EventType.Iran, isHoliday = false, month = 2, day = 2
    ),
    CalendarRecord(
        title = "روز بزرگداشت شیخ بهایی",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 3
    ),
    CalendarRecord(
        title = "روز معماری", type = EventType.Iran, isHoliday = false, month = 2, day = 3
    ),
    CalendarRecord(
        title = "سالروز شهادت امیر سپهبد قرنی (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 3
    ),
    CalendarRecord(
        title = "شکست حمله نظامی آمریکا به ایران در طبس (۱۳۵۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 5
    ),
    CalendarRecord(
        title = "روز ایمنی حمل و نقل", type = EventType.Iran, isHoliday = false, month = 2, day = 7
    ),
    CalendarRecord(
        title = "روز شوراها", type = EventType.Iran, isHoliday = false, month = 2, day = 9
    ),
    CalendarRecord(
        title = "روز ملی خلیج فارس", type = EventType.Iran, isHoliday = false, month = 2, day = 10
    ),
    CalendarRecord(
        title = "آغاز عملیات بیت المقدس (۱۳۶۱ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 10
    ),
    CalendarRecord(
        title = "شهادت استاد مرتضی مطهری (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 12
    ),
    CalendarRecord(
        title = "روز معلم", type = EventType.Iran, isHoliday = false, month = 2, day = 12
    ),
    CalendarRecord(
        title = "روز بزرگداشت شیخ صدوق",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 15
    ),
    CalendarRecord(
        title = "روز بیماری‌های خاص و صعب‌العلاج",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 18
    ),
    CalendarRecord(
        title = "روز بزرگداشت شیخ کلینی",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 19
    ),
    CalendarRecord(
        title = "روز اسناد ملی و میراث مکتوب",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 19
    ),
    CalendarRecord(
        title = "لغو امتیاز تنباکو به فتوای آیت‌اللّه میرزا حسن شیرازی (۱۲۷۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 24
    ),
    CalendarRecord(
        title = "روز پاسداشت زبان فارسی و بزرگداشت حکیم ابوالقاسم فردوسی",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 25
    ),
    CalendarRecord(
        title = "روز ارتباطات و روابط عمومی",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 27
    ),
    CalendarRecord(
        title = "روز بزرگداشت حکیم عمر خیام",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 28
    ),
    CalendarRecord(
        title = "روز ملی جمعیت", type = EventType.Iran, isHoliday = false, month = 2, day = 30
    ),
    CalendarRecord(
        title = "روز اهدای عضو، اهدای زندگی",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 31
    ),
    CalendarRecord(
        title = "روز بهره‌وری و بهینه‌سازی مصرف",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 1
    ),
    CalendarRecord(
        title = "روز بزرگداشت ملّاصدرا (صدرالمتألهین)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 1
    ),
    CalendarRecord(
        title = "فتح خرمشهر در عملیات بیت‌المقدس (۱۳۶۱ ه‍.ش) و روز مقاومت، ایثار و پیروزی",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 3
    ),
    CalendarRecord(
        title = "روز دزفول", type = EventType.Iran, isHoliday = false, month = 3, day = 4
    ),
    CalendarRecord(
        title = "روز مقاومت و پایداری", type = EventType.Iran, isHoliday = false, month = 3, day = 4
    ),
    CalendarRecord(
        title = "روز نسیم مهر (روز حمایت از خانواده زندانیان)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 5
    ),
    CalendarRecord(
        title = "افتتاح اولین دورهٔ مجلس شورای اسلامی (۱۳۵۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 7
    ),
    CalendarRecord(
        title = "رحلت حضرت امام خمینی (ره) رهبر کبیر انقلاب و بنیان‌گذار جمهوری اسلامی ایران (۱۳۶۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = true,
        month = 3,
        day = 14
    ),
    CalendarRecord(
        title = "انتخاب حضرت آیت‌اللّه امام خامنه‌ای به رهبری (۱۳۶۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 14
    ),
    CalendarRecord(
        title = "قیام خونین ۱۵ خرداد (۱۳۴۲ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = true,
        month = 3,
        day = 15
    ),
    CalendarRecord(
        title = "زندانی‌شدن حضرت امام خمینی (ره) به دست مأموران ستم‌شاهی پهلوی (۱۳۴۲ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 15
    ),
    CalendarRecord(
        title = "روز صنایع دستی", type = EventType.Iran, isHoliday = false, month = 3, day = 20
    ),
    CalendarRecord(
        title = "روز ملی فرش", type = EventType.Iran, isHoliday = false, month = 3, day = 20
    ),
    CalendarRecord(
        title = "شهادت آیت‌اللّه سعیدی به دست مأموران ستم‌شاهی پهلوی (۱۳۴۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 20
    ),
    CalendarRecord(
        title = "شهادت سربازان دلیر اسلام: بخارایی، امانی، صفار هرندی و نیک‌نژاد (۱۳۴۴ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 26
    ),
    CalendarRecord(
        title = "روز جهاد کشاورزی (تشکیل جهاد سازندگی به فرمان حضرت امام خمینی (ره)) (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 27
    ),
    CalendarRecord(
        title = "درگذشت دکتر علی شریعتی (۱۳۵۶ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 29
    ),
    CalendarRecord(
        title = "شهادت زائران حرم رضوی (ع) به دست ایادی آمریکا در روز عاشورا (۱۳۷۳ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 30
    ),
    CalendarRecord(
        title = "شهادت دکتر مصطفی چمران (۱۳۶۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 31
    ),
    CalendarRecord(
        title = "روز بسیج استادان", type = EventType.Iran, isHoliday = false, month = 3, day = 31
    ),
    CalendarRecord(
        title = "روز تبلیغ و اطلاع‌رسانی دینی (سالروز صدور فرمان حضرت امام خمینی رحمة‌اللّه علیه مبنی بر تأسیس سازمان تبلیغات اسلامی) (۱۳۶۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 1
    ),
    CalendarRecord(
        title = "روز اصناف", type = EventType.Iran, isHoliday = false, month = 4, day = 1
    ),
    CalendarRecord(
        title = "شهادت مظلومانهٔ آیت‌اللّه دکتر بهشتی و ۷۲ تن از یاران حضرت امام خمینی (ره) با انفجار بمب به دست منافقان در دفتر مرکزی حزب جمهوری اسلامی (۱۳۶۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 7
    ),
    CalendarRecord(
        title = "روز قوهٔ قضائیه", type = EventType.Iran, isHoliday = false, month = 4, day = 7
    ),
    CalendarRecord(
        title = "سالروز بمباران شیمیایی شهر سردشت (۱۳۶۶ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 7
    ),
    CalendarRecord(
        title = "روز مبارزه با سلاح‌های شیمیایی و میکروبی",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 8
    ),
    CalendarRecord(
        title = "روز صنعت و معدن", type = EventType.Iran, isHoliday = false, month = 4, day = 10
    ),
    CalendarRecord(
        title = "روز آزادسازی شهر مهران",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 10
    ),
    CalendarRecord(
        title = "روز بزرگداشت صائب تبریزی",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 10
    ),
    CalendarRecord(
        title = "شهادت آیت‌اللّه صدوقی چهارمین شهید محراب به دست منافقان (۱۳۶۱ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 11
    ),
    CalendarRecord(
        title = "حملهٔ ددمنشانهٔ ناوگان آمریکای جنایتکار به هواپیمای مسافربری جمهوری اسلامی ایران (۱۳۶۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 12
    ),
    CalendarRecord(
        title = "روز افشای حقوق بشر آمریکایی",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 12
    ),
    CalendarRecord(
        title = "روز بزرگداشت علامه امینی (۱۳۴۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 12
    ),
    CalendarRecord(
        title = "روز قلم", type = EventType.Iran, isHoliday = false, month = 4, day = 14
    ),
    CalendarRecord(
        title = "روز شهرداری و دهیاری",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 14
    ),
    CalendarRecord(
        title = "روز مالیات", type = EventType.Iran, isHoliday = false, month = 4, day = 16
    ),
    CalendarRecord(
        title = "روز ادبیات کودکان و نوجوانان",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 18
    ),
    CalendarRecord(
        title = "کشف توطئهٔ آمریکایی در پایگاه هوایی شهید نوژه (کودتای نافرجام نقاب) (۱۳۵۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 18
    ),
    CalendarRecord(
        title = "روز عفاف و حجاب", type = EventType.Iran, isHoliday = false, month = 4, day = 21
    ),
    CalendarRecord(
        title = "حمله به مسجد گوهرشاد و کشتار مردم به دست رضاخان (۱۳۱۴ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 21
    ),
    CalendarRecord(
        title = "روز بزرگداشت خوارزمی",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 22
    ),
    CalendarRecord(
        title = "روز فناوری اطلاعات", type = EventType.Iran, isHoliday = false, month = 4, day = 22
    ),
    CalendarRecord(
        title = "روز گفت‌وگو و تعامل سازنده با جهان",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 23
    ),
    CalendarRecord(
        title = "گشایش نخستین مجلس خبرگان رهبری (۱۳۶۲ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 23
    ),
    CalendarRecord(
        title = "روز بهزیستی و تأمین اجتماعی",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 25
    ),
    CalendarRecord(
        title = "سالروز تأسیس نهاد شورای نگهبان (۱۳۵۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 26
    ),
    CalendarRecord(
        title = "اعلام پذیرش قطعنامهٔ ۵۹۸ شورای امنیت از سوی ایران (۱۳۶۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 27
    ),
    CalendarRecord(
        title = "روز بزرگداشت آیت‌اللّه سید ابوالقاسم کاشانی",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 30
    ),
    CalendarRecord(
        title = "روز بزرگداشت شیخ صفی‌الدین اردبیلی",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 4
    ),
    CalendarRecord(
        title = "سالروز عملیات افتخار‌آفرین مرصاد (۱۳۶۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 5
    ),
    CalendarRecord(
        title = "روز کارآفرینی و آموزش‌های فنّی‌و‌حرفه‌ای",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 6
    ),
    CalendarRecord(
        title = "روز شعر و ادبیات آیینی",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 8
    ),
    CalendarRecord(
        title = "روز بزرگداشت محتشم کاشانی",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 8
    ),
    CalendarRecord(
        title = "روز بزرگداشت شیخ شهاب‌الدین سهروردی (شیخ اشراق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 8
    ),
    CalendarRecord(
        title = "روز اهدای خون", type = EventType.Iran, isHoliday = false, month = 5, day = 9
    ),
    CalendarRecord(
        title = "شهادت آیت‌اللّه شیخ فضل‌اللّه نوری (۱۲۸۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 11
    ),
    CalendarRecord(
        title = "صدور فرمان مشروطیت (۱۲۸۵ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 14
    ),
    CalendarRecord(
        title = "روز حقوق بشر اسلامی و کرامت انسانی",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 14
    ),
    CalendarRecord(
        title = "سالروز شهادت امیر سرلشکر خلبان عباس بابایی (۱۳۶۶ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 15
    ),
    CalendarRecord(
        title = "تشکیل جهاد دانشگاهی (۱۳۵۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 16
    ),
    CalendarRecord(
        title = "روز خبرنگار", type = EventType.Iran, isHoliday = false, month = 5, day = 17
    ),
    CalendarRecord(
        title = "روز بزرگداشت شهدای مدافع حرم",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 18
    ),
    CalendarRecord(
        title = "روز حمایت از صنایع کوچک",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 21
    ),
    CalendarRecord(
        title = "روز تشکّل‌ها و مشارکت‌های اجتماعی",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 22
    ),
    CalendarRecord(
        title = "روز مقاومت اسلامی", type = EventType.Iran, isHoliday = false, month = 5, day = 23
    ),
    CalendarRecord(
        title = "آغاز بازگشت آزادگان به میهن اسلامی (۱۳۶۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 26
    ),
    CalendarRecord(
        title = "کودتای آمریکا برای بازگرداندن شاه (۱۳۳۲ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 28
    ),
    CalendarRecord(
        title = "گشایش مجلس خبرگان برای بررسی نهایی قانون اساسی جمهوری اسلامی ایران (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 28
    ),
    CalendarRecord(
        title = "روز بزرگداشت علامه مجلسی",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 30
    ),
    CalendarRecord(
        title = "روز صنعت دفاعی", type = EventType.Iran, isHoliday = false, month = 5, day = 31
    ),
    CalendarRecord(
        title = "روز بزرگداشت ابوعلی سینا",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 1
    ),
    CalendarRecord(
        title = "روز پزشک", type = EventType.Iran, isHoliday = false, month = 6, day = 1
    ),
    CalendarRecord(
        title = "آغاز هفتهٔ دولت", type = EventType.Iran, isHoliday = false, month = 6, day = 2
    ),
    CalendarRecord(
        title = "شهادت سید ‌علی اندرزگو (در روز ۱۹ ماه مبارک رمضان) (۱۳۵۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 2
    ),
    CalendarRecord(
        title = "اِشغال ایران توسّط متّفقین و فرار رضاخان (۱۳۲۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 3
    ),
    CalendarRecord(
        title = "روز کارمند", type = EventType.Iran, isHoliday = false, month = 6, day = 4
    ),
    CalendarRecord(
        title = "روز بزرگداشت محمّدبن زکریای رازی",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 5
    ),
    CalendarRecord(
        title = "روز داروسازی", type = EventType.Iran, isHoliday = false, month = 6, day = 5
    ),
    CalendarRecord(
        title = "روز کشتی", type = EventType.Iran, isHoliday = false, month = 6, day = 5
    ),
    CalendarRecord(
        title = "روز مبارزه با تروریسم (انفجار دفتر نخست‌وزیری به دست منافقان و شهادت مظلومانهٔ شهیدان رجایی و باهنر) (۱۳۶۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 8
    ),
    CalendarRecord(
        title = "روز بانکداری اسلامی (سالروز تصویب قانون عملیات بانکی بدون ربا) (۱۳۶۲ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 10
    ),
    CalendarRecord(
        title = "روز تشکیل قرارگاه پدافند هوایی حضرت خاتم‌الانبیا صلی اللّه علیه و آله (۱۳۷۱ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 10
    ),
    CalendarRecord(
        title = "روز صنعت چاپ", type = EventType.Iran, isHoliday = false, month = 6, day = 11
    ),
    CalendarRecord(
        title = "روز مبارزه با استعمار انگلیس (سالروز شهادت رئیسعلی دلواری - ۱۲۹۴ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 12
    ),
    CalendarRecord(
        title = "روز بهوَرز", type = EventType.Iran, isHoliday = false, month = 6, day = 12
    ),
    CalendarRecord(
        title = "روز تعاون", type = EventType.Iran, isHoliday = false, month = 6, day = 13
    ),
    CalendarRecord(
        title = "روز بزرگداشت ابوریحان بیرونی",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 13
    ),
    CalendarRecord(
        title = "روز مردم‌شناسی", type = EventType.Iran, isHoliday = false, month = 6, day = 13
    ),
    CalendarRecord(
        title = "شهادت آیت‌اللّه قدّوسی و سرتیپ وحید دستجردی (۱۳۶۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 14
    ),
    CalendarRecord(
        title = "قیام ۱۷ شهریور و کشتار جمعی از مردم به‌دست مأموران ستم‌شاهی پهلوی (۱۳۵۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 17
    ),
    CalendarRecord(
        title = "وفات آیت‌اللّه سید محمود طالقانی اوّلین حضرت امام جمعهٔ تهران (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 19
    ),
    CalendarRecord(
        title = "شهادت دومین شهید محراب آیت‌اللّه مدنی به دست منافقان (۱۳۶۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 20
    ),
    CalendarRecord(
        title = "روز سینما", type = EventType.Iran, isHoliday = false, month = 6, day = 21
    ),
    CalendarRecord(
        title = "روز شعر و ادب فارسی", type = EventType.Iran, isHoliday = false, month = 6, day = 27
    ),
    CalendarRecord(
        title = "روز بزرگداشت استاد سید‌ محمّد‌حسین شهریار",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 27
    ),
    CalendarRecord(
        title = "آغاز جنگ تحمیلی (۱۳۵۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 31
    ),
    CalendarRecord(
        title = "آغاز هفتهٔ دفاع مقدّس",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 31
    ),
    CalendarRecord(
        title = "روز بزرگداشت شهدای منا",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 2
    ),
    CalendarRecord(
        title = "روز سرباز", type = EventType.Iran, isHoliday = false, month = 7, day = 4
    ),
    CalendarRecord(
        title = "شکست حصر آبادان در عملیات ثامن‌الائمه (ع) (۱۳۶۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 5
    ),
    CalendarRecord(
        title = "روز گردشگری", type = EventType.Iran, isHoliday = false, month = 7, day = 5
    ),
    CalendarRecord(
        title = "روز بزرگداشت فرماندهان شهید دفاع مقدّس",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 7
    ),
    CalendarRecord(
        title = "شهادت سرداران اسلام: فلاحی، فکوری، نامجو، کلاهدوز و جهان‌آرا (۱۳۶۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 7
    ),
    CalendarRecord(
        title = "روز آتش‌نشانی و ایمنی",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 7
    ),
    CalendarRecord(
        title = "روز بزرگداشت شمس", type = EventType.Iran, isHoliday = false, month = 7, day = 7
    ),
    CalendarRecord(
        title = "روز بزرگداشت مولوی", type = EventType.Iran, isHoliday = false, month = 7, day = 8
    ),
    CalendarRecord(
        title = "روز همبستگی و همدردی با کودکان و نوجوانان فلسطینی",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 9
    ),
    CalendarRecord(
        title = "هجرت حضرت امام خمینی (ره) از عراق به پاریس (۱۳۵۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 13
    ),
    CalendarRecord(
        title = "روز نیروی انتظامی", type = EventType.Iran, isHoliday = false, month = 7, day = 13
    ),
    CalendarRecord(
        title = "روز دامپزشکی", type = EventType.Iran, isHoliday = false, month = 7, day = 14
    ),
    CalendarRecord(
        title = "روز روستا و عشایر", type = EventType.Iran, isHoliday = false, month = 7, day = 15
    ),
    CalendarRecord(
        title = "روز بزرگداشت حافظ", type = EventType.Iran, isHoliday = false, month = 7, day = 20
    ),
    CalendarRecord(
        title = "شهادت پنجمین شهید محراب آیت‌اللّه اشرفی اصفهانی به دست منافقان (۱۳۶۱ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 23
    ),
    CalendarRecord(
        title = "روز ملی پارالمپیک", type = EventType.Iran, isHoliday = false, month = 7, day = 24
    ),
    CalendarRecord(
        title = "روز پیوند اولیا و مربیان",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 24
    ),
    CalendarRecord(
        title = "سالروز واقعهٔ به آتش کشیدن مسجد جامع شهر کرمان به دست دژخیمان حکومت پهلوی (۱۳۵۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 24
    ),
    CalendarRecord(
        title = "روز تربیت‌بدنی و ورزش",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 26
    ),
    CalendarRecord(
        title = "روز صادرات", type = EventType.Iran, isHoliday = false, month = 7, day = 29
    ),
    CalendarRecord(
        title = "شهادت مظلومانهٔ آیت‌اللّه حاج سید مصطفی خمینی (۱۳۵۶ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 1
    ),
    CalendarRecord(
        title = "روز آمار و برنامه‌ریزی",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 1
    ),
    CalendarRecord(
        title = "اعتراض و افشاگری حضرت امام خمینی (ره) علیه پذیرش کاپیتولاسیون (۱۳۴۳ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 4
    ),
    CalendarRecord(
        title = "شهادت محمّدحسین فهمیده (بسیجی ۱۳ ساله) (۱۳۵۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 8
    ),
    CalendarRecord(
        title = "روز نوجوان و بسیج دانش‌آموزی",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 8
    ),
    CalendarRecord(
        title = "روز پدافند غیرعامل", type = EventType.Iran, isHoliday = false, month = 8, day = 8
    ),
    CalendarRecord(
        title = "شهادت آیت‌اللّه قاضی طباطبایی، اوّلین شهید محراب به دست منافقان (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 10
    ),
    CalendarRecord(
        title = "تسخیر لانهٔ جاسوسی آمریکا به دست دانشجویان پیرو خط حضرت امام (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 13
    ),
    CalendarRecord(
        title = "روز ملی مبارزه با استکبار جهانی",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 13
    ),
    CalendarRecord(
        title = "روز دانش‌آموز", type = EventType.Iran, isHoliday = false, month = 8, day = 13
    ),
    CalendarRecord(
        title = "تبعید حضرت امام خمینی (ره) از ایران به ترکیه (۱۳۴۳ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 13
    ),
    CalendarRecord(
        title = "روز فرهنگ عمومی", type = EventType.Iran, isHoliday = false, month = 8, day = 14
    ),
    CalendarRecord(
        title = "روز کیفیت", type = EventType.Iran, isHoliday = false, month = 8, day = 18
    ),
    CalendarRecord(
        title = "روز کتاب، کتاب‌خوانی و کتابدار",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 24
    ),
    CalendarRecord(
        title = "روز بزرگداشت آیت‌اللّه علامه سید محمّدحسین طباطبایی (۱۳۶۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 24
    ),
    CalendarRecord(
        title = "سالروز آزادسازی سوسنگرد",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 26
    ),
    CalendarRecord(
        title = "روز حکمت و فلسفه", type = EventType.Iran, isHoliday = false, month = 8, day = 30
    ),
    CalendarRecord(
        title = "روز بزرگداشت ابونصر فارابی",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 30
    ),
    CalendarRecord(
        title = "روز بسیج مستضعفان (تشکیل بسیج مستضعفان به فرمان حضرت امام خمینی (ره)) (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 5
    ),
    CalendarRecord(
        title = "سالروز قیام مردم گرگان (۱۳۵۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 5
    ),
    CalendarRecord(
        title = "روز نیروی دریایی", type = EventType.Iran, isHoliday = false, month = 9, day = 7
    ),
    CalendarRecord(
        title = "روز بزرگداشت شیخ مفید",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 9
    ),
    CalendarRecord(
        title = "شهادت آیت‌اللّه سید ‌حسن مدرّس (۱۳۱۶ ه‍.ش) و روز مجلس ",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 10
    ),
    CalendarRecord(
        title = "شهادت میرزا‌کوچک‌خان جنگلی (۱۳۰۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 11
    ),
    CalendarRecord(
        title = "روز قانون اساسی جمهوری اسلامی ایران (تصویب قانون اساسی جمهوری اسلامی ایران) (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 12
    ),
    CalendarRecord(
        title = "روز بیمه", type = EventType.Iran, isHoliday = false, month = 9, day = 13
    ),
    CalendarRecord(
        title = "روز دانشجو", type = EventType.Iran, isHoliday = false, month = 9, day = 16
    ),
    CalendarRecord(
        title = "معرّفی عراق به عنوان مسئول و آغازگر جنگ از سوی سازمان ملل (۱۳۷۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 18
    ),
    CalendarRecord(
        title = "تشکیل شورای عالی انقلاب فرهنگی به فرمان حضرت امام خمینی (ره) (۱۳۶۳ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 19
    ),
    CalendarRecord(
        title = "شهادت آیت‌اللّه دستغیب، سومین شهید محراب به دست منافقان (۱۳۶۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 20
    ),
    CalendarRecord(
        title = "روز پژوهش", type = EventType.Iran, isHoliday = false, month = 9, day = 25
    ),
    CalendarRecord(
        title = "روز حمل‌و‌نقل و رانندگان",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 26
    ),
    CalendarRecord(
        title = "شهادت آیت‌اللّه دکتر محمّد مفتّح (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 27
    ),
    CalendarRecord(
        title = "روز وحدت حوزه و دانشگاه",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 27
    ),
    CalendarRecord(
        title = "روز جهان عاری از خشونت و افراطی‌گری",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 27
    ),
    CalendarRecord(
        title = "روز تجلیل از شهید تندگویان",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 29
    ),
    CalendarRecord(
        title = "شب یلدا (چله)", type = EventType.Iran, isHoliday = false, month = 9, day = 30
    ),
    CalendarRecord(
        title = "روز ثبت‌احوال", type = EventType.Iran, isHoliday = false, month = 10, day = 3
    ),
    CalendarRecord(
        title = "روز بزرگداشت رودکی", type = EventType.Iran, isHoliday = false, month = 10, day = 4
    ),
    CalendarRecord(
        title = "روز ایمنی در برابر زلزله و کاهش اثرات بلایای طبیعی",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 5
    ),
    CalendarRecord(
        title = "سالروز تشکیل نهضت سوادآموزی به فرمان حضرت امام خمینی (ره) (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 7
    ),
    CalendarRecord(
        title = "شهادت آیت‌اللّه حسین غفّاری به دست مأموران ستم‌شاهی پهلوی (۱۳۵۳ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 7
    ),
    CalendarRecord(
        title = "روز صنعت پتروشیمی", type = EventType.Iran, isHoliday = false, month = 10, day = 8
    ),
    CalendarRecord(
        title = "روز بصیرت و میثاق امّت با ولایت",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 9
    ),
    CalendarRecord(
        title = "روز جهانی مقاومت", type = EventType.Iran, isHoliday = false, month = 10, day = 13
    ),
    CalendarRecord(
        title = "شهادت الگوی اخلاص و عمل سردار سپهبد قاسم سلیمانی به دست استکبار جهانی",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 13
    ),
    CalendarRecord(
        title = "ابلاغ پیام تاریخی حضرت امام خمینی (ره) به گورباچف رهبر شوروی سابق (۱۳۶۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 13
    ),
    CalendarRecord(
        title = "روز شهدای دانشجو (شهادت سیدحسین علم‌الهدی و همرزمان وی در هویزه)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 16
    ),
    CalendarRecord(
        title = "اجرای طرح استعماری حذف حجاب (کشف حجاب) به دست رضاخان (۱۳۱۴ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 17
    ),
    CalendarRecord(
        title = "روز بزرگداشت خواجوی کرمانی",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 17
    ),
    CalendarRecord(
        title = "قیام خونین مردم قم (۱۳۵۶ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 19
    ),
    CalendarRecord(
        title = "شهادت میرزا تقی خان امیرکبیر (۱۲۳۰ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 20
    ),
    CalendarRecord(
        title = "تشکیل شورای انقلاب به فرمان حضرت امام خمینی (ره) (۱۳۵۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 22
    ),
    CalendarRecord(
        title = "فرار شاه معدوم (۱۳۵۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 26
    ),
    CalendarRecord(
        title = "شهادت شهیدان: نواب صفوی، طهماسبی، برادران واحدی و ذوالقدر از فدائیان اسلام (۱۳۳۴ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 27
    ),
    CalendarRecord(
        title = "روز غزه", type = EventType.Iran, isHoliday = false, month = 10, day = 29
    ),
    CalendarRecord(
        title = "سالروز حماسهٔ مردم آمل",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 6
    ),
    CalendarRecord(
        title = "روز آواها و نواهای ایرانی",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 6
    ),
    CalendarRecord(
        title = "روز بزرگداشت صفی‌الدین اُرمَوی",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 6
    ),
    CalendarRecord(
        title = "سالروز بازگشت حضرت امام خمینی (ره) به ایران و آغاز دههٔ مبارک فجر انقلاب اسلامی",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 12
    ),
    CalendarRecord(
        title = "روز فنّاوری فضایی", type = EventType.Iran, isHoliday = false, month = 11, day = 14
    ),
    CalendarRecord(
        title = "روز نیروی هوایی", type = EventType.Iran, isHoliday = false, month = 11, day = 19
    ),
    CalendarRecord(
        title = "شکسته‌شدن حکومت‌نظامی به فرمان حضرت امام خمینی (ره) (۱۳۵۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 21
    ),
    CalendarRecord(
        title = "پیروزی انقلاب اسلامی ایران و سقوط نظام شاهنشاهی (۱۳۵۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = true,
        month = 11,
        day = 22
    ),
    CalendarRecord(
        title = "صدور حکم تاریخی حضرت امام خمینی (ره) مبنی بر ارتداد سلمان‌رشدی نویسندهٔ خائن کتاب آیات شیطانی (۱۳۶۷ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 25
    ),
    CalendarRecord(
        title = "قیام مردم تبریز به مناسبت چهلمین روز شهادت شهدای قم (۱۳۵۶ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 29
    ),
    CalendarRecord(
        title = "روز اقتصاد مقاومتی و کارآفرینی",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 29
    ),
    CalendarRecord(
        title = "کودتای انگلیسی رضاخان (۱۲۹۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 3
    ),
    CalendarRecord(
        title = "روز بزرگداشت خواجه‌نصیرالدّین طوسی",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 5
    ),
    CalendarRecord(
        title = "روز مهندسی", type = EventType.Iran, isHoliday = false, month = 12, day = 5
    ),
    CalendarRecord(
        title = "روز امور تربیتی و تربیت اسلامی",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 8
    ),
    CalendarRecord(
        title = "روز بزرگداشت حکیم حاج ملاهادی سبزواری",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 8
    ),
    CalendarRecord(
        title = "روز حمایت از بیماران نادر",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 8
    ),
    CalendarRecord(
        title = "روز حمایت از حقوق مصرف‌کنندگان",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 9
    ),
    CalendarRecord(
        title = "روز احسان و نیکوکاری",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 14
    ),
    CalendarRecord(
        title = "روز ترویج فرهنگ قرض‌الحسنه",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 14
    ),
    CalendarRecord(
        title = "روز درختکاری", type = EventType.Iran, isHoliday = false, month = 12, day = 15
    ),
    CalendarRecord(
        title = "روز بزرگداشت سید ‌جمال‌الدّین اسدآبادی",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 18
    ),
    CalendarRecord(
        title = "سالروز تأسیس کانون‌های فرهنگی‌و‌هنری مساجد کشور",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 18
    ),
    CalendarRecord(
        title = "روز راهیان نور", type = EventType.Iran, isHoliday = false, month = 12, day = 20
    ),
    CalendarRecord(
        title = "روز بزرگداشت نظامی گنجوی",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 21
    ),
    CalendarRecord(
        title = "روز بزرگداشت شهدا (سالروز صدور فرمان حضرت امام خمینی (ره)، مبنی بر تأسیس بنیاد شهید انقلاب اسلامی) (۱۳۵۸ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 22
    ),
    CalendarRecord(
        title = "روز بزرگداشت پروین اعتصامی",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 25
    ),
    CalendarRecord(
        title = "بمباران شیمیایی حلبچه به دست ارتش بعث عراق (۱۳۶۶ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 25
    ),
    CalendarRecord(
        title = "روز ملّی‌شدن صنعت نفت ایران (۱۳۲۹ ه‍.ش)",
        type = EventType.Iran,
        isHoliday = true,
        month = 12,
        day = 29
    ),
    CalendarRecord(
        title = "جشن نوروز، نوروز جمشیدی (جمشید پیشدادی) - ابتدای بهار",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 1,
        day = 1
    ),
    CalendarRecord(
        title = "نوروز بزرگ (هودرو)، زادروز آشو زرتشت - روییدن مشی و مشیانه",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 1,
        day = 6
    ),
    CalendarRecord(
        title = "آیین نیایش پیر هریشت از ۷ تا ۱۱ فروردین",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 1,
        day = 7
    ),
    CalendarRecord(
        title = "سیزده نوروز، سیزده‌بدر",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 1,
        day = 13
    ),
    CalendarRecord(
        title = "جشن فرودینگان",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 1,
        day = 19
    ),
    CalendarRecord(
        title = "جشن اردیبهشتگان، پوشیدن لباس سپید به نشانه پاکی",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 2,
        day = 2
    ),
    CalendarRecord(
        title = "جشن چلمو (چله بهار) - گاهان بار میدیوزرم‌گاه از ۱۰ تا ۱۴ اردیبهشت",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 2,
        day = 10
    ),
    CalendarRecord(
        title = "جشن پنجاه بدر",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 2,
        day = 18
    ),
    CalendarRecord(
        title = "بزرگداشت استاد توس فردوسی بزرگ",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 2,
        day = 25
    ),
    CalendarRecord(
        title = "جشن خوردادگان، امشاسپند خورداد نگاهبان آبها",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 3,
        day = 4
    ),
    CalendarRecord(
        title = "آیین نیایش ستی پیر و پیر سبز (چک چک)",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 3,
        day = 24
    ),
    CalendarRecord(
        title = "جشن ابتدای تیر ماه، آب پاشونک",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 3,
        day = 29
    ),
    CalendarRecord(
        title = "آیین نیایش پیر نارستانه",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 4,
        day = 2
    ),
    CalendarRecord(
        title = "گاهان بار میدیوشهیم‌گاه از ۸ تا ۱۲ تیر",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 4,
        day = 8
    ),
    CalendarRecord(
        title = "جشن تیرگان، آب پاشونک",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 4,
        day = 10
    ),
    CalendarRecord(
        title = "آیین نیایش پارس بانو از ۱۳ تا ۱۷ تیر",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 4,
        day = 13
    ),
    CalendarRecord(
        title = "جشن امردادگان، امشاسپند امرداد نگاهبان رستنی‌ها",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 5,
        day = 3
    ),
    CalendarRecord(
        title = "جشن چله تابستان",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 5,
        day = 6
    ),
    CalendarRecord(
        title = "آیین نیایش پیر نارکی از ۱۲ تا ۱۶ مرداد",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 5,
        day = 12
    ),
    CalendarRecord(
        title = "جشن شهریورگان، امشاسپند شهریور نگاهبان فلزات",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 5,
        day = 30
    ),
    CalendarRecord(
        title = "جشن خزان", type = EventType.AncientIran, isHoliday = false, month = 6, day = 3
    ),
    CalendarRecord(
        title = "گاهان‌بار پتیه‌شهیم‌گاه از ۲۱ تا ۲۵ شهریور",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 6,
        day = 21
    ),
    CalendarRecord(
        title = "جشن مهرگان", type = EventType.AncientIran, isHoliday = false, month = 7, day = 10
    ),
    CalendarRecord(
        title = "جشن آبانگان", type = EventType.AncientIran, isHoliday = false, month = 8, day = 4
    ),
    CalendarRecord(
        title = "روز کوروش بزرگ",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 8,
        day = 7
    ),
    CalendarRecord(
        title = "جشن پاییزانه", type = EventType.AncientIran, isHoliday = false, month = 8, day = 9
    ),
    CalendarRecord(
        title = "جشن گالشی", type = EventType.AncientIran, isHoliday = false, month = 8, day = 21
    ),
    CalendarRecord(
        title = "جشن آذرگان", type = EventType.AncientIran, isHoliday = false, month = 9, day = 3
    ),
    CalendarRecord(
        title = "اولین جشن دی‌گان",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 9,
        day = 25
    ),
    CalendarRecord(
        title = "جشن شب یلدا", type = EventType.AncientIran, isHoliday = false, month = 9, day = 30
    ),
    CalendarRecord(
        title = "دومین جشن دی‌گان",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 10,
        day = 2
    ),
    CalendarRecord(
        title = "جشن سیر و سور",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 10,
        day = 8
    ),
    CalendarRecord(
        title = "سومین جشن دی‌گان",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 10,
        day = 9
    ),
    CalendarRecord(
        title = "چهارمین جشن دی‌گان",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 10,
        day = 17
    ),
    CalendarRecord(
        title = "جشن بهمنگان، روز پدر، بهمن (منش نیک) امشاسپند",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 10,
        day = 26
    ),
    CalendarRecord(
        title = "جشن نوسده", type = EventType.AncientIran, isHoliday = false, month = 10, day = 29
    ),
    CalendarRecord(
        title = "جشن میانه زمستان",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 11,
        day = 9
    ),
    CalendarRecord(
        title = "جشن سده، آتش افروزی به هنگام غروب آفتاب",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 11,
        day = 10
    ),
    CalendarRecord(
        title = "جشن اسفندگان، روز مادر و روز عشق پاک",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 11,
        day = 29
    ),
    CalendarRecord(
        title = "جشن گلدان (اینجه)، روز درختکاری",
        type = EventType.AncientIran,
        isHoliday = false,
        month = 12,
        day = 14
    ),
)

public val islamicEvents: List<CalendarRecord> = listOf(
    CalendarRecord(
        title = "روز عاشورا", type = EventType.Afghanistan, isHoliday = true, month = 1, day = 10
    ),
    CalendarRecord(
        title = "روز میلاد نبی (ص)",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 3,
        day = 12
    ),
    CalendarRecord(
        title = "اول ماه مبارک رمضان",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 9,
        day = 1
    ),
    CalendarRecord(
        title = "روز حمایت از اطفال آسیب‌پذیر، یتیم و بی‌سرپرست",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 9,
        day = 15
    ),
    CalendarRecord(
        title = "روز گرامیداشت از نزول قرآن عظیم‌الشان",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 9,
        day = 27
    ),
    CalendarRecord(
        title = "عید سعید فطر", type = EventType.Afghanistan, isHoliday = true, month = 10, day = 1
    ),
    CalendarRecord(
        title = "عید سعید فطر، روز دوم",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 10,
        day = 2
    ),
    CalendarRecord(
        title = "عید سعید فطر، روز سوم",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 10,
        day = 3
    ),
    CalendarRecord(
        title = "روز عرفه و عید سعید اضحی",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 12,
        day = 9
    ),
    CalendarRecord(
        title = "روز عرفه و عید سعید اضحی",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 12,
        day = 10
    ),
    CalendarRecord(
        title = "روز عرفه و عید سعید اضحی",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 12,
        day = 11
    ),
    CalendarRecord(
        title = "روز عرفه و عید سعید اضحی",
        type = EventType.Afghanistan,
        isHoliday = true,
        month = 12,
        day = 12
    ),
    CalendarRecord(
        title = "آغاز سال هجری قمری", type = EventType.Iran, isHoliday = false, month = 1, day = 1
    ),
    CalendarRecord(
        title = "تاسوعای حسینی", type = EventType.Iran, isHoliday = true, month = 1, day = 9
    ),
    CalendarRecord(
        title = "عاشورای حسینی", type = EventType.Iran, isHoliday = true, month = 1, day = 10
    ),
    CalendarRecord(
        title = "روز تجلیل از اسرا و مفقودان",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 11
    ),
    CalendarRecord(
        title = "شهادت حضرت امام زین‌العابدین (ع) (۹۵ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 12
    ),
    CalendarRecord(
        title = "شهادت حضرت امام زین‌العابدین (ع) (۹۵ ه‍.ق) به روایتی",
        type = EventType.Iran,
        isHoliday = false,
        month = 1,
        day = 25
    ),
    CalendarRecord(
        title = "ولادت حضرت امام محمد باقر (ع) (۵۷ ﻫ.ق) به روایتی",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 3
    ),
    CalendarRecord(
        title = "شهادت حضرت امام حسن مجتبی (ع) (۵۰ ه‍.ق) (به روایتی)",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 7
    ),
    CalendarRecord(
        title = "روز بزرگداشت سلمان فارسی",
        type = EventType.Iran,
        isHoliday = false,
        month = 2,
        day = 7
    ),
    CalendarRecord(
        title = "اربعین حسینی", type = EventType.Iran, isHoliday = true, month = 2, day = 20
    ),
    CalendarRecord(
        title = "روز وقف", type = EventType.Iran, isHoliday = false, month = 2, day = 27
    ),
    CalendarRecord(
        title = "رحلت حضرت رسول اکرم (ص) (۱۱ ه‍.ق) – شهادت حضرت امام حسن مجتبی (ع) (۵۰ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = true,
        month = 2,
        day = 28
    ),
    CalendarRecord(
        title = "هجرت رسول اکرم (ص) از مکه به مدینه",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 1
    ),
    CalendarRecord(
        title = "شهادت حضرت امام حسن عسکری (ع) (۲۶۰ ه‍.ق) و آغاز امامت حضرت ولی عصر (عج)",
        type = EventType.Iran,
        isHoliday = true,
        month = 3,
        day = 8
    ),
    CalendarRecord(
        title = "ولادت حضرت رسول اکرم (ص) به روایت اهل سنت (۵۳ سال قبل از هجرت) - آغاز هفتهٔ وحدت",
        type = EventType.Iran,
        isHoliday = false,
        month = 3,
        day = 12
    ),
    CalendarRecord(
        title = "ولادت حضرت رسول اکرم (ص) (۵۳ سال قبل از هجرت) و روز اخلاق و مهرورزی",
        type = EventType.Iran,
        isHoliday = true,
        month = 3,
        day = 17
    ),
    CalendarRecord(
        title = "ولادت حضرت امام جعفر صادق (ع) مؤسس مذهب جعفری (۸۳ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = true,
        month = 3,
        day = 17
    ),
    CalendarRecord(
        title = "ولادت شاه عبدالعظیم حسنی (ع)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 4
    ),
    CalendarRecord(
        title = "ولادت حضرت امام حسن عسکری (ع) (۲۳۲ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 8
    ),
    CalendarRecord(
        title = "وفات حضرت معصومه (س) (۲۰۱ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 4,
        day = 10
    ),
    CalendarRecord(
        title = "ولادت حضرت زینب (س) (۵ ه‍.ق) و روز پرستار",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 5
    ),
    CalendarRecord(
        title = "وفات حضرت فاطمهٔ زهرا (س) (۱۱ ه‍.ق) به روایتی",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 13
    ),
    CalendarRecord(
        title = "وفات حضرت فاطمهٔ زهرا (س) (۱۱ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = true,
        month = 6,
        day = 3
    ),
    CalendarRecord(
        title = "سالروز وفات حضرت ام‌البنین (س) - روز تکریم مادران و همسران شهدا",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 13
    ),
    CalendarRecord(
        title = "روز زن و مادر و ولادت حضرت فاطمهٔ زهرا (س) (سال هشتم قبل از هجرت)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 20
    ),
    CalendarRecord(
        title = "تولد حضرت امام خمینی (ره) رهبر کبیر انقلاب اسلامی (۱۳۲۰ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 20
    ),
    CalendarRecord(
        title = "ولادت حضرت امام محمد باقر (ع) (۵۷ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 1
    ),
    CalendarRecord(
        title = "شهادت حضرت امام علی النقی الهادی (ع) (۲۵۴ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 3
    ),
    CalendarRecord(
        title = "ولادت حضرت امام محمد تقی (ع) «جوادالائمه» (۱۹۵ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 10
    ),
    CalendarRecord(
        title = "ولادت حضرت امام علی (ع) (۲۳ سال قبل از هجرت)",
        type = EventType.Iran,
        isHoliday = true,
        month = 7,
        day = 13
    ),
    CalendarRecord(
        title = "روز پدر", type = EventType.Iran, isHoliday = false, month = 7, day = 13
    ),
    CalendarRecord(
        title = "آغاز ایام‌البیض (اعتکاف)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 13
    ),
    CalendarRecord(
        title = "وفات حضرت زینب (س) (۶۲ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 15
    ),
    CalendarRecord(
        title = "تغییر قبلهٔ مسلمین از بیت‌المقدس به مکهٔ معظمه (۲ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 15
    ),
    CalendarRecord(
        title = "شهادت حضرت امام موسی کاظم (ع) (۱۸۳ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 7,
        day = 25
    ),
    CalendarRecord(
        title = "مبعث حضرت رسول اکرم (ص) (۱۳ سال قبل از هجرت)",
        type = EventType.Iran,
        isHoliday = true,
        month = 7,
        day = 27
    ),
    CalendarRecord(
        title = "ولادت حضرت امام حسین (ع) (۴ ه‍.ق) و روز پاسدار",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 3
    ),
    CalendarRecord(
        title = "ولادت حضرت ابوالفضل العباس (ع) (۲۶ ه‍.ق) و روز جانباز",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 4
    ),
    CalendarRecord(
        title = "ولادت حضرت امام زین‌العابدین (ع) (۳۸ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 5
    ),
    CalendarRecord(
        title = "روز صحیفهٔ سجادیه", type = EventType.Iran, isHoliday = false, month = 8, day = 5
    ),
    CalendarRecord(
        title = "ولادت حضرت علی اکبر (ع) (۳۳ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 11
    ),
    CalendarRecord(
        title = "روز جوان", type = EventType.Iran, isHoliday = false, month = 8, day = 11
    ),
    CalendarRecord(
        title = "ولادت حضرت قائم (عج) (۲۵۵ ه‍.ق) و روز جهانی مستضعفان",
        type = EventType.Iran,
        isHoliday = true,
        month = 8,
        day = 15
    ),
    CalendarRecord(
        title = "روز سربازان گمنام حضرت امام زمان (عج)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 15
    ),
    CalendarRecord(
        title = "وفات حضرت خدیجه (س) (۳ سال قبل از هجرت)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 10
    ),
    CalendarRecord(
        title = "ولادت حضرت امام حسن مجتبی (ع) (۳ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 15
    ),
    CalendarRecord(
        title = "روز اکرام و تکریم خیّرین",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 15
    ),
    CalendarRecord(
        title = "شب قدر", type = EventType.Iran, isHoliday = false, month = 9, day = 18
    ),
    CalendarRecord(
        title = "ضربت خوردن حضرت امام علی (ع) (۴۰ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 9,
        day = 19
    ),
    CalendarRecord(
        title = "روز نهج‌البلاغه", type = EventType.Iran, isHoliday = false, month = 9, day = 19
    ),
    CalendarRecord(
        title = "شب قدر", type = EventType.Iran, isHoliday = false, month = 9, day = 20
    ),
    CalendarRecord(
        title = "شهادت حضرت امام علی (ع) (۴۰ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = true,
        month = 9,
        day = 21
    ),
    CalendarRecord(
        title = "شب قدر", type = EventType.Iran, isHoliday = false, month = 9, day = 22
    ),
    CalendarRecord(
        title = "عید سعید فطر", type = EventType.Iran, isHoliday = true, month = 10, day = 1
    ),
    CalendarRecord(
        title = "تعطیل به مناسبت عید سعید فطر",
        type = EventType.Iran,
        isHoliday = true,
        month = 10,
        day = 2
    ),
    CalendarRecord(
        title = "روز فرهنگ پهلوانی و ورزش زورخانه‌ای",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 17
    ),
    CalendarRecord(
        title = "فتح اندلس به دست مسلمانان (۹۲ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 21
    ),
    CalendarRecord(
        title = "شهادت حضرت امام جعفر صادق (ع) (۱۴۸ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = true,
        month = 10,
        day = 25
    ),
    CalendarRecord(
        title = "ولادت حضرت معصومه (س) (۱۷۳ ه‍.ق) و روز دختران",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 1
    ),
    CalendarRecord(
        title = "آغاز دههٔ کرامت", type = EventType.Iran, isHoliday = false, month = 11, day = 1
    ),
    CalendarRecord(
        title = "روز تجلیل از امام‌زادگان و بقاع متبرکه",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 5
    ),
    CalendarRecord(
        title = "روز بزرگداشت حضرت صالح بن موسی کاظم (ع)",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 5
    ),
    CalendarRecord(
        title = "روز بزرگداشت حضرت احمدبن‌موسی شاهچراغ (ع)",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 6
    ),
    CalendarRecord(
        title = "ولادت حضرت امام رضا (ع) (۱۴۸ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 11
    ),
    CalendarRecord(
        title = "سالروز ازدواج حضرت امام علی (ع) و حضرت فاطمه (س) (۲ ه‍.ق) – روز ازدواج",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 1
    ),
    CalendarRecord(
        title = "شهادت زائران خانهٔ خدا به دست مأموران آل سعود (۱۳۶۶ ه‍.ش برابر با ۶ ذی‌الحجه ۱۴۰۷ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 6
    ),
    CalendarRecord(
        title = "شهادت حضرت امام محمد باقر (ع) (۱۱۴ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 7
    ),
    CalendarRecord(
        title = "روز عرفه (روز نیایش)",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 9
    ),
    CalendarRecord(
        title = "عید سعید قربان", type = EventType.Iran, isHoliday = true, month = 12, day = 10
    ),
    CalendarRecord(
        title = "آغاز دههٔ امامت و ولایت",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 10
    ),
    CalendarRecord(
        title = "ولادت حضرت امام علی النقی الهادی (ع) (۲۱۲ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 15
    ),
    CalendarRecord(
        title = "تعطیلات رسمی ایران(غدیر)",
        type = EventType.Iran,
        isHoliday = true,
        month = 12,
        day = 18
    ),
    CalendarRecord(
        title = "ولادت حضرت امام موسی کاظم (ع) (۱۲۸ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 20
    ),
    CalendarRecord(
        title = "روز مباهلهٔ پیامبر اسلام (ص) (۱۰ ه‍.ق)",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 24
    ),
    CalendarRecord(
        title = "روز خانواده و تکریم بازنشستگان",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 25
    ),
)

public val gregorianEvents: List<CalendarRecord> = listOf(
    CalendarRecord(
        title = "روز ملی مبارزه علیه مواد انفجاری تعبیه شده",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 1,
        day = 21
    ),
    CalendarRecord(
        title = "روز جهانی زن", type = EventType.Afghanistan, isHoliday = false, month = 3, day = 8
    ),
    CalendarRecord(
        title = "روز جهانی حقوق مستهلک",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 3,
        day = 15
    ),
    CalendarRecord(
        title = "روز جهانی آب", type = EventType.Afghanistan, isHoliday = false, month = 3, day = 22
    ),
    CalendarRecord(
        title = "روز جهانی هواشناسی",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 3,
        day = 23
    ),
    CalendarRecord(
        title = "روز بین‌المللی کارگر",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 5,
        day = 1
    ),
    CalendarRecord(
        title = "روز قلم", type = EventType.Afghanistan, isHoliday = false, month = 5, day = 6
    ),
    CalendarRecord(
        title = "روز جهانی تیلی‌کمیونیکیشن",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 5,
        day = 17
    ),
    CalendarRecord(
        title = "روز بین‌المللی موزیم‌ها",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 5,
        day = 18
    ),
    CalendarRecord(
        title = "روز بین‌المللی طفل",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 6,
        day = 1
    ),
    CalendarRecord(
        title = "هفته محیط زیست",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 6,
        day = 5
    ),
    CalendarRecord(
        title = "روز بین‌المللی پناهنده‌گان",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 6,
        day = 20
    ),
    CalendarRecord(
        title = "روز جهانی المپیک",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 6,
        day = 23
    ),
    CalendarRecord(
        title = "هفته مبارزه علیه مواد مخدر، روز جهانی مبارزه علیه مواد مخدر",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 6,
        day = 26
    ),
    CalendarRecord(
        title = "روز جهانی نفوس",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 7,
        day = 11
    ),
    CalendarRecord(
        title = "هفته تغذیه از شیر مادر",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 8,
        day = 1
    ),
    CalendarRecord(
        title = "روز بین المللی یادبود و گرامیداشت از قربانیان تروریزم",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 8,
        day = 21
    ),
    CalendarRecord(
        title = "روز بین‌المللی سواد",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 9,
        day = 8
    ),
    CalendarRecord(
        title = "روز جهانی صلح",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 9,
        day = 21
    ),
    CalendarRecord(
        title = "روز جهانی توریزم",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 9,
        day = 27
    ),
    CalendarRecord(
        title = "روز جهانی معلم",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 10,
        day = 5
    ),
    CalendarRecord(
        title = "روز جهانی پست",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 10,
        day = 9
    ),
    CalendarRecord(
        title = "روز جهانی کاهش خطرپذیری",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 10,
        day = 12
    ),
    CalendarRecord(
        title = "روز جهانی غذا",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 10,
        day = 16
    ),
    CalendarRecord(
        title = "روز جهانی ملل متحد",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 10,
        day = 24
    ),
    CalendarRecord(
        title = "روز جهانی محصلان",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 11,
        day = 17
    ),
    CalendarRecord(
        title = "روز جهانی هوانوردی",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 12,
        day = 7
    ),
    CalendarRecord(
        title = "روز جهانی مهاجرت",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 12,
        day = 18
    ),
    CalendarRecord(
        title = "روز جهانی سینما",
        type = EventType.Afghanistan,
        isHoliday = false,
        month = 12,
        day = 28
    ),
    CalendarRecord(
        title = "آغاز سال میلادی", type = EventType.Iran, isHoliday = false, month = 1, day = 1
    ),
    CalendarRecord(
        title = "روز جهانی گمرک", type = EventType.Iran, isHoliday = false, month = 1, day = 26
    ),
    CalendarRecord(
        title = "روز جهانی آب", type = EventType.Iran, isHoliday = false, month = 3, day = 22
    ),
    CalendarRecord(
        title = "روز جهانی هواشناسی", type = EventType.Iran, isHoliday = false, month = 3, day = 23
    ),
    CalendarRecord(
        title = "روز جهانی کار و کارگر",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 1
    ),
    CalendarRecord(
        title = "روز جهانی ماما", type = EventType.Iran, isHoliday = false, month = 5, day = 5
    ),
    CalendarRecord(
        title = "روز جهانی صلیب سرخ و هلال احمر",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 8
    ),
    CalendarRecord(
        title = "روز جهانی موزه و میراث فرهنگی",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 18
    ),
    CalendarRecord(
        title = "روز جهانی بدون دخانیات",
        type = EventType.Iran,
        isHoliday = false,
        month = 5,
        day = 31
    ),
    CalendarRecord(
        title = "روز جهانی والدین", type = EventType.Iran, isHoliday = false, month = 6, day = 1
    ),
    CalendarRecord(
        title = "روز جهانی محیط زیست", type = EventType.Iran, isHoliday = false, month = 6, day = 5
    ),
    CalendarRecord(
        title = "روز جهانی بیابان‌زدایی",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 17
    ),
    CalendarRecord(
        title = "روز جهانی مبارزه با مواد مخدر",
        type = EventType.Iran,
        isHoliday = false,
        month = 6,
        day = 26
    ),
    CalendarRecord(
        title = "روز جهانی شیر مادر", type = EventType.Iran, isHoliday = false, month = 8, day = 1
    ),
    CalendarRecord(
        title = "انفجار بمب اتمی آمریکا در هیروشیما با بیش از ۱۶۰هزار کشته و مجروح (۱۹۴۵ میلادی)",
        type = EventType.Iran,
        isHoliday = false,
        month = 8,
        day = 6
    ),
    CalendarRecord(
        title = "روز جهانی مسجد", type = EventType.Iran, isHoliday = false, month = 8, day = 21
    ),
    CalendarRecord(
        title = "روز جهانی جهانگردی", type = EventType.Iran, isHoliday = false, month = 9, day = 27
    ),
    CalendarRecord(
        title = "روز جهانی دریانوردی", type = EventType.Iran, isHoliday = false, month = 9, day = 30
    ),
    CalendarRecord(
        title = "روز جهانی ناشنوایان", type = EventType.Iran, isHoliday = false, month = 9, day = 30
    ),
    CalendarRecord(
        title = "روز جهانی سالمندان", type = EventType.Iran, isHoliday = false, month = 10, day = 1
    ),
    CalendarRecord(
        title = "روز جهانی کودک", type = EventType.Iran, isHoliday = false, month = 10, day = 8
    ),
    CalendarRecord(
        title = "روز جهانی پست", type = EventType.Iran, isHoliday = false, month = 10, day = 9
    ),
    CalendarRecord(
        title = "روز جهانی استاندارد",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 14
    ),
    CalendarRecord(
        title = "روز جهانی نابینایان (عصای سفید)",
        type = EventType.Iran,
        isHoliday = false,
        month = 10,
        day = 15
    ),
    CalendarRecord(
        title = "روز جهانی غذا", type = EventType.Iran, isHoliday = false, month = 10, day = 16
    ),
    CalendarRecord(
        title = "روز جهانی علم در خدمت صلح و توسعه",
        type = EventType.Iran,
        isHoliday = false,
        month = 11,
        day = 10
    ),
    CalendarRecord(
        title = "روز جهانی مبارزه با ایدز",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 1
    ),
    CalendarRecord(
        title = "روز جهانی معلولان", type = EventType.Iran, isHoliday = false, month = 12, day = 3
    ),
    CalendarRecord(
        title = "روز جهانی هواپیمایی", type = EventType.Iran, isHoliday = false, month = 12, day = 7
    ),
    CalendarRecord(
        title = "ولادت حضرت عیسی مسیح (ع)",
        type = EventType.Iran,
        isHoliday = false,
        month = 12,
        day = 25
    ),
    CalendarRecord(
        title = "روز جهانی بریل",
        type = EventType.International,
        isHoliday = false,
        month = 1,
        day = 4
    ),
    CalendarRecord(
        title = "روز جهانی آموزش",
        type = EventType.International,
        isHoliday = false,
        month = 1,
        day = 24
    ),
    CalendarRecord(
        title = "روز جهانی سرطان",
        type = EventType.International,
        isHoliday = false,
        month = 2,
        day = 4
    ),
    CalendarRecord(
        title = "روز جهانی مبارزه با ناقص‌سازی زنان",
        type = EventType.International,
        isHoliday = false,
        month = 2,
        day = 6
    ),
    CalendarRecord(
        title = "روز جهانی حبوبات",
        type = EventType.International,
        isHoliday = false,
        month = 2,
        day = 10
    ),
    CalendarRecord(
        title = "روز جهانی زن و دختر در علم",
        type = EventType.International,
        isHoliday = false,
        month = 2,
        day = 11
    ),
    CalendarRecord(
        title = "روز جهانی رادیو",
        type = EventType.International,
        isHoliday = false,
        month = 2,
        day = 13
    ),
    CalendarRecord(
        title = "روز جهانی عدالت اجتماعی",
        type = EventType.International,
        isHoliday = false,
        month = 2,
        day = 20
    ),
    CalendarRecord(
        title = "روز جهانی زبان مادری",
        type = EventType.International,
        isHoliday = false,
        month = 2,
        day = 21
    ),
    CalendarRecord(
        title = "روز جهانی بدون تبعیض",
        type = EventType.International,
        isHoliday = false,
        month = 3,
        day = 1
    ),
    CalendarRecord(
        title = "روز جهانی حیات وحش",
        type = EventType.International,
        isHoliday = false,
        month = 3,
        day = 3
    ),
    CalendarRecord(
        title = "روز جهانی زنان",
        type = EventType.International,
        isHoliday = false,
        month = 3,
        day = 8
    ),
    CalendarRecord(
        title = "روز جهانی شادی",
        type = EventType.International,
        isHoliday = false,
        month = 3,
        day = 20
    ),
    CalendarRecord(
        title = "روز جهانی شعر",
        type = EventType.International,
        isHoliday = false,
        month = 3,
        day = 21
    ),
    CalendarRecord(
        title = "روز جهانی سندروم داون",
        type = EventType.International,
        isHoliday = false,
        month = 3,
        day = 21
    ),
    CalendarRecord(
        title = "روز بین‌المللی جنگل‌ها",
        type = EventType.International,
        isHoliday = false,
        month = 3,
        day = 21
    ),
    CalendarRecord(
        title = "روز جهانی سل",
        type = EventType.International,
        isHoliday = false,
        month = 3,
        day = 24
    ),
    CalendarRecord(
        title = "روز بین‌المللی حق بر صحت و درستی دربارهٔ نقض فاحش حقوق بشر و منزلت قربانیان",
        type = EventType.International,
        isHoliday = false,
        month = 3,
        day = 24
    ),
    CalendarRecord(
        title = "روز بین‌المللی یادبود قربانیان بردگی و تجارت برده از آن سوی اقیانوس اطلس",
        type = EventType.International,
        isHoliday = false,
        month = 3,
        day = 25
    ),
    CalendarRecord(
        title = "روز جهانی سلامت",
        type = EventType.International,
        isHoliday = false,
        month = 4,
        day = 7
    ),
    CalendarRecord(
        title = "روز جهانی زمین پاک",
        type = EventType.International,
        isHoliday = false,
        month = 4,
        day = 22
    ),
    CalendarRecord(
        title = "روز جهانی کتاب و حق مؤلف",
        type = EventType.International,
        isHoliday = false,
        month = 4,
        day = 23
    ),
    CalendarRecord(
        title = "روز جهانی مالاریا",
        type = EventType.International,
        isHoliday = false,
        month = 4,
        day = 25
    ),
    CalendarRecord(
        title = "روز جهانی گرافیک",
        type = EventType.International,
        isHoliday = false,
        month = 4,
        day = 27
    ),
    CalendarRecord(
        title = "روز جهانی ایمنی و بهداشت حرفه‌ای",
        type = EventType.International,
        isHoliday = false,
        month = 4,
        day = 28
    ),
    CalendarRecord(
        title = "روز جهانی آزادی مطبوعات",
        type = EventType.International,
        isHoliday = false,
        month = 5,
        day = 3
    ),
    CalendarRecord(
        title = "روز جهانی پرستار",
        type = EventType.International,
        isHoliday = false,
        month = 5,
        day = 12
    ),
    CalendarRecord(
        title = "روز جهانی خانواده",
        type = EventType.International,
        isHoliday = false,
        month = 5,
        day = 15
    ),
    CalendarRecord(
        title = "روز جهانی زنبور",
        type = EventType.International,
        isHoliday = false,
        month = 5,
        day = 20
    ),
    CalendarRecord(
        title = "روز جهانی حافظان صلح ملل متحد",
        type = EventType.International,
        isHoliday = false,
        month = 5,
        day = 29
    ),
    CalendarRecord(
        title = "روز جهانی صنایع دستی",
        type = EventType.International,
        isHoliday = false,
        month = 6,
        day = 10
    ),
    CalendarRecord(
        title = "روز جهانی منع کار کودکان",
        type = EventType.International,
        isHoliday = false,
        month = 6,
        day = 12
    ),
    CalendarRecord(
        title = "روز جهانی اهدای خون",
        type = EventType.International,
        isHoliday = false,
        month = 6,
        day = 14
    ),
    CalendarRecord(
        title = "روز جهانی پناهندگان",
        type = EventType.International,
        isHoliday = false,
        month = 6,
        day = 20
    ),
    CalendarRecord(
        title = "روز جهانی موسیقی",
        type = EventType.International,
        isHoliday = false,
        month = 6,
        day = 21
    ),
    CalendarRecord(
        title = "روز جهانی قربانیان خشونت",
        type = EventType.International,
        isHoliday = false,
        month = 6,
        day = 26
    ),
    CalendarRecord(
        title = "روز جهانی جمعیت",
        type = EventType.International,
        isHoliday = false,
        month = 6,
        day = 26
    ),
    CalendarRecord(
        title = "روز جهانی جوانان",
        type = EventType.International,
        isHoliday = false,
        month = 8,
        day = 12
    ),
    CalendarRecord(
        title = "روز جهانی چپ دستان",
        type = EventType.International,
        isHoliday = false,
        month = 8,
        day = 13
    ),
    CalendarRecord(
        title = "روز جهانی انسان دوستی",
        type = EventType.International,
        isHoliday = false,
        month = 8,
        day = 19
    ),
    CalendarRecord(
        title = "روز جهانی عکاسی",
        type = EventType.International,
        isHoliday = false,
        month = 8,
        day = 19
    ),
    CalendarRecord(
        title = "روز جهانی باسوادی",
        type = EventType.International,
        isHoliday = false,
        month = 9,
        day = 8
    ),
    CalendarRecord(
        title = "روز جهانی جلوگیری از خودکشی",
        type = EventType.International,
        isHoliday = false,
        month = 9,
        day = 10
    ),
    CalendarRecord(
        title = "روز جهانی آلزایمر",
        type = EventType.International,
        isHoliday = false,
        month = 9,
        day = 21
    ),
    CalendarRecord(
        title = "روز جهانی صلح",
        type = EventType.International,
        isHoliday = false,
        month = 9,
        day = 21
    ),
    CalendarRecord(
        title = "روز جهانی ترجمه",
        type = EventType.International,
        isHoliday = false,
        month = 9,
        day = 30
    ),
    CalendarRecord(
        title = "روز بین‌المللی قهوه",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 1
    ),
    CalendarRecord(
        title = "روز جهانی معلم",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 5
    ),
    CalendarRecord(
        title = "روز جهانی بهداشت روان",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 10
    ),
    CalendarRecord(
        title = "روز جهانی دختران",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 11
    ),
    CalendarRecord(
        title = "روز جهانی هنر",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 15
    ),
    CalendarRecord(
        title = "روز ملل متحد و روز جهانی توسعه اطلاعات",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 24
    ),
    CalendarRecord(
        title = "روز جهانی هنرمند",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 25
    ),
    CalendarRecord(
        title = "روز جهانی میراث سمعی و بصری",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 27
    ),
    CalendarRecord(
        title = "روز جهانی کاردرمانی",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 27
    ),
    CalendarRecord(
        title = "روز جهانی شهرها",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 31
    ),
    CalendarRecord(
        title = "جشن هالووین",
        type = EventType.International,
        isHoliday = false,
        month = 10,
        day = 31
    ),
    CalendarRecord(
        title = "روز بین‌المللی پیشگیری از سوء استفاده از محیط زیست در جنگ و مناقشات مسلحانه",
        type = EventType.International,
        isHoliday = false,
        month = 11,
        day = 6
    ),
    CalendarRecord(
        title = "روز جهانی حسابداری",
        type = EventType.International,
        isHoliday = false,
        month = 11,
        day = 10
    ),
    CalendarRecord(
        title = "روز جهانی دیابت",
        type = EventType.International,
        isHoliday = false,
        month = 11,
        day = 14
    ),
    CalendarRecord(
        title = "روز جهانی تلویزیون",
        type = EventType.International,
        isHoliday = false,
        month = 11,
        day = 21
    ),
    CalendarRecord(
        title = "روز جهانی مبارزه با خشونت علیه زنان",
        type = EventType.International,
        isHoliday = false,
        month = 11,
        day = 25
    ),
    CalendarRecord(
        title = "روز جهانی همبستگی با مردم فلسطین",
        type = EventType.International,
        isHoliday = false,
        month = 11,
        day = 29
    ),
    CalendarRecord(
        title = "روز جهانی لغو برده‌داری",
        type = EventType.International,
        isHoliday = false,
        month = 12,
        day = 2
    ),
    CalendarRecord(
        title = "روز جهانی حقوق بشر",
        type = EventType.International,
        isHoliday = false,
        month = 12,
        day = 10
    ),
    CalendarRecord(
        title = "روز جهانی کوهستان",
        type = EventType.International,
        isHoliday = false,
        month = 12,
        day = 11
    ),
)

public val nepaliEvents: List<CalendarRecord> = listOf(
)

public val irregularRecurringEvents: List<Map<String, String>> = listOf(
    mapOf(
        "calendar" to "Persian",
        "rule" to "single event",
        "year" to "1401",
        "month" to "8",
        "day" to "3",
        "type" to "Iran",
        "title" to "خورشیدگرفتگی جزئی قابل مشاهده در ایران",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Persian",
        "rule" to "second week of month",
        "month" to "1",
        "type" to "Afghanistan",
        "title" to "هفته جیولوجست‌های افغانستان (هفته دوم حمل)",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Persian",
        "rule" to "last week of month",
        "month" to "2",
        "type" to "Afghanistan",
        "title" to "هفته کتاب‌خوانی (هفته اخیر ثور)",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Gregorian",
        "rule" to "week from day",
        "day" to "5",
        "month" to "6",
        "type" to "Afghanistan",
        "title" to "هفته محیط زیست (۵-۱۱ جون)",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Gregorian",
        "rule" to "week from day",
        "day" to "26",
        "month" to "6",
        "type" to "Afghanistan",
        "title" to "هفته مبارزه علیه مواد مخدر که از تاریخ ۲۶ جون (روز جهانی مبارزه علیه مواد مخدر) آغاز می‌گردد",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Gregorian",
        "rule" to "week from day",
        "day" to "1",
        "month" to "8",
        "type" to "Afghanistan",
        "title" to "هفته تغذیه از شیر مادر (۱-۷ آگست)",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Persian",
        "rule" to "week from day",
        "day" to "14",
        "month" to "10",
        "type" to "Afghanistan",
        "title" to "هفته قانون اساسی افغانستان (۱۴-۲۰ جدی)",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Hijri",
        "rule" to "last weekday of month",
        "weekday" to "7",
        "month" to "9",
        "type" to "Iran",
        "title" to "روز جهانی قدس (آخرین جمعهٔ ماه رمضان)",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Persian",
        "rule" to "last weekday of month",
        "weekday" to "4",
        "month" to "12",
        "type" to "AncientIran",
        "title" to "چهارشنبه‌سوری (آخرین سه‌شنبه سال)",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Gregorian",
        "rule" to "last weekday of month",
        "weekday" to "7",
        "month" to "11",
        "type" to "International",
        "title" to "جمعهٔ سیاه یا بلک فرایدی (آخرین جمعهٔ ماه نوامبر)",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Gregorian",
        "rule" to "nth day from",
        "nth" to "256",
        "month" to "1",
        "day" to "1",
        "type" to "International",
        "title" to "روز جهانی برنامه‌نویس (روز ۲۵۶م سال میلادی)",
        "holiday" to "false",
    ),
    mapOf(
        "calendar" to "Hijri",
        "rule" to "end of month",
        "month" to "2",
        "type" to "Iran",
        "title" to "شهادت حضرت امام رضا (ع) (۲۰۳ ه‍.ق) (۳۰ صَفَر یا انتهای ماه)",
        "holiday" to "true",
    ),
    mapOf(
        "calendar" to "Hijri",
        "rule" to "end of month",
        "month" to "11",
        "type" to "Iran",
        "title" to "شهادت حضرت امام محمد تقی (ع) (۲۲۰ ه‍.ق) (۳۰ ذی‌القعده یا انتهای ماه)",
        "holiday" to "false",
    ),
)
