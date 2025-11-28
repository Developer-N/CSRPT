package ir.namoo.commons.downloader

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import kotlin.math.roundToLong

fun HttpClient.downloadFile(file: File, url: String): Flow<DownloadResult> {
    return callbackFlow {
        try {
            prepareGet(url).execute { httpResponse ->
                val channel: ByteReadChannel = httpResponse.body()
                val contentLength = httpResponse.contentLength() ?: -1L

                if (contentLength > 0) {
                    send(DownloadResult.TotalSize(contentLength))
                }

                var downloadedBytes = 0L
                val buffer = ByteArray(1024 * 8) // 8KB buffer

                file.outputStream().use { output ->
                    while (!channel.isClosedForRead) {
                        val bytes = channel.readAvailable(buffer)
                        if (bytes == -1) break

                        output.write(buffer, 0, bytes)
                        downloadedBytes += bytes
                        send(DownloadResult.DownloadedByte(downloadedBytes))
                        if (contentLength > 0) {
                            val progress = (downloadedBytes * 100f / contentLength).roundToLong()
                            send(DownloadResult.Progress(progress))
                        }
                    }
                }
                send(DownloadResult.Success)
            }
        } catch (e: TimeoutCancellationException) {
            send(DownloadResult.Error("Connection timed out", e))
        } catch (t: Throwable) {
            send(DownloadResult.Error("Failed to connect. message => ${t.message}"))
        }
        awaitClose { channel.close() }
    }
}
