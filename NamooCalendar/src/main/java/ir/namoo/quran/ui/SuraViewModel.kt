package ir.namoo.quran.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity

class SuraViewModel(application: Application) : AndroidViewModel(application) {

    private val ayaList: MutableLiveData<MutableList<QuranEntity>> by lazy {
        MutableLiveData<MutableList<QuranEntity>>().also {
            it.value =
                QuranDB.getInstance(application.applicationContext).quranDao()
                    .getAllFor(SuraViewActivity.sura)
        }
    }

    fun getAyas(): MutableLiveData<MutableList<QuranEntity>> {
        return ayaList
    }
    
}//end of class SuraViewModel