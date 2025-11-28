package ir.namoo.religiousprayers.ui.intro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
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
    val addedCities = viewModel.addedCityModel
    var query by remember { mutableStateOf("") }
    val downloadingCityID by viewModel.downloadingCityID.collectAsState()
    val focus = LocalFocusManager.current

    val filteredCities by remember {
        derivedStateOf {
            if (query.isEmpty()) addedCities
            else addedCities.filter { it.name.contains(query) }
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.loadData()
    }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                text = stringResource(id = R.string.welcome_please_select_your_city),
                color = MaterialTheme.colorScheme.primary,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                value = query,
                onValueChange = { query = it },
                enabled = downloadingCityID == -1 && !isLoading,
                label = { Text(text = stringResource(id = R.string.search)) },
                trailingIcon = {
                    IconButton(onClick = {
                        query = ""
                        focus.clearFocus()
                    }, enabled = query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(id = R.string.cancel),
                            tint = if (query.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                shape = MaterialTheme.shapes.extraLarge,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            AnimatedVisibility(
                visible = isLoading, enter = slideInVertically(), exit = shrinkVertically()
            ) {
                LoadingUIElement()
            }

            AnimatedVisibility(visible = !isLoading && query.isEmpty() && addedCities.isEmpty()) {
                ElevatedButton(
                    modifier = Modifier.padding(
                        16.dp
                    ), onClick = { viewModel.loadData() }) {
                    Text(
                        text = stringResource(id = R.string.str_retry), fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = stringResource(id = R.string.str_retry)
                    )
                }
            }
            if (filteredCities.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    items(items = filteredCities, key = { it.id }) { city ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                .padding(2.dp)
                                .clickable(enabled = downloadingCityID == -1, onClick = {
                                    if (downloadingCityID == -1) viewModel.downloadAndStart(
                                        context, city, startMainActivity
                                    )
                                }), shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    text = city.name,
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                    fontWeight = FontWeight.SemiBold
                                )
                                AnimatedVisibility(
                                    modifier = Modifier.padding(8.dp),
                                    visible = downloadingCityID == city.id
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp), strokeCap = StrokeCap.Round
                                    )
                                }
                                AnimatedVisibility(visible = downloadingCityID != city.id) {
                                    IconButton(onClick = {
                                        if (downloadingCityID == -1) viewModel.downloadAndStart(
                                            context, city, startMainActivity
                                        )
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.CloudDownload,
                                            contentDescription = stringResource(id = R.string.download),
                                            tint = MaterialTheme.colorScheme.primary
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
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { goToCustomLocation() }) {
                        Text(text = stringResource(id = R.string.custom_location))
                        Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "location")
                    }
                }
            }
        }//end of card
    }// end of column
}//end of fun IntroDownloadLocationScreen
