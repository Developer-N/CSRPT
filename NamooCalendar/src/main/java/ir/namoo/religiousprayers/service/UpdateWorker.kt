package ir.namoo.religiousprayers.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.namoo.religiousprayers.utils.logException
import ir.namoo.religiousprayers.utils.setChangeDateWorker
import ir.namoo.religiousprayers.utils.update
import ir.namoo.religiousprayers.utils.updateStoredPreference
import kotlinx.coroutines.coroutineScope

class UpdateWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        runCatching {
            setChangeDateWorker(applicationContext)
            updateStoredPreference(applicationContext)
            update(applicationContext, true)
        }.onFailure(logException)
        Result.success()
    }
}
