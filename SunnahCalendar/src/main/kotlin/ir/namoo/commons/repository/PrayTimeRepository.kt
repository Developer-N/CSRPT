package ir.namoo.commons.repository

import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.PrayTimesResponse
import ir.namoo.commons.model.ProvinceModel
import ir.namoo.commons.utils.modelToDBTimes
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesEntity
import ir.namoo.religiousprayers.praytimeprovider.EditedPrayTimesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PrayTimeRepository(
    private val localRepository: LocalPrayTimeRepository,
    private val remoteRepository: RemoteRepository
) {
    fun getAndUpdateCities() = flow {
        runCatching {
            emit(DataState.Loading)
            val remoteCountryList = remoteRepository.getAllCountries()
            localRepository.insertCountries(remoteCountryList)

            val remoteProvinceList = remoteRepository.getAllProvinces()
            localRepository.insertProvinces(remoteProvinceList)

            val remoteCityList = remoteRepository.getAllCities()
            localRepository.insertCities(remoteCityList)

            emit(DataState.Success(localRepository.getAllCity()))

        }.onFailure { emit(DataState.Error(it.message ?: "Error get cities")) }
            .getOrElse { emit(DataState.Error(it.message ?: "Error get cities")) }
    }.flowOn(Dispatchers.IO)

    suspend fun getLocalCityList(): List<CityModel> {
        runCatching {
            return localRepository.getAllCity()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getLocalProvinceList(): List<ProvinceModel> {
        runCatching {
            return localRepository.getAllProvinces()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    fun getAddedCity() = flow {
        runCatching {
            emit(DataState.Loading)
            if (localRepository.getAllCountries().isEmpty())
                localRepository.insertCountries(remoteRepository.getAllCountries())

            if (localRepository.getAllProvinces().isEmpty())
                localRepository.insertProvinces(remoteRepository.getAllProvinces())

            if (localRepository.getAllCity().isEmpty()) {
                localRepository.insertCities(remoteRepository.getAllCities())
            }
            emit(DataState.Success(remoteRepository.getAddedCities()))
        }.onFailure { emit(DataState.Error(it.message ?: "Error get cities")) }
            .getOrElse { emit(DataState.Error(it.message ?: "Error get cities")) }
    }.flowOn(Dispatchers.IO)

    fun getTimesForCityAndSaveToLocalDB(id: Int) = flow {
        runCatching {
            emit(DataState.Loading)
            when (val result = remoteRepository.getPrayTimesFor(id)) {
                is DataResult.Error -> emit(DataState.Error(result.message))
                is DataResult.Success -> {
                    result.data as PrayTimesResponse
                    if (result.data.status == 1) {
                        localRepository.clearDownloadFor(id)
                        localRepository.insertToDownload(modelToDBTimes(result.data.data))
                        emit(DataState.Success(true))
                    } else emit(DataState.Error(result.data.msg))
                }
            }
        }.onFailure { emit(DataState.Error(it.message ?: "Error get times")) }
            .getOrElse { emit(DataState.Error(it.message ?: "Error get times")) }
    }

    fun getLastUpdateInfo() = flow {
        emit(DataState.Loading)
        runCatching {
            when (val result = remoteRepository.getLastUpdateInfo()) {
                is DataResult.Error -> emit(DataState.Error(result.message))
                is DataResult.Success -> emit(DataState.Success(result.data))
            }
        }.onFailure {
            emit(DataState.Error("Error get updates! ${it.message}"))
            logException
        }
    }.flowOn(Dispatchers.IO)


    fun getApplicationInfo() = flow {
        runCatching {
            val appInfo = remoteRepository.getApplicationInfo()
            if (appInfo == null) emit(DataResult.Error("Error get Application Info"))
            else emit(DataResult.Success(appInfo))
        }.onFailure {
            emit(DataResult.Error("Error get Application Info $it"))
            logException
        }
    }.flowOn(Dispatchers.IO)


    fun getAthansOrAlarms(type: Int) = flow {
        runCatching {
            emit(DataState.Loading)
            when (val result = remoteRepository.getAthansOrAlarms(type)) {
                is DataResult.Error -> emit(DataState.Error(result.message))
                is DataResult.Success<*> -> emit(DataState.Success(result.data))
            }
        }.onFailure {
            emit(DataState.Error("Error getAthansOrAlarms: message: ${it.message}"))
            logException
        }
    }.flowOn(Dispatchers.IO)

    fun sendEvent(event: String) = flow {
        runCatching {
            emit(DataState.Loading)
            when (val result = remoteRepository.sendEvent(EventRequest(event))) {
                is DataResult.Error -> emit(DataState.Error(result.message))
                is DataResult.Success<*> -> emit(DataState.Success(result.data))
            }
        }.onFailure {
            emit(DataState.Error("Error sendEvent: message ${it.message}"))
            logException
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getDownloadedTimesForCity(cityId: Int): List<DownloadedPrayTimesEntity> =
        localRepository.getDownloadedTimesFor(cityId)

    suspend fun getDownloadedTimesForCity(cityId: Int, dayNumber: Int): DownloadedPrayTimesEntity? =
        localRepository.getDownloadedTimeForCity(cityId, dayNumber)

    suspend fun getEditedTime(dayNumber: Int): EditedPrayTimesEntity? =
        localRepository.getEdited(dayNumber)

    suspend fun getAllEditedTimes(): List<EditedPrayTimesEntity> =
        localRepository.getAllEditedTimes()

    suspend fun insertEdit(newEditTimes: List<EditedPrayTimesEntity>) =
        localRepository.insertEdit(newEditTimes)

    suspend fun clearEditTimes() = localRepository.clearEditTimes()

    suspend fun updateEditedTimes(times: MutableList<EditedPrayTimesEntity>) =
        localRepository.updateEditedTimes(times)
}
