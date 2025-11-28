package com.byagowi.persiancalendar.generated

import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public enum class EventSource(
  public val link: String,
) {
  Afghanistan("https://w.mudl.gov.af/sites/default/files/2020-03/Calendar%202020%20Website.pdf"),
  Iran("https://calendar.ut.ac.ir/documents/2139738/7092644/Calendar-1405.pdf"),
  AncientIran("~https://raw.githubusercontent.com/ilius/starcal/master/plugins/iran-ancient-data.txt"),
  International("~https://www.un.org/en/sections/observances/international-days/"),
  Nepal(""),
  ;
}

public class CalendarRecord(
  public val title: String,
  public val source: EventSource,
  public val isHoliday: Boolean,
  public val month: Int,
  public val day: Int,
)

public val persianEvents: List<CalendarRecord> = listOf(
  CalendarRecord(
    title = "نوروز",
    source = EventSource.Afghanistan, isHoliday = true, month = 1, day = 1
  ),
  CalendarRecord(
    title = "جشن دهقان",
    source = EventSource.Afghanistan, isHoliday = true, month = 1, day = 2
  ),
  CalendarRecord(
    title = "روز زنگ مکتب",
    source = EventSource.Afghanistan, isHoliday = false, month = 1, day = 3
  ),
  CalendarRecord(
    title = "روز مطبوعات",
    source = EventSource.Afghanistan, isHoliday = false, month = 2, day = 5
  ),
  CalendarRecord(
    title = "کودتای حزب دموکراتیک خلق افغانستان (سال ۱۳۵۷)",
    source = EventSource.Afghanistan, isHoliday = false, month = 2, day = 7
  ),
  CalendarRecord(
    title = "پیروزی جهاد مقدس افغانستان (سال ۱۳۷۱)",
    source = EventSource.Afghanistan, isHoliday = true, month = 2, day = 8
  ),
  CalendarRecord(
    title = "روز مادر",
    source = EventSource.Afghanistan, isHoliday = false, month = 3, day = 24
  ),
  CalendarRecord(
    title = "روز استرداد استقلال کشور (سال ۱۲۹۸ مصادف با ۱۹۱۹ میلادی)",
    source = EventSource.Afghanistan, isHoliday = true, month = 5, day = 28
  ),
  CalendarRecord(
    title = "روز همبستگی با برادران پشتون و بلوچ",
    source = EventSource.Afghanistan, isHoliday = false, month = 6, day = 9
  ),
  CalendarRecord(
    title = "روز شهادت قهرمان ملی افغانستان (آغاز هفتهٔ شهید)",
    source = EventSource.Afghanistan, isHoliday = true, month = 6, day = 18
  ),
  CalendarRecord(
    title = "روز هنر",
    source = EventSource.Afghanistan, isHoliday = false, month = 7, day = 16
  ),
  CalendarRecord(
    title = "روز جیودیزیست‌های کشور",
    source = EventSource.Afghanistan, isHoliday = false, month = 7, day = 21
  ),
  CalendarRecord(
    title = "آغاز هفتهٔ مخصوص سره میاشت",
    source = EventSource.Afghanistan, isHoliday = false, month = 7, day = 24
  ),
  CalendarRecord(
    title = "روز ملی زبان اوزبیکی",
    source = EventSource.Afghanistan, isHoliday = false, month = 7, day = 29
  ),
  CalendarRecord(
    title = "روز وحدت ملی و شهادت شش عضو ولسی جرگه شورای ملی در بغلان (سال ۱۳۸۶)",
    source = EventSource.Afghanistan, isHoliday = false, month = 8, day = 15
  ),
  CalendarRecord(
    title = "روز تحقیق و پژوهش",
    source = EventSource.Afghanistan, isHoliday = false, month = 9, day = 13
  ),
  CalendarRecord(
    title = "روز تجاوز ارتش اتحاد شوروی سابق بر حریم کشور (سال ۱۳۵۸)",
    source = EventSource.Afghanistan, isHoliday = false, month = 10, day = 6
  ),
  CalendarRecord(
    title = "هفتهٔ قانون اساسی افغانستان (۱۴-۲۰ جدی)",
    source = EventSource.Afghanistan, isHoliday = false, month = 10, day = 14
  ),
  CalendarRecord(
    title = "روز متعاقدین",
    source = EventSource.Afghanistan, isHoliday = false, month = 11, day = 10
  ),
  CalendarRecord(
    title = "روز شکست و خروج ارتش اتحاد شوروی سابق از افغانستان",
    source = EventSource.Afghanistan, isHoliday = true, month = 11, day = 26
  ),
  CalendarRecord(
    title = "روز تکبیر و قیام مردم کابل در برابر تجاوز ارتش اتحاد شوروی سابق",
    source = EventSource.Afghanistan, isHoliday = false, month = 12, day = 3
  ),
  CalendarRecord(
    title = "روز ملی حمایت از نیروهای دفاعی و امنیتی کشور",
    source = EventSource.Afghanistan, isHoliday = false, month = 12, day = 9
  ),
  CalendarRecord(
    title = "روز حفاظت از میراث‌های فرهنگی کشور",
    source = EventSource.Afghanistan, isHoliday = false, month = 12, day = 20
  ),
  CalendarRecord(
    title = "روز شهید وحدت ملی استاد عبدالعلی مزاری (۱۳۷۳)",
    source = EventSource.Afghanistan, isHoliday = false, month = 12, day = 22
  ),
  CalendarRecord(
    title = "روز قیام مردم هرات بر علیه جمهوری دموکراتیک خلق افغانستان",
    source = EventSource.Afghanistan, isHoliday = false, month = 12, day = 24
  ),
  CalendarRecord(
    title = "روز ملی خبرنگار",
    source = EventSource.Afghanistan, isHoliday = false, month = 12, day = 27
  ),
  CalendarRecord(
    title = "آغاز نوروز",
    source = EventSource.Iran, isHoliday = true, month = 1, day = 1
  ),
  CalendarRecord(
    title = "نوروز",
    source = EventSource.Iran, isHoliday = true, month = 1, day = 2
  ),
  CalendarRecord(
    title = "هجوم مأموران ستم‌شاهی پهلوی به مدرسهٔ فیضیهٔ قم (۱۳۴۲ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 2
  ),
  CalendarRecord(
    title = "آغاز عملیات فتح‌المبین (۱۳۶۱ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 2
  ),
  CalendarRecord(
    title = "نوروز",
    source = EventSource.Iran, isHoliday = true, month = 1, day = 3
  ),
  CalendarRecord(
    title = "نوروز",
    source = EventSource.Iran, isHoliday = true, month = 1, day = 4
  ),
  CalendarRecord(
    title = "زادروز زرتشت پیامبر",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 6
  ),
  CalendarRecord(
    title = "روز هنرهای نمایشی",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 7
  ),
  CalendarRecord(
    title = "روز جمهوری اسلامی ایران",
    source = EventSource.Iran, isHoliday = true, month = 1, day = 12
  ),
  CalendarRecord(
    title = "روز طبیعت",
    source = EventSource.Iran, isHoliday = true, month = 1, day = 13
  ),
  CalendarRecord(
    title = "روز ذخایر ژنتیکی و زیستی",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 15
  ),
  CalendarRecord(
    title = "روز سلامتی",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 18
  ),
  CalendarRecord(
    title = "شهادت آیت‌اللّه سیدمحمدباقر صدر و خواهر ایشان بنت‌الهدی به دست حکومت بعث عراق (۱۳۵۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 19
  ),
  CalendarRecord(
    title = "روز ملی فناوری هسته‌ای",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 20
  ),
  CalendarRecord(
    title = "شهادت سید مرتضی آوینی",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 20
  ),
  CalendarRecord(
    title = "روز هنر انقلاب اسلامی",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 20
  ),
  CalendarRecord(
    title = "شهادت امیر سپهبد علی صیاد شیرازی (۱۳۷۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 21
  ),
  CalendarRecord(
    title = "سالروز افتتاح حساب شمارهٔ ۱۰۰ به فرمان حضرت امام خمینی (ره) و تأسیس بنیاد مسکن انقلاب اسلامی (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 21
  ),
  CalendarRecord(
    title = "روز بزرگداشت عطار نیشابوری",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 25
  ),
  CalendarRecord(
    title = "روز ارتش جمهوری اسلامی و نیروی زمینی",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 29
  ),
  CalendarRecord(
    title = "روز گندم و نان",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 31
  ),
  CalendarRecord(
    title = "روز بزرگداشت سعدی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 1
  ),
  CalendarRecord(
    title = "روز نثر فارسی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 1
  ),
  CalendarRecord(
    title = "روز شهدای ورزشکار",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 1
  ),
  CalendarRecord(
    title = "تأسیس سپاه پاسداران انقلاب اسلامی (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 2
  ),
  CalendarRecord(
    title = "سالروز اعلام انقلاب فرهنگی (۱۳۵۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 2
  ),
  CalendarRecord(
    title = "روز زمین پاک",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 2
  ),
  CalendarRecord(
    title = "روز بزرگداشت شیخ بهایی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 3
  ),
  CalendarRecord(
    title = "روز معماری",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 3
  ),
  CalendarRecord(
    title = "سالروز شهادت امیر سپهبد قرنی (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 3
  ),
  CalendarRecord(
    title = "شکست حملهٔ نظامی آمریکا به ایران در طبس (۱۳۵۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 5
  ),
  CalendarRecord(
    title = "روز ایمنی حمل و نقل",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 7
  ),
  CalendarRecord(
    title = "روز شوراها",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 9
  ),
  CalendarRecord(
    title = "روز ملی خلیج فارس",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 10
  ),
  CalendarRecord(
    title = "آغاز عملیات بیت‌المقدس (۱۳۶۱ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 10
  ),
  CalendarRecord(
    title = "شهادت استاد مرتضی مطهری (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 12
  ),
  CalendarRecord(
    title = "روز معلم",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 12
  ),
  CalendarRecord(
    title = "روز بزرگداشت شیخ صدوق",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 15
  ),
  CalendarRecord(
    title = "روز بیماری‌های خاص و صعب‌العلاج",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 18
  ),
  CalendarRecord(
    title = "روز بزرگداشت شیخ کلینی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 19
  ),
  CalendarRecord(
    title = "روز اسناد ملی و میراث مکتوب",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 19
  ),
  CalendarRecord(
    title = "روز گل محمدی و گلاب",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 20
  ),
  CalendarRecord(
    title = "لغو امتیاز تنباکو به فتوای آیت‌الله میرزا حسن شیرازی (۱۲۷۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 24
  ),
  CalendarRecord(
    title = "روز پاسداشت زبان فارسی و بزرگداشت حکیم ابوالقاسم فردوسی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 25
  ),
  CalendarRecord(
    title = "روز ارتباطات و روابط عمومی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 27
  ),
  CalendarRecord(
    title = "روز بزرگداشت حکیم عمر خیام",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 28
  ),
  CalendarRecord(
    title = "روز ملی جمعیت",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 30
  ),
  CalendarRecord(
    title = "روز اهدای عضو، اهدای زندگی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 31
  ),
  CalendarRecord(
    title = "روز بهره‌وری و بهینه‌سازی مصرف",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 1
  ),
  CalendarRecord(
    title = "روز بزرگداشت ملاصدرا (صدرالمتألهین)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 1
  ),
  CalendarRecord(
    title = "فتح خرمشهر در عملیات بیت‌المقدس (۱۳۶۱ ه‍.ش) و روز مقاومت، ایثار و پیروزی",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 3
  ),
  CalendarRecord(
    title = "روز مقاومت و پایداری",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 4
  ),
  CalendarRecord(
    title = "روز دزفول",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 4
  ),
  CalendarRecord(
    title = "روز نسیم مهر (روز حمایت از خانواده زندانیان)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 5
  ),
  CalendarRecord(
    title = "افتتاح اولین دورهٔ مجلس شورای اسلامی (۱۳۵۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 7
  ),
  CalendarRecord(
    title = "رحلت حضرت امام خمینی (ره) رهبر کبیر انقلاب و بنیان‌گذار جمهوری اسلامی ایران (۱۳۶۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = true, month = 3, day = 14
  ),
  CalendarRecord(
    title = "انتخاب حضرت آیت‌الله امام خامنه‌ای به رهبری (۱۳۶۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 14
  ),
  CalendarRecord(
    title = "قیام خونین ۱۵ خرداد (۱۳۴۲ ه‍.ش)",
    source = EventSource.Iran, isHoliday = true, month = 3, day = 15
  ),
  CalendarRecord(
    title = "زندانی شدن حضرت امام خمینی (ره) به دست مأموران ستم شاهی پهلوی (۱۳۴۲ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 15
  ),
  CalendarRecord(
    title = "روز صنایع دستی",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 20
  ),
  CalendarRecord(
    title = "روز ملی فرش",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 20
  ),
  CalendarRecord(
    title = "شهادت آیت‌الله سعیدی به دست مأموران ستم‌شاهی پهلوی (۱۳۴۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 20
  ),
  CalendarRecord(
    title = "روز شهدای اقتدار و اتحاد ملی ایران",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 23
  ),
  CalendarRecord(
    title = "شهادت سربازان دلیر اسلام: بخارایی، امانی، صفار هرندی و نیک‌نژاد (۱۳۴۴ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 26
  ),
  CalendarRecord(
    title = "روز جهاد کشاورزی (تشکیل جهاد سازندگی به فرمان حضرت امام خمینی (ره)) (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 27
  ),
  CalendarRecord(
    title = "درگذشت دکتر علی شریعتی (۱۳۵۶ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 29
  ),
  CalendarRecord(
    title = "شهادت زائران حرم رضوی (ع) به دست ایادی آمریکا (عاشورای ۱۳۷۳ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 30
  ),
  CalendarRecord(
    title = "شهادت دکتر مصطفی چمران (۱۳۶۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 31
  ),
  CalendarRecord(
    title = "روز بسیج استادان",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 31
  ),
  CalendarRecord(
    title = "سالروز صدور فرمان حضرت امام خمینی رحمة‌الله علیه مبنی بر تأسیس سازمان تبلیغات اسلامی (۱۳۶۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 1
  ),
  CalendarRecord(
    title = "روز تبلیغ و اطلاع‌رسانی دینی",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 1
  ),
  CalendarRecord(
    title = "روز اصناف",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 1
  ),
  CalendarRecord(
    title = "شهادت مظلومانهٔ آیت‌الله دکتر بهشتی و ۷۲ تن از یاران حضرت امام خمینی (ره) با انفجار بمب به دست منافقان در دفتر مرکزی حزب جمهوری اسلامی (۱۳۶۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 7
  ),
  CalendarRecord(
    title = "روز قوهٔ قضائیه",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 7
  ),
  CalendarRecord(
    title = "بمباران شیمیایی شهر سردشت (۱۳۶۶ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 7
  ),
  CalendarRecord(
    title = "روز مبارزه با سلاح‌های شیمیایی و میکروبی",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 8
  ),
  CalendarRecord(
    title = "روز صنعت و معدن",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 10
  ),
  CalendarRecord(
    title = "روز دیپلماسی فرهنگی و تعامل با جهان",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 10
  ),
  CalendarRecord(
    title = "روز آزادسازی شهر مهران",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 10
  ),
  CalendarRecord(
    title = "روز بزرگداشت صائب تبریزی",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 10
  ),
  CalendarRecord(
    title = "یاد روز ورود حضرت امام رضا (ع) به نیشابور و نقل حدیث سلسلةالذهب",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 10
  ),
  CalendarRecord(
    title = "شهادت چهارمین شهید محراب، آیت‌الله صدوقی به دست منافقان (۱۳۶۱ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 11
  ),
  CalendarRecord(
    title = "حملهٔ ددمنشانهٔ ناوگان آمریکای جنایتکار به هواپیمای مسافربری جمهوری اسلامی ایران (۱۳۶۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 12
  ),
  CalendarRecord(
    title = "روز افشای حقوق بشر آمریکایی",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 12
  ),
  CalendarRecord(
    title = "روز بزرگداشت علامه امینی (۱۳۴۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 12
  ),
  CalendarRecord(
    title = "روز قلم",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 14
  ),
  CalendarRecord(
    title = "روز شهرداری و دهیاری",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 14
  ),
  CalendarRecord(
    title = "روز مالیات",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 16
  ),
  CalendarRecord(
    title = "روز ادبیات کودکان و نوجوانان",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 18
  ),
  CalendarRecord(
    title = "کشف توطئهٔ آمریکایی در پایگاه هوایی شهید نوژه (کودتای نافرجام نقاب) (۱۳۵۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 18
  ),
  CalendarRecord(
    title = "روز عفاف و حجاب",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 21
  ),
  CalendarRecord(
    title = "حمله به مسجد گوهرشاد و کشتار مردم به دست رضاخان (۱۳۱۴ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 21
  ),
  CalendarRecord(
    title = "روز بزرگداشت خوارزمی",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 22
  ),
  CalendarRecord(
    title = "روز فناوری اطلاعات",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 22
  ),
  CalendarRecord(
    title = "روز گفت‌وگو و تعامل سازنده با جهان",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 23
  ),
  CalendarRecord(
    title = "گشایش نخستین مجلس خبرگان رهبری (۱۳۶۲ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 23
  ),
  CalendarRecord(
    title = "روز بهزیستی و تأمین اجتماعی",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 25
  ),
  CalendarRecord(
    title = "سالروز تأسیس نهاد شورای نگهبان (۱۳۵۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 26
  ),
  CalendarRecord(
    title = "اعلام پذیرش قطعنامهٔ ۵۹۸ شورای امنیت از سوی ایران (۱۳۶۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 27
  ),
  CalendarRecord(
    title = "روز بزرگداشت آیت‌الله سید ابوالقاسم کاشانی",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 30
  ),
  CalendarRecord(
    title = "روز بزرگداشت شیخ صفی‌الدین اردبیلی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 4
  ),
  CalendarRecord(
    title = "سالروز عملیات افتخار‌آفرین مرصاد (۱۳۶۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 5
  ),
  CalendarRecord(
    title = "روز اقامهٔ اولین نماز جمعه با حکم حضرت امام خمینی (ره) در سال ۱۳۵۸",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 5
  ),
  CalendarRecord(
    title = "روز کارآفرینی و آموزش‌های فنی‌و‌حرفه‌ای",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 6
  ),
  CalendarRecord(
    title = "روز شعر و ادبیات آیینی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 8
  ),
  CalendarRecord(
    title = "روز بزرگداشت محتشم کاشانی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 8
  ),
  CalendarRecord(
    title = "روز بزرگداشت شیخ شهاب‌الدین سهروردی (شیخ اشراق)",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 8
  ),
  CalendarRecord(
    title = "روز زنجان",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 8
  ),
  CalendarRecord(
    title = "روز اهدای خون",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 9
  ),
  CalendarRecord(
    title = "شهادت آیت‌الله شیخ فضل‌الله نوری (۱۲۸۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 11
  ),
  CalendarRecord(
    title = "صدور فرمان مشروطیت (۱۲۸۵ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 14
  ),
  CalendarRecord(
    title = "روز حقوق بشر اسلامی و کرامت انسانی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 14
  ),
  CalendarRecord(
    title = "سالروز شهادت امیر سرلشکر خلبان عباس بابایی (۱۳۶۶ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 15
  ),
  CalendarRecord(
    title = "تشکیل جهاد دانشگاهی (۱۳۵۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 16
  ),
  CalendarRecord(
    title = "سالروز شهادت محمود صارمی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 17
  ),
  CalendarRecord(
    title = "روز خبرنگار",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 17
  ),
  CalendarRecord(
    title = "روز بزرگداشت شهدای مدافع حرم",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 18
  ),
  CalendarRecord(
    title = "روز حمایت از صنایع کوچک",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 21
  ),
  CalendarRecord(
    title = "روز تشکل‌ها و مشارکت‌های اجتماعی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 22
  ),
  CalendarRecord(
    title = "روز مقاومت اسلامی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 23
  ),
  CalendarRecord(
    title = "آغاز بازگشت آزادگان به میهن اسلامی (۱۳۶۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 26
  ),
  CalendarRecord(
    title = "کودتای آمریکا برای بازگرداندن شاه (۱۳۳۲ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 28
  ),
  CalendarRecord(
    title = "گشایش مجلس خبرگان برای بررسی نهایی قانون اساسی جمهوری اسلامی ایران (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 28
  ),
  CalendarRecord(
    title = "روز بزرگداشت علامهٔ مجلسی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 30
  ),
  CalendarRecord(
    title = "روز صنعت دفاعی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 31
  ),
  CalendarRecord(
    title = "روز عسل",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 31
  ),
  CalendarRecord(
    title = "روز بزرگداشت ابوعلی سینا",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 1
  ),
  CalendarRecord(
    title = "روز پزشک",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 1
  ),
  CalendarRecord(
    title = "روز همدان",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 1
  ),
  CalendarRecord(
    title = "آغاز هفتهٔ دولت",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 2
  ),
  CalendarRecord(
    title = "شهادت سید ‌علی اندرزگو (در روز ۱۹ ماه مبارک رمضان) (۱۳۵۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 2
  ),
  CalendarRecord(
    title = "اِشغال ایران توسط متفقین و فرار رضاخان (۱۳۲۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 3
  ),
  CalendarRecord(
    title = "روز کارمند",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 4
  ),
  CalendarRecord(
    title = "روز بزرگداشت محمدبن زکریای رازی",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 5
  ),
  CalendarRecord(
    title = "روز داروسازی",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 5
  ),
  CalendarRecord(
    title = "روز کُشتی",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 5
  ),
  CalendarRecord(
    title = "انفجار دفتر نخست‌وزیری به دست منافقان و شهادت مظلومانهٔ شهیدان رجایی و باهنر (۱۳۶۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 8
  ),
  CalendarRecord(
    title = "روز مبارزه با تروریسم",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 8
  ),
  CalendarRecord(
    title = "سالروز تصویب قانون عملیات بانکی بدون ربا (۱۳۶۲ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 10
  ),
  CalendarRecord(
    title = "روز بانکداری اسلامی",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 10
  ),
  CalendarRecord(
    title = "روز تشکیل قرارگاه پدافند هوایی حضرت خاتم‌الانبیا (ص) (۱۳۷۱ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 10
  ),
  CalendarRecord(
    title = "روز صنعت چاپ",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 11
  ),
  CalendarRecord(
    title = "سالروز شهادت رئیسعلی دلواری (۱۲۹۴ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 12
  ),
  CalendarRecord(
    title = "روز مبارزه با استعمار انگلیس",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 12
  ),
  CalendarRecord(
    title = "روز بهوَرز",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 12
  ),
  CalendarRecord(
    title = "روز تعاون",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 13
  ),
  CalendarRecord(
    title = "روز بزرگداشت ابوریحان بیرونی",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 13
  ),
  CalendarRecord(
    title = "روز علوم پایه",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 13
  ),
  CalendarRecord(
    title = "روز مردم‌شناسی",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 13
  ),
  CalendarRecord(
    title = "سالروز زلزله فردوس در سال ۱۳۴۷",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 13
  ),
  CalendarRecord(
    title = "روز حرکت‌های جهادی و امداد مردمی",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 13
  ),
  CalendarRecord(
    title = "شهادت آیت‌الله قدوسی و سرتیپ وحید دستجردی (۱۳۶۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 14
  ),
  CalendarRecord(
    title = "قیام ۱۷ شهریور و کشتار جمعی از مردم به‌دست مأموران ستم‌شاهی پهلوی (۱۳۵۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 17
  ),
  CalendarRecord(
    title = "وفات آیت‌الله سیدمحمود طالقانی اولین امام جمعهٔ تهران (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 19
  ),
  CalendarRecord(
    title = "شهادت دومین شهید محراب، آیت‌الله مدنی به دست منافقان (۱۳۶۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 20
  ),
  CalendarRecord(
    title = "روز سینما",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 21
  ),
  CalendarRecord(
    title = "روز خرما",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 25
  ),
  CalendarRecord(
    title = "روز شعر و ادب فارسی",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 27
  ),
  CalendarRecord(
    title = "روز بزرگداشت استاد سید‌ محمد‌حسین شهریار",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 27
  ),
  CalendarRecord(
    title = "آغاز جنگ تحمیلی (۱۳۵۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 31
  ),
  CalendarRecord(
    title = "آغاز هفتهٔ دفاع مقدس",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 31
  ),
  CalendarRecord(
    title = "روز پرچم",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 1
  ),
  CalendarRecord(
    title = "روز بزرگداشت شهدای منا",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 2
  ),
  CalendarRecord(
    title = "روز سرباز",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 4
  ),
  CalendarRecord(
    title = "شکست حصر آبادان در عملیات ثامن‌الائمه (ع) (۱۳۶۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 5
  ),
  CalendarRecord(
    title = "روز گردشگری",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 5
  ),
  CalendarRecord(
    title = "شهادت سرداران اسلام: فلاحی، فکوری، نامجو، کلاهدوز و جهان‌آرا (۱۳۶۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 7
  ),
  CalendarRecord(
    title = "روز بزرگداشت فرماندهان شهید دفاع مقدس",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 7
  ),
  CalendarRecord(
    title = "روز آتش‌نشانی و ایمنی",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 7
  ),
  CalendarRecord(
    title = "روز بزرگداشت شمس",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 7
  ),
  CalendarRecord(
    title = "روز بزرگداشت مولوی",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 8
  ),
  CalendarRecord(
    title = "روز نخبگان",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 10
  ),
  CalendarRecord(
    title = "هجرت حضرت امام خمینی (ره) از عراق به پاریس (۱۳۵۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 13
  ),
  CalendarRecord(
    title = "روز نیروی انتظامی",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 13
  ),
  CalendarRecord(
    title = "روز دامپزشکی",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 14
  ),
  CalendarRecord(
    title = "روز روستا و عشایر",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 15
  ),
  CalendarRecord(
    title = "روز جهانی حماسهٔ فلسطین",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 15
  ),
  CalendarRecord(
    title = "طوفان الاقصی",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 15
  ),
  CalendarRecord(
    title = "روز بزرگداشت حافظ",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 20
  ),
  CalendarRecord(
    title = "شهادت پنجمین شهید محراب، آیت‌الله اشرفی اصفهانی به دست منافقان (۱۳۶۱ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 23
  ),
  CalendarRecord(
    title = "روز ملی پارالمپیک",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 24
  ),
  CalendarRecord(
    title = "روز پیوند اولیا و مربیان",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 24
  ),
  CalendarRecord(
    title = "سالروز واقعهٔ به آتش کشیدن مسجد جامع شهر کرمان به دست دژخیمان حکومت پهلوی (۱۳۵۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 24
  ),
  CalendarRecord(
    title = "روز نسل‌کشی کودکان و زنان فلسطینی",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 25
  ),
  CalendarRecord(
    title = "روز تربیت‌بدنی و ورزش",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 26
  ),
  CalendarRecord(
    title = "روز صادرات",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 29
  ),
  CalendarRecord(
    title = "شهادت مظلومانهٔ آیت‌الله حاج سید مصطفی خمینی (۱۳۵۶ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 1
  ),
  CalendarRecord(
    title = "روز آمار و برنامه‌ریزی",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 1
  ),
  CalendarRecord(
    title = "روز بزرگداشت ابوالفضل بیهقی",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 1
  ),
  CalendarRecord(
    title = "اعتراض و افشاگری حضرت امام خمینی (ره) علیه پذیرش کاپیتولاسیون (۱۳۴۳ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 4
  ),
  CalendarRecord(
    title = "روز زعفران",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 5
  ),
  CalendarRecord(
    title = "روز انار",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 7
  ),
  CalendarRecord(
    title = "شهادت محمدحسین فهمیده (بسیجی ۱۳ ساله) (۱۳۵۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 8
  ),
  CalendarRecord(
    title = "روز نوجوان و بسیج دانش‌آموزی",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 8
  ),
  CalendarRecord(
    title = "روز پدافند غیرعامل",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 8
  ),
  CalendarRecord(
    title = "شهادت اولین شهید محراب، آیت‌الله قاضی طباطبایی به دست منافقان (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 10
  ),
  CalendarRecord(
    title = "تسخیر لانهٔ جاسوسی آمریکا به دست دانشجویان پیرو خط حضرت امام (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 13
  ),
  CalendarRecord(
    title = "روز ملی مبارزه با استکبار جهانی",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 13
  ),
  CalendarRecord(
    title = "روز دانش‌آموز",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 13
  ),
  CalendarRecord(
    title = "تبعید حضرت امام خمینی (ره) از ایران به ترکیه (۱۳۴۳ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 13
  ),
  CalendarRecord(
    title = "روز فرهنگ عمومی",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 14
  ),
  CalendarRecord(
    title = "روز مازندران",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 14
  ),
  CalendarRecord(
    title = "روز کیفیت",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 18
  ),
  CalendarRecord(
    title = "روز هوافضا",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 21
  ),
  CalendarRecord(
    title = "روز کتاب، کتاب‌خوانی و کتابدار",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 24
  ),
  CalendarRecord(
    title = "روز بزرگداشت آیت‌الله علامه سید محمّدحسین طباطبایی (۱۳۶۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 24
  ),
  CalendarRecord(
    title = "روز اصفهان",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 25
  ),
  CalendarRecord(
    title = "سالروز آزادسازی سوسنگرد",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 26
  ),
  CalendarRecord(
    title = "روز قهرمان ملی",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 30
  ),
  CalendarRecord(
    title = "روز بزرگداشت ابونصر فارابی",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 30
  ),
  CalendarRecord(
    title = "روز حکمت و فلسفه",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 30
  ),
  CalendarRecord(
    title = "روز زیتون",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 4
  ),
  CalendarRecord(
    title = "تشکیل بسیج مستضعفان به فرمان حضرت امام خمینی (ره) (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 5
  ),
  CalendarRecord(
    title = "روز بسیج مستضعفان",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 5
  ),
  CalendarRecord(
    title = "سالروز قیام مردم گرگان (۱۳۵۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 5
  ),
  CalendarRecord(
    title = "روز نیروی دریایی",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 7
  ),
  CalendarRecord(
    title = "روز نوآوری و فناوری ساخت ایران",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 7
  ),
  CalendarRecord(
    title = "روز بزرگداشت شیخ مفید",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 9
  ),
  CalendarRecord(
    title = "روز جزایر سه‌گانهٔ خلیج فارس (بوموسی، تنب بزرگ و تنب کوچک)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 9
  ),
  CalendarRecord(
    title = "شهادت آیت‌الله سید حسن مدرس (۱۳۱۶ ه‍.ش) و روز مجلس",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 10
  ),
  CalendarRecord(
    title = "شهادت میرزا‌ کوچک‌خان جنگلی (۱۳۰۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 11
  ),
  CalendarRecord(
    title = "تصویب قانون اساسی جمهوری اسلامی ایران (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 12
  ),
  CalendarRecord(
    title = "روز قانون اساسی جمهوری اسلامی ایران",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 12
  ),
  CalendarRecord(
    title = "روز بیمه",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 13
  ),
  CalendarRecord(
    title = "روز دانشجو",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 16
  ),
  CalendarRecord(
    title = "معرفی عراق به عنوان مسئول و آغازگر جنگ از سوی سازمان ملل (۱۳۷۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 18
  ),
  CalendarRecord(
    title = "تشکیل شورای عالی انقلاب فرهنگی به فرمان حضرت امام خمینی (ره) (۱۳۶۳ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 19
  ),
  CalendarRecord(
    title = "شهادت سومین شهید محراب، آیت‌الله دستغیب به دست منافقان (۱۳۶۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 20
  ),
  CalendarRecord(
    title = "روز پژوهش",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 25
  ),
  CalendarRecord(
    title = "روز حمل‌و‌نقل و رانندگان",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 26
  ),
  CalendarRecord(
    title = "شهادت آیت‌الله دکتر محمد مفتح (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 27
  ),
  CalendarRecord(
    title = "روز وحدت حوزه و دانشگاه",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 27
  ),
  CalendarRecord(
    title = "روز جهان عاری از خشونت و افراطی‌گری",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 27
  ),
  CalendarRecord(
    title = "روز تجلیل از شهید تندگویان",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 29
  ),
  CalendarRecord(
    title = "شب یلدا (چله)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 30
  ),
  CalendarRecord(
    title = "ترویج فرهنگ میهمانی و پیوند با خویشان",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 30
  ),
  CalendarRecord(
    title = "روز ثبت احوال",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 3
  ),
  CalendarRecord(
    title = "روز بزرگداشت رودکی",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 4
  ),
  CalendarRecord(
    title = "روز ایمنی در برابر زلزله و کاهش اثرات بلایای طبیعی",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 5
  ),
  CalendarRecord(
    title = "سالروز تشکیل نهضت سوادآموزی به فرمان حضرت امام خمینی (ره) (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 7
  ),
  CalendarRecord(
    title = "شهادت آیت‌الله حسین غفاری به دست مأموران ستم‌شاهی پهلوی (۱۳۵۳ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 7
  ),
  CalendarRecord(
    title = "روز صنعت پتروشیمی",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 8
  ),
  CalendarRecord(
    title = "روز بصیرت و میثاق امت با ولایت",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 9
  ),
  CalendarRecord(
    title = "روز بزرگداشت علامه مصباح یزدی",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 12
  ),
  CalendarRecord(
    title = "روز علوم انسانی اسلامی",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 12
  ),
  CalendarRecord(
    title = "روز جهانی مقاومت",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 13
  ),
  CalendarRecord(
    title = "شهادت الگوی اخلاص و عمل سردار سپهبد قاسم سلیمانی به دست استکبار جهانی",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 13
  ),
  CalendarRecord(
    title = "ابلاغ پیام تاریخی حضرت امام خمینی (ره) به گورباچف رهبر شوروی سابق (۱۳۶۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 13
  ),
  CalendarRecord(
    title = "شهادت سیدحسین علم‌الهدی و همرزمان وی در هویزه",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 16
  ),
  CalendarRecord(
    title = "روز شهدای دانشجو",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 16
  ),
  CalendarRecord(
    title = "اجرای طرح استعماری حذف حجاب (کشف حجاب) به دست رضاخان (۱۳۱۴ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 17
  ),
  CalendarRecord(
    title = "روز بزرگداشت خواجوی کرمانی",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 17
  ),
  CalendarRecord(
    title = "روز کرمان",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 17
  ),
  CalendarRecord(
    title = "قیام خونین مردم قم (۱۳۵۶ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 19
  ),
  CalendarRecord(
    title = "شهادت میرزا تقی‌خان امیرکبیر (۱۲۳۰ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 20
  ),
  CalendarRecord(
    title = "تشکیل شورای انقلاب به فرمان حضرت امام خمینی (ره) (۱۳۵۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 22
  ),
  CalendarRecord(
    title = "روز تاریخ‌نگاری انقلاب اسلامی",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 25
  ),
  CalendarRecord(
    title = "فرار شاه معدوم (۱۳۵۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 26
  ),
  CalendarRecord(
    title = "شهادت نواب صفوی، طهماسبی، برادران واحدی و ذوالقدر از فدائیان اسلام (۱۳۳۴ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 27
  ),
  CalendarRecord(
    title = "روز بزرگداشت خاقانی شروانی",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 1
  ),
  CalendarRecord(
    title = "سالروز حماسهٔ مردم آمل",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 6
  ),
  CalendarRecord(
    title = "روز بزرگداشت صفی‌الدین اُرمَوی",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 6
  ),
  CalendarRecord(
    title = "روز آواها و نواهای ایرانی",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 6
  ),
  CalendarRecord(
    title = "سالروز بازگشت حضرت امام خمینی (ره) به ایران و آغاز دههٔ مبارک فجر انقلاب اسلامی",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 12
  ),
  CalendarRecord(
    title = "روز فناوری فضایی",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 14
  ),
  CalendarRecord(
    title = "روز نیروی هوایی",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 19
  ),
  CalendarRecord(
    title = "روز چهارمحال و بختیاری",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 1
  ),
  CalendarRecord(
    title = "شکسته شدن حکومت‌نظامی به فرمان حضرت امام خمینی (ره) (۱۳۵۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 21
  ),
  CalendarRecord(
    title = "پیروزی انقلاب اسلامی ایران و سقوط نظام شاهنشاهی (۱۳۵۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = true, month = 11, day = 22
  ),
  CalendarRecord(
    title = "صدور حکم تاریخی حضرت امام خمینی (ره) مبنی بر ارتداد سلمان‌رشدی نویسندهٔ خائن کتاب آیات شیطانی (۱۳۶۷ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 25
  ),
  CalendarRecord(
    title = "قیام مردم تبریز به مناسبت چهلمین روز شهادت شهدای قم (۱۳۵۶ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 29
  ),
  CalendarRecord(
    title = "روز اقتصاد مقاومتی و کارآفرینی",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 29
  ),
  CalendarRecord(
    title = "کودتای انگلیسی رضاخان (۱۲۹۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 3
  ),
  CalendarRecord(
    title = "روز بزرگداشت خواجه‌نصیرالدین طوسی",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 5
  ),
  CalendarRecord(
    title = "روز مهندسی",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 5
  ),
  CalendarRecord(
    title = "روز امور تربیتی و تربیت اسلامی",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 8
  ),
  CalendarRecord(
    title = "روز بزرگداشت حکیم حاج ملاهادی سبزواری",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 8
  ),
  CalendarRecord(
    title = "روز حمایت از بیماران نادر",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 8
  ),
  CalendarRecord(
    title = "روز حمایت از حقوق مصرف‌کنندگان",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 9
  ),
  CalendarRecord(
    title = "روز احسان و نیکوکاری",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 14
  ),
  CalendarRecord(
    title = "روز ترویج فرهنگ قرض‌الحسنه",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 14
  ),
  CalendarRecord(
    title = "روز درختکاری",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 15
  ),
  CalendarRecord(
    title = "روز آموزش همگانی حفظ محیط زیست",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 15
  ),
  CalendarRecord(
    title = "روز بزرگداشت سید ‌جمال‌الدین اسدآبادی",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 18
  ),
  CalendarRecord(
    title = "سالروز تأسیس کانون‌های فرهنگی‌و‌هنری مساجد کشور",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 18
  ),
  CalendarRecord(
    title = "روز بوشهر",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 18
  ),
  CalendarRecord(
    title = "روز راهیان نور",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 20
  ),
  CalendarRecord(
    title = "روز بزرگداشت نظامی گنجوی",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 21
  ),
  CalendarRecord(
    title = "سالروز صدور فرمان حضرت امام خمینی (ره)، مبنی بر تأسیس بنیاد شهید انقلاب اسلامی (۱۳۵۸ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 22
  ),
  CalendarRecord(
    title = "روز بزرگداشت شهدا",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 22
  ),
  CalendarRecord(
    title = "روز بزرگداشت پروین اعتصامی",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 25
  ),
  CalendarRecord(
    title = "بمباران شیمیایی حلبچه به دست ارتش بعث عراق (۱۳۶۶ ه‍.ش)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 25
  ),
  CalendarRecord(
    title = "روز ملی شدن صنعت نفت ایران (۱۳۲۹ ه‍.ش)",
    source = EventSource.Iran, isHoliday = true, month = 12, day = 29
  ),
  CalendarRecord(
    title = "روز آزمایشگاهیان",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 30
  ),
  CalendarRecord(
    title = "روز روان‌شناس و مشاور",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 9
  ),
  CalendarRecord(
    title = "روز صنعت بتن آماده",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 15
  ),
  CalendarRecord(
    title = "روز صنعت تهویه مطبوع",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 15
  ),
  CalendarRecord(
    title = "روز مشاغل خانگی و تولید خانواده‌محور",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 22
  ),
  CalendarRecord(
    title = "روز بوم‌گردی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 31
  ),
  CalendarRecord(
    title = "روز نقشه‌برداری",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 7
  ),
  CalendarRecord(
    title = "روز مشاور املاک",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 8
  ),
  CalendarRecord(
    title = "روز صنعت موتورسیکلت",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 30
  ),
  CalendarRecord(
    title = "روز صنعت ابزارآلات",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 5
  ),
  CalendarRecord(
    title = "روز عینک‌سازی و بینایی‌سنجی",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 6
  ),
  CalendarRecord(
    title = "روز حمایت از تولید ملی و مبارزه با قاچاق کالا",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 12
  ),
  CalendarRecord(
    title = "روز خیاط، صنعت نساجی و پوشاک",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 12
  ),
  CalendarRecord(
    title = "روز صنعت قیر و آسفالت",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 18
  ),
  CalendarRecord(
    title = "روز خلبان",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 30
  ),
  CalendarRecord(
    title = "روز کفاش، صنعت کفش و چرم",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 25
  ),
  CalendarRecord(
    title = "روز فوریت‌های پزشکی (اورژانس)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 26
  ),
  CalendarRecord(
    title = "روز صنعت آسانسور و پله‌برقی",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 24
  ),
  CalendarRecord(
    title = "روز صنعت ساختمان",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 1
  ),
  CalendarRecord(
    title = "روز محیط‌بان",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 8
  ),
  CalendarRecord(
    title = "روز صنعت نوشت‌افزار",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 25
  ),
  CalendarRecord(
    title = "روز صنعت سرب و روی",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 1
  ),
  CalendarRecord(
    title = "روز حسابدار",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 15
  ),
  CalendarRecord(
    title = "روز سد و نیروگاه برق‌آبی",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 18
  ),
  CalendarRecord(
    title = "روز صنعت مس",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 22
  ),
  CalendarRecord(
    title = "روز آرایشگر",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 1
  ),
  CalendarRecord(
    title = "روز دفاتر اسناد رسمی",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 6
  ),
  CalendarRecord(
    title = "روز صنعت سیمان",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 8
  ),
  CalendarRecord(
    title = "روز قناد، صنعت شیرینی و شکلات",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 20
  ),
  CalendarRecord(
    title = "روز معاینه فنی خودرو",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 29
  ),
  CalendarRecord(
    title = "روز ویراستار",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 11
  ),
  CalendarRecord(
    title = "روز بازاریاب و مدیر فروش",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 10
  ),
  CalendarRecord(
    title = "روز کارشناس و متخصص تغذیه",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 16
  ),
  CalendarRecord(
    title = "روز خادمان آرامستان",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 21
  ),
  CalendarRecord(
    title = "روز صنعت طلا، جواهر، نقره و گوهرسنگ‌ها",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 23
  ),
  CalendarRecord(
    title = "نوروز، نوروز جمشیدی (جمشید پیشدادی) - ابتدای بهار",
    source = EventSource.AncientIran, isHoliday = false, month = 1, day = 1
  ),
  CalendarRecord(
    title = "نوروز بزرگ (هودرو)، زادروز آشو زرتشت - روییدن مشی و مشیانه",
    source = EventSource.AncientIran, isHoliday = false, month = 1, day = 6
  ),
  CalendarRecord(
    title = "آیین نیایش پیر هریشت از ۷ تا ۱۱ فروردین",
    source = EventSource.AncientIran, isHoliday = false, month = 1, day = 7
  ),
  CalendarRecord(
    title = "سیزده نوروز، سیزده‌بدر",
    source = EventSource.AncientIran, isHoliday = false, month = 1, day = 13
  ),
  CalendarRecord(
    title = "جشن فرودینگان",
    source = EventSource.AncientIran, isHoliday = false, month = 1, day = 19
  ),
  CalendarRecord(
    title = "جشن اردیبهشتگان، پوشیدن لباس سپید به نشانه پاکی",
    source = EventSource.AncientIran, isHoliday = false, month = 2, day = 2
  ),
  CalendarRecord(
    title = "جشن چلمو (چله بهار) - گاهان بار میدیوزرم‌گاه از ۱۰ تا ۱۴ اردیبهشت",
    source = EventSource.AncientIran, isHoliday = false, month = 2, day = 10
  ),
  CalendarRecord(
    title = "جشن پنجاه بدر",
    source = EventSource.AncientIran, isHoliday = false, month = 2, day = 19
  ),
  CalendarRecord(
    title = "بزرگداشت استاد توس فردوسی بزرگ",
    source = EventSource.AncientIran, isHoliday = false, month = 2, day = 25
  ),
  CalendarRecord(
    title = "جشن خوردادگان، امشاسپند خورداد نگاهبان آبها",
    source = EventSource.AncientIran, isHoliday = false, month = 3, day = 4
  ),
  CalendarRecord(
    title = "آیین نیایش ستی پیر و پیر سبز (چک چک)",
    source = EventSource.AncientIran, isHoliday = false, month = 3, day = 24
  ),
  CalendarRecord(
    title = "جشن ابتدای تیر ماه، آب پاشونک",
    source = EventSource.AncientIran, isHoliday = false, month = 3, day = 29
  ),
  CalendarRecord(
    title = "آیین نیایش پیر نارستانه",
    source = EventSource.AncientIran, isHoliday = false, month = 4, day = 2
  ),
  CalendarRecord(
    title = "گاهان بار میدیوشهیم‌گاه از ۸ تا ۱۲ تیر",
    source = EventSource.AncientIran, isHoliday = false, month = 4, day = 8
  ),
  CalendarRecord(
    title = "جشن تیرگان، آب پاشونک",
    source = EventSource.AncientIran, isHoliday = false, month = 4, day = 10
  ),
  CalendarRecord(
    title = "آیین نیایش پارس بانو از ۱۳ تا ۱۷ تیر",
    source = EventSource.AncientIran, isHoliday = false, month = 4, day = 13
  ),
  CalendarRecord(
    title = "جشن امردادگان، امشاسپند امرداد نگاهبان رستنی‌ها",
    source = EventSource.AncientIran, isHoliday = false, month = 5, day = 3
  ),
  CalendarRecord(
    title = "جشن چلهٔ تابستان",
    source = EventSource.AncientIran, isHoliday = false, month = 5, day = 6
  ),
  CalendarRecord(
    title = "آیین نیایش پیر نارکی از ۱۲ تا ۱۶ مرداد",
    source = EventSource.AncientIran, isHoliday = false, month = 5, day = 12
  ),
  CalendarRecord(
    title = "جشن شهریورگان، امشاسپند شهریور نگاهبان فلزات",
    source = EventSource.AncientIran, isHoliday = false, month = 5, day = 30
  ),
  CalendarRecord(
    title = "جشن خزان",
    source = EventSource.AncientIran, isHoliday = false, month = 6, day = 3
  ),
  CalendarRecord(
    title = "گاهان‌بار پتیه‌شهیم‌گاه از ۲۱ تا ۲۵ شهریور",
    source = EventSource.AncientIran, isHoliday = false, month = 6, day = 21
  ),
  CalendarRecord(
    title = "جشن مهرگان",
    source = EventSource.AncientIran, isHoliday = false, month = 7, day = 10
  ),
  CalendarRecord(
    title = "جشن آبانگان",
    source = EventSource.AncientIran, isHoliday = false, month = 8, day = 4
  ),
  CalendarRecord(
    title = "روز کوروش بزرگ",
    source = EventSource.AncientIran, isHoliday = false, month = 8, day = 7
  ),
  CalendarRecord(
    title = "جشن پاییزانه",
    source = EventSource.AncientIran, isHoliday = false, month = 8, day = 9
  ),
  CalendarRecord(
    title = "جشن گالشی",
    source = EventSource.AncientIran, isHoliday = false, month = 8, day = 21
  ),
  CalendarRecord(
    title = "جشن آذرگان",
    source = EventSource.AncientIran, isHoliday = false, month = 9, day = 3
  ),
  CalendarRecord(
    title = "اولین جشن دی‌گان",
    source = EventSource.AncientIran, isHoliday = false, month = 9, day = 25
  ),
  CalendarRecord(
    title = "جشن شب یلدا",
    source = EventSource.AncientIran, isHoliday = false, month = 9, day = 30
  ),
  CalendarRecord(
    title = "دومین جشن دی‌گان",
    source = EventSource.AncientIran, isHoliday = false, month = 10, day = 2
  ),
  CalendarRecord(
    title = "جشن سیر و سور",
    source = EventSource.AncientIran, isHoliday = false, month = 10, day = 8
  ),
  CalendarRecord(
    title = "سومین جشن دی‌گان",
    source = EventSource.AncientIran, isHoliday = false, month = 10, day = 9
  ),
  CalendarRecord(
    title = "چهارمین جشن دی‌گان",
    source = EventSource.AncientIran, isHoliday = false, month = 10, day = 17
  ),
  CalendarRecord(
    title = "جشن بهمنگان، روز پدر، بهمن (منش نیک) امشاسپند",
    source = EventSource.AncientIran, isHoliday = false, month = 10, day = 26
  ),
  CalendarRecord(
    title = "جشن نوسده",
    source = EventSource.AncientIran, isHoliday = false, month = 10, day = 29
  ),
  CalendarRecord(
    title = "جشن میانهٔ زمستان",
    source = EventSource.AncientIran, isHoliday = false, month = 11, day = 9
  ),
  CalendarRecord(
    title = "جشن سده، آتش افروزی به هنگام غروب آفتاب",
    source = EventSource.AncientIran, isHoliday = false, month = 11, day = 10
  ),
  CalendarRecord(
    title = "جشن اسفندگان، روز مادر و روز عشق پاک",
    source = EventSource.AncientIran, isHoliday = false, month = 11, day = 29
  ),
  CalendarRecord(
    title = "جشن گلدان (اینجه، رسیدگی به امور نباتات)",
    source = EventSource.AncientIran, isHoliday = false, month = 12, day = 14
  ),
)

