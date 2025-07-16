package ir.namoo.quran.sura

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import kotlinx.coroutines.launch

@Composable
fun PlayerComponent(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isAutoScroll: Boolean = false,
    showAutoScroll: Boolean = false,
    qariPhotoLink: String = "",
    aya: Int = 1,
    sura: String = "الفاتحه",
    duration: Long = 0,
    currentPosition: Long = 0,
    pause: () -> Unit = {},
    resume: () -> Unit = {},
    stop: () -> Unit = {},
    seekTo: (Long) -> Unit = {},
    next: () -> Unit = {},
    nextSura: () -> Unit = {},
    previous: () -> Unit = {},
    previousSura: () -> Unit = {},
    onAutoScrollChange: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val alpha by animateFloatAsState(targetValue = if (isAutoScroll) 1f else 0.5f)
    val position by animateFloatAsState(targetValue = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f)
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.large.copy(
                bottomEnd = CornerSize(0.dp), bottomStart = CornerSize(0.dp)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(targetState = qariPhotoLink, label = "qariPhotoLink") {
                    GlideImage(
                        modifier = Modifier
                            .padding(vertical = 2.dp, horizontal = 8.dp)
                            .size(54.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(color = MaterialTheme.colorScheme.surfaceContainer),
                        imageModel = { it.trim() },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Fit, alignment = Alignment.Center
                        ),
                        component = rememberImageComponent {
                            +ShimmerPlugin(
                                Shimmer.Flash(
                                    baseColor = MaterialTheme.colorScheme.surfaceContainer,
                                    highlightColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        },
                        failure = {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        })
                }
                Column(modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        AnimatedContent(targetState = sura, label = "sura") {
                            Text(
                                text = formatNumber(stringResource(R.string.sura) + ": $it"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                        AnimatedContent(targetState = aya, label = "aya") {
                            Text(
                                text = formatNumber(stringResource(R.string.aya) + ": $it"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Next Button
                        IconButton(
                            onClick = nextSura,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardDoubleArrowRight,
                                contentDescription = "Next"
                            )
                        }

                        IconButton(
                            onClick = next,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next")
                        }

                        //Play Pause
                        AnimatedContent(targetState = isPlaying, label = "play") {
                            IconButton(
                                modifier = Modifier.size(54.dp),
                                onClick = { if (it) pause() else resume() },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    modifier = Modifier.size(68.dp),
                                    imageVector = if (it) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = if (isPlaying) "Pause" else "Play"
                                )
                            }
                        }

                        // Previous Button
                        IconButton(
                            onClick = previous,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous"
                            )
                        }
                        IconButton(
                            onClick = previousSura,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardDoubleArrowLeft,
                                contentDescription = "Next"
                            )
                        }
                        // Auto Scroll
                        AnimatedVisibility(visible = showAutoScroll) {
                            IconButton(
                                onClick = onAutoScrollChange,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = alpha
                                    )
                                )
                            ) {
                                Icon(
                                    modifier = Modifier.rotate(90f),
                                    imageVector = Icons.Default.DoubleArrow,
                                    contentDescription = "scroll"
                                )
                            }
                        }
                    }
                }
            }
            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 8.dp),
                value = position,
                onValueChange = {
                    coroutineScope.launch {
                        seekTo((it * duration).toLong())
                    }
                })
        }

        Icon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable(onClick = stop),
            imageVector = Icons.Default.Close,
            contentDescription = "Stop",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, locale = "fa")
@Composable
private fun PreviewPlayerComponent() {
    MaterialTheme {
        PlayerComponent(modifier = Modifier.fillMaxWidth())
    }
}
