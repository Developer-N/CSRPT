package ir.namoo.quran.ui.fragments.chapter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ir.namoo.quran.db.*

class ChapterViewModel(application: Application) : AndroidViewModel(application) {
    private val chapterList: MutableLiveData<MutableList<ChapterEntity>> by lazy {
        MutableLiveData<MutableList<ChapterEntity>>().also {
            it.value =
                QuranDB.getInstance(application.applicationContext).chaptersDao().getAllChapters()
        }
    }

    fun getChapters(): MutableLiveData<MutableList<ChapterEntity>> {
        return chapterList
    }

    fun getPage(page: Int): LiveData<List<PageEntity>> {
        return QuranDB.getInstance(getApplication()).pjhDao().getAllPage(page)
    }

    fun getAllHezb(): LiveData<List<HizbEntity>> {
        return QuranDB.getInstance(getApplication()).pjhDao().getAllHezb()
    }

    fun getAllJuz(): LiveData<List<JuzEntity>> {
        return QuranDB.getInstance(getApplication()).pjhDao().getAllJuz()
    }
}//end of class ChapterViewModel