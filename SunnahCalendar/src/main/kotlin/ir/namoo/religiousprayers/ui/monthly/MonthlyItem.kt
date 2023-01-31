package ir.namoo.religiousprayers.ui.monthly

import android.graphics.Typeface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getFromStringId
import io.github.persiancalendar.praytimes.PrayTimes

@Composable
fun MonthlyItemUIElement(
    prayTime: PrayTimes,
    monthDaySummary: String,
    typeface: Typeface,
    fontSize: TextUnit,
    cardColor: Color,
    textColor: Color
) {

    val fajr = Clock.fromHoursFraction(prayTime.fajr).toMinutes().toFloat()
    val maghrib = Clock.fromHoursFraction(prayTime.maghrib).toMinutes().toFloat()
    val dayLength = Clock.fromMinutesCount((maghrib - fajr).toInt())
    val dayLong = stringResource(R.string.length_of_day) + spacedColon +
            dayLength.asRemainingTime(LocalContext.current.resources, short = false)

    Card(
        modifier = Modifier
            .padding(4.dp, 2.dp)
            .fillMaxWidth()
            .padding(2.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = monthDaySummary,
                    fontSize = fontSize,
                    fontFamily = FontFamily(typeface),
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    modifier = Modifier.weight(2f),
                    text = dayLong,
                    fontSize = fontSize,
                    fontFamily = FontFamily(typeface),
                    color = textColor
                )
            }
            Divider(modifier = Modifier.padding(8.dp, 2.dp), thickness = 2.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp, 2.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.fajr),
                        fontSize = fontSize,
                        fontFamily = FontFamily(typeface),
                        color = textColor
                    )
                    Text(
                        text = formatNumber(
                            prayTime.getFromStringId(R.string.fajr).toFormattedString()
                        ),
                        fontFamily = FontFamily(typeface),
                        fontSize = fontSize,
                        color = textColor
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.sunrise),
                        fontSize = fontSize,
                        fontFamily = FontFamily(typeface),
                        color = textColor
                    )
                    Text(
                        text = formatNumber(
                            prayTime.getFromStringId(R.string.sunrise).toFormattedString()
                        ),
                        fontFamily = FontFamily(typeface),
                        fontSize = fontSize,
                        color = textColor
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.dhuhr),
                        fontSize = fontSize,
                        fontFamily = FontFamily(typeface),
                        color = textColor
                    )
                    Text(
                        text = formatNumber(
                            prayTime.getFromStringId(R.string.dhuhr).toFormattedString()
                        ),
                        fontFamily = FontFamily(typeface),
                        fontSize = fontSize,
                        color = textColor
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.asr),
                        fontSize = fontSize,
                        fontFamily = FontFamily(typeface),
                        color = textColor
                    )
                    Text(
                        text = formatNumber(
                            prayTime.getFromStringId(R.string.asr).toFormattedString()
                        ),
                        fontFamily = FontFamily(typeface),
                        fontSize = fontSize,
                        color = textColor
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.maghrib),
                        fontSize = fontSize,
                        fontFamily = FontFamily(typeface),
                        color = textColor
                    )
                    Text(
                        text = formatNumber(
                            prayTime.getFromStringId(R.string.maghrib).toFormattedString()
                        ),
                        fontFamily = FontFamily(typeface),
                        fontSize = fontSize,
                        color = textColor
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.isha),
                        fontSize = fontSize,
                        fontFamily = FontFamily(typeface),
                        color = textColor
                    )
                    Text(
                        text = formatNumber(
                            prayTime.getFromStringId(R.string.isha).toFormattedString()
                        ),
                        fontFamily = FontFamily(typeface),
                        fontSize = fontSize,
                        color = textColor
                    )
                }
            }
        }
    }
}
