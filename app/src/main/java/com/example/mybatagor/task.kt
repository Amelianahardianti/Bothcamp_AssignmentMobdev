package com.example.mybatagor.model

import com.google.firebase.firestore.PropertyName

data class Task(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val priority: String = "",

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var isCompleted: Boolean = false
)
