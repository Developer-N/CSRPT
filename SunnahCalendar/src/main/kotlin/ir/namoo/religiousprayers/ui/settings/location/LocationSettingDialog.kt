package ir.namoo.religiousprayers.ui.settings.location

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.settings.SettingsSection
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import ir.namoo.religiousprayers.ui.settings.MyLocationSelector
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ShowLocationDialog(
    closeDialog: () -> Unit, viewModel: LocationSettingViewModel = koinViewModel()
) {
    val context = LocalContext.current
    viewModel.loadData(context)

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val cityName by viewModel.cityName.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val provinceList by viewModel.provinceList.collectAsState()
    val selectedProvince by viewModel.selectedProvince.collectAsState()
    val cityList by viewModel.cityList.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    AlertDialog(onDismissRequest = { closeDialog() }, icon = {
        Box(contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = isLoading, enter = expandIn(), exit = shrinkOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(5.dp),
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 6.dp
                )
            }
            Icon(
                imageVector = Icons.Default.EditLocation,
                contentDescription = stringResource(id = R.string.location)
            )
        }
    }, dismissButton = {
        ElevatedAssistChip(
            modifier = Modifier.padding(horizontal = 5.dp),
            onClick = { closeDialog() },
            shape = MaterialTheme.shapes.large,
            label = { Text(text = stringResource(id = R.string.cancel)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.cancel)
                )
            },
            elevation = AssistChipDefaults.elevatedAssistChipElevation(elevation = 2.dp)
        )
    }, confirmButton = {
        ElevatedAssistChip(modifier = Modifier.padding(horizontal = 5.dp), onClick = {
            if (cityName.isNotEmpty()) {
                viewModel.saveCityInfo(context)
                closeDialog()
            }
        }, shape = MaterialTheme.shapes.large, label = {
            Text(text = stringResource(id = R.string.save_location))
        }, trailingIcon = {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = stringResource(id = R.string.save_location)
            )
        }, elevation = AssistChipDefaults.elevatedAssistChipElevation(elevation = 2.dp),
            enabled = cityName.isNotEmpty() && !isLoading
        )
    }, text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            SettingsSection(title = stringResource(id = R.string.select_city))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
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

                MyLocationSelector(locationList = cList,
                    selectedLocation = selectedCity?.name ?: "",
                    onSelectedLocationChange = { name ->
                        viewModel.updateSelectedCity(cityList.find { it.name == name })
                    })
            }
            HorizontalDivider(modifier = Modifier.padding(2.dp))
            SettingsSection(title = stringResource(id = R.string.gps_location))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ElevatedAssistChip(onClick = {
                    if (locationPermissions.allPermissionsGranted) {
                        viewModel.getCurrentLocation(context)
                    } else {
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                }, label = {
                    Text(text = stringResource(id = R.string.gps_location))
                }, trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(id = R.string.location)
                    )
                }, elevation = AssistChipDefaults.elevatedAssistChipElevation(elevation = 2.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(2.dp))
            SettingsSection(title = stringResource(id = R.string.selected_location))
            OutlinedTextField(
                value = cityName,
                label = {
                    Text(text = stringResource(id = R.string.city))
                },
                onValueChange = { viewModel.updateCityInfo(it, latitude, longitude) },
                singleLine = true,
                supportingText = {
                    Text(
                        text = formatNumber(
                            " ${stringResource(id = R.string.longitude)}: $longitude \n${
                                stringResource(id = R.string.latitude)
                            }: $latitude"
                        )
                    )
                },
                enabled = !isLoading,
                isError = cityName.isEmpty()
            )

        }
    })
}

@Preview
@Composable
fun PreviewLocationDialog() {
    ShowLocationDialog(closeDialog = {})
}
