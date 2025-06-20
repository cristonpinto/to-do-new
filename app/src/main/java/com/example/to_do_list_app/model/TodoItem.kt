package com.example.to_do_list_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

/**
 * Data class representing a single todo item in a list
 * This class is used both for Room database and Firebase storage
 */
@Entity(
    tableName = "todo_items",
    foreignKeys = [
        ForeignKey(
            entity = TodoList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listId: Long = 0,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val position: Int = 0, // For ordering items within a list
    val firebaseId: String = "" // Used to sync with Firebase
) {
    // Required empty constructor for Firebase
    constructor() : this(0, 0, "", false, Date(), 0, "")

    // Create a map for Firebase storage
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "description" to description,
            "isCompleted" to isCompleted,
            "createdAt" to createdAt.time,
            "position" to position
        )
    }
}
