package ir.namoo.quran.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity

class SuraViewModel constructor(private val db: QuranDB) : ViewModel() {

    private val ayaList: MutableLiveData<MutableList<QuranEntity>> by lazy {
        MutableLiveData<MutableList<QuranEntity>>().also {
            it.value = db.quranDao().getAllFor(SuraViewActivity.sura)
        }
    }

    fun getAyas(): MutableLiveData<MutableList<QuranEntity>> {
        return ayaList
    }

}//end of class SuraViewModel
