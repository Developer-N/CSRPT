package ir.namoo.commons.repository

import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.service.PrayTimesService
import kotlinx.coroutines.flow.flow

class PrayTimeRepository constructor(private var prayTimeService: PrayTimesService) {

    suspend fun getAddedCities() = flow {
        runCatching {
            emit(DataResult.Success(prayTimeService.getAddedCities()))
        }.onFailure {
            emit(DataResult.Error("Error get added cities! ${it.message}"))
            logException
        }
    }

    suspend fun getPrayTimeFor(cityID: Int) = flow {
        runCatching {
            emit(DataResult.Success(prayTimeService.getPrayTimesFor(cityID)))
        }.onFailure {
            emit(DataResult.Error("Error get prayTimes! ${it.message}"))
            logException
        }
    }

    suspend fun getLastUpdateInfo() = flow {
        runCatching {
            emit(DataResult.Success(prayTimeService.getLastUpdateInfo()))
        }.onFailure {
            emit(DataResult.Error("Error get updates! ${it.message}"))
            logException
        }
    }

    suspend fun getApplicationInfo() = flow {
        runCatching {
            emit(DataResult.Success(prayTimeService.getApplicationInfo()))
        }.onFailure {
            emit(DataResult.Error("Error get Appliation Info $it"))
            logException
        }
    }

}
