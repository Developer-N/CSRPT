package ir.namoo.religiousprayers.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ir.namoo.religiousprayers.PREF_FIRST_START
import ir.namoo.religiousprayers.PREF_GEOCODED_CITYNAME
import ir.namoo.religiousprayers.PREF_LATITUDE
import ir.namoo.religiousprayers.PREF_LONGITUDE
import ir.namoo.religiousprayers.utils.appPrefs

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (appPrefs.getBoolean(PREF_FIRST_START, true)
            || appPrefs.getString(PREF_GEOCODED_CITYNAME, "").isNullOrEmpty()
            || appPrefs.getString(PREF_LATITUDE, "0.0") == "0.0"
            || appPrefs.getString(PREF_LONGITUDE, "0.0") == "0.0"
        )
            startActivity(Intent(this, IntroActivity::class.java))
        else
            startActivity(Intent(this, MainActivity::class.java))
        finish()
    }//end of onCreate
}//end of class