public val islamicEvents: List<CalendarRecord> = listOf(
  CalendarRecord(
    title = "روز عاشورا",
    source = EventSource.Afghanistan, isHoliday = true, month = 1, day = 10
  ),
  CalendarRecord(
    title = "روز میلاد نبی (ص)",
    source = EventSource.Afghanistan, isHoliday = true, month = 3, day = 12
  ),
  CalendarRecord(
    title = "اول ماه مبارک رمضان",
    source = EventSource.Afghanistan, isHoliday = true, month = 9, day = 1
  ),
  CalendarRecord(
    title = "روز حمایت از اطفال آسیب‌پذیر، یتیم و بی‌سرپرست",
    source = EventSource.Afghanistan, isHoliday = false, month = 9, day = 15
  ),
  CalendarRecord(
    title = "روز گرامیداشت از نزول قرآن عظیم‌الشان",
    source = EventSource.Afghanistan, isHoliday = false, month = 9, day = 27
  ),
  CalendarRecord(
    title = "عید سعید فطر",
    source = EventSource.Afghanistan, isHoliday = true, month = 10, day = 1
  ),
  CalendarRecord(
    title = "عید سعید فطر، روز دوم",
    source = EventSource.Afghanistan, isHoliday = true, month = 10, day = 2
  ),
  CalendarRecord(
    title = "عید سعید فطر، روز سوم",
    source = EventSource.Afghanistan, isHoliday = true, month = 10, day = 3
  ),
  CalendarRecord(
    title = "روز عرفه و عید سعید اضحی",
    source = EventSource.Afghanistan, isHoliday = true, month = 12, day = 9
  ),
  CalendarRecord(
    title = "روز عرفه و عید سعید اضحی",
    source = EventSource.Afghanistan, isHoliday = true, month = 12, day = 10
  ),
  CalendarRecord(
    title = "روز عرفه و عید سعید اضحی",
    source = EventSource.Afghanistan, isHoliday = true, month = 12, day = 11
  ),
  CalendarRecord(
    title = "روز عرفه و عید سعید اضحی",
    source = EventSource.Afghanistan, isHoliday = true, month = 12, day = 12
  ),
  CalendarRecord(
    title = "آغاز سال هجری قمری",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 1
  ),
  CalendarRecord(
    title = "روز امر به معروف و نهی از منکر",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 2
  ),
  CalendarRecord(
    title = "تاسوعا",
    source = EventSource.Iran, isHoliday = true, month = 1, day = 9
  ),
  CalendarRecord(
    title = "عاشورا",
    source = EventSource.Iran, isHoliday = true, month = 1, day = 10
  ),
  CalendarRecord(
    title = "روز تجلیل از اسرا و مفقودان",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 11
  ),
  CalendarRecord(
    title = "شهادت حضرت امام زین‌العابدین (ع) (۹۵ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 12
  ),
  CalendarRecord(
    title = "شهادت حضرت امام زین‌العابدین (ع) (۹۵ ه‍.ق) به روایتی",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 25
  ),
  CalendarRecord(
    title = "یاد روز قیام مردم سیستان به خون‌خواهی شهدای کربلا (سال ۶۲ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 25
  ),
  CalendarRecord(
    title = "ولادت حضرت امام محمد باقر (ع) (۵۷ ﻫ.ق) به روایتی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 3
  ),
  CalendarRecord(
    title = "شهادت حضرت امام حسن مجتبی (ع) (۵۰ ه‍.ق) به روایتی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 7
  ),
  CalendarRecord(
    title = "روز بزرگداشت سلمان فارسی",
    source = EventSource.Iran, isHoliday = false, month = 2, day = 7
  ),
  CalendarRecord(
    title = "اربعین حسینی",
    source = EventSource.Iran, isHoliday = true, month = 2, day = 20
  ),
  CalendarRecord(
    title = "رحلت حضرت رسول اکرم (ص) (۱۱ ه‍.ق) – شهادت حضرت امام حسن مجتبی (ع) (۵۰ ه‍.ق)",
    source = EventSource.Iran, isHoliday = true, month = 2, day = 28
  ),
  CalendarRecord(
    title = "هجرت رسول اکرم (ص) از مکه به مدینه",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 1
  ),
  CalendarRecord(
    title = "شهادت حضرت امام حسن عسکری (ع) (۲۶۰ ه‍.ق) و آغاز امامت حضرت ولی عصر (عج)",
    source = EventSource.Iran, isHoliday = true, month = 3, day = 8
  ),
  CalendarRecord(
    title = "ولادت حضرت رسول اکرم (ص) به روایت اهل سنت (۵۳ سال قبل از هجرت)",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 12
  ),
  CalendarRecord(
    title = "آغاز هفتهٔ وحدت",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 12
  ),
  CalendarRecord(
    title = "روز سیستان و بلوچستان",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 14
  ),
  CalendarRecord(
    title = "روز وقف",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 16
  ),
  CalendarRecord(
    title = "ولادت حضرت رسول اکرم (ص) (۵۳ سال قبل از هجرت) و روز اخلاق و مهرورزی",
    source = EventSource.Iran, isHoliday = true, month = 3, day = 17
  ),
  CalendarRecord(
    title = "ولادت حضرت امام جعفر صادق (ع) مؤسس مذهب جعفری (۸۳ ه‍.ق)",
    source = EventSource.Iran, isHoliday = true, month = 3, day = 17
  ),
  CalendarRecord(
    title = "ولادت حضرت عبدالعظیم حسنی (ع)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 4
  ),
  CalendarRecord(
    title = "ولادت حضرت امام حسن عسکری (ع) (۲۳۲ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 8
  ),
  CalendarRecord(
    title = "وفات حضرت معصومه (س) (۲۰۱ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 10
  ),
  CalendarRecord(
    title = "ولادت حضرت زینب (س) (۵ ه‍.ق) و روز پرستار",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 5
  ),
  CalendarRecord(
    title = "وفات حضرت فاطمهٔ زهرا (س) (۱۱ ه‍.ق) به روایتی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 13
  ),
  CalendarRecord(
    title = "وفات حضرت فاطمهٔ زهرا (س) (۱۱ ه‍.ق)",
    source = EventSource.Iran, isHoliday = true, month = 6, day = 3
  ),
  CalendarRecord(
    title = "سالروز وفات حضرت ام‌البنین (س)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 13
  ),
  CalendarRecord(
    title = "روز تکریم مادران و همسران شهدا",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 13
  ),
  CalendarRecord(
    title = "روز زن و مادر و ولادت حضرت فاطمهٔ زهرا (س) (سال هشتم قبل از هجرت)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 20
  ),
  CalendarRecord(
    title = "تولد حضرت امام خمینی (ره) رهبر کبیر انقلاب اسلامی (۱۳۲۰ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 20
  ),
  CalendarRecord(
    title = "ولادت حضرت امام محمد باقر (ع) (۵۷ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 1
  ),
  CalendarRecord(
    title = "شهادت حضرت امام علی النقی الهادی (ع) (۲۵۴ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 3
  ),
  CalendarRecord(
    title = "ولادت حضرت امام محمد تقی (ع) «جوادالائمه» (۱۹۵ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 10
  ),
  CalendarRecord(
    title = "ولادت حضرت امام علی (ع) (۲۳ سال قبل از هجرت)",
    source = EventSource.Iran, isHoliday = true, month = 7, day = 13
  ),
  CalendarRecord(
    title = "روز پدر",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 13
  ),
  CalendarRecord(
    title = "آغاز ایام‌البیض (اعتکاف)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 13
  ),
  CalendarRecord(
    title = "ارتحال حضرت زینب (س) (۶۲ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 15
  ),
  CalendarRecord(
    title = "تغییر قبلهٔ مسلمین از بیت‌المقدس به مکهٔ معظمه (۲ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 15
  ),
  CalendarRecord(
    title = "شهادت حضرت امام موسی کاظم (ع) (۱۸۳ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 7, day = 25
  ),
  CalendarRecord(
    title = "مبعث حضرت رسول اکرم (ص) (۱۳ سال قبل از هجرت)",
    source = EventSource.Iran, isHoliday = true, month = 7, day = 27
  ),
  CalendarRecord(
    title = "ولادت حضرت امام حسین (ع) (۴ ه‍.ق) و روز پاسدار",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 3
  ),
  CalendarRecord(
    title = "ولادت حضرت ابوالفضل العباس (ع) (۲۶ ه‍.ق) و روز جانباز",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 4
  ),
  CalendarRecord(
    title = "ولادت حضرت امام زین‌العابدین (ع) (۳۸ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 5
  ),
  CalendarRecord(
    title = "روز صحیفهٔ سجادیه",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 5
  ),
  CalendarRecord(
    title = "ولادت حضرت علی اکبر (ع) (۳۳ ه‍.ق) و روز جوان",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 11
  ),
  CalendarRecord(
    title = "ولادت حضرت قائم (عج) (۲۵۵ ه‍.ق) و روز جهانی مستضعفان",
    source = EventSource.Iran, isHoliday = true, month = 8, day = 15
  ),
  CalendarRecord(
    title = "روز سربازان گمنام حضرت امام زمان (عج)",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 15
  ),
  CalendarRecord(
    title = "وفات حضرت خدیجه (س) (۳ سال قبل از هجرت)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 10
  ),
  CalendarRecord(
    title = "ولادت حضرت امام حسن مجتبی (ع) (۳ ه‍.ق) و روز اکرام و تکریم خیرین",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 15
  ),
  CalendarRecord(
    title = "شب قدر",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 18
  ),
  CalendarRecord(
    title = "ضربت خوردن حضرت امام علی (ع) (۴۰ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 19
  ),
  CalendarRecord(
    title = "روز نهج‌البلاغه",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 19
  ),
  CalendarRecord(
    title = "شب قدر",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 20
  ),
  CalendarRecord(
    title = "شهادت حضرت امام علی (ع) (۴۰ ه‍.ق)",
    source = EventSource.Iran, isHoliday = true, month = 9, day = 21
  ),
  CalendarRecord(
    title = "شب قدر",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 22
  ),
  CalendarRecord(
    title = "عید سعید فطر",
    source = EventSource.Iran, isHoliday = true, month = 10, day = 1
  ),
  CalendarRecord(
    title = "تعطیل به مناسبت عید سعید فطر",
    source = EventSource.Iran, isHoliday = true, month = 10, day = 2
  ),
  CalendarRecord(
    title = "روز فرهنگ پهلوانی و ورزش زورخانه‌ای",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 17
  ),
  CalendarRecord(
    title = "فتح اندلس به دست مسلمانان (۹۲ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 21
  ),
  CalendarRecord(
    title = "شهادت حضرت امام جعفر صادق (ع) (۱۴۸ ه‍.ق)",
    source = EventSource.Iran, isHoliday = true, month = 10, day = 25
  ),
  CalendarRecord(
    title = "ولادت حضرت معصومه (س) (۱۷۳ ه‍.ق) و روز دختران",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 1
  ),
  CalendarRecord(
    title = "آغاز دههٔ کرامت",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 1
  ),
  CalendarRecord(
    title = "روز تجلیل از امامزادگان و بقاع متبرکه",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 5
  ),
  CalendarRecord(
    title = "روز بزرگداشت حضرت صالح بن موسی کاظم (ع)",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 5
  ),
  CalendarRecord(
    title = "روز بزرگداشت حضرت احمدبن‌موسی شاهچراغ (ع)",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 6
  ),
  CalendarRecord(
    title = "ولادت حضرت امام رضا (ع) (۱۴۸ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 11
  ),
  CalendarRecord(
    title = "سالروز ازدواج حضرت امام علی (ع) و حضرت فاطمه (س) (۲ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 1
  ),
  CalendarRecord(
    title = "روز ازدواج",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 1
  ),
  CalendarRecord(
    title = "شهادت مظلومانهٔ زائران خانهٔ خدا به دست مأموران آل سعود (۱۳۶۶ ه‍.ش برابر با ۶ ذی‌الحجه ۱۴۰۷ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 6
  ),
  CalendarRecord(
    title = "شهادت حضرت امام محمد باقر (ع) (۱۱۴ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 7
  ),
  CalendarRecord(
    title = "روز عرفه (روز نیایش)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 9
  ),
  CalendarRecord(
    title = "عید سعید قربان",
    source = EventSource.Iran, isHoliday = true, month = 12, day = 10
  ),
  CalendarRecord(
    title = "آغاز دههٔ امامت و ولایت",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 10
  ),
  CalendarRecord(
    title = "ولادت حضرت امام علی النقی الهادی (ع) (۲۱۲ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 15
  ),
  CalendarRecord(
    title = "تعطیلات رسمی ایران(غدیر)",
    source = EventSource.Iran, isHoliday = true, month = 12, day = 18
  ),
  CalendarRecord(
    title = "ولادت حضرت امام موسی کاظم (ع) (۱۲۸ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 20
  ),
  CalendarRecord(
    title = "روز مباهلهٔ پیامبر اسلام (ص) (۱۰ ه‍.ق)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 24
  ),
  CalendarRecord(
    title = "روز خانواده و تکریم بازنشستگان",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 25
  ),
)

