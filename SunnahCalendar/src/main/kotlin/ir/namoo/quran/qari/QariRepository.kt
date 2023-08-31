package ir.namoo.quran.qari

import android.app.DownloadManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QariRepository(
    private val qariDB: QariDB,
    private val qariRemoteRepository: RemoteQariRepository,
    private val context: Context
) {

    suspend fun getQariList(): List<QariEntity> {
        runCatching {
            val downloadManager = context.getSystemService<DownloadManager>()
            qariDB.qariDAO().clearQariDB()
            qariRemoteRepository.getAllQari().forEach { qariModel ->
                qariDB.qariDAO().insert(qariModel.toEntity())
                withContext(Dispatchers.IO) {//Download and catch photos
                    context.getQariLocalPhotoFile(qariModel.photoLink?.trim())?.let { photoFile ->
                        qariModel.photoLink?.trim()?.let { link ->
                            if (!photoFile.exists()) {
                                val request = DownloadManager.Request(link.toUri())
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                    .setDestinationUri(photoFile.toUri())
                                    .setTitle(qariModel.name)
                                    .setAllowedOverMetered(true)
                                    .setAllowedOverRoaming(true)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                    request.setRequiresCharging(false)
                                downloadManager?.enqueue(request)
                            }
                        }
                    }

                }
            }
            return qariDB.qariDAO().getAllQari().sortedBy { it.name }
        }.onFailure { return qariDB.qariDAO().getAllQari().sortedBy { it.name } }
            .getOrElse { return qariDB.qariDAO().getAllQari().sortedBy { it.name } }
    }

    suspend fun getLocalQariList(): List<QariEntity> {
        runCatching {
            return qariDB.qariDAO().getAllQari().sortedBy { it.name }
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

}
