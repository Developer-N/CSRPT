package ir.namoo.quran.settings.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranSettingsDAO {
    @Query("select * from translate_setting")
    suspend fun getTranslatesSettings(): List<TranslateSetting>

    @Insert
    suspend fun insertTranslateSetting(translateSetting: List<TranslateSetting>)

    @Update
    suspend fun updateTranslateSetting(translateSetting: TranslateSetting)

    @Delete
    suspend fun deleteTranslateSetting(translateSetting: TranslateSetting)
}
