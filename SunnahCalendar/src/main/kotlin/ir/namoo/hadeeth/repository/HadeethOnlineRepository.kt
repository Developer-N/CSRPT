package ir.namoo.hadeeth.repository

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import ir.namoo.commons.repository.DataResult

class HadeethOnlineRepository(private val httpClient: HttpClient) {
    private val baseApi = "https://hadeethenc.com/api/v1"

    suspend fun getLanguages(): DataResult<Any> {
        runCatching {
            Log.e("HadeethOnlineRepository", "getLanguages: ")
            val result = httpClient.get("$baseApi/languages")
            Log.e("HadeethOnlineRepository", "getLanguages: ${result.body<String>()}")
            return when (result.status) {
                HttpStatusCode.OK -> DataResult.Success(result.body<List<Language>>())
                else -> DataResult.Error("Error get languages, message: ${result.body<String>()}")
            }
        }.onFailure {
            return DataResult.Error("Error get languages, message: ${it.message}")
        }.getOrElse { return DataResult.Error("Error get languages, message: ${it.message}") }
    }

    suspend fun getCategories(language: String): DataResult<Any> {
        runCatching {
            Log.e("HadeethOnlineRepository", "getCategories: language: $language ")
            val result = httpClient.get("$baseApi/categories/list/?language=$language")
            Log.e("HadeethOnlineRepository", "getCategories: ${result.body<String>()}")
            return when (result.status) {
                HttpStatusCode.OK -> DataResult.Success(result.body<List<Category>>())
                else -> DataResult.Error("Error get categories, message: ${result.body<String>()}")
            }
        }.onFailure {
            return DataResult.Error("Error get categories, message: ${it.message}")
        }.getOrElse {
            return DataResult.Error("Error get categories, message: ${it.message}")
        }
    }

    suspend fun getHadeethList(
        language: String, categoryId: Int, page: Int, perPage: Int
    ): DataResult<Any> {
        runCatching {
            Log.e(
                "HadeethOnlineRepository",
                "getHadeethList: language: $language, categoryId: $categoryId, page: $page, perPage: $perPage"
            )
            val result =
                httpClient.get("$baseApi/hadeeths/list/?language=$language&category_id=$categoryId&page=$page&per_page=$perPage")
            Log.e("HadeethOnlineRepository", "getHadeethList: ${result.body<String>()}")
            return when (result.status) {
                HttpStatusCode.OK -> DataResult.Success(result.body<HadeethList>())
                else -> DataResult.Error("Error get hadeeth list, message: ${result.body<String>()}")
            }
        }.onFailure {
            return DataResult.Error("Error get hadeeth list, message: ${it.message}")
        }.getOrElse {
            return DataResult.Error("Error get hadeeth list, message: ${it.message}")
        }
    }

    suspend fun getHadeeth(language: String, hadeethID: Int): DataResult<Any> {
        runCatching {
            Log.e(
                "HadeethOnlineRepository",
                "getHadeeth: language: $language, hadeethID: $hadeethID"
            )
            val result = httpClient.get("$baseApi/hadeeths/one/?language=$language&id=$hadeethID")
            Log.e("HadeethOnlineRepository", "getHadeeth: ${result.body<String>()}")
            return when (result.status) {
                HttpStatusCode.OK -> DataResult.Success(result.body<Hadeeth>())
                else -> DataResult.Error("Error get hadeeth, message: ${result.body<String>()}")
            }
        }.onFailure {
            return DataResult.Error("Error get hadeeth, message: ${it.message}")
        }.getOrElse {
            return DataResult.Error("Error get hadeeth, message: ${it.message}")
        }
    }

}
