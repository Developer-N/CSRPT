package ir.namoo.religiousprayers.ui.downloadtimes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.cardColor
import ir.namoo.commons.utils.iconColor

@Composable
fun CityItemUIElement(
    city: CityModel, searchText: String, cityItemState: CityItemState, download: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp, 2.dp)
            .fillMaxWidth()
            .clickable { download() },
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AnimatedVisibility(
                visible = cityItemState.isSelected,
                enter = slideInHorizontally(animationSpec = spring()),
                exit = shrinkHorizontally(animationSpec = spring())
            ) {
                Icon(
                    Icons.Filled.Check,
                    modifier = Modifier.weight(1f),
                    contentDescription = "Selected",
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            val cityName = buildAnnotatedString {
                if (searchText.isNotEmpty() && city.name.contains(searchText)) {
                    val index = city.name.indexOf(searchText)
                    for (i in 0..<index) append("${city.name[i]}")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error
                        )
                    ) {
                        append(searchText)
                    }
                    for (i in (index + searchText.length)..<city.name.length) append("${city.name[i]}")
                } else append(city.name)
            }
            Text(
                text = cityName, modifier = Modifier.weight(4f), fontFamily = FontFamily(appFont)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = city.lastUpdate,
                modifier = Modifier.weight(4f),
                fontFamily = FontFamily(appFont)
            )
            Spacer(modifier = Modifier.width(4.dp))
            AnimatedVisibility(visible = cityItemState.isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .weight(1f)
                        .size(26.dp),
                    strokeCap = StrokeCap.Round
                )
            }
            AnimatedVisibility(visible = !cityItemState.isDownloading) {

                IconButton(modifier = Modifier
                    .weight(1f)
                    .height(32.dp), onClick = { download() }) {
                    Icon(
                        imageVector = if (cityItemState.isDownloaded) Icons.Filled.DownloadDone else Icons.Filled.CloudDownload,
                        contentDescription = "Selected",
                        tint = iconColor
                    )
                }
            }
        }
    }
}

//@Preview(locale = "fa", showBackground = true)
//@Preview(locale = "fa", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
//@Composable
//fun PC() {
//    Mdc3Theme {
//        CityItemUIElement(
//            CityModel(
//                1,
//                "سردشت",
//                36.155278,
//                45.478889,
//                0,
//                2,
//                "2022-02-06T05:12:11.000000Z",
//                "2022-02-06T05:12:11.000000Z"
//            ),
//            Typeface.createFromAsset(LocalContext.current.assets, "fonts/Vazirmatn.ttf"),
//            Color.DarkGray,
//            Color.White,
//            Color.DarkGray
//        )
//    }
//}
