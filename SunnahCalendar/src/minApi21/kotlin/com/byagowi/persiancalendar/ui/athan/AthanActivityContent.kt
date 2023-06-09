package com.byagowi.persiancalendar.ui.athan

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.getPrayTimeName
import com.google.accompanist.drawablepainter.rememberDrawablePainter

fun setAthanActivityContent(activity: ComponentActivity, prayerKey: String, onClick: () -> Unit) {
    val cityName = activity.appPrefs.cityName
    activity.setContent { AthanActivityContent(prayerKey, cityName, onClick) }
}

@Composable
private fun AthanActivityContent(prayerKey: String, cityName: String?, onClick: () -> Unit) {
    Box(modifier = Modifier.clickable { onClick() }) {
        Image(
            painter = rememberDrawablePainter(
                PatternDrawable(
                    prayerKey, darkBaseColor = Theme.isNightMode(LocalContext.current),
                    dp = 1.dp.value
                )
            ),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.padding(horizontal = 30.dp, vertical = 80.dp)) {
            val textStyle = LocalTextStyle.current.copy(
                color = Color.White, fontWeight = FontWeight.Bold,
                shadow = Shadow(color = Color.Black, blurRadius = 2f, offset = Offset(1f, 1f))
            )
            Text(stringResource(getPrayTimeName(prayerKey)), fontSize = 36.sp, style = textStyle)
            if (cityName != null) {
                var visible by remember { mutableStateOf(false) }
                val density = LocalDensity.current
                // Just an exaggerated demo for https://developer.android.com/jetpack/compose/animation#animatedvisibility
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically { with(density) { -40.dp.roundToPx() } }
                            + expandVertically(expandFrom = Alignment.Top)
                            + fadeIn(initialAlpha = 0.3f),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut()
                ) {
                    Text(
                        stringResource(R.string.in_city_time, cityName),
                        fontSize = 18.sp,
                        style = textStyle,
                        modifier = Modifier.padding(top = 10.dp).fillMaxWidth().height(200.dp)
                    )
                }
                visible = true
            }
        }
    }
}

@Preview
@Composable
private fun AthanActivityContentPreview() = AthanActivityContent(FAJR_KEY, "CITY NAME") {}
