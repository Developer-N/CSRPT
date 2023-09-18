package ir.namoo.commons.repository

import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.CountryModel
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.model.ProvinceModel
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDB
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesEntity

class LocalPrayTimeRepository(
    private val locationsDB: LocationsDB,
    private val downloadPrayTimeDB: DownloadedPrayTimesDB
) {
    suspend fun insertCountries(countryList: List<CountryModel>) {
        runCatching {
            locationsDB.countryDAO().insert(countryList)
        }.onFailure { }
    }

    suspend fun insertProvinces(provinceList: List<ProvinceModel>) {
        runCatching {
            locationsDB.provinceDAO().insert(provinceList)
        }.onFailure { }
    }

    suspend fun insertCities(cityList: List<CityModel>) {
        runCatching {
            locationsDB.cityDAO().insert(cityList)
        }.onFailure { }
    }

    suspend fun getAllCity(): List<CityModel> {
        runCatching { return locationsDB.cityDAO().getAllCity() }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }
    }

    suspend fun getAllProvinces(): List<ProvinceModel> {
        runCatching {
            return locationsDB.provinceDAO().getAllProvinces()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun clearDownloadFor(id: Int) {
        runCatching {
            downloadPrayTimeDB.downloadedPrayTimes().clearDownloadFor(id)
        }.onFailure { }
    }

    suspend fun insertToDownload(prayTimesList: List<DownloadedPrayTimesEntity>) {
        runCatching {
            downloadPrayTimeDB.downloadedPrayTimes().insertToDownload(prayTimesList)
        }.onFailure { }
    }
}
