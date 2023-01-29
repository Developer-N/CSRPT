package ir.namoo.quran.ui.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity

class QuranViewModel constructor(private val db: QuranDB) : ViewModel() {
    private val quranVerses: MutableLiveData<MutableList<QuranEntity>> by lazy {
        MutableLiveData<MutableList<QuranEntity>>().also {
            it.value = db.quranDao().getAll()
        }
    }

    fun getAllVerses(): MutableLiveData<MutableList<QuranEntity>> {
        return quranVerses
    }


    fun update() {
        quranVerses.value = db.quranDao().getAll()
    }
}//end of class
