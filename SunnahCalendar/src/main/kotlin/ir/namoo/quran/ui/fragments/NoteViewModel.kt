package ir.namoo.quran.ui.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val db: QuranDB) : ViewModel() {
    private val notes: MutableLiveData<MutableList<QuranEntity>> by lazy {
        MutableLiveData<MutableList<QuranEntity>>().also {
            it.value = db.quranDao().getAllNotes()
        }
    }

    fun getNoteList(): MutableLiveData<MutableList<QuranEntity>> {
        return notes
    }

    fun update() {
        notes.value = db.quranDao().getAllNotes()
    }
}//end of class NoteViewModel
