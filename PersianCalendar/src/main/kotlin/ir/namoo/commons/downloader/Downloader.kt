package ir.namoo.commons.downloader

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import kotlin.math.roundToInt

suspend fun HttpClient.downloadFile(file: File, url: String): Flow<DownloadResult> {
    return callbackFlow {
        try {
            val response = request {
                url(url)
                method = HttpMethod.Get
                onDownload { bytesSentTotal, contentLength ->
                    val progress = (bytesSentTotal * 100f / contentLength).roundToInt()
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
