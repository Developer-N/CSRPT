package ir.namoo.commons.locationtracker

import android.location.Location

sealed class LocationResult {
    data class Success(val location: Location) : LocationResult()
    data object Loading : LocationResult()
    data class Error(val message: String, val cause: Exception? = null) : LocationResult()
}
