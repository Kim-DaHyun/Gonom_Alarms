package com.example.sharlam.navigation.model

data class GroupAlarms (
        var groupName : String = "",
        var joinTimeStamp : Long = 0,
        var members : List<String> = listOf()
)