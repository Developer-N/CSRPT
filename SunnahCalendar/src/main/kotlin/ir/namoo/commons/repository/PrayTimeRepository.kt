package ir.namoo.commons.repository

import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.ProvinceModel
import ir.namoo.commons.model.ServerAthanModel
import ir.namoo.commons.utils.modelToDBTimes
import kotlinx.coroutines.flow.flow

class PrayTimeRepository(
    private val localRepository: LocalPrayTimeRepository,
    private val remoteRepository: RemotePrayTimeRepository
) {
    suspend fun updateAndGetCityList(): List<CityModel> {
        runCatching {

            val remoteCountryList = remoteRepository.getAllCountries()
            localRepository.insertCountries(remoteCountryList)

            val remoteProvinceList = remoteRepository.getAllProvinces()
            localRepository.insertProvinces(remoteProvinceList)

            val remoteCityList = remoteRepository.getAllCities()
            localRepository.insertCities(remoteCityList)

            return localRepository.getAllCity()

        }.onFailure { return localRepository.getAllCity() }
            .getOrElse { return localRepository.getAllCity() }
    }

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

    suspend fun getAddedCity(): List<CityModel> {
        runCatching {
            return remoteRepository.getAddedCities()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getTimesForCityAndSaveToLocalDB(cityModel: CityModel): Boolean {
        runCatching {
            val times = remoteRepository.getPrayTimesFor(cityModel.id)
            localRepository.clearDownloadFor(cityModel.id)
            localRepository.insertToDownload(modelToDBTimes(times))
            return true
        }.onFailure { return false }.getOrElse { return false }
    }

    suspend fun getLastUpdateInfo() = flow {
        runCatching {
            val list = remoteRepository.getLastUpdateInfo()
            if (list.isEmpty()) emit(DataResult.ConnectionError())
            else emit(DataResult.Success(list))
        }.onFailure {
            emit(DataResult.Error("Error get updates! ${it.message}"))
            logException
        }
    }


    suspend fun getApplicationInfo() = flow {
        runCatching {
            val appInfo = remoteRepository.getApplicationInfo()
            if (appInfo == null) emit(DataResult.Error("Error get Application Info"))
            else emit(DataResult.Success(appInfo))
        }.onFailure {
            emit(DataResult.Error("Error get Application Info $it"))
            logException
        }
    }

    suspend fun getAthans(): List<ServerAthanModel> = remoteRepository.getAthans()

    suspend fun getAlarms(): List<ServerAthanModel> = remoteRepository.getAlarms()
}
