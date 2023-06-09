package ir.namoo.religiousprayers.ui.about

import android.graphics.Typeface
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R

@Composable
fun InfoUIElement(
    typeface: Typeface, versionDescription: String
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Column {
            InfoTitle(typeface)
            Spacer(modifier = Modifier.height(8.dp))
            InfoVersion(typeface, versionDescription)
            Spacer(modifier = Modifier.height(8.dp))
            InfoDescription(typeface)
        }

    }
}

@Composable
fun InfoTitle(typeface: Typeface) {
    Text(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        text = stringResource(id = R.string.namoo_developer_group),
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily(typeface)
    )
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun InfoVersion(typeface: Typeface, versionDescription: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier
                .padding(6.dp)
                .weight(2f),
            text = versionDescription,
            fontSize = 12.sp,
            fontFamily = FontFamily(typeface)
        )
        val image = AnimatedImageVector.animatedVectorResource(R.drawable.app_icon_animated)
        val state = remember { mutableStateOf(false) }
        LaunchedEffect(key1 = "app_icon_animated") {
            state.value = true
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Image(
                modifier = Modifier
                    .weight(1f),
                painter = rememberAnimatedVectorPainter(image, state.value),
                contentDescription = stringResource(
                    id = R.string.app_name
                )
            )
        }
    }
}

@Composable
fun InfoDescription(typeface: Typeface) {
    Text(
        modifier = Modifier.padding(6.dp),
        text = stringResource(id = R.string.str_info_msg),
        fontSize = 12.sp,
        fontFamily = FontFamily(typeface)
    )
}
