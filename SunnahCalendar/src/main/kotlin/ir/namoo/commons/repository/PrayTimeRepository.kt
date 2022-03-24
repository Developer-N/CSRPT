package ir.namoo.commons.repository

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import ir.namoo.commons.service.PrayTimesService
import javax.inject.Inject

@Module
@InstallIn(ActivityRetainedComponent::class)
class PrayTimeRepository @Inject constructor(var prayTimeService: PrayTimesService) {

    suspend fun getAddedCities() = prayTimeService.getAddedCities()

    suspend fun getPrayTimeFor(cityID: Int) = prayTimeService.getPrayTimesFor(cityID)

}
