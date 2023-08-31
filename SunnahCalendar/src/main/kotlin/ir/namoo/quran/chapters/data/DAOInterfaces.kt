package ir.namoo.quran.chapters.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update


@Dao
interface ChaptersDao {
    @Query("SELECT * FROM chapters")
    suspend fun getAllChapters(): List<ChapterEntity>

    @Query("SELECT * FROM chapters where sura=:sura")
    suspend fun getChapter(sura: Int): ChapterEntity

    @Update
    suspend fun update(chaptersEntity: ChapterEntity)

}//end of interface

@Dao
interface PJHDao {
    @Query("SELECT * FROM page where page=:page")
    suspend fun getPage(page: Int): PageEntity

    @Query("SELECT * FROM juz where id=:juz")
    suspend fun getJuz(juz: Int): JuzEntity

    @Query("SELECT * FROM hezb where hizb=:hizp")
    suspend fun getHizb(hizp: Int): HizbEntity
}
