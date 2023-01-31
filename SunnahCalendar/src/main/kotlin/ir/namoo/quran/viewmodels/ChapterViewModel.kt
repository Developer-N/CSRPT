package ir.namoo.quran.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.HizbEntity
import ir.namoo.quran.db.JuzEntity
import ir.namoo.quran.db.PageEntity
import ir.namoo.quran.db.QuranDB

class ChapterViewModel constructor(private val db: QuranDB) : ViewModel() {

    private val chapterList: MutableLiveData<MutableList<ChapterEntity>> by lazy {
        MutableLiveData<MutableList<ChapterEntity>>().also {
            it.value = db.chaptersDao().getAllChapters()
        }
    }

    fun getChapters(): MutableLiveData<MutableList<ChapterEntity>> {
        return chapterList
    }

    fun getPage(page: Int): LiveData<List<PageEntity>> {
        return db.pjhDao().getAllPage(page)
    }

    fun getAllHezb(): LiveData<List<HizbEntity>> {
        return db.pjhDao().getAllHezb()
    }

    fun getAllJuz(): LiveData<List<JuzEntity>> {
        return db.pjhDao().getAllJuz()
    }
}//end of class ChapterViewModel
