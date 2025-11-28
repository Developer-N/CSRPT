package ir.namoo.quran.sura

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.numeral
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
import ir.namoo.quran.utils.vazirmatnFont

@Composable
fun AyaItem(
    modifier: Modifier = Modifier,
    quran: QuranEntity,
    translates: List<TranslateItem>? = null,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    onBookmark: () -> Unit,
    animations: List<State<Float>>,
    onCopyClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    onBookmarkClick: (QuranEntity) -> Unit,
    onNoteUpdate: (String) -> Unit,
    onBtnPlayClick: (QuranEntity) -> Unit
) {
    var content = ""
    val numeral by numeral.collectAsState()
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
            style = SpanStyle(
                fontFamily = FontFamily(vazirmatnFont)
            )
        ) {
            append(numeral.format(quran.verseID))
        }
        withStyle(
            style = SpanStyle(
                fontFamily = FontFamily(uthmanTahaFont)
            )
        ) {
            append("﴾")
        }
    }
    var noteText by remember { mutableStateOf(quran.note) }
    var showNotePanel by remember { mutableStateOf(false) }
    val btnPlaySize by animateIntAsState(
        targetValue = if (isPlaying) 56 else 28, label = "size"
    )
    val background by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.surfaceContainerHigh
        else MaterialTheme.colorScheme.surfaceContainerLow
    )
    var showDeleteDialog by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (isBookmarked) 1f else 0.1f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring())
            .padding(vertical = 1.dp, horizontal = 4.dp)
            .background(
                color = background, shape = MaterialTheme.shapes.small
            )
            .padding(vertical = 2.dp, horizontal = 4.dp)
    ) {
        if (quran.verseID == 1 && quran.surahID != 1 && quran.surahID != 9) SelectionContainer {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp, 2.dp),
                text = stringResource(id = R.string.str_bismillah),
                fontFamily = FontFamily(quranFont),
                fontSize = quranFontSize.sp,
                lineHeight = (quranFontSize * 1.7).sp,
                textAlign = TextAlign.Center
            )
        }
        SelectionContainer {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp, 2.dp),
                text = quranText,
                fontFamily = FontFamily(quranFont),
                fontSize = quranFontSize.sp,
                lineHeight = (quranFontSize * 1.7).sp,
                softWrap = true,
                overflow = TextOverflow.Visible,

                )
        }
        content += quranText
        content += "\n\n"
        if (!translates.isNullOrEmpty()) HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
        translates?.forEach { t ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp, 0.dp),
                text = t.name,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
            content += t.name
            content += ": "
            CompositionLocalProvider(values = arrayOf(if (t.translateType == TranslateType.ENGLISH) LocalLayoutDirection provides LayoutDirection.Ltr else LocalLayoutDirection provides LayoutDirection.Rtl)) {
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp, 0.dp),
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
                        } * 1.7).sp,
                        softWrap = true,
                        textAlign = TextAlign.Justify,
                        overflow = TextOverflow.Visible
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
                        for ((index, i) in (8..size.width.toInt() - 8 step 10).withIndex()) {
                            val currentSize = animations[index % animations.size].value
                            drawLine(
                                color = barColor,
                                start = Offset(i.toFloat(), size.height - 5),
                                end = Offset(i.toFloat(), size.height - currentSize - 5),
                                strokeWidth = 8f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                },
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onCopyClick(content) },
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = stringResource(id = R.string.copy)
                )
            }
            IconButton(
                onClick = { onShareClick(content) },
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(id = R.string.share)
                )
            }
            AnimatedContent(targetState = showNotePanel, label = "note") {
                IconButton(
                    onClick = { showNotePanel = !showNotePanel },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (it) Icons.Rounded.Close else if (quran.note.isNullOrBlank()) Icons.Rounded.AddComment else Icons.Rounded.EditNote,
                        contentDescription = stringResource(id = R.string.notes)
                    )
                }
            }
            AnimatedContent(targetState = quran.fav, label = "fav") {
                IconButton(
                    onClick = { onBookmarkClick(quran) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (it == 1) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                        contentDescription = stringResource(id = R.string.bookmarks)
                    )
                }
            }
            IconButton(
                onClick = { onBtnPlayClick(quran) },
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    modifier = Modifier.size(btnPlaySize.dp),
                    imageVector = if (isPlaying) Icons.Rounded.StopCircle else Icons.Rounded.PlayCircle,
                    contentDescription = stringResource(id = R.string.play)
                )
            }
            IconButton(
                onClick = onBookmark,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                )
            ) {
                Icon(imageVector = Icons.Default.Book, contentDescription = "")
            }
        }
        AnimatedVisibility(
            visible = showNotePanel, enter = expandVertically(), exit = shrinkVertically()
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp, 2.dp),
                value = noteText ?: "",
                onValueChange = { noteText = it },
                label = { Text(text = stringResource(id = R.string.your_note)) },
                leadingIcon = {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !noteText.isNullOrBlank(),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(id = R.string.delete)
                        )
                    }

                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onNoteUpdate(noteText ?: "")
                            showNotePanel = false
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        enabled = noteText?.isNotEmpty() == true && quran.note != noteText
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = stringResource(id = R.string.delete)
                        )
                    }
                },
                shape = MaterialTheme.shapes.extraLarge,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }
        if (showDeleteDialog) {
            AlertDialog(onDismissRequest = { showDeleteDialog = false }, confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        noteText = ""
                        onNoteUpdate(noteText ?: "")
                        showNotePanel = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = stringResource(id = R.string.yes), fontWeight = FontWeight.SemiBold
                    )
                }
            }, dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = stringResource(id = R.string.no), fontWeight = FontWeight.SemiBold
                    )
                }
            }, title = { Text(text = stringResource(id = R.string.alert)) }, text = {
                Text(
                    text = stringResource(id = R.string.delete_note_alert_message),
                    fontWeight = FontWeight.SemiBold
                )
            }, icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error
                )
            })
        }
    }
}
