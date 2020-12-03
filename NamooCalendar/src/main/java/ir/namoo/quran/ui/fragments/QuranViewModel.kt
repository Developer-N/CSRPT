package ir.namoo.quran.ui.fragments

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val quranVerses: MutableLiveData<MutableList<QuranEntity>> by lazy {
        MutableLiveData<MutableList<QuranEntity>>().also {
            it.value = QuranDB.getInstance(application.applicationContext).quranDao().getAll()
        }
    }

    fun getAllVerses(): MutableLiveData<MutableList<QuranEntity>> {
        return quranVerses
    }


    fun update(context: Context) {
        quranVerses.value = QuranDB.getInstance(context).quranDao().getAll()
    }
}//end of class