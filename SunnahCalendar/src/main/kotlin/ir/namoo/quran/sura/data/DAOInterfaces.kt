package ir.namoo.quran.sura.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface QuranDAO {

    @Query("select * from quran where surah_id=:sura")
    suspend fun getQuran(sura: Int): List<QuranEntity>

    @Query("select * from quran where surah_id=:sura and verse_id=:aya")
    suspend fun getQuran(sura: Int, aya: Int): QuranEntity?

    @Update
    suspend fun updateQuran(quranEntity: QuranEntity)

    @Query("select * from tafsirs where sura_id=:sura")
    suspend fun getTafsir(sura: Int): List<TafsirEntity>

    @Query("select * from tafsirs")
    suspend fun getTafsir(): List<TafsirEntity>

    @Update
    suspend fun updateTafsir(tafsirEntity: TafsirEntity)

    @Query("select * from quran where fav=1")
    suspend fun getBookmarks(): List<QuranEntity>

    @Query("select * from quran where note not null")
    suspend fun getNotes(): List<QuranEntity>

    @Query("select * from quran where quran_withoutharkat like :query")
    suspend fun searchInQuran(query: String): List<QuranEntity>

    @Query("select * from tafsirs where khorramdel like :query or sahih_international like :query or asan like :query or puxta like :query or hazhar like :query or roshn like :query or tawhid like :query or rebar like :query or maisar like :query or raman like :query or zhian like :query or sanahi like :query")
    suspend fun searchInTafsirs(query: String): List<TafsirEntity>

    @Query("select * from quran where id=:id")
    suspend fun getVerse(id: Int): QuranEntity
}
