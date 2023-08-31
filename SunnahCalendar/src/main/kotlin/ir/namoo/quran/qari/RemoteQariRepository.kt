package ir.namoo.quran.qari

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import ir.namoo.commons.BASE_API_URL
import ir.namoo.commons.model.ServerResponseModel

class RemoteQariRepository(private val httpClient: HttpClient) {

    suspend fun getAllQari(): List<QariModel> {
        runCatching {
            val res: ServerResponseModel<List<QariModel>> =
                httpClient.get("$BASE_API_URL/app/getqaries").body()
            return res.data
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

}
