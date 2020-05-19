package ir.namoo.religiousprayers.service

//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import ir.namoo.religiousprayers.utils.setChangeDateWorker

//class UpdateWorker(val context: Context, workerParams: WorkerParameters) :
//    CoroutineWorker(context, workerParams) {
//    override suspend fun doWork(): Result = withContext(Dispatchers.Main) {
//        try {
//            setChangeDateWorker(context)
//            updateStoredPreference(applicationContext)
//            update(applicationContext, true)
//            Result.success()
//        } catch (e: Throwable) {
//            e.printStackTrace()
//            Result.failure()
//        }
//    }
//}
