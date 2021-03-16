package com.example.sharlam.navigation.model

data class AlarmDTO(var Title : String? = null,
                    var SoundUrl : String? = null,
                    var Timestamp : Long? = null,
                    var Targethours : Int? = null,
                    var Targetminutes : Int? =null,
                    var TargetDays : MutableList<Boolean> = MutableList<Boolean>(7,{false})
)