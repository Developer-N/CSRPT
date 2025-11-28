package ir.namoo.religiousprayers.ui.intro

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.numeral
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import ir.namoo.religiousprayers.ui.settings.MyLocationSelector
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
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
    ElevatedCard(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring())
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
                    LoadingUIElement()
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
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = FontWeight.SemiBold
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
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            fontWeight = FontWeight.SemiBold
        )
        Button(
            modifier = Modifier.padding(8.dp),
            onClick = { locationPermissions.launchMultiplePermissionRequest() }) {
            Text(
                text = stringResource(id = R.string.ok),
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontWeight = FontWeight.SemiBold
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
    val numeral by numeral.collectAsState()
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
                    stringResource(id = R.string.city)
                )
            },
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
                text = numeral.format("${stringResource(id = R.string.latitude)} = $latitude"),
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                fontWeight = FontWeight.SemiBold,
                color = if (latitude.isEmpty()) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

            Text(
                modifier = Modifier.padding(4.dp),
                text = numeral.format(" ${stringResource(id = R.string.longitude)} = $longitude "),
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                fontWeight = FontWeight.SemiBold,
                color = if (latitude.isEmpty()) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

        }
        AnimatedVisibility(visible = !isLoading && provinceList.isNotEmpty() && cityList.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.padding(8.dp))

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    text = stringResource(id = R.string.select_location_if_not_detect),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
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

                    MyLocationSelector(
                        locationList = pList,
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
                HorizontalDivider(modifier = Modifier.padding(8.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(enabled = !isLoading, onClick = { viewModel.getCurrentLocation(context) }) {
                Text(text = stringResource(id = R.string.renew_location))
                Icon(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    imageVector = Icons.Filled.Autorenew,
                    contentDescription = stringResource(id = R.string.location)
                )
            }
            Button(
                enabled = !isLoading && city.isNotEmpty() && latitude.isNotEmpty() && longitude.isNotEmpty(),
                onClick = {
                    viewModel.saveAndContinue(
                        context, startMainActivity
                    )
                }) {
                Text(text = stringResource(id = R.string.save_location))
                Icon(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    imageVector = Icons.Filled.Save,
                    contentDescription = stringResource(id = R.string.location)
                )
            }
        }
    }
}