public val gregorianEvents: List<CalendarRecord> = listOf(
  CalendarRecord(
    title = "روز ملی مبارزه علیه مواد انفجاری تعبیه شده",
    source = EventSource.Afghanistan, isHoliday = false, month = 1, day = 21
  ),
  CalendarRecord(
    title = "روز جهانی زن",
    source = EventSource.Afghanistan, isHoliday = false, month = 3, day = 8
  ),
  CalendarRecord(
    title = "روز جهانی حقوق مستهلک",
    source = EventSource.Afghanistan, isHoliday = false, month = 3, day = 15
  ),
  CalendarRecord(
    title = "روز جهانی آب",
    source = EventSource.Afghanistan, isHoliday = false, month = 3, day = 22
  ),
  CalendarRecord(
    title = "روز جهانی هواشناسی",
    source = EventSource.Afghanistan, isHoliday = false, month = 3, day = 23
  ),
  CalendarRecord(
    title = "روز بین‌المللی کارگر",
    source = EventSource.Afghanistan, isHoliday = false, month = 5, day = 1
  ),
  CalendarRecord(
    title = "روز قلم",
    source = EventSource.Afghanistan, isHoliday = false, month = 5, day = 6
  ),
  CalendarRecord(
    title = "روز جهانی تیلی‌کمیونیکیشن",
    source = EventSource.Afghanistan, isHoliday = false, month = 5, day = 17
  ),
  CalendarRecord(
    title = "روز بین‌المللی موزیم‌ها",
    source = EventSource.Afghanistan, isHoliday = false, month = 5, day = 18
  ),
  CalendarRecord(
    title = "روز بین‌المللی طفل",
    source = EventSource.Afghanistan, isHoliday = false, month = 6, day = 1
  ),
  CalendarRecord(
    title = "هفتهٔ محیط زیست (۵-۱۱ جون)",
    source = EventSource.Afghanistan, isHoliday = false, month = 6, day = 5
  ),
  CalendarRecord(
    title = "روز بین‌المللی پناهنده‌گان",
    source = EventSource.Afghanistan, isHoliday = false, month = 6, day = 20
  ),
  CalendarRecord(
    title = "روز جهانی المپیک",
    source = EventSource.Afghanistan, isHoliday = false, month = 6, day = 23
  ),
  CalendarRecord(
    title = "هفتهٔ مبارزه علیه مواد مخدر، روز جهانی مبارزه علیه مواد مخدر",
    source = EventSource.Afghanistan, isHoliday = false, month = 6, day = 26
  ),
  CalendarRecord(
    title = "روز جهانی نفوس",
    source = EventSource.Afghanistan, isHoliday = false, month = 7, day = 11
  ),
  CalendarRecord(
    title = "هفتهٔ تغذیه از شیر مادر (۱-۷ آگست)",
    source = EventSource.Afghanistan, isHoliday = false, month = 8, day = 1
  ),
  CalendarRecord(
    title = "روز بین المللی یادبود و گرامیداشت از قربانیان تروریزم",
    source = EventSource.Afghanistan, isHoliday = false, month = 8, day = 21
  ),
  CalendarRecord(
    title = "روز بین‌المللی سواد",
    source = EventSource.Afghanistan, isHoliday = false, month = 9, day = 8
  ),
  CalendarRecord(
    title = "روز جهانی صلح",
    source = EventSource.Afghanistan, isHoliday = false, month = 9, day = 21
  ),
  CalendarRecord(
    title = "روز جهانی توریزم",
    source = EventSource.Afghanistan, isHoliday = false, month = 9, day = 27
  ),
  CalendarRecord(
    title = "روز جهانی معلم",
    source = EventSource.Afghanistan, isHoliday = false, month = 10, day = 5
  ),
  CalendarRecord(
    title = "روز جهانی پست",
    source = EventSource.Afghanistan, isHoliday = false, month = 10, day = 9
  ),
  CalendarRecord(
    title = "روز جهانی کاهش خطرپذیری",
    source = EventSource.Afghanistan, isHoliday = false, month = 10, day = 12
  ),
  CalendarRecord(
    title = "روز جهانی غذا",
    source = EventSource.Afghanistan, isHoliday = false, month = 10, day = 16
  ),
  CalendarRecord(
    title = "روز جهانی ملل متحد",
    source = EventSource.Afghanistan, isHoliday = false, month = 10, day = 24
  ),
  CalendarRecord(
    title = "روز جهانی محصلان",
    source = EventSource.Afghanistan, isHoliday = false, month = 11, day = 17
  ),
  CalendarRecord(
    title = "روز جهانی هوانوردی",
    source = EventSource.Afghanistan, isHoliday = false, month = 12, day = 7
  ),
  CalendarRecord(
    title = "روز جهانی مهاجرت",
    source = EventSource.Afghanistan, isHoliday = false, month = 12, day = 18
  ),
  CalendarRecord(
    title = "روز جهانی سینما",
    source = EventSource.Afghanistan, isHoliday = false, month = 12, day = 28
  ),
  CalendarRecord(
    title = "آغاز سال میلادی",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 1
  ),
  CalendarRecord(
    title = "روز جهانی گمرک",
    source = EventSource.Iran, isHoliday = false, month = 1, day = 26
  ),
  CalendarRecord(
    title = "روز جهانی آب",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 22
  ),
  CalendarRecord(
    title = "روز جهانی هواشناسی",
    source = EventSource.Iran, isHoliday = false, month = 3, day = 23
  ),
  CalendarRecord(
    title = "روز جهانی کودک فلسطینی",
    source = EventSource.Iran, isHoliday = false, month = 4, day = 5
  ),
  CalendarRecord(
    title = "روز جهانی کار و کارگر",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 1
  ),
  CalendarRecord(
    title = "روز جهانی ماما",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 5
  ),
  CalendarRecord(
    title = "روز جهانی صلیب سرخ و هلال احمر",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 8
  ),
  CalendarRecord(
    title = "روز جهانی موزه و میراث فرهنگی",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 18
  ),
  CalendarRecord(
    title = "روز جهانی بدون دخانیات",
    source = EventSource.Iran, isHoliday = false, month = 5, day = 31
  ),
  CalendarRecord(
    title = "روز جهانی والدین",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 1
  ),
  CalendarRecord(
    title = "روز جهانی محیط زیست",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 5
  ),
  CalendarRecord(
    title = "روز جهانی بیابان‌زدایی",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 17
  ),
  CalendarRecord(
    title = "روز جهانی مبارزه با مواد مخدر",
    source = EventSource.Iran, isHoliday = false, month = 6, day = 26
  ),
  CalendarRecord(
    title = "روز جهانی شیر مادر",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 1
  ),
  CalendarRecord(
    title = "انفجار بمب اتمی آمریکا در هیروشیما با بیش از ۱۶۰هزار کشته و مجروح (۱۹۴۵ میلادی)",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 6
  ),
  CalendarRecord(
    title = "روز جهانی مسجد",
    source = EventSource.Iran, isHoliday = false, month = 8, day = 21
  ),
  CalendarRecord(
    title = "روز جهانی جهانگردی",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 27
  ),
  CalendarRecord(
    title = "روز جهانی دریانوردی",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 30
  ),
  CalendarRecord(
    title = "روز جهانی ناشنوایان",
    source = EventSource.Iran, isHoliday = false, month = 9, day = 30
  ),
  CalendarRecord(
    title = "روز جهانی سالمندان",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 1
  ),
  CalendarRecord(
    title = "روز جهانی کودک",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 8
  ),
  CalendarRecord(
    title = "روز جهانی پست",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 9
  ),
  CalendarRecord(
    title = "روز جهانی استاندارد",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 14
  ),
  CalendarRecord(
    title = "روز جهانی نابینایان (عصای سفید)",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 15
  ),
  CalendarRecord(
    title = "روز جهانی غذا",
    source = EventSource.Iran, isHoliday = false, month = 10, day = 16
  ),
  CalendarRecord(
    title = "روز جهانی علم در خدمت صلح و توسعه",
    source = EventSource.Iran, isHoliday = false, month = 11, day = 10
  ),
  CalendarRecord(
    title = "روز جهانی مبارزه با ایدز",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 1
  ),
  CalendarRecord(
    title = "روز جهانی معلولان",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 3
  ),
  CalendarRecord(
    title = "روز جهانی هواپیمایی",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 7
  ),
  CalendarRecord(
    title = "ولادت حضرت عیسی مسیح (ع)",
    source = EventSource.Iran, isHoliday = false, month = 12, day = 25
  ),
  CalendarRecord(
    title = "روز جهانی بریل",
    source = EventSource.International, isHoliday = false, month = 1, day = 4
  ),
  CalendarRecord(
    title = "روز جهانی آموزش",
    source = EventSource.International, isHoliday = false, month = 1, day = 24
  ),
  CalendarRecord(
    title = "روز جهانی تالاب‌ها",
    source = EventSource.International, isHoliday = false, month = 2, day = 2
  ),
  CalendarRecord(
    title = "روز جهانی سرطان",
    source = EventSource.International, isHoliday = false, month = 2, day = 4
  ),
  CalendarRecord(
    title = "روز جهانی مبارزه با ناقص‌سازی زنان",
    source = EventSource.International, isHoliday = false, month = 2, day = 6
  ),
  CalendarRecord(
    title = "روز جهانی حبوبات",
    source = EventSource.International, isHoliday = false, month = 2, day = 10
  ),
  CalendarRecord(
    title = "روز جهانی زن و دختر در علم",
    source = EventSource.International, isHoliday = false, month = 2, day = 11
  ),
  CalendarRecord(
    title = "روز جهانی رادیو",
    source = EventSource.International, isHoliday = false, month = 2, day = 13
  ),
  CalendarRecord(
    title = "روز جهانی عدالت اجتماعی",
    source = EventSource.International, isHoliday = false, month = 2, day = 20
  ),
  CalendarRecord(
    title = "روز جهانی زبان مادری",
    source = EventSource.International, isHoliday = false, month = 2, day = 21
  ),
  CalendarRecord(
    title = "روز جهانی بدون تبعیض",
    source = EventSource.International, isHoliday = false, month = 3, day = 1
  ),
  CalendarRecord(
    title = "روز جهانی حیات وحش",
    source = EventSource.International, isHoliday = false, month = 3, day = 3
  ),
  CalendarRecord(
    title = "روز جهانی زن",
    source = EventSource.International, isHoliday = false, month = 3, day = 8
  ),
  CalendarRecord(
    title = "روز جهانی شادی",
    source = EventSource.International, isHoliday = false, month = 3, day = 20
  ),
  CalendarRecord(
    title = "روز جهانی شعر",
    source = EventSource.International, isHoliday = false, month = 3, day = 21
  ),
  CalendarRecord(
    title = "روز جهانی سندروم داون",
    source = EventSource.International, isHoliday = false, month = 3, day = 21
  ),
  CalendarRecord(
    title = "روز بین‌المللی جنگل‌ها",
    source = EventSource.International, isHoliday = false, month = 3, day = 21
  ),
  CalendarRecord(
    title = "روز جهانی سل",
    source = EventSource.International, isHoliday = false, month = 3, day = 24
  ),
  CalendarRecord(
    title = "روز بین‌المللی حق بر صحت و درستی دربارهٔ نقض فاحش حقوق بشر و منزلت قربانیان",
    source = EventSource.International, isHoliday = false, month = 3, day = 24
  ),
  CalendarRecord(
    title = "روز بین‌المللی یادبود قربانیان بردگی و تجارت برده از آن سوی اقیانوس اطلس",
    source = EventSource.International, isHoliday = false, month = 3, day = 25
  ),
  CalendarRecord(
    title = "روز جهانی تئاتر",
    source = EventSource.International, isHoliday = false, month = 3, day = 27
  ),
  CalendarRecord(
    title = "روز جهانی کتاب کودک",
    source = EventSource.International, isHoliday = false, month = 4, day = 2
  ),
  CalendarRecord(
    title = "روز جهانی کارتونیست‌ها",
    source = EventSource.International, isHoliday = false, month = 4, day = 7
  ),
  CalendarRecord(
    title = "روز جهانی سلامت",
    source = EventSource.International, isHoliday = false, month = 4, day = 7
  ),
  CalendarRecord(
    title = "روز جهانی هنر (سالروز تولد داوینچی)",
    source = EventSource.International, isHoliday = false, month = 4, day = 15
  ),
  CalendarRecord(
    title = "روز جهانی کبد",
    source = EventSource.International, isHoliday = false, month = 4, day = 19
  ),
  CalendarRecord(
    title = "روز جهانی زمین پاک",
    source = EventSource.International, isHoliday = false, month = 4, day = 22
  ),
  CalendarRecord(
    title = "روز جهانی کتاب و حق مؤلف",
    source = EventSource.International, isHoliday = false, month = 4, day = 23
  ),
  CalendarRecord(
    title = "روز جهانی مالاریا",
    source = EventSource.International, isHoliday = false, month = 4, day = 25
  ),
  CalendarRecord(
    title = "روز جهانی گرافیک",
    source = EventSource.International, isHoliday = false, month = 4, day = 27
  ),
  CalendarRecord(
    title = "روز جهانی ایمنی و بهداشت حرفه‌ای",
    source = EventSource.International, isHoliday = false, month = 4, day = 28
  ),
  CalendarRecord(
    title = "روز جهانی آزادی مطبوعات",
    source = EventSource.International, isHoliday = false, month = 5, day = 3
  ),
  CalendarRecord(
    title = "روز جهانی پرستار",
    source = EventSource.International, isHoliday = false, month = 5, day = 12
  ),
  CalendarRecord(
    title = "روز جهانی زنان در ریاضیات",
    source = EventSource.International, isHoliday = false, month = 5, day = 12
  ),
  CalendarRecord(
    title = "روز جهانی خانواده",
    source = EventSource.International, isHoliday = false, month = 5, day = 15
  ),
  CalendarRecord(
    title = "روز جهانی پسران",
    source = EventSource.International, isHoliday = false, month = 5, day = 16
  ),
  CalendarRecord(
    title = "روز جهانی زنبور",
    source = EventSource.International, isHoliday = false, month = 5, day = 20
  ),
  CalendarRecord(
    title = "روز جهانی حافظان صلح ملل متحد",
    source = EventSource.International, isHoliday = false, month = 5, day = 29
  ),
  CalendarRecord(
    title = "روز جهانی حمایت از کودکان قربانی خشونت",
    source = EventSource.International, isHoliday = false, month = 6, day = 4
  ),
  CalendarRecord(
    title = "روز جهانی صنایع دستی",
    source = EventSource.International, isHoliday = false, month = 6, day = 10
  ),
  CalendarRecord(
    title = "روز جهانی منع کار کودکان",
    source = EventSource.International, isHoliday = false, month = 6, day = 12
  ),
  CalendarRecord(
    title = "روز جهانی اهدای خون",
    source = EventSource.International, isHoliday = false, month = 6, day = 14
  ),
  CalendarRecord(
    title = "روز جهانی پناهندگان",
    source = EventSource.International, isHoliday = false, month = 6, day = 20
  ),
  CalendarRecord(
    title = "روز جهانی موسیقی",
    source = EventSource.International, isHoliday = false, month = 6, day = 21
  ),
  CalendarRecord(
    title = "روز جهانی قربانیان خشونت",
    source = EventSource.International, isHoliday = false, month = 6, day = 26
  ),
  CalendarRecord(
    title = "روز جهانی جمعیت",
    source = EventSource.International, isHoliday = false, month = 7, day = 11
  ),
  CalendarRecord(
    title = "روز جهانی جوانان",
    source = EventSource.International, isHoliday = false, month = 8, day = 12
  ),
  CalendarRecord(
    title = "روز جهانی چپ‌دستان",
    source = EventSource.International, isHoliday = false, month = 8, day = 13
  ),
  CalendarRecord(
    title = "روز جهانی انسان دوستی",
    source = EventSource.International, isHoliday = false, month = 8, day = 19
  ),
  CalendarRecord(
    title = "روز جهانی عکاسی",
    source = EventSource.International, isHoliday = false, month = 8, day = 19
  ),
  CalendarRecord(
    title = "روز جهانی مبارزه با آزمایش‌های هسته‌ای",
    source = EventSource.International, isHoliday = false, month = 8, day = 29
  ),
  CalendarRecord(
    title = "روز جهانی باسوادی",
    source = EventSource.International, isHoliday = false, month = 9, day = 8
  ),
  CalendarRecord(
    title = "روز جهانی جلوگیری از خودکشی",
    source = EventSource.International, isHoliday = false, month = 9, day = 10
  ),
  CalendarRecord(
    title = "روز جهانی مردم‌سالاری",
    source = EventSource.International, isHoliday = false, month = 9, day = 15
  ),
  CalendarRecord(
    title = "روز جهانی حفاظت از لایهٔ اُزن",
    source = EventSource.International, isHoliday = false, month = 9, day = 16
  ),
  CalendarRecord(
    title = "روز جهانی آلزایمر",
    source = EventSource.International, isHoliday = false, month = 9, day = 21
  ),
  CalendarRecord(
    title = "روز جهانی صلح",
    source = EventSource.International, isHoliday = false, month = 9, day = 21
  ),
  CalendarRecord(
    title = "روز جهانی ترجمه",
    source = EventSource.International, isHoliday = false, month = 9, day = 30
  ),
  CalendarRecord(
    title = "روز بین‌المللی قهوه",
    source = EventSource.International, isHoliday = false, month = 10, day = 1
  ),
  CalendarRecord(
    title = "روز جهانی معلم",
    source = EventSource.International, isHoliday = false, month = 10, day = 5
  ),
  CalendarRecord(
    title = "روز جهانی بهداشت روان",
    source = EventSource.International, isHoliday = false, month = 10, day = 10
  ),
  CalendarRecord(
    title = "روز جهانی دختران",
    source = EventSource.International, isHoliday = false, month = 10, day = 11
  ),
  CalendarRecord(
    title = "روز ملل متحد و روز جهانی توسعه اطلاعات",
    source = EventSource.International, isHoliday = false, month = 10, day = 24
  ),
  CalendarRecord(
    title = "روز جهانی هنرمند",
    source = EventSource.International, isHoliday = false, month = 10, day = 25
  ),
  CalendarRecord(
    title = "روز جهانی میراث سمعی و بصری",
    source = EventSource.International, isHoliday = false, month = 10, day = 27
  ),
  CalendarRecord(
    title = "روز جهانی کاردرمانی",
    source = EventSource.International, isHoliday = false, month = 10, day = 27
  ),
  CalendarRecord(
    title = "روز جهانی شهرها",
    source = EventSource.International, isHoliday = false, month = 10, day = 31
  ),
  CalendarRecord(
    title = "جشن هالووین",
    source = EventSource.International, isHoliday = false, month = 10, day = 31
  ),
  CalendarRecord(
    title = "روز بین‌المللی پیشگیری از سوء استفاده از محیط زیست در جنگ و مناقشات مسلحانه",
    source = EventSource.International, isHoliday = false, month = 11, day = 6
  ),
  CalendarRecord(
    title = "روز جهانی حسابداری",
    source = EventSource.International, isHoliday = false, month = 11, day = 10
  ),
  CalendarRecord(
    title = "روز جهانی دیابت",
    source = EventSource.International, isHoliday = false, month = 11, day = 14
  ),
  CalendarRecord(
    title = "روز جهانی مرد",
    source = EventSource.International, isHoliday = false, month = 11, day = 19
  ),
  CalendarRecord(
    title = "روز جهانی تلویزیون",
    source = EventSource.International, isHoliday = false, month = 11, day = 21
  ),
  CalendarRecord(
    title = "روز جهانی مبارزه با خشونت علیه زنان",
    source = EventSource.International, isHoliday = false, month = 11, day = 25
  ),
  CalendarRecord(
    title = "روز جهانی همبستگی با مردم فلسطین",
    source = EventSource.International, isHoliday = false, month = 11, day = 29
  ),
  CalendarRecord(
    title = "روز جهانی لغو برده‌داری",
    source = EventSource.International, isHoliday = false, month = 12, day = 2
  ),
  CalendarRecord(
    title = "روز جهانی داوطلبان پیشرفت اجتماعی",
    source = EventSource.International, isHoliday = false, month = 12, day = 5
  ),
  CalendarRecord(
    title = "روز جهانی مبارزه با فساد",
    source = EventSource.International, isHoliday = false, month = 12, day = 9
  ),
  CalendarRecord(
    title = "روز جهانی حقوق بشر",
    source = EventSource.International, isHoliday = false, month = 12, day = 10
  ),
  CalendarRecord(
    title = "روز جهانی کوهستان",
    source = EventSource.International, isHoliday = false, month = 12, day = 11
  ),
)

