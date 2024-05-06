package com.example.kodaapplication

import com.google.firebase.database.PropertyName

data class childData(
    val age: String? = "",
    val childId: String? = "",
    val parentId: String? = "",
    val firstName: String? = "",
    val lastName: String? = "",
    @PropertyName("apps") val apps: Map<String, Any> = emptyMap()
    /*this is a data class for the children*/
)