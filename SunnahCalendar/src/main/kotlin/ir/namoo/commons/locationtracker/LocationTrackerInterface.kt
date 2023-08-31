package ir.namoo.commons.locationtracker

import kotlinx.coroutines.flow.Flow

interface LocationTrackerInterface {
    suspend fun getCurrentLocation(): Flow<LocationResult>
}