public val nepaliEvents: List<CalendarRecord> = listOf(
)

public val irregularRecurringEvents: List<Map<String, String>> = listOf(
  mapOf(
    "calendar" to "Persian",
    "rule" to "nth weekday of month",
    "nth" to "2",
    "weekday" to "7",
    "month" to "1",
    "type" to "Afghanistan",
    "title" to "هفتهٔ جیولوجست‌های افغانستان (هفتهٔ دوم حمل)",
    "holiday" to "false",
  ),
  mapOf(
    "calendar" to "Persian",
    "rule" to "last weekday of month",
    "weekday" to "7",
    "month" to "2",
    "type" to "Afghanistan",
    "title" to "هفتهٔ کتاب‌خوانی (هفتهٔ اخیر ثور)",
    "holiday" to "false",
  ),
  mapOf(
    "calendar" to "Persian",
    "rule" to "nth weekday of month",
    "nth" to "2",
    "weekday" to "6",
    "month" to "7",
    "type" to "Iran",
    "title" to "آیین مذهبی قالیشویان اردهال - بزرگداشت امامزاده علی بن محمدباقر (ع) (دومین جمعهٔ مهر)",
    "holiday" to "false",
  ),
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
  mapOf(
    "calendar" to "Hijri",
    "rule" to "last weekday of month",
    "weekday" to "6",
    "month" to "9",
    "type" to "Iran",
    "title" to "روز جهانی قدس (آخرین جمعهٔ رمضان)",
    "holiday" to "false",
  ),
  mapOf(
    "calendar" to "Persian",
    "rule" to "last weekday of month",
    "weekday" to "4",
    "offset" to "-1",
    "month" to "12",
    "type" to "Iran",
    "title" to "روز تکریم همسایگان (شب آخرین چهارشنبهٔ سال)",
    "holiday" to "false",
  ),
  mapOf(
    "calendar" to "Persian",
    "rule" to "last weekday of month",
    "weekday" to "3",
    "month" to "12",
    "type" to "AncientIran",
    "title" to "چهارشنبه‌سوری (آخرین سه‌شنبهٔ سال)",
    "holiday" to "false",
  ),
  mapOf(
    "calendar" to "Gregorian",
    "rule" to "nth weekday of month",
    "nth" to "3",
    "weekday" to "5",
    "month" to "11",
    "type" to "International",
    "title" to "روز جهانی فلسفه (سومین پنجشنبهٔ نوامبر)",
    "holiday" to "false",
  ),
  mapOf(
    "calendar" to "Gregorian",
    "rule" to "last weekday of month",
    "weekday" to "1",
    "month" to "1",
    "type" to "International",
    "title" to "روز جهانی کمک به جذامیان (آخرین یکشنبهٔ ژانویه)",
    "holiday" to "false",
  ),
  mapOf(
    "calendar" to "Gregorian",
    "rule" to "last weekday of month",
    "weekday" to "6",
    "month" to "11",
    "type" to "International",
    "title" to "جمعهٔ سیاه یا بلک فرایدی (آخرین جمعهٔ نوامبر)",
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
)
