package com.byagowi.persiancalendar.ui.settings.locationathan.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.shareText
import com.byagowi.persiancalendar.utils.TEN_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.formatCoordinateISO6709
import com.byagowi.persiancalendar.utils.friendlyName
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.saveLocation
import com.google.openlocationcode.OpenLocationCode
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
private fun AskForLocationPermissionDialog(setGranted: (Boolean) -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return setGranted(true)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { setGranted(it.entries.any()) }

    var showDialog by rememberSaveable { mutableStateOf(true) }
    if (showDialog) AppDialog(
        title = { Text(stringResource(R.string.location_access)) },
        confirmButton = {
            TextButton(onClick = {
                showDialog = false
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }) { Text(stringResource(R.string.continue_button)) }
        },
        dismissButton = {
            TextButton(onClick = { setGranted(false) }) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = { setGranted(false) }
    ) {
        Text(
            stringResource(R.string.phone_location_required),
            Modifier.padding(horizontal = SettingsHorizontalPaddingItem.dp),
        )
    }
}

@Composable
fun GPSLocationDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    var isGranted by remember { mutableStateOf<Boolean?>(null) }
    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return if (isGranted == null) AskForLocationPermissionDialog { isGranted = it }
        else onDismissRequest()
    }

    var message by remember { mutableStateOf(context.getString(R.string.wait_for_gps)) }
    var coordinates by remember { mutableStateOf<Coordinates?>(null) }
    var cityName by remember { mutableStateOf<String?>(null) }
    var countryCode by remember { mutableStateOf<String?>(null) }
    var isOneProviderEnabled by remember { mutableStateOf(false) }
    val locationManager = remember { context.getSystemService<LocationManager>() }
        ?: return onDismissRequest()

    run {
        var showPhoneSettingsDialog by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(TWO_SECONDS_IN_MILLIS)
            if (isOneProviderEnabled) delay(TEN_SECONDS_IN_MILLIS)
            runCatching {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showPhoneSettingsDialog = true
                }
            }.onFailure(logException)
        }
        if (showPhoneSettingsDialog) {
            return AppDialog(
                onDismissRequest = onDismissRequest,
                confirmButton = {
                    TextButton(onClick = {
                        onDismissRequest()
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }.onFailure(logException)
                    }) { Text(stringResource(R.string.accept)) }
                }
            ) {
                Text(
                    stringResource(R.string.gps_internet_desc),
                    modifier = Modifier.padding(horizontal = SettingsHorizontalPaddingItem.dp),
                )
            }
        }
    }

    DisposableEffect(Unit) {
        val locationListener = object : LocationListener {
            @Deprecated("")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onLocationChanged(location: Location) {
                coordinates =
                    Coordinates(location.latitude, location.longitude, location.altitude)
            }

            override fun onProviderEnabled(provider: String) {
                isOneProviderEnabled = true
                message = context.getString(R.string.wait_for_gps)
            }

            override fun onProviderDisabled(provider: String) {
                if (!isOneProviderEnabled)
                    message = context.getString(R.string.enable_location_services)
            }
        }

        if (LocationManager.GPS_PROVIDER in locationManager.allProviders) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0f, locationListener
            )
        }
        if (LocationManager.NETWORK_PROVIDER in locationManager.allProviders) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener
            )
        }

        onDispose {
            @SuppressLint("MissingPermission")
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) locationManager.removeUpdates(locationListener)
        }
    }

    coordinates?.also { coord ->
        LaunchedEffect(coord.latitude, coord.longitude) {
            launch(Dispatchers.IO) {
                runCatching {
                    val result = Geocoder(context, language.value.asSystemLocale())
                        .getFromLocation(coord.latitude, coord.longitude, 1)
                        ?.firstOrNull()
                    countryCode = result?.countryCode
                    cityName = result?.friendlyName
                }.onFailure(logException).getOrNull()
            }
        }
        LaunchedEffect(coord.latitude, coord.longitude) {
            // Don't set elevation/altitude even from GPS, See #1011
            val coordinate = Coordinates(coord.latitude, coord.longitude, .0)
            context.preferences.saveLocation(coordinate, cityName ?: "", countryCode ?: "")
        }
    }

    AppDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = coordinates?.run {
            {
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.close)) }
            }
        },
    ) {
        val textModifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = (if (coordinates == null) 16 else 0).dp,
            )
            .fillMaxWidth()
        val coord = coordinates ?: return@AppDialog Text(
            message, textModifier, textAlign = TextAlign.Center,
        )
        val text = buildAnnotatedString {
            appendLine(
                "%s$spacedColon%.2f%s%s$spacedColon%.7f".format(
                    Locale.getDefault(),
                    stringResource(R.string.latitude), coord.latitude, "\n",
                    stringResource(R.string.longitude), coord.longitude
                )
            )
            val geoLink = "geo:${coord.latitude},${coord.longitude}"
            withLink(
                link = LinkAnnotation.Clickable(
                    tag = "pluscode",
                    styles = TextLinkStyles(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                ) { context.shareText(geoLink, cityName ?: "") }
            ) { appendLine(geoLink) }
            appendLine(formatCoordinateISO6709(coord.latitude, coord.longitude, coord.elevation))
            cityName?.also(::appendLine)
            countryCode?.also(::appendLine)
            val plusLink =
                "https://plus.codes/" + OpenLocationCode.encode(coord.latitude, coord.longitude)
            withLink(
                link = LinkAnnotation.Clickable(
                    tag = "pluscode",
                    styles = TextLinkStyles(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                ) { context.shareText(plusLink, cityName ?: "") }
            ) { append(plusLink) }
        }
        SelectionContainer { Text(text, modifier = textModifier, textAlign = TextAlign.Center) }
    }
}
