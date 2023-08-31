package ir.namoo.religiousprayers.ui.azkar.data

import com.byagowi.persiancalendar.utils.logException

class AzkarRepository(private val azkarDB: AzkarDB) {
    suspend fun getAzkarChapters(): List<AzkarChapter> {
        runCatching {
            return azkarDB.azkarDao().getAzkarChapters()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getAzkarItem(chapterID: Int): List<AzkarItem> {
        runCatching {
            return azkarDB.azkarDao().getAzkarItems(chapterID)
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun getAzkarChapter(chapterID: Int) = azkarDB.azkarDao().getAzkarChapter(chapterID)

    suspend fun getAzkarReferences(chapterID: Int) =
        azkarDB.azkarDao().getAzkarReferences(chapterID)

    suspend fun updateAzkarChapter(azkarChapter: AzkarChapter) {
        runCatching {
            azkarDB.azkarDao().updateAzkarChapter(azkarChapter)
        }.onFailure { logException }
    }

}
