package com.example.sharlam.navigation.model

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {

    //@TypeConverter
    //fun listToJson(value : Array<Boolean>?) = Gson().toJson(value)
    //
    //@TypeConverter
    //fun jsonToList(value: String) = Gson().fromJson(value, Array<Boolean>::class.java)

    @TypeConverter
    fun NumsToJson(value : Array<String>?): String = Gson().toJson(value)

    @TypeConverter
    fun JsonToNums(value : String) = Gson().fromJson(value,Array<String>::class.java)
}