package com.example.sharlam.navigation.model

import android.content.Context
import androidx.room.*

@Database(entities = arrayOf(SAlarmEntitiy::class), version = 2)
@TypeConverters(Converters::class)
abstract class SAlarmDatabase : RoomDatabase() {
    abstract fun salarmDAO() : SAlarmDAO

    companion object {
        var INSTANCE : SAlarmDatabase? = null

        fun getInstance(context: Context) : SAlarmDatabase? {
            if(INSTANCE == null){
                synchronized(SAlarmDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                    SAlarmDatabase::class.java,"salarm.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}