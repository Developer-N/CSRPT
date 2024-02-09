package ir.namoo.religiousprayers.ui.shared

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R

@Composable
fun LoadingUIElement() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "infinite alpha")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.1f, targetValue = 1f, animationSpec = infiniteRepeatable(
                animation = tween(500), repeatMode = RepeatMode.Reverse
            ), label = "alpha"
        )
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            modifier = Modifier
                .alpha(alpha)
                .padding(4.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.loading)
        )
    }
}
