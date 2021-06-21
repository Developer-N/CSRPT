package ir.namoo.religiousprayers

import androidx.annotation.Keep
import androidx.multidex.MultiDexApplication
import ir.namoo.religiousprayers.utils.initUtils

class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
//        ReleaseDebugDifference.mainApplication(this)
        initUtils(applicationContext)
    }

    // Can I haz these resources not removed?!
    // Workaround for weird AGP 4.1.0 >= used resource removal issues
    @Keep
    private val heyAndroidBuildToolsWeNeedTheseAndItIsUnbelievableYouAreRemovingThem = listOf(
        R.drawable.blue_shade_background,
        R.raw.bismillah,
        R.raw.adhan_morning_mishari,
        R.raw.adhan_nasser_al_qatami
    )
}
