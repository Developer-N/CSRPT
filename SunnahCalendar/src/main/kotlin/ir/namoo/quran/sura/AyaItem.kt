package ir.namoo.quran.sura

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.quran.sura.data.QuranEntity
import ir.namoo.quran.sura.data.TranslateItem
import ir.namoo.quran.sura.data.TranslateType
import ir.namoo.quran.utils.englishFont
import ir.namoo.quran.utils.englishFontSize
import ir.namoo.quran.utils.farsiFont
import ir.namoo.quran.utils.farsiFontSize
import ir.namoo.quran.utils.kurdishFont
import ir.namoo.quran.utils.kurdishFontSize
import ir.namoo.quran.utils.quranFont
import ir.namoo.quran.utils.quranFontSize
import ir.namoo.quran.utils.uthmanTahaFont

@Composable
fun AyaItem(
    modifier: Modifier,
    quran: QuranEntity,
    translates: List<TranslateItem>? = null,
    isPlaying: Boolean,
    animations: List<State<Float>>,
    onCopyClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    onBookmarkClick: (QuranEntity) -> Unit,
    onNoteUpdate: (String) -> Unit,
    onBtnPlayClick: (QuranEntity) -> Unit
) {
    var content = ""
    val quranText = buildAnnotatedString {
        append(quran.quranArabic)
        append(" ")
        withStyle(
            style = SpanStyle(
                fontFamily = FontFamily(uthmanTahaFont)
            )
        ) {
            append("﴿")
        }
        withStyle(
            style = SpanStyle()
        ) {
            append(formatNumber(quran.verseID))
        }
        withStyle(
            style = SpanStyle(
                fontFamily = FontFamily(uthmanTahaFont)
            )
        ) {
            append("﴾")
        }
    }
    val noteText = remember { mutableStateOf(quran.note) }
    val showNotePanel = remember { mutableStateOf(false) }
    val btnPlayScale = remember { Animatable(1f) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isPlaying) {
        btnPlayScale.animateTo(if (isPlaying) 1.5f else 1f)
    }
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring())
            .padding(2.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            if (quran.verseID == 1 && quran.surahID != 1 && quran.surahID != 9)
                CompositionLocalProvider(
                    LocalTextSelectionColors provides TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.secondaryContainer,
                        backgroundColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    SelectionContainer {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp, 2.dp),
                            text = stringResource(id = R.string.str_bismillah),
                            fontFamily = FontFamily(quranFont),
                            fontSize = quranFontSize.sp,
                            lineHeight = (quranFontSize * 1.7).sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            CompositionLocalProvider(
                LocalTextSelectionColors provides TextSelectionColors(
                    handleColor = MaterialTheme.colorScheme.secondaryContainer,
                    backgroundColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 2.dp),
                        text = quranText,
                        fontFamily = FontFamily(quranFont),
                        fontSize = quranFontSize.sp,
                        lineHeight = (quranFontSize * 1.7).sp,
                        softWrap = true
                    )
                }
            }
            content += quranText
            content += "\n\n"
        }
        translates?.forEach { t ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp),
                text = t.name,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
            content += t.name
            content += ": "
            CompositionLocalProvider(values = arrayOf(if (t.translateType == TranslateType.ENGLISH) LocalLayoutDirection provides LayoutDirection.Ltr else LocalLayoutDirection provides LayoutDirection.Rtl)) {
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 0.dp),
                        text = t.text.trim(),
                        fontFamily = FontFamily(
                            when (t.translateType) {
                                TranslateType.KURDISH -> kurdishFont
                                TranslateType.FARSI -> farsiFont
                                TranslateType.ENGLISH -> englishFont
                            }
                        ),
                        fontSize = when (t.translateType) {
                            TranslateType.KURDISH -> kurdishFontSize
                            TranslateType.FARSI -> farsiFontSize
                            TranslateType.ENGLISH -> englishFontSize
                        }.sp,
                        lineHeight = (when (t.translateType) {
                            TranslateType.KURDISH -> kurdishFontSize
                            TranslateType.FARSI -> farsiFontSize
                            TranslateType.ENGLISH -> englishFontSize
                        } * 1.7).sp
                    )
                }
                content += t.text.trim()
                content += "\n\n"
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
        val barColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (isPlaying) {
                        for ((index, i) in (30..size.width.toInt() - 20 step 20).withIndex()) {
                            val currentSize = animations[index % animations.size].value
                            drawLine(
                                color = barColor,
                                start = Offset(i.toFloat(), size.height),
                                end = Offset(i.toFloat(), size.height - currentSize),
                                strokeWidth = 15f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                },
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onCopyClick(content) }) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = stringResource(id = R.string.copy),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { onShareClick(content) }) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(id = R.string.share),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { showNotePanel.value = !showNotePanel.value }) {
                Icon(
                    imageVector = if (showNotePanel.value) Icons.Rounded.Close else if (quran.note.isNullOrBlank()) Icons.Rounded.AddComment else Icons.Rounded.EditNote,
                    contentDescription = stringResource(id = R.string.notes),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { onBookmarkClick(quran) }) {
                Icon(
                    imageVector = if (quran.fav == 1) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                    contentDescription = stringResource(id = R.string.bookmarks),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(modifier = Modifier
                .scale(btnPlayScale.value),
                onClick = { onBtnPlayClick(quran) }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.StopCircle else Icons.Rounded.PlayCircle,
                    contentDescription = stringResource(id = R.string.play),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        AnimatedVisibility(
            visible = showNotePanel.value, enter = expandVertically(), exit = shrinkVertically()
        ) {
            TextField(modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp, 2.dp),
                value = noteText.value ?: "",
                onValueChange = { noteText.value = it },
                label = { Text(text = stringResource(id = R.string.your_note)) },
                leadingIcon = {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !noteText.value.isNullOrBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(id = R.string.delete),
                            tint = if (noteText.value.isNullOrBlank()) MaterialTheme.colorScheme.scrim else MaterialTheme.colorScheme.error
                        )
                    }

                },
                trailingIcon = {
                    IconButton(onClick = {
                        onNoteUpdate(noteText.value ?: "")
                        showNotePanel.value = false
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = stringResource(id = R.string.delete),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                })
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        noteText.value = ""
                        onNoteUpdate(noteText.value ?: "")
                        showNotePanel.value = false
                    }) {
                        Text(text = stringResource(id = R.string.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(text = stringResource(id = R.string.no))
                    }
                },
                title = { Text(text = stringResource(id = R.string.alert)) },
                text = { Text(text = stringResource(id = R.string.delete_note_alert_message)) },
                icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = "") })
        }
    }
}
