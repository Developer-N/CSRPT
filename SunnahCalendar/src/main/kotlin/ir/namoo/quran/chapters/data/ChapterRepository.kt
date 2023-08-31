package ir.namoo.quran.chapters.data

import ir.namoo.quran.db.QuranDB


class ChapterRepository(private val quranDB: QuranDB) {

    suspend fun getAllChapters(): List<ChapterEntity> {
        runCatching {
            return quranDB.chapterDAO().getAllChapters()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun updateChapter(chapterEntity: ChapterEntity) {
        quranDB.chapterDAO().update(chapterEntity)
    }

    suspend fun getPage(page: Int): PageEntity {
        return quranDB.pjhDAO().getPage(page)
    }

    suspend fun getJuz(juz: Int): JuzEntity {
        return quranDB.pjhDAO().getJuz(juz)
    }

    suspend fun getHizb(hizb: Int): HizbEntity {
        return quranDB.pjhDAO().getHizb(hizb)
    }
}


