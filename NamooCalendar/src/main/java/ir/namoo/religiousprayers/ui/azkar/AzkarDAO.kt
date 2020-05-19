package ir.namoo.religiousprayers.ui.azkar

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update


@Dao
interface AzkarDAO {
    @Query("SELECT * FROM azkars")
    fun getAllAzkar(): List<AzkarsEntity>

    @Query("SELECT * FROM titles")
    fun getAzkarTitleFor(): List<AzkarTitels>

    @Query("SELECT * FROM titles where id=:id")
    fun getAzkarTitleFor(id: Int): AzkarTitels

    @Query("SELECT * FROM azkars where idTitle=:id")
    fun getAzkarsFor(id: Int): List<AzkarsEntity>

    @Update
    fun updateAzkarTitle(azkarTitleEntity: AzkarTitels)

}