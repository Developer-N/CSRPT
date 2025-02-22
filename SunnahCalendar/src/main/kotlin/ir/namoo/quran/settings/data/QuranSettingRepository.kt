package ir.namoo.quran.settings.data

import ir.namoo.commons.repository.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class QuranSettingRepository(private val quranSettingDB: QuranSettingDB) {

    fun getTranslatesSettings() = flow {
        emit(DataState.Loading)
        runCatching {
            var res = quranSettingDB.quranSettingDAO().getTranslatesSettings()
            if (res.isEmpty()) res = addDefaultTranslateSettings()
            var deleted = false
            for (index in res.indices) {//Fix Xiaomi BUG :|
                val t = res[index]
                for (i in index + 1..<res.size) if (res[i].name == t.name) {
                    quranSettingDB.quranSettingDAO().deleteTranslateSetting(res[i])
                    deleted = true
                }
            }
            if (deleted) res = quranSettingDB.quranSettingDAO().getTranslatesSettings()
            // fix another issue in Xiaomi :|
            var haveDuplicates = false
            for (r in res)
                if (res.filter { it.priority == r.priority }.size > 1) {
                    haveDuplicates = true
                    break
                }
            if (haveDuplicates) {
                for (r in res) {
                    quranSettingDB.quranSettingDAO().updateTranslateSetting(r.copy(priority = r.id))
                }
                res = quranSettingDB.quranSettingDAO().getTranslatesSettings()
            }
            emit(DataState.Success(res.sortedBy { it.priority }))
        }.onFailure { emit(DataState.Error(it.message ?: "Error")) }
            .getOrElse { emit(DataState.Error(it.message ?: "Error")) }
    }.flowOn(Dispatchers.IO)

    suspend fun updateTranslateSetting(translateSetting: TranslateSetting) {
        runCatching {
            quranSettingDB.quranSettingDAO().updateTranslateSetting(translateSetting)
        }
    }

    private suspend fun addDefaultTranslateSettings(): List<TranslateSetting> {
        runCatching {
            val settings = mutableListOf<TranslateSetting>()

            settings.add(
                TranslateSetting(name = "فارسی نور(خرم دل)", isActive = true, priority = 1)
            )
            settings.add(TranslateSetting(name = "صحیح - انگلیسی", isActive = false, priority = 2))
            settings.add(TranslateSetting(name = "ئاسان", isActive = true, priority = 3))
            settings.add(TranslateSetting(name = "پوختە", isActive = false, priority = 4))
            settings.add(TranslateSetting(name = "هەژار", isActive = false, priority = 5))
            settings.add(TranslateSetting(name = "ڕوشن", isActive = false, priority = 6))
            settings.add(TranslateSetting(name = "تەوحید", isActive = false, priority = 7))
            settings.add(TranslateSetting(name = "ڕێبەر", isActive = false, priority = 8))
            settings.add(TranslateSetting(name = "مویەسەر", isActive = false, priority = 9))
            settings.add(TranslateSetting(name = "ڕامان", isActive = false, priority = 10))
            settings.add(TranslateSetting(name = "ژیان", isActive = false, priority = 11))
            settings.add(TranslateSetting(name = "سەناهی", isActive = false, priority = 12))

            quranSettingDB.quranSettingDAO().insertTranslateSetting(settings)
            return quranSettingDB.quranSettingDAO().getTranslatesSettings()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }
}//end of class QuranSettingRepository
