package com.byagowi.persiancalendar.ui.converter

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class ConverterScreenMode(@StringRes val title: Int) {
    Converter(R.string.date_converter),
    Distance(R.string.days_distance),
    Calculator(R.string.calculator),
    TimeZones(R.string.time_zones),
    QrCode(R.string.qr_code),
}
