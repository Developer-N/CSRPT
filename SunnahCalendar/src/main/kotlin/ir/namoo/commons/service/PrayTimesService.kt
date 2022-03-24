package ir.namoo.commons.service

import io.ktor.client.*
import io.ktor.client.request.*
import ir.namoo.commons.BASE_API_URL
import ir.namoo.commons.model.*
import javax.inject.Inject

class PrayTimesService @Inject constructor(private val httpClient: HttpClient) {

    suspend fun getAllCities(): List<CityModel> {
        val res = httpClient.get<ServerResponseModel<List<CityModel>>>("$BASE_API_URL/getCities")
        return res.data
    }

    suspend fun getAllProvinces(): List<ProvinceModel> {
        val res =
            httpClient.get<ServerResponseModel<List<ProvinceModel>>>("$BASE_API_URL/getProvinces")
        return res.data
    }

    suspend fun getAllCountries(): List<CountryModel> {
        val res =
            httpClient.get<ServerResponseModel<List<CountryModel>>>("$BASE_API_URL/getCountries")
        return res.data
    }

    suspend fun getAddedCities(): List<CityModel> {
        val res =
            httpClient.get<ServerResponseModel<List<CityModel>>>("$BASE_API_URL/getAddedCities")
        return res.data
    }

    suspend fun getPrayTimesFor(id: Int): List<PrayTimesModel> {
        val res =
            httpClient.get<ServerResponseModel<List<PrayTimesModel>>>("$BASE_API_URL/getTimes/$id")
        return res.data
    }

    suspend fun getLastUpdateInfo(): List<UpdateModel> {
        val res =
            httpClient.get<ServerResponseModel<List<UpdateModel>>>("$BASE_API_URL/app/updates/ir.namoo.religiousprayers")
        return res.data
    }

    suspend fun getAthans(): List<ServerAthanModel> {
        val res =
            httpClient.get<ServerResponseModel<List<ServerAthanModel>>>("$BASE_API_URL/app/athans")
        return res.data
    }

    suspend fun getAlarms(): List<ServerAthanModel> {
        val res =
            httpClient.get<ServerResponseModel<List<ServerAthanModel>>>("$BASE_API_URL/app/alarms")
        return res.data
    }
}
