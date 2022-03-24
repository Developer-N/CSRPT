package ir.namoo.quran.ui.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(private val db: QuranDB) : ViewModel() {
    private val bookmarks: MutableLiveData<MutableList<QuranEntity>> by lazy {
        MutableLiveData<MutableList<QuranEntity>>().also {
            it.value = db.quranDao().getAllBookmarks()
        }
    }

    fun getBookmarkList(): MutableLiveData<MutableList<QuranEntity>> {
        return bookmarks
    }

    fun update() {
        bookmarks.value = db.quranDao().getAllBookmarks()
    }
}//end of class BookmarkViewModel
