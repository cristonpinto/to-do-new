package com.example.to_do_list_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data class representing a todo list which can contain multiple items
 * This class is used both for Room database and Firebase storage
 */
@Entity(tableName = "todo_lists")
data class TodoList(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val userId: String = "", // References the Firebase user ID
    val createdAt: Date = Date(),
    val category: String = "Personal", // Default category
    val firebaseId: String = "" // Used to sync with Firebase
) {
    // Required empty constructor for Firebase
    constructor() : this(0, "", "", Date(), "Personal", "")

    // Create a map for Firebase storage
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "userId" to userId,
            "createdAt" to createdAt.time,
            "category" to category
        )
    }
}
