package ir.namoo.commons.repository

import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.CountryModel
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.model.ProvinceModel
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDB
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesEntity
import ir.namoo.religiousprayers.praytimeprovider.EditedPrayTimesEntity
import ir.namoo.religiousprayers.praytimeprovider.PrayTimesDB

class LocalPrayTimeRepository(
    private val locationsDB: LocationsDB,
    private val downloadPrayTimeDB: DownloadedPrayTimesDB,
    private val editedPrayTimesDB: PrayTimesDB
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

    suspend fun getAllCountries(): List<CountryModel> {
        runCatching {
            return locationsDB.countryDAO().getAllCountries()
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

    suspend fun getDownloadedTimeForCity(cityId: Int, dayNumber: Int): DownloadedPrayTimesEntity? {
        runCatching {
            return downloadPrayTimeDB.downloadedPrayTimes().getDownloadFor(cityId, dayNumber)
        }.onFailure { return null }.getOrElse { return null }
    }

    suspend fun getEdited(dayNumber: Int): EditedPrayTimesEntity? {
        runCatching {
            return editedPrayTimesDB.prayTimes().getEdited(dayNumber)
        }.onFailure { return null }.getOrElse { return null }
    }

    suspend fun getDownloadedTimesFor(cityId: Int): List<DownloadedPrayTimesEntity> {
        runCatching {
            return downloadPrayTimeDB.downloadedPrayTimes().getDownloadFor(cityId)
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getAllEditedTimes(): List<EditedPrayTimesEntity> {
        runCatching {
            return editedPrayTimesDB.prayTimes().getAllEdited()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun insertEdit(newEditTimes: List<EditedPrayTimesEntity>) {
        runCatching {
            clearEditTimes()
            editedPrayTimesDB.prayTimes().insertEdited(newEditTimes)
        }.onFailure { }
    }

    suspend fun clearEditTimes() {
        runCatching {
            editedPrayTimesDB.prayTimes().clearEditedPrayTimes()
        }
    }

    suspend fun updateEditedTimes(times: MutableList<EditedPrayTimesEntity>) =
        editedPrayTimesDB.prayTimes().updateEdited(times)
}
