package ir.namoo.religiousprayers.praytimes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PrayTimesDAO {

    @Query("select * from CurrentPrayTimes")
    fun getAll(): List<CurrentPrayTimesEntity>

    @Query("select * from CurrentPrayTimes where dayNumber=:dayNumber")
    fun get(dayNumber: Int): CurrentPrayTimesEntity

    @Insert
    fun insert(prayTimes: List<CurrentPrayTimesEntity>)

    @Query("delete from CurrentPrayTimes")
    fun cleanCurrentPrayTimes()

    //    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    @Query("select * from EditedPrayTimes")
    fun getAllEdited(): MutableList<EditedPrayTimesEntity>?

    @Query("select * from EditedPrayTimes where dayNumber=:dayNumber")
    fun getEdited(dayNumber: Int): EditedPrayTimesEntity

    @Insert
    fun insertEdited(prayTimes: List<EditedPrayTimesEntity>)

    @Update
    fun updateEdited(prayTimes: List<EditedPrayTimesEntity>)

    @Query("delete from EditedPrayTimes")
    fun cleanEditedPrayTimes()

}