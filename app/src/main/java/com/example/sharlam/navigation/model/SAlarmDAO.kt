package com.example.sharlam.navigation.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface SAlarmDAO {

    @Insert(onConflict = REPLACE)
    fun insert(alarm : SAlarmEntitiy)

    @Query("SELECT * FROM alarm ORDER BY TimeStamp DESC")
    fun getAll() : List<SAlarmEntitiy>

    @Delete
    fun delete(alarm : SAlarmEntitiy)

}