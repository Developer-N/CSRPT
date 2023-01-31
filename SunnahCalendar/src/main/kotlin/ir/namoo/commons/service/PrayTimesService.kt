package ir.namoo.commons.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import ir.namoo.commons.BASE_API_URL
import ir.namoo.commons.model.*

class PrayTimesService constructor(private val httpClient: HttpClient) {

    suspend fun getAllCities(): List<CityModel> {
        val res: ServerResponseModel<List<CityModel>> =
            httpClient.get("$BASE_API_URL/getCities").body()
        return res.data
    }

    suspend fun getAllProvinces(): List<ProvinceModel> {
        val res: ServerResponseModel<List<ProvinceModel>> =
            httpClient.get("$BASE_API_URL/getProvinces").body()
        return res.data
    }

    suspend fun getAllCountries(): List<CountryModel> {
        val res: ServerResponseModel<List<CountryModel>> =
            httpClient.get("$BASE_API_URL/getCountries").body()
        return res.data
    }

    suspend fun getAddedCities(): List<CityModel> {
        val res: ServerResponseModel<List<CityModel>> =
            httpClient.get("$BASE_API_URL/getAddedCities").body()
        return res.data
    }

    suspend fun getPrayTimesFor(id: Int): List<PrayTimesModel> {
        val res: ServerResponseModel<List<PrayTimesModel>> =
            httpClient.get("$BASE_API_URL/getTimes/$id").body()
        return res.data
    }

    suspend fun getLastUpdateInfo(): List<UpdateModel> {
        val res: ServerResponseModel<List<UpdateModel>> =
            httpClient.get("$BASE_API_URL/app/updates/ir.namoo.religiousprayers").body()
        return res.data
    }

    suspend fun getApplicationInfo(): ApplicationModel {
        val res: ServerResponseModel<ApplicationModel> =
            httpClient.get("$BASE_API_URL/app/info/ir.namoo.religiousprayers").body()
        return res.data
    }

    suspend fun getAthans(): List<ServerAthanModel> {
        val res: ServerResponseModel<List<ServerAthanModel>> =
            httpClient.get("$BASE_API_URL/app/athans").body()
        return res.data
    }

    suspend fun getAlarms(): List<ServerAthanModel> {
        val res: ServerResponseModel<List<ServerAthanModel>> =
            httpClient.get("$BASE_API_URL/app/alarms").body()
        return res.data
    }
}
