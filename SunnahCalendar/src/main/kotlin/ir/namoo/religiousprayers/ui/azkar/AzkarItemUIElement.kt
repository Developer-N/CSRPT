package ir.namoo.religiousprayers.ui.azkar

import android.graphics.Typeface
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.religiousprayers.ui.azkar.data.AzkarItem
import ir.namoo.religiousprayers.ui.azkar.data.AzkarReference
import ir.namoo.religiousprayers.ui.shared.PlayerComponent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AzkarItemUIElement(
    item: AzkarItem,
    reference: AzkarReference,
    lang: String,
    arabicTypeface: Typeface,
    itemState: AzkarItemState,
    isPlaying: Boolean,
    currentPosition: Int,
    duration: Int,
    isPlayingCurrentItem: Boolean,
    pause: () -> Unit,
    resume: () -> Unit,
    seekTo: (Float) -> Unit,
    play: () -> Unit,
    stop: () -> Unit,
    download: () -> Unit,
    delete: () -> Unit,
    addReadCount: () -> Unit,
    resetReadCount: () -> Unit
) {

    var expanded by remember { mutableStateOf(false) }
    val rotate by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotate")
    val downloadProgress by animateFloatAsState(
        targetValue = itemState.progress, label = "download progress", animationSpec = tween()
    )
    ElevatedCard(
        modifier = Modifier
            .padding(4.dp, 2.dp)
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
                )
            )
            .padding(4.dp, 2.dp)
            .combinedClickable(onClick = addReadCount, onLongClick = resetReadCount)
            .drawBehind {

            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 4.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
        ) {
            SelectionContainer {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    text = item.arabic ?: "-",
                    fontFamily = FontFamily(arabicTypeface),
                    fontSize = 22.sp,
                    lineHeight = 35.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (lang != Language.AR.code) SelectionContainer {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp), text = when (lang) {
                        Language.FA.code -> item.persian
                        else -> item.kurdish
                    } ?: "---"
                )
            }
            Row(
                modifier = Modifier
                    .padding(8.dp, 4.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    ElevatedButton(onClick = { expanded = !expanded }) {
                        Text(
                            text = stringResource(id = R.string.reference),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Icon(
                            modifier = Modifier.rotate(rotate),
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.reference)
                        )
                    }
                    AnimatedVisibility(
                        visible = !itemState.isDownloading,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally()
                    ) {
                        IconButton(
                            onClick = {
                                if (!itemState.isFileExist) download()
                                else if (isPlayingCurrentItem) stop()
                                else play()
                            }
                        ) {
                            Icon(
                                imageVector = if (itemState.isFileExist) {
                                    if (isPlayingCurrentItem) Icons.Filled.StopCircle
                                    else Icons.Filled.PlayCircle
                                } else Icons.Filled.CloudDownload,
                                contentDescription = stringResource(id = R.string.play),
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = itemState.isDownloading,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally()
                    ) {
                        CircularProgressIndicator(
                            progress = { downloadProgress / 100f },
                            modifier = Modifier
                                .size(42.dp)
                                .padding(4.dp, 1.dp),
                            strokeWidth = 6.dp,
                            strokeCap = StrokeCap.Round,
                        )
                    }
                    AnimatedVisibility(
                        visible = itemState.isFileExist,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally()
                    ) {
                        IconButton(
                            onClick = { delete() },
                            enabled = !isPlayingCurrentItem,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(id = R.string.delete),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                AnimatedContent(targetState = itemState.readCount, label = "read count") {
                    Text(
                        modifier = Modifier
                            .padding(4.dp)
                            .alpha(if (it == 0) 0.2f else 1f),
                        text = formatNumber(it),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp), text = (when (lang) {
                        Language.CKB.code -> reference.kurdish
                        Language.FA.code -> reference.persian
                        else -> reference.arabic
                    }) ?: "--"
                )
            }
            AnimatedVisibility(visible = isPlayingCurrentItem) {
                PlayerComponent(isPlaying = isPlaying,
                    totalDuration = duration.toFloat(),
                    onPlayPauseClick = {
                        if (isPlaying) pause()
                        else resume()
                    },
                    progress = currentPosition.toFloat(),
                    onSeek = { seekTo(it) })
            }
        }
    }
}
