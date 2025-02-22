package ir.namoo.religiousprayers.ui.about

import android.os.Build
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.about.createIconRandomEffects

@Composable
fun InfoUIElement(versionDescription: String) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Column {
            InfoTitle()
            Spacer(modifier = Modifier.height(8.dp))
            InfoVersion(versionDescription)
            Spacer(modifier = Modifier.height(8.dp))
            InfoDescription()
        }

    }
}

@Composable
fun InfoTitle() {
    Text(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        text = stringResource(id = R.string.namoo_developer_group),
        textAlign = TextAlign.Center,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun InfoVersion(versionDescription: String) {
    var logoAnimationAtEnd by remember { mutableStateOf(false) }
    var logoEffect by remember { mutableStateOf<RenderEffect?>(null) }
    val effectsGenerator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) createIconRandomEffects()
        else null
    }
    LaunchedEffect(Unit) { logoAnimationAtEnd = !logoAnimationAtEnd }

    Row(modifier = Modifier
        .fillMaxWidth()
        .semantics { this.hideFromAccessibility() }
        .clickable {
            logoAnimationAtEnd = false
            logoAnimationAtEnd = true
            logoEffect = effectsGenerator
                ?.invoke()
                ?.asComposeRenderEffect()
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(6.dp)
                .weight(2f),
            text = versionDescription,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        val image = AnimatedImageVector.animatedVectorResource(R.drawable.app_icon_animated)

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Image(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { renderEffect = logoEffect },
                painter = rememberAnimatedVectorPainter(image, logoAnimationAtEnd),
                contentDescription = stringResource(
                    id = R.string.app_name
                ), contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun InfoDescription() {
    Text(
        modifier = Modifier.padding(6.dp),
        text = stringResource(id = R.string.str_info_msg),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
    )
}
