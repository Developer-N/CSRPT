package ir.namoo.quran.sura.data

import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.db.QuranDB

class QuranRepository(private val quranDB: QuranDB) {

    suspend fun getQuran(sura: Int): List<QuranEntity> {
        runCatching { return quranDB.quranDAO().getQuran(sura) }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }
    }

    suspend fun getQuran(sura: Int, aya: Int): QuranEntity? {
        runCatching {
            return quranDB.quranDAO().getQuran(sura, aya)
        }.onFailure { return null }
            .getOrElse { return null }
    }

    suspend fun getTafsir(sura: Int): List<TafsirEntity> {
        runCatching { return quranDB.quranDAO().getTafsir(sura) }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }
    }

    suspend fun getChapter(sura: Int): ChapterEntity? {
        runCatching { return quranDB.chapterDAO().getChapter(sura) }.onFailure { return null }
            .getOrElse { return null }
    }

    suspend fun updateQuran(quran: QuranEntity) {
        runCatching {
            quranDB.quranDAO().updateQuran(quran)
        }
    }

    suspend fun getBookmarks(): List<QuranEntity> {
        runCatching { return quranDB.quranDAO().getBookmarks() }
            .onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getNotes(): List<QuranEntity> {
        runCatching { return quranDB.quranDAO().getNotes().filterNot { it.note.isNullOrBlank() } }
            .onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun searchInQuran(query: String): List<QuranEntity> {
        runCatching {
            return quranDB.quranDAO().searchInQuran("%$query%")
        }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }
    }

    suspend fun searchInTafsirs(query: String): List<TafsirEntity> {
        runCatching {
            return quranDB.quranDAO().searchInTafsirs("%$query%")
        }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }

    }

}
