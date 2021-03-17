package com.example.sharlam.navigation.model

data class UserDTO (
    var UserID : String = "",
    var accountTimeStamp : Long? = null,
    var age : Int? = null,
    var male : Boolean? = null,
    var groupSize : Int = 0
)