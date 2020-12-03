package ir.namoo.religiousprayers.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.namoo.religiousprayers.utils.setChangeDateWorker
import ir.namoo.religiousprayers.utils.update
import ir.namoo.religiousprayers.utils.updateStoredPreference
import kotlinx.coroutines.coroutineScope

const val TAG = "UpdateWorker"

class UpdateWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            setChangeDateWorker(applicationContext)
            updateStoredPreference(applicationContext)
            update(applicationContext, true)
            Result.success()
        } catch (e: Throwable) {
            Log.e(TAG, "Error running UpdateWorker: ", e)
            Result.failure()
        }
    }
}
