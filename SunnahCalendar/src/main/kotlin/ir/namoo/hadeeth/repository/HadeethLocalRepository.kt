package ir.namoo.hadeeth.repository

class HadeethLocalRepository(private val hadeethDB: HadeethDB) {

    suspend fun getAllLanguages(): List<LanguageEntity> =
        hadeethDB.languageDao().getAllLanguages()

    suspend fun insertLanguages(languages: List<LanguageEntity>) =
        hadeethDB.languageDao().insertLanguages(languages)

    suspend fun deleteAllLanguages() =
        hadeethDB.languageDao().deleteAllLanguages()

    suspend fun getCategories(language: String): List<CategoryEntity> =
        hadeethDB.categoryDao().getAllCategories(language)

    suspend fun insertCategories(categories: List<CategoryEntity>) =
        hadeethDB.categoryDao().insertCategories(categories)

    suspend fun deleteAllCategories(language: String) =
        hadeethDB.categoryDao().deleteAllCategories(language)

    suspend fun deleteAllCategories() =
        hadeethDB.categoryDao().deleteAllCategories()

    suspend fun getSettings(): SettingEntity {
        val settings = hadeethDB.settingDao().getSettings()
        if (settings == null) {
            val newSettings = SettingEntity(1, "fa")
            hadeethDB.settingDao().insertSettings(newSettings)
            return newSettings
        } else {
            return settings
        }
    }

    suspend fun updateSettings(settings: SettingEntity) =
        hadeethDB.settingDao().updateSettings(settings)

}
