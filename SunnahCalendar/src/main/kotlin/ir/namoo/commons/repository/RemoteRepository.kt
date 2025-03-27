package ir.namoo.commons.repository

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import ir.namoo.commons.BASE_API_URL
import ir.namoo.commons.model.ApplicationModel
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.CountryModel
import ir.namoo.commons.model.PrayTimesResponse
import ir.namoo.commons.model.ProvinceModel
import ir.namoo.commons.model.ServerAthanModel
import ir.namoo.commons.model.ServerResponseModel
import ir.namoo.commons.model.UpdateModel

class RemoteRepository(private val httpClient: HttpClient) {

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

    suspend fun getPrayTimesFor(id: Int): DataResult<Any> {
        runCatching {
            val response = httpClient.get("$BASE_API_URL/getTimes/$id")
            Log.e(
                "RemoteRepository",
                "getPrayTimesFor $id: ${response.status} ${response.body<String>()}"
            )
            return when (response.status) {
                HttpStatusCode.OK -> DataResult.Success(response.body<PrayTimesResponse>())
                else -> DataResult.Error("Error get times, message: ${response.body<String>()}")
            }
        }.onFailure { return DataResult.Error("Error get times, message: ${it.message}") }
            .getOrElse { return DataResult.Error("Error get times, message: ${it.message}") }
    }

    suspend fun getLastUpdateInfo(): DataResult<Any> {
        runCatching {
            val result = httpClient.get("$BASE_API_URL/app/updates/ir.namoo.religiousprayers")
            Log.e(
                "RemoteRepository",
                "getLastUpdateInfo: ${result.status} ${result.body<String>()}"
            )
            return when (result.status) {
                HttpStatusCode.OK -> DataResult.Success(result.body<ServerResponseModel<List<UpdateModel>>>().data)
                else -> DataResult.Error("Error get updates, message: ${result.body<String>()}")
            }
        }.onFailure { return DataResult.Error("Error get updates, message: ${it.message}") }
            .getOrElse { return DataResult.Error("Error get updates, message: ${it.message}") }
    }

    suspend fun getApplicationInfo(): ApplicationModel? {
        runCatching {
            val res: ServerResponseModel<ApplicationModel> =
                httpClient.get("$BASE_API_URL/app/info/ir.namoo.religiousprayers").body()
            return res.data
        }.onFailure { return null }.getOrElse { return null }
    }

    suspend fun getAthansOrAlarms(type: Int): DataResult<Any> {
        runCatching {
            Log.e("RemoteRepository", "getAthansOrAlarms")
            val result =
                httpClient.get(if (type == 1) "$BASE_API_URL/app/athans" else "$BASE_API_URL/app/alarms")
            Log.e(
                "RemoteRepository",
                "getAthansOrAlarms: ${result.status} ${result.body<String>()}"
            )
            return when (result.status) {
                HttpStatusCode.OK -> DataResult.Success(result.body<ServerResponseModel<List<ServerAthanModel>>>().data)
                else -> DataResult.Error("Error getAthansOrAlarms")
            }
        }.onFailure { return DataResult.Error("Error getAthansOrAlarms, message: ${it.message}") }
            .getOrElse { return DataResult.Error("Error getAthansOrAlarms, message: ${it.message}") }
    }
}
