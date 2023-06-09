package ir.namoo.commons.koin

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CacheInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        val cacheControl = CacheControl.Builder().maxAge(1, TimeUnit.MINUTES).build()

        return response.newBuilder().header("Cache-Control", cacheControl.toString()).build()
    }
}
