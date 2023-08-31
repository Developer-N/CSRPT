package ir.namoo.commons.downloader

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import kotlin.math.roundToLong

suspend fun HttpClient.downloadFile(file: File, url: String): Flow<DownloadResult> {
    return callbackFlow {
        try {
            val response = request {
                url(url)
                method = HttpMethod.Get
                onDownload { bytesSentTotal, contentLength ->
                    send(DownloadResult.TotalSize(contentLength))
                    val progress = (bytesSentTotal * 100f / contentLength).roundToLong()
                    send(DownloadResult.Progress(progress))
                }
            }
            file.writeBytes(response.body())
            send(DownloadResult.Success)
        } catch (e: TimeoutCancellationException) {
            send(DownloadResult.Error("Connection timed out", e))
        } catch (t: Throwable) {
            send(DownloadResult.Error("Failed to connect"))
        }
        awaitClose { channel.close() }
    }
}
