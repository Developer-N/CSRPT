package ir.namoo.quran.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChaptersDao {
    @Query("SELECT * FROM chapters")
    fun getAllChapters(): MutableList<ChapterEntity>

    @Query("SELECT * FROM chapters where sura=:sura")
    fun getChapter(sura: Int): ChapterEntity

    @Query("SELECT * FROM chapters where sura=:sura")
    fun getChapter1(sura: Int): ChapterEntity

    @Update
    fun update(chaptersEntity: ChapterEntity)

}//end of interface

@Dao
interface PJHDao {
    @Query("SELECT * FROM safhe where page=:page")
    fun getAllPage(page: Int): LiveData<List<PageEntity>>

    @Query("SELECT * FROM juz")
    fun getAllJuz(): LiveData<List<JuzEntity>>

    @Query("SELECT * FROM hezb")
    fun getAllHezb(): LiveData<List<HizbEntity>>
}

@Dao
interface QuranDao {
    @Query("SELECT * FROM quran_all WHERE sura=:sura")
    fun getAllFor(sura: Int): MutableList<QuranEntity>?

    @Query("SELECT * FROM quran_all WHERE `index`=:index")
    fun getVerseByIndex(index:Int): QuranEntity?

    @Query("SELECT * FROM quran_all WHERE fav==1")
    fun getAllBookmarks(): MutableList<QuranEntity>?

    @Query("SELECT * FROM quran_all WHERE note!='-'")
    fun getAllNotes(): MutableList<QuranEntity>?

    @Query("SELECT * FROM quran_all")
    fun getAll(): MutableList<QuranEntity>

    @Update
    fun update(quranEntity: QuranEntity)

}//end of interface QuranTN
