package ir.namoo.religiousprayers.ui.azkar

import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ElevatedAssistChip
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import ir.namoo.religiousprayers.ui.azkar.data.AzkarItem
import ir.namoo.religiousprayers.ui.azkar.data.AzkarReference
import kotlinx.coroutines.launch

@Composable
fun AzkarItemUIElement(
    item: AzkarItem,
    reference: AzkarReference,
    lang: String,
    arabicTypeface: Typeface,
    itemState: AzkarItemState,
    play: () -> Unit,
    stop: () -> Unit,
    download: () -> Unit,
    delete: () -> Unit
) {

    var expanded by remember { mutableStateOf(false) }
    val rotate = remember { Animatable(0f) }
    val playScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    ElevatedCard(
        modifier = Modifier
            .padding(4.dp, 2.dp)
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring())
            .padding(4.dp, 2.dp)
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
                    text = item.arabic ?: "---",
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
                modifier = Modifier.padding(8.dp, 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedAssistChip(
                    onClick = {
                        expanded = !expanded
                        coroutineScope.launch {
                            rotate.animateTo(if (expanded) 180f else 0f, animationSpec = tween())
                        }
                    },
                    label = { Text(text = stringResource(id = R.string.reference)) },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(
                                id = R.string.reference
                            ), modifier = Modifier
                                .rotate(rotate.value)
                                .size(32.dp)
                        )
                    }
                )
                if (!item.sound.isNullOrEmpty()) AnimatedVisibility(
                    visible = !itemState.isDownloading,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                playScale.animateTo(0.5f, animationSpec = tween(100))
                                playScale.animateTo(1f, animationSpec = tween(100))
                            }
                            if (!itemState.isFileExists) download()
                            else if (!itemState.isPlaying) play()
                            else stop()
                        }, modifier = Modifier.scale(playScale.value)
                    ) {
                        Icon(
                            imageVector = if (itemState.isFileExists) {
                                if (itemState.isPlaying) Icons.Filled.StopCircle
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
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp, 1.dp),
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 4.dp
                    )
                }
                AnimatedVisibility(
                    visible = itemState.isFileExists,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    IconButton(
                        onClick = { delete() },
                        enabled = !itemState.isPlaying,
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
        }
    }
}
