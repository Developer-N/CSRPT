package ir.namoo.quran.notes

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.NotesScreen(
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    navigateToVerse: (Int, Int) -> Unit,
    viewModel: NotesViewModel = koinViewModel()
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.loadData()
    }
    val isLoading by viewModel.isLoading.collectAsState()
    val notes = viewModel.notes
    val chapters = viewModel.chapters

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = stringResource(id = R.string.notes))
        }, navigationIcon = {
            NavigationOpenDrawerIcon(animatedContentScope, openDrawer)
        }, colors = appTopAppBarColors(), actions = {
            if (notes.isNotEmpty()) AnimatedContent(
                targetState = notes.size, label = "Size"
            ) {
                IconButton(onClick = {}) {
                    Text(text = formatNumber(it), fontWeight = FontWeight.SemiBold)
                }
            }
        })
    }) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            shape = materialCornerExtraLargeTop()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                if (notes.isNotEmpty() && chapters.isNotEmpty()) LazyColumn {
                    items(items = notes, key = { it.id }) { quran ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .animateItem(
                                    fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ), shape = MaterialTheme.shapes.large
                        ) {
                            var note by remember { mutableStateOf(quran.note ?: "") }
                            var title =
                                chapters.find { it.sura == quran.surahID }?.nameArabic ?: " - "
                            title += " " + stringResource(id = R.string.aya) + " "
                            title += formatNumber(quran.verseID)
                            var showDeleteDialog by remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = title, fontWeight = FontWeight.SemiBold)
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(id = R.string.bookmarks),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.updateNote(quran.copy(note = note))
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.note_saved),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    enabled = note != quran.note,
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Save,
                                        contentDescription = stringResource(id = R.string.bookmarks)
                                    )
                                }
                                ElevatedButton(onClick = {
                                    navigateToVerse(quran.surahID, quran.verseID)
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.Reply,
                                        contentDescription = stringResource(id = R.string.go_to_aya)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(id = R.string.go_to_aya),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            HorizontalDivider()
                            TextField(modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                value = note,
                                onValueChange = { note = it },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.EditNote,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                })
                            if (showDeleteDialog) AlertDialog(icon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                                title = {},
                                text = {
                                    Text(
                                        text = stringResource(id = R.string.delete_note_alert_message),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                onDismissRequest = { showDeleteDialog = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showDeleteDialog = false
                                            viewModel.deleteNote(quran)
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.yes),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text(
                                            text = stringResource(id = R.string.no),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                })
                        }
                    }
                }
                else if (!isLoading) NothingFoundUIElement()
            }
        }
    }
}
