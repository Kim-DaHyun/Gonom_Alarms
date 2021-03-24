package com.example.sharlam.navigation.model

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface SAlarmDAO {

    @Insert(onConflict = REPLACE)
    fun insert(alarm : SAlarmEntitiy)

    @Query("SELECT * FROM alarm ORDER BY TimeStamp DESC")
    fun getAll() : List<SAlarmEntitiy>

    @Delete
    fun delete(alarm : SAlarmEntitiy)

    @Query("UPDATE alarm SET TargetDays = :targetDays WHERE TimeStamp = :timeStamp")
    fun update(timeStamp : Long, targetDays : Byte)
}