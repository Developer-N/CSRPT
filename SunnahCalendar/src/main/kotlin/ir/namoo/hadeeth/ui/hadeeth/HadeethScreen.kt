package ir.namoo.hadeeth.ui.hadeeth

import android.content.Intent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDropdownMenu
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.isOnCI
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.hadeeth.repository.LanguageEntity
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HadeethScreen(
    hadeethID: String,
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    viewModel: HadeethViewModel = koinViewModel()
) {
    val context = LocalContext.current
    viewModel.loadData(context, hadeethID)
    val isLoading by viewModel.isLoading.collectAsState()
    val hadeeth by viewModel.hadeeth.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val content by viewModel.content.collectAsState()
    val languages = viewModel.languages
    val showRetry by viewModel.showRetry.collectAsState()


    Scaffold(topBar = {
        HadeethTopBar(
            hadeethID = hadeethID,
            animatedContentScope = animatedContentScope,
            languages = languages,
            selectedLanguage = selectedLanguage,
            content = content,
            openDrawer = openDrawer,
            onLanguageSelected = { viewModel.onLanguageSelected(context, it, hadeethID) }
        )
    }) { paddingValues ->
        Surface(
            shape = materialCornerExtraLargeTop(),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedVisibility(visible = isLoading) {
                    LoadingUIElement()
                }
                AnimatedVisibility(visible = showRetry) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            text = stringResource(R.string.hadeeth_not_exist),
                            textAlign = TextAlign.Justify
                        )
                        ElevatedButton(onClick = { viewModel.loadData(context, hadeethID) }) {
                            Text(
                                text = stringResource(R.string.str_retry),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = stringResource(R.string.str_retry)
                            )
                        }
                    }
                }
                hadeeth?.let { hadeeth ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = MaterialTheme.shapes.large
                            )
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                    ) {

                        Text(
                            text = formatNumber(hadeeth.title),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = formatNumber(hadeeth.hadeeth),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Justify

                        )
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    shape = MaterialTheme.shapes.extraLarge
                                ), horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = hadeeth.attribution,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = hadeeth.grade,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // =============== sharh
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = MaterialTheme.shapes.extraLarge
                            )
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        text = stringResource(R.string.sharh),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                        text = formatNumber(hadeeth.explanation),
                        textAlign = TextAlign.Justify,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    //======================= benefits
                    if (hadeeth.hints.isNotEmpty()) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = MaterialTheme.shapes.extraLarge
                                )
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            text = stringResource(R.string.benefits),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            hadeeth.hints.forEachIndexed { index, text ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                ) {
                                    Text(
                                        text = formatNumber("${index + 1}. "), fontSize = 20.sp
                                    )
                                    Text(
                                        text = formatNumber(text),
                                        textAlign = TextAlign.Justify,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    // ============================== Words
                    hadeeth.wordsMeanings?.let { wordsMeanings ->
                        if (wordsMeanings.isNotEmpty()) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        shape = MaterialTheme.shapes.extraLarge
                                    )
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                text = stringResource(R.string.words_meaning),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            wordsMeanings.forEach { word ->
                                val text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                        append("${word.word}: ")
                                    }
                                    append(word.meaning)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                ) {
                                    Text(text = text, textAlign = TextAlign.Justify)
                                }
                            }
                        }
                    }
                    // ============================== References
                    hadeeth.reference?.let { reference ->
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = MaterialTheme.shapes.extraLarge
                                )
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            text = stringResource(R.string.reference),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            text = formatNumber(reference),
                            textAlign = TextAlign.Justify,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SharedTransitionScope.HadeethTopBar(
    hadeethID: String,
    animatedContentScope: AnimatedContentScope,
    languages: List<LanguageEntity>,
    selectedLanguage: String,
    content: String,
    openDrawer: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var showLanguages by remember { mutableStateOf(false) }
    TopAppBar(title = {
        Text(
            text = formatNumber(
                stringResource(
                    R.string.hadeeth_number, hadeethID
                )
            )
        )
    },
        navigationIcon = { NavigationOpenDrawerIcon(animatedContentScope, openDrawer) },
        colors = appTopAppBarColors(),
        actions = {
            IconButton(modifier = if (LocalContext.current.isOnCI()) Modifier else Modifier.sharedElement(
                rememberSharedContentState(key = "key_language"),
                animatedVisibilityScope = animatedContentScope
            ), onClick = { showLanguages = true }) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = stringResource(R.string.language)
                )
                AppDropdownMenu(expanded = showLanguages,
                    onDismissRequest = { showLanguages = false },
                    content = {
                        languages.forEach { language ->
                            AppDropdownMenuItem(text = {
                                Text(
                                    text = language.native,
                                    fontWeight = if (language.code == selectedLanguage) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }, onClick = {
                                onLanguageSelected(language.code)
                                showLanguages = false
                            }, trailingIcon = {
                                if (language.code == selectedLanguage) Icon(
                                    imageVector = Icons.Default.Check, contentDescription = null
                                )
                            })
                        }
                    })
            }
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, content)
                context.startActivity(
                    Intent.createChooser(
                        intent, context.getString(R.string.share)
                    )
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.sharh)
                )
            }
        })
}
