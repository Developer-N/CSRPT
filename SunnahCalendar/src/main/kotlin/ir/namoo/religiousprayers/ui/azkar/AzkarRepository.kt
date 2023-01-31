package ir.namoo.religiousprayers.ui.azkar

import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.repository.DataResult
import kotlinx.coroutines.flow.flow

class AzkarRepository(private val azkarDB: AzkarDB) {
    suspend fun getAzkarChapters() = flow {
        runCatching {
            emit(DataResult.Success(azkarDB.azkarDao().getAzkarChapters()))
        }.onFailure {
            emit(DataResult.Error("Get Azkar Chapters Error!"))
            logException
        }
    }

    suspend fun getAzkarItem(chapterID: Int) = flow {
        runCatching {
            emit(DataResult.Success(azkarDB.azkarDao().getAzkarItems(chapterID)))
        }.onFailure {
            emit(DataResult.Error("Get Azkar Items Error!"))
            logException
        }
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
