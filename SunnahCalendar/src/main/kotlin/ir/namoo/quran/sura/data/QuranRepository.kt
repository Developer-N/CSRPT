package ir.namoo.quran.sura.data

import android.util.Log
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.db.QCFDatabase
import ir.namoo.quran.db.QPCDatabase
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.Words

class QuranRepository(
    private val quranDB: QuranDB,
    private val quranQCFDatabase: QCFDatabase,
    private val qpcDatabase: QPCDatabase
) {

    suspend fun getQuran(sura: Int): List<QuranEntity> {
        runCatching { return quranDB.quranDAO().getQuran(sura) }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }
    }

    suspend fun getQuranPage(page: Int): List<QuranEntity> {
        runCatching {
            return quranDB.quranDAO().getQuranPage(page)
        }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }
    }


    suspend fun getQuran(sura: Int, aya: Int): QuranEntity? {
        runCatching {
            return quranDB.quranDAO().getQuran(sura, aya)
        }.onFailure { return null }.getOrElse { return null }
    }

    suspend fun getTafsir(sura: Int): List<TafsirEntity> {
        runCatching { return quranDB.quranDAO().getTafsir(sura) }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }
    }
    suspend fun getTafsirPage(page: Int): List<TafsirEntity> {
        runCatching {
            val pageVerses = quranDB.quranDAO().getQuranPage(page)
            val tafsirs = mutableListOf<TafsirEntity>()
            pageVerses.forEach { verse->
                tafsirs.add(quranDB.quranDAO().getTafsirVerse(verse.id))
            }
            return tafsirs
        }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }
    }

    suspend fun getTafsir(): List<TafsirEntity> {
        runCatching { return quranDB.quranDAO().getTafsir() }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }
    }

    suspend fun updateTafsir(tafsirEntity: TafsirEntity) =
        quranDB.quranDAO().updateTafsir(tafsirEntity)

    suspend fun getChapter(sura: Int): ChapterEntity? {
        runCatching { return quranDB.chapterDAO().getChapter(sura) }.onFailure { return null }
            .getOrElse { return null }
    }

    suspend fun updateQuran(quran: QuranEntity) {
        runCatching {
            quranDB.quranDAO().updateQuran(quran)
        }
    }

    suspend fun getVerse(id: Int): QuranEntity? {
        runCatching {
            return quranDB.quranDAO().getVerse(id)
        }.onFailure { return null }.getOrElse { return null }
    }

    suspend fun getBookmarks(): List<QuranEntity> {
        runCatching { return quranDB.quranDAO().getBookmarks() }.onFailure { return emptyList() }
            .getOrElse { return emptyList() }
    }

    suspend fun getNotes(): List<QuranEntity> {
        runCatching {
            return quranDB.quranDAO().getNotes().filterNot { it.note.isNullOrBlank() }
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun searchInQuran(query: String): List<QuranEntity> {
        runCatching {
            return quranDB.quranDAO().searchInQuran("%$query%")
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun searchInTafsirs(query: String): List<TafsirEntity> {
        runCatching {
            return quranDB.quranDAO().searchInTafsirs("%$query%")
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }

    }

    suspend fun getQuranQCFVerses(page: Int) = quranQCFDatabase.qcfDao().getQuran(page)
    suspend fun getQPCVerses(sura: Int, ayah: Int): List<Words> {
        runCatching {
            return qpcDatabase.qpcDao().getVerse(sura, ayah)
        }.onFailure {
            Log.e("NAMOO", "Error get words for : $sura - $ayah ==>> ${it.message}", it)
            return emptyList()
        }.getOrElse { return emptyList() }
    }
}
