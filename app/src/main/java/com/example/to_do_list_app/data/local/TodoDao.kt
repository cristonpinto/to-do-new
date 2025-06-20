package com.example.to_do_list_app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.to_do_list_app.model.TodoItem
import com.example.to_do_list_app.model.TodoList
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Todo Lists
 * Defines database operations for TodoList entities
 */
@Dao
interface TodoListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TodoList): Long

    @Update
    suspend fun updateList(list: TodoList)

    @Delete
    suspend fun deleteList(list: TodoList)

    @Query("SELECT * FROM todo_lists WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllLists(userId: String): Flow<List<TodoList>>

    @Query("SELECT * FROM todo_lists WHERE id = :listId")
    suspend fun getListById(listId: Long): TodoList?

    @Query("SELECT * FROM todo_lists WHERE title LIKE '%' || :searchQuery || '%' AND userId = :userId")
    fun searchLists(searchQuery: String, userId: String): Flow<List<TodoList>>
}

/**
 * Data Access Object (DAO) for Todo Items
 * Defines database operations for TodoItem entities
 */
@Dao
interface TodoItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: TodoItem): Long

    @Update
    suspend fun updateItem(item: TodoItem)

    @Delete
    suspend fun deleteItem(item: TodoItem)

    @Query("SELECT * FROM todo_items WHERE listId = :listId ORDER BY position ASC")
    fun getItemsByListId(listId: Long): Flow<List<TodoItem>>

    @Query("UPDATE todo_items SET position = :newPosition WHERE id = :itemId")
    suspend fun updateItemPosition(itemId: Long, newPosition: Int)

    @Query("SELECT * FROM todo_items WHERE description LIKE '%' || :searchQuery || '%'")
    fun searchItems(searchQuery: String): Flow<List<TodoItem>>

    @Query("SELECT MAX(position) FROM todo_items WHERE listId = :listId")
    suspend fun getMaxPositionForList(listId: Long): Int?
}
