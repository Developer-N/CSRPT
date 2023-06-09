package ir.namoo.religiousprayers.ui.calendar

import android.graphics.Typeface
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import kotlinx.coroutines.launch

@Composable
fun TimeItemUIElement(
    modifier: Modifier,
    timeState: TimeState,
    @DrawableRes icon: Int,
    cardColor: Color,
    iconColor: Color,
    textFont: Typeface,
    remTextColor: Color,
    stateClick: () -> Unit
) {
    val coroutine = rememberCoroutineScope()
    val scale = remember { Animatable(1.1f) }
    val infinity = rememberInfiniteTransition()
    val alpha = infinity.animateFloat(
        initialValue = 0.1f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse
        )
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = CardDefaults.outlinedShape,
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .weight(1f)
                    .scale(1.1f),
                painter = painterResource(id = icon),
                contentDescription = timeState.name,
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                modifier = Modifier.weight(4f),
                text = timeState.name,
                fontSize = 14.sp,
                fontFamily = FontFamily(textFont)
            )
            Text(
                modifier = Modifier
                    .weight(4f)
                    .alpha(if (timeState.remaining.isBlank()) 1f else alpha.value),
                text = timeState.remaining,
                fontSize = 12.sp,
                fontFamily = FontFamily(textFont),
                color = remTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.weight(2f),
                text = timeState.time,
                fontSize = 14.sp,
                fontFamily = FontFamily(textFont)
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .scale(scale.value), onClick = {
                stateClick()
                coroutine.launch {
                    scale.animateTo(0.1f, animationSpec = tween(100))
                    scale.animateTo(1.1f, animationSpec = tween(100))
                }
            }) {
                Icon(
                    painter = painterResource(id = if (timeState.state) R.drawable.ic_baseline_volume_up_24 else R.drawable.ic_baseline_volume_off_24),
                    contentDescription = timeState.name,
                    tint = iconColor
                )
            }

        }
    }
}
