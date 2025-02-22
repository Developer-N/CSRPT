package ir.namoo.commons.locationtracker

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.byagowi.persiancalendar.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationTracker(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val context: Context
) : LocationTrackerInterface {
    override suspend fun getCurrentLocation() = flow {
        emit(LocationResult.Loading)
        val hasAccessFineLocationPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasAccessCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val isGpsEnabled =
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )

        if (!isGpsEnabled && !(hasAccessCoarseLocationPermission || hasAccessFineLocationPermission)) {
            emit(LocationResult.Error(context.getString(R.string.turn_on_location)))
        }
        emit(suspendCancellableCoroutine { cont ->
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .apply {
                    addOnSuccessListener {
                        if (it == null) cont.resume(LocationResult.Error(context.getString(R.string.location_not_detected)))
                        else cont.resume(LocationResult.Success(it))
                    }
                    addOnFailureListener {
                        cont.resume(LocationResult.Error(context.getString(R.string.location_not_detected)))
                    }
                    if (isComplete) {
                        if (isSuccessful) {
                            cont.resume(LocationResult.Success(result))
                        } else {
                            cont.resume(LocationResult.Error(context.getString(R.string.location_not_detected)))
                        }
                        return@suspendCancellableCoroutine
                    }
                }
        })
    }
}
