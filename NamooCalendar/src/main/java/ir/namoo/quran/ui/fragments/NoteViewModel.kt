package ir.namoo.quran.ui.fragments

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val notes: MutableLiveData<MutableList<QuranEntity>> by lazy {
        MutableLiveData<MutableList<QuranEntity>>().also {
            it.value =
                QuranDB.getInstance(application.applicationContext).quranDao().getAllNotes()
        }
    }

    fun getNoteList(): MutableLiveData<MutableList<QuranEntity>> {
        return notes
    }

    fun update(context: Context) {
        notes.value =
            QuranDB.getInstance(context.applicationContext).quranDao().getAllNotes()
    }
}//end of class NoteViewModel