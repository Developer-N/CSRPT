package ir.namoo.religiousprayers.ui.shared

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.ranges.rangeTo

@Composable
fun PlayerComponent(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    totalDuration: Float,
    onPlayPauseClick: () -> Unit,
    progress: Float,
    onSeek: (Float) -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            IconButton(
                onClick = onPlayPauseClick,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    modifier = Modifier.scale(2f),
                    imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = "Play/Pause"
                )
            }
            Slider(
                value = animatedProgress,
                onValueChange = { onSeek(it) },
                valueRange = 0f..totalDuration,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PrevPlayerComponent() {
    PlayerComponent(
        isPlaying = false,
        totalDuration = 5684f,
        onPlayPauseClick = {},
        progress = 500f,
        onSeek = {}
    )
}
