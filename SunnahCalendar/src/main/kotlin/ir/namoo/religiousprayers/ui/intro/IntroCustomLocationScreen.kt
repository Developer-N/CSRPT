package ir.namoo.religiousprayers.ui.intro

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.cardColor
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun IntroCustomLocationScreen(
    startMainActivity: () -> Unit, viewModel: IntroCustomLocationViewModel = koinViewModel()
) {

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()


    LaunchedEffect(key1 = locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.getCurrentLocation(context)
        }
    }
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .alpha(0.95f)
            .animateContentSize(animationSpec = spring()),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        AnimatedContent(
            targetState = locationPermissions.allPermissionsGranted, label = "location"
        ) { areGranted ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = isLoading, enter = slideInVertically(), exit = shrinkVertically()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(50.dp)
                            .size(50.dp),
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 10.dp
                    )
                }
                AnimatedVisibility(
                    visible = message.isNotEmpty(),
                    enter = slideInVertically(),
                    exit = shrinkVertically()
                ) {
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(8.dp),
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily(appFont),
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                }

                AnimatedVisibility(
                    visible = areGranted && !isLoading,
                    enter = slideInVertically(),
                    exit = shrinkVertically()
                ) {
                    ShowLocationSection(
                        viewModel = viewModel,
                        context = context,
                        isLoading = isLoading,
                        startMainActivity = startMainActivity
                    )
                }

                AnimatedVisibility(
                    visible = !areGranted && !isLoading,
                    enter = slideInVertically(),
                    exit = shrinkVertically()
                ) {
                    GetPermissionSection(locationPermissions = locationPermissions)
                }
            }
        }
    }

}//end of IntroCustomLocationScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetPermissionSection(locationPermissions: MultiplePermissionsState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(id = R.string.first_setup_location_message),
            fontFamily = FontFamily(appFont),
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
        Button(modifier = Modifier.padding(8.dp),
            onClick = { locationPermissions.launchMultiplePermissionRequest() }) {
            Text(
                text = stringResource(id = R.string.ok),
                fontFamily = FontFamily(appFont),
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            )
        }
    }
}

@Composable
fun ShowLocationSection(
    viewModel: IntroCustomLocationViewModel,
    context: Context,
    isLoading: Boolean,
    startMainActivity: () -> Unit
) {
    val focus = LocalFocusManager.current
    val city by viewModel.city.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val provinceList by viewModel.provinceList.collectAsState()
    val selectedProvince by viewModel.selectedProvince.collectAsState()
    val cityList by viewModel.cityList.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val message by viewModel.message.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            modifier = Modifier.padding(4.dp),
            value = city,
            onValueChange = { viewModel.updateCity(it) },
            label = {
                Text(
                    stringResource(id = R.string.city), fontFamily = FontFamily(appFont)
                )
            },
            textStyle = TextStyle(
                fontFamily = FontFamily(appFont)
            ),
            isError = city.isEmpty(),
            maxLines = 1,
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            supportingText = {
                AnimatedVisibility(
                    visible = city.isEmpty(),
                    enter = slideInVertically(),
                    exit = shrinkVertically()
                ) {
                    Text(
                        text = stringResource(id = R.string.enter_city_name),
                        fontFamily = FontFamily(appFont),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = formatNumber("${stringResource(id = R.string.latitude)} = $latitude"),
                fontFamily = FontFamily(appFont),
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = if (latitude.isEmpty()) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

            Text(
                modifier = Modifier.padding(4.dp),
                text = formatNumber(" ${stringResource(id = R.string.longitude)} = $longitude "),
                fontFamily = FontFamily(appFont),
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = if (latitude.isEmpty()) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

        }
        AnimatedVisibility(visible = !isLoading && (message.isNotEmpty() || city.isEmpty())) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Divider(modifier = Modifier.padding(8.dp))

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    text = stringResource(id = R.string.select_location_if_not_detect),
                    fontFamily = FontFamily(appFont),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val pList = mutableListOf<String>()
                    pList.clear()
                    provinceList.forEach { pList.add(it.name) }

                    MyLocationSelector(locationList = pList,
                        selectedLocation = selectedProvince?.name ?: "",
                        onSelectedLocationChange = { name ->
                            viewModel.updateSelectedProvince(provinceList.find { it.name == name })
                        })

                    val cList = mutableListOf<String>()
                    cList.clear()
                    cityList.forEach { cList.add(it.name) }

                    MyLocationSelector(
                        locationList = cList,
                        selectedLocation = selectedCity?.name ?: "",
                        onSelectedLocationChange = { name ->
                            viewModel.updateSelectedCity(cityList.find { it.name == name })
                        }
                    )
                }
                Divider(modifier = Modifier.padding(8.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(enabled = !isLoading, onClick = { viewModel.getCurrentLocation(context) }) {
                Text(
                    text = stringResource(id = R.string.renew_location),
                    fontFamily = FontFamily(appFont)
                )
                Icon(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    imageVector = Icons.Filled.Autorenew,
                    contentDescription = stringResource(id = R.string.location)
                )
            }
            Button(enabled = !isLoading && city.isNotEmpty() && latitude.isNotEmpty() && longitude.isNotEmpty(),
                onClick = {
                    viewModel.saveAndContinue(
                        context, startMainActivity
                    )
                }) {
                Text(
                    text = stringResource(id = R.string.save_location),
                    fontFamily = FontFamily(appFont)
                )
                Icon(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    imageVector = Icons.Filled.Save,
                    contentDescription = stringResource(id = R.string.location)
                )
            }
        }
    }
}

@Composable
fun MyLocationSelector(
    modifier: Modifier = Modifier,
    locationList: List<String>,
    selectedLocation: String,
    onSelectedLocationChange: (String) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val rotate = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    ElevatedAssistChip(modifier = modifier.padding(4.dp), onClick = {
        expanded.value = !expanded.value
        coroutineScope.launch {
            rotate.animateTo(
                if (expanded.value) 180f else 0f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }, label = {
        Text(text = selectedLocation, fontFamily = FontFamily(appFont))
    }, leadingIcon = {
        Icon(
            modifier = Modifier.rotate(rotate.value),
            imageVector = Icons.Filled.ExpandCircleDown,
            contentDescription = "DropDown"
        )
        DropdownMenu(expanded = expanded.value, onDismissRequest = {
            expanded.value = false
            coroutineScope.launch {
                rotate.animateTo(
                    if (expanded.value) 180f else 0f, animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }) {
            locationList.forEach { location ->
                DropdownMenuItem(text = {
                    Text(
                        text = location, fontFamily = FontFamily(appFont)
                    )
                }, onClick = {
                    onSelectedLocationChange(location)
                    expanded.value = false
                    coroutineScope.launch {
                        rotate.animateTo(
                            if (expanded.value) 180f else 0f, animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                }, trailingIcon = {
                    if (selectedLocation == location) Icon(
                        imageVector = Icons.Filled.Check, contentDescription = "Check"
                    )
                })
            }
        }
    })
}
