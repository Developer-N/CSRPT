package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.calculateDatePartsDifference
import io.github.persiancalendar.calendar.PersianDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class CalendarTests {
    @ParameterizedTest
    @CsvSource(
        "1397, 1, 31",
        "1397, 2, 31",
        "1397, 3, 31",
        "1397, 4, 31",
        "1397, 5, 31",
        "1397, 6, 31",
        "1397, 7, 30",
        "1397, 8, 30",
        "1397, 9, 30",
        "1397, 10, 30",
        "1397, 11, 30",
        "1397, 12, 29",
    )
    fun `getMonthLength calculating correctness`(year: Int, month: Int, day: Int) {
        assertEquals(day, CalendarType.SHAMSI.getMonthLength(year, month))
    }

    @Test
    fun `leap years correctness`() {
        // Matches with https://calendar.ut.ac.ir/Fa/News/Data/Doc/KabiseShamsi1206-1498-new.pdf
        val leapYears = listOf(
            1210, 1214, 1218, 1222, 1226, 1230, 1234, 1238, 1243, 1247, 1251, 1255, 1259, 1263,
            1267, 1271, 1276, 1280, 1284, 1288, 1292, 1296, 1300, 1304, 1309, 1313, 1317, 1321,
            1325, 1329, 1333, 1337, 1342, 1346, 1350, 1354, 1358, 1362, 1366, 1370, 1375, 1379,
            1383, 1387, 1391, 1395, 1399, 1403, 1408, 1412, 1416, 1420, 1424, 1428, 1432, 1436,
            1441, 1445, 1449, 1453, 1457, 1461, 1465, 1469, 1474, 1478, 1482, 1486, 1490, 1494,
            1498
        )

        (1206..1498).forEach {
            assertEquals(
                if (it in leapYears) 30 else 29,
                CalendarType.SHAMSI.getMonthLength(it, 12),
                it.toString()
            )
        }
    }

    @ParameterizedTest
    @CsvSource(
        "1400, 12, 29, 2",
        "1400, 12, 28, 1",
        "1400, 12, 27, 7",
        "1400, 12, 26, 6",
        "1400, 12, 25, 5",
        "1400, 12, 24, 4",
        "1400, 12, 23, 3",
        "1400, 12, 27, 7",
        "1399, 12, 29, 7",
        "1398, 12, 23, 7",
        "1397, 12, 24, 7",
        "1396, 12, 25, 7",
        "1395, 12, 27, 7",
        "1394, 12, 28, 7",
        "1393, 12, 29, 7",
        "1392, 12, 23, 7",
        "1391, 12, 25, 7",
        "1390, 12, 26, 7",
    )
    fun `getLastDayOfWeekOfMonth calculations correctness`(
        year: Int, month: Int, day: Int, dayOfWeek: Int
    ) {
        assertEquals(day, CalendarType.SHAMSI.getLastWeekDayOfMonth(year, month, dayOfWeek))
    }

    @ParameterizedTest
    @CsvSource(
        "0, 1398, 9, 9",
        "1, 1398, 9, 10",
        "2, 1398, 9, 11",
        "3, 1398, 9, 12",
        "4, 1398, 9, 13",
        "5, 1398, 9, 14",
        "6, 1398, 9, 15",
        "0, 1398, 9, 16",
        "1, 1398, 9, 17",
        "2, 1398, 9, 18",
        "3, 1398, 9, 19",
        "4, 1398, 9, 20",
        "5, 1398, 9, 21",
        "6, 1398, 9, 22",
    )
    fun `dayOfWeek calculations correctness`(
        dayOfWeek: Int, year: Int, month: Int, dayOfMonth: Int
    ) {
        assertEquals(dayOfWeek, Jdn(PersianDate(year, month, dayOfMonth)).dayOfWeek)
    }

    @ParameterizedTest
    @CsvSource(
        "1400, 11, 15, 1363, 3, 19, 37, 7, 27",
        "1400, 8, 1, 1400, 6, 31, 0, 1, 1",
        "1400, 11, 15, 1363, 11, 15, 37, 0, 0",
        "1400, 11, 15, 1363, 11, 14, 37, 0, 1",
        "1400, 11, 15, 1363, 11, 16, 36, 11, 29",
        "1400, 11, 15, 1400, 7, 15, 0, 4, 0",
        "1400, 11, 15, 1400, 8, 15, 0, 3, 0",
        "1400, 11, 15, 1400, 9, 15, 0, 2, 0",
        "1400, 11, 15, 1400, 10, 15, 0, 1, 0",
        // Adopted from http://www.ssu.ac.ir/cms/fileadmin/user_upload/Moavenatha/MBehdashti/Gostaresh/pdf/amar/dastor/Mohasebe-Sen.pdf
        // Except we don't have the same result
        "1379, 6, 10, 1360, 5, 20, 19, 0, 21",
        "1379, 6, 10, 1360, 5, 20, 19, 0, 21",
        "1360, 6, 10, 1360, 5, 20, 0, 0, 21",
    )
    fun `test date parts difference`(
        higherYear: Int, higherMonth: Int, higherDay: Int,
        lowerYear: Int, lowerMonth: Int, lowerDay: Int,
        year: Int, month: Int, day: Int,
    ) {
        val higher = PersianDate(higherYear, higherMonth, higherDay)
        val lower = PersianDate(lowerYear, lowerMonth, lowerDay)
        val (y, m, d) = calculateDatePartsDifference(higher, lower, CalendarType.SHAMSI)
        assertEquals(year, y)
        assertEquals(month, m)
        assertEquals(day, d)
    }

    @ParameterizedTest
    @CsvSource(
        "1400, 12, 15, 75, 89",
        "1400, 10, 1, 1, 89",
        "1400, 12, 29, 89, 89",
        "1399, 12, 30, 90, 90",
        "1399, 9, 30, 90, 90",
        "1399, 8, 30, 60, 90",
        "1399, 7, 30, 30, 90",
        "1399, 6, 31, 93, 93",
        "1399, 4, 1, 1, 93",
        "1399, 3, 31, 93, 93",
    )
    fun `season passed days`(year: Int, month: Int, day: Int, passedDays: Int, daysCount: Int) {
        val jdn = Jdn(CalendarType.SHAMSI, year, month, day)
        val (seasonPassedDays, seasonDaysCount) = jdn.calculatePersianSeasonPassedDaysAndCount()
        assertEquals(passedDays, seasonPassedDays)
        assertEquals(daysCount, seasonDaysCount)
    }
}
