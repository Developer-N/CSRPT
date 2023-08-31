package ir.namoo.religiousprayers.ui.intro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.cardColor
import ir.namoo.commons.utils.iconColor
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroDownloadLocationScreen(
    viewModel: IntroDownloadViewModel = koinViewModel(),
    startMainActivity: () -> Unit,
    goToCustomLocation: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val addedCities by viewModel.addedCityModel.collectAsState()
    val query by viewModel.query.collectAsState()
    val downloadingCityID by viewModel.downloadingCityID.collectAsState()
    val focus = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isLoading && query.isEmpty() && addedCities.isEmpty()) Button(modifier = Modifier.padding(
            16.dp
        ), onClick = { viewModel.loadData() }) {
            Text(
                text = stringResource(id = R.string.str_retry),
                fontFamily = FontFamily(appFont),
                fontSize = 20.sp
            )
            Icon(
                imageVector = Icons.Filled.Sync,
                contentDescription = stringResource(id = R.string.str_retry)
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 100.dp)
                .alpha(0.95f)
                .animateContentSize(animationSpec = spring()),
            colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
            elevation = CardDefaults.elevatedCardElevation()
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                text = stringResource(id = R.string.welcome_please_select_your_city),
                fontFamily = FontFamily(appFont),
                color = MaterialTheme.colorScheme.primary,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                textAlign = TextAlign.Center
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                value = query,
                onValueChange = { viewModel.updateQuery(it) },
                enabled = downloadingCityID == -1 && !isLoading,
                label = {
                    Text(
                        text = stringResource(id = R.string.search),
                        fontFamily = FontFamily(appFont),
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.updateQuery("")
                        focus.clearFocus()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(id = R.string.cancel),
                            tint = if (query.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.scrim
                        )
                    }
                },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() })
            )
            AnimatedVisibility(
                visible = isLoading, enter = slideInVertically(), exit = shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .height(8.dp),
                    strokeCap = StrokeCap.Round
                )
            }
            if (addedCities.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    items(items = addedCities, key = { it.id }) { city ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement()
                                .padding(2.dp)
                                .clickable {
                                    if (downloadingCityID == -1) viewModel.downloadAndStart(
                                        context, city, startMainActivity
                                    )
                                },
                            colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
                            elevation = CardDefaults.elevatedCardElevation()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .weight(3f),
                                    text = city.name,
                                    fontFamily = FontFamily(appFont)
                                )
                                AnimatedVisibility(visible = downloadingCityID == city.id) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .weight(1f)
                                            .padding(4.dp),
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                                AnimatedVisibility(visible = downloadingCityID != city.id) {
                                    IconButton(modifier = Modifier.weight(1f), onClick = {
                                        if (downloadingCityID == -1) viewModel.downloadAndStart(
                                            context, city, startMainActivity
                                        )
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.CloudDownload,
                                            contentDescription = stringResource(id = R.string.download),
                                            tint = iconColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (!isLoading && query.isNotEmpty()) NothingFoundUIElement()

            AnimatedVisibility(
                visible = !isLoading && addedCities.isEmpty(),
                enter = slideInVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { goToCustomLocation() }) {
                        Text(
                            text = stringResource(id = R.string.custom_location),
                            fontFamily = FontFamily(appFont)
                        )
                        Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "location")
                    }
                }
            }
        }//end of card
    }// end of column
}//end of fun IntroDownloadLocationScreen
