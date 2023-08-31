package ir.namoo.quran.notes

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.cardColor
import ir.namoo.commons.utils.colorAppBar
import ir.namoo.commons.utils.colorOnAppBar
import ir.namoo.commons.utils.iconColor
import ir.namoo.quran.utils.quranFont
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun NotesScreen(
    drawerState: DrawerState,
    navController: NavHostController,
    viewModel: NotesViewModel = koinViewModel()
) {
    viewModel.loadData()
    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Scaffold(topBar = {
        TopAppBar(title = {
            Column {
                Text(
                    text = stringResource(id = R.string.notes),
                    fontFamily = FontFamily(appFont),
                    fontSize = 16.sp
                )
                AnimatedVisibility(visible = !isLoading) {
                    Text(
                        text = formatNumber(notes.size),
                        fontFamily = FontFamily(appFont),
                        fontSize = 14.sp
                    )
                }
            }
        }, navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    drawerState.apply {
                        if (isOpen) close() else open()
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Menu, contentDescription = "Menu"
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorAppBar,
            titleContentColor = colorOnAppBar,
            navigationIconContentColor = colorOnAppBar,
            actionIconContentColor = colorOnAppBar
        )
        )
    }) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 4.dp)
                        .height(2.dp),
                    strokeCap = StrokeCap.Round
                )
            }
            if (notes.isNotEmpty() && chapters.isNotEmpty()) LazyColumn(state = rememberLazyListState()) {
                items(items = notes, key = { it.id }) { quran ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .animateItemPlacement(),
                        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
                        elevation = CardDefaults.elevatedCardElevation()
                    ) {
                        var note by remember { mutableStateOf(quran.note ?: "") }
                        var title = chapters.find { it.sura == quran.surahID }?.nameArabic ?: " - "
                        title += " " + stringResource(id = R.string.aya) + " "
                        title += formatNumber(quran.verseID)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text(
                                modifier = Modifier,
                                text = title,
                                fontSize = 16.sp,
                                fontFamily = FontFamily(appFont)
                            )
                            IconButton(onClick = {
                                MaterialAlertDialogBuilder(context).apply {
                                    setTitle(R.string.alert)
                                    setMessage(R.string.delete_note_alert_message)
                                    setPositiveButton(R.string.yes) { _, _ ->
                                        viewModel.deleteNote(quran)
                                    }
                                    setNegativeButton(R.string.no) { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    show()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = stringResource(id = R.string.bookmarks),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            IconButton(onClick = {
                                quran.note = note
                                viewModel.updateNoteInDB(quran)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.note_saved),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Save,
                                    contentDescription = stringResource(id = R.string.bookmarks),
                                    tint = iconColor
                                )
                            }
                            ElevatedAssistChip(modifier = Modifier,
                                onClick = { navController.navigate("sura/${quran.surahID}/${quran.verseID}") },
                                label = {
                                    Text(
                                        text = stringResource(
                                            id = R.string.go_to_aya
                                        ), fontFamily = FontFamily(appFont)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowCircleRight,
                                        contentDescription = stringResource(id = R.string.go_to_aya)
                                    )
                                })
                        }
                        Divider()
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            value = note,
                            onValueChange = { note = it },
                            textStyle = TextStyle(fontFamily = FontFamily(quranFont)),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.EditNote,
                                    contentDescription = "Edit"
                                )
                            }
                        )
                    }
                }
            }
            else if (!isLoading) NothingFoundUIElement()
        }
    }
}
