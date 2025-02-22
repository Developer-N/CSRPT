package com.byagowi.persiancalendar.utils

import java.util.concurrent.TimeUnit

val QUARTER_SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1) / 4
val HALF_SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1) / 2
val ONE_SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1)
val TWO_SECONDS_IN_MILLIS = TimeUnit.SECONDS.toMillis(2)
val FIVE_SECONDS_IN_MILLIS = TimeUnit.SECONDS.toMillis(5)
val TEN_SECONDS_IN_MILLIS = TimeUnit.SECONDS.toMillis(10)
val THIRTY_SECONDS_IN_MILLIS = TimeUnit.SECONDS.toMillis(30)
val ONE_MINUTE_IN_MILLIS = TimeUnit.MINUTES.toMillis(1)
val SIX_MINUTES_IN_MILLIS = TimeUnit.MINUTES.toMillis(6)
val FIFTEEN_MINUTES_IN_MILLIS = TimeUnit.MINUTES.toMillis(15)
val HALF_HOUR_IN_MILLIS = TimeUnit.MINUTES.toMillis(30)
val ONE_HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1)
val DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)

const val MINUTES_PER_DAY = 60 * 24

// Matches https://github.com/aosp-mirror/platform_frameworks_base/blob/1dcde70/services/core/java/com/android/server/notification/NotificationManagerService.java#L382
val THREE_SECONDS_AND_HALF_IN_MILLIS = TimeUnit.SECONDS.toMillis(7) / 2
