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
            //fix sunset sound files
            if (chapterID == 28 && azkarDB.azkarDao().getAzkarItem(100)?.sound == null) {
                listOf(100, 101, 103, 104, 112, 113).zip(
                    listOf("abc", "abd", "aba", "abe", "abf", "abg")
                ).forEach { item ->
                    azkarDB.azkarDao().getAzkarItem(item.first)?.let { zkr ->
                        zkr.sound = item.second
                        azkarDB.azkarDao().updateAzkarItem(zkr)
                    }
                }
            }
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
