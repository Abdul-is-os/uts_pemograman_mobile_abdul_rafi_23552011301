package com.contoh.abrtodolist

data class ToDoItem(
    val id: Long,
    var text: String,
    var isCompleted: Boolean = false
)