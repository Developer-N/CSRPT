package ir.namoo.commons.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import ir.namoo.commons.BASE_API_URL
import ir.namoo.commons.model.ApplicationModel
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.CountryModel
import ir.namoo.commons.model.PrayTimesModel
import ir.namoo.commons.model.ProvinceModel
import ir.namoo.commons.model.ServerAthanModel
import ir.namoo.commons.model.ServerResponseModel
import ir.namoo.commons.model.UpdateModel

class RemotePrayTimeRepository(private val httpClient: HttpClient) {

    suspend fun getAllCities(): List<CityModel> {
        runCatching {
            val res: ServerResponseModel<List<CityModel>> =
                httpClient.get("$BASE_API_URL/getCities").body()
            return res.data
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getAllProvinces(): List<ProvinceModel> {
        runCatching {
            val res: ServerResponseModel<List<ProvinceModel>> =
                httpClient.get("$BASE_API_URL/getProvinces").body()
            return res.data
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getAllCountries(): List<CountryModel> {
        runCatching {
            val res: ServerResponseModel<List<CountryModel>> =
                httpClient.get("$BASE_API_URL/getCountries").body()
            return res.data
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getAddedCities(): List<CityModel> {
        runCatching {
            val res: ServerResponseModel<List<CityModel>> =
                httpClient.get("$BASE_API_URL/getAddedCities").body()
            return res.data
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getPrayTimesFor(id: Int): List<PrayTimesModel> {
        runCatching {
            val res: ServerResponseModel<List<PrayTimesModel>> =
                httpClient.get("$BASE_API_URL/getTimes/$id").body()
            return res.data
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getLastUpdateInfo(): List<UpdateModel> {
        runCatching {
            val res: ServerResponseModel<List<UpdateModel>> =
                httpClient.get("$BASE_API_URL/app/updates/ir.namoo.religiousprayers").body()
            return res.data
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getApplicationInfo(): ApplicationModel? {
        runCatching {
            val res: ServerResponseModel<ApplicationModel> =
                httpClient.get("$BASE_API_URL/app/info/ir.namoo.religiousprayers").body()
            return res.data
        }.onFailure { return null }.getOrElse { return null }
    }

    suspend fun getAthans(): List<ServerAthanModel> {
        runCatching {
            val res: ServerResponseModel<List<ServerAthanModel>> =
                httpClient.get("$BASE_API_URL/app/athans").body()
            return res.data
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getAlarms(): List<ServerAthanModel> {
        runCatching {
            val res: ServerResponseModel<List<ServerAthanModel>> =
                httpClient.get("$BASE_API_URL/app/alarms").body()
            return res.data
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }
}
