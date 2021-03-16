package com.example.sharlam.navigation.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm")
data class SAlarmEntitiy(

    @PrimaryKey
    var TimeStamp : Long?,
    var title : String = "",
    var SoundUrl : String = "",
    var Targethours : Int?,
    var Targetminutes : Int?,
    var TargetDays : Array<Boolean>? = Array(7) { false },
    @ColumnInfo(name = "targetnums")
    var TargetNums : Array<String>? = arrayOf()
)