package ir.namoo.religiousprayers

import androidx.multidex.MultiDexApplication
import ir.namoo.religiousprayers.utils.initUtils

class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
//        ReleaseDebugDifference.mainApplication(this)
        initUtils(applicationContext)
    }
}
