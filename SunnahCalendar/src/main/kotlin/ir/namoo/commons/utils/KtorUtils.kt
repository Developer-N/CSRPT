package ir.namoo.commons.utils

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import timber.log.Timber

object KtorUtils {
    fun configureHttpClient(config: HttpClientConfig<*>, json: Json) {
        config.apply {
            defaultRequest {
//                header("APIKEY", API_KEY)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30 * 1000
                connectTimeoutMillis = 30 * 1000
                socketTimeoutMillis = 30 * 1000
            }

            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
                acceptContentTypes = acceptContentTypes + ContentType.Any
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.d(message)
                    }
                }
            }
        }
    }

}
