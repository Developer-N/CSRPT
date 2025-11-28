package ir.namoo.quran.chapters.data

import android.util.Log
import ir.namoo.quran.db.QCFChapters
import ir.namoo.quran.db.QCFDatabase
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.utils.chapterException


class ChapterRepository(private val quranDB: QuranDB, private val qcfDatabase: QCFDatabase) {

    suspend fun getAllChapters(): List<ChapterEntity> {
        runCatching {
            return quranDB.chapterDAO().getAllChapters()
        }.onFailure { ex ->
            chapterException = ex
            return emptyList()
        }.getOrElse { return emptyList() }
    }

    suspend fun updateChapter(chapterEntity: ChapterEntity) =
        quranDB.chapterDAO().update(chapterEntity)

    suspend fun getPage(page: Int) = quranDB.pjhDAO().getPage(page)
    suspend fun getJuz(juz: Int) = quranDB.pjhDAO().getJuz(juz)
    suspend fun getHizb(hizb: Int) = quranDB.pjhDAO().getHizb(hizb)
    suspend fun getVerse(id: Int) = quranDB.quranDAO().getVerse(id)
    suspend fun getQCFChapters(): List<QCFChapters> {
        runCatching {
            return qcfDatabase.qcfDao().getChapters()
        }.onFailure { ex ->
            Log.e("TAG", "getQCFChapters:${ex.message} ", ex)
            return emptyList()
        }.getOrElse { return emptyList() }
    }
}


