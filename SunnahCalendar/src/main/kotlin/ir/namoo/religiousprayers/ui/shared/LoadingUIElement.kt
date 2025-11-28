package ir.namoo.religiousprayers.ui.shared

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R

@Composable
fun LoadingUIElement(text: String = stringResource(id = R.string.loading)) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite alpha")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(1000), repeatMode = RepeatMode.Restart
        ), label = "alpha"
    )
    val radius by infiniteTransition.animateFloat(
        initialValue = 5f, targetValue = 15f, animationSpec = infiniteRepeatable(
            animation = tween(1000), repeatMode = RepeatMode.Reverse
        ), label = "radius"
    )
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        shadowElevation = 2.dp,
        border = BorderStroke(width = 0.5.dp, color = color)
    ) {
        Box(modifier = Modifier.drawBehind(onDraw = {
            val height = size.height
            val width = size.width
            drawCircle(
                color = color,
                radius = radius.dp.toPx(),
                center = Offset(x = 50f, y = height / 2),
            )
            drawCircle(
                color = color,
                radius = radius.dp.toPx(),
                center = Offset(x = width - 50f, y = height / 2),
            )
        })) {
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .alpha(alpha)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(locale = "fa")
@Composable
fun LoadingUIElementPreview() {
    MaterialTheme {
        LoadingUIElement()
    }
}

