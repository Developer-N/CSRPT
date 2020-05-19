package ir.namoo.religiousprayers.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CityDBDAO {
    @Query("select * from city")
    fun getAllCity():List<CityInDB>;
}