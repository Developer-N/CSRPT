package ir.namoo.quran.ui.fragments

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity

class BookmarkViewModel(application: Application) : AndroidViewModel(application) {
    private val bookmarks: MutableLiveData<MutableList<QuranEntity>> by lazy {
        MutableLiveData<MutableList<QuranEntity>>().also {
            it.value =
                QuranDB.getInstance(application.applicationContext).quranDao().getAllBookmarks()
        }
    }

    fun getBookmarkList(): MutableLiveData<MutableList<QuranEntity>> {
        return bookmarks
    }

    fun update(context: Context) {
        bookmarks.value =
            QuranDB.getInstance(context.applicationContext).quranDao().getAllBookmarks()
    }
}//end of class BookmarkViewModel