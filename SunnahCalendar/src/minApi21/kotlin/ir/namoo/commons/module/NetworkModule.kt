package ir.namoo.commons.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import ir.namoo.commons.utils.KtorUtils
import kotlinx.serialization.json.Json
import okhttp3.Cache
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideCache(@ApplicationContext context: Context): Cache {
        return Cache(context.cacheDir, 10L * 1024 * 1024)
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        json: Json,
        cacheInterceptor: CacheInterceptor,
        cache: Cache
    ): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                config {
                    cache(cache)
                }
                addNetworkInterceptor(cacheInterceptor)
            }
            KtorUtils.configureHttpClient(this, json)
        }
    }
}
