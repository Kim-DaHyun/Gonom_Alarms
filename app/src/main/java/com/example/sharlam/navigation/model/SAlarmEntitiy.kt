package com.example.sharlam.navigation.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm")
data class SAlarmEntitiy(

    @PrimaryKey
    var TimeStamp : Long = 0,
    var title : String = "",
    var SoundUrl : String = "",
    var Targethours : Int = 0,
    var Targetminutes : Int = 0,
    var TargetDays : Byte = 0,

    @ColumnInfo(name = "targetnums")
    var TargetNums : Array<String> = arrayOf(),
    @ColumnInfo(name = "ktargetnums")
    var KTargetNums : Array<String> = arrayOf()
)