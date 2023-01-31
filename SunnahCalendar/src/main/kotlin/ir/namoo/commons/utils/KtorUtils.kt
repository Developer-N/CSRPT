package ir.namoo.commons.utils

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import timber.log.Timber

object KtorUtils {
    fun configureHttpClient(config: HttpClientConfig<*>) {
        config.apply {
            defaultRequest {
//                header("APIKEY", API_KEY)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30 * 1000
                connectTimeoutMillis = 30 * 1000
                socketTimeoutMillis = 30 * 1000
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
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
