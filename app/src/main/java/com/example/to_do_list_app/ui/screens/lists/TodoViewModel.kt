package com.example.to_do_list_app.ui.screens.lists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_do_list_app.data.local.AppDatabase
import com.example.to_do_list_app.model.TodoItem
import com.example.to_do_list_app.model.TodoList
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * State data class for managing todo lists
 */
data class TodoListState(
    val lists: List<TodoList> = emptyList(),
    val currentList: TodoList? = null,
    val items: List<TodoItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for managing todo lists and items
 *
 * This class was created with assistance from GitHub Copilot
 */
class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FirebaseDatabase.getInstance().reference
    private val roomDb = AppDatabase.getDatabase(application)
    private val todoListDao = roomDb.todoListDao()
    private val todoItemDao = roomDb.todoItemDao()

    private val _todoState = MutableStateFlow(TodoListState())
    val todoState: StateFlow<TodoListState> = _todoState.asStateFlow()

    /**
     * Creates a new todo list
     */
    fun createList(title: String, userId: String, category: String = "Personal") {
        _todoState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val list = TodoList(
                    title = title,
                    userId = userId,
                    category = category
                )

                // Save to Room database first
                val localId = todoListDao.insertList(list)

                // Save to Firebase
                val listRef = database.child("lists").push()
                val firebaseId = listRef.key ?: throw Exception("Failed to generate Firebase key")
                val listWithIds = list.copy(id = localId, firebaseId = firebaseId)

                // Create a map with only the fields we want to store in Firebase
                val firebaseData = mapOf(
                    "title" to listWithIds.title,
                    "userId" to listWithIds.userId,
                    "category" to listWithIds.category,
                    "createdAt" to listWithIds.createdAt.time
                )

                listRef.setValue(firebaseData).await()

                // Update Room with Firebase ID
                todoListDao.updateList(listWithIds)

                _todoState.update { currentState ->
                    currentState.copy(
                        lists = currentState.lists + listWithIds,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _todoState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Loads all items for a specific list
     */
    fun loadItems(listId: Long) {
        viewModelScope.launch {
            try {
                // Get the current list
                val list = todoListDao.getListById(listId)
                _todoState.update { it.copy(currentList = list) }

                // Observe items from Room database
                todoItemDao.getItemsByListId(listId).collect { items ->
                    _todoState.update { it.copy(items = items) }
                }
            } catch (e: Exception) {
                _todoState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    /**
     * Adds a new item to a todo list
     */
    fun addItem(listId: Long, description: String) {
        _todoState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val item = TodoItem(
                    listId = listId,
                    description = description,
                    position = _todoState.value.items.size
                )

                // Save to Room database first
                val localId = todoItemDao.insertItem(item)

                // Save to Firebase
                val itemRef = database.child("items").push()
                val firebaseId = itemRef.key ?: throw Exception("Failed to generate Firebase key")
                val itemWithIds = item.copy(id = localId, firebaseId = firebaseId)
                itemRef.setValue(itemWithIds.toMap()).await()

                // Update Room with Firebase ID
                todoItemDao.updateItem(itemWithIds)

                _todoState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _todoState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Updates an existing todo item
     */
    fun updateItem(item: TodoItem) {
        _todoState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Update in Room
                todoItemDao.updateItem(item)

                // Update in Firebase
                database.child("items").child(item.firebaseId).setValue(item.toMap()).await()

                _todoState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _todoState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Deletes a todo item
     */
    fun deleteItem(item: TodoItem) {
        _todoState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Delete from Room
                todoItemDao.deleteItem(item)

                // Delete from Firebase
                database.child("items").child(item.firebaseId).removeValue().await()

                _todoState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _todoState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Loads all lists for a specific user
     */
    fun loadLists(userId: String) {
        _todoState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Observe lists from Room database
                todoListDao.getAllLists(userId).collect { lists ->
                    _todoState.update {
                        it.copy(
                            lists = lists,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _todoState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Searches for lists containing a specific string
     */
    fun searchLists(query: String, userId: String) {
        viewModelScope.launch {
            try {
                todoListDao.searchLists(query, userId).collect { lists ->
                    _todoState.update {
                        it.copy(lists = lists)
                    }
                }
            } catch (e: Exception) {
                _todoState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    /**
     * Creates a new todo task item
     */
    fun createTask(description: String, listId: Long) {
        _todoState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Get the current list to verify it exists
                val list = todoListDao.getListById(listId) ?: throw Exception("List not found")

                // Create the item with the next position
                val maxPosition = todoItemDao.getMaxPositionForList(listId) ?: 0
                val item = TodoItem(
                    listId = listId,
                    description = description,
                    position = maxPosition + 1
                )

                // Save to Room database first
                val localId = todoItemDao.insertItem(item)

                // Save to Firebase
                val itemRef = database.child("items").push()
                val firebaseId = itemRef.key ?: throw Exception("Failed to generate Firebase key")
                val itemWithIds = item.copy(id = localId, firebaseId = firebaseId)

                // Create a map with only the fields we want to store in Firebase
                val firebaseData = mapOf(
                    "listId" to list.firebaseId,
                    "description" to itemWithIds.description,
                    "isCompleted" to itemWithIds.isCompleted,
                    "createdAt" to itemWithIds.createdAt.time,
                    "position" to itemWithIds.position
                )

                itemRef.setValue(firebaseData).await()

                // Update Room with Firebase ID
                todoItemDao.updateItem(itemWithIds)

                // Refresh the items list if we're in a list
                _todoState.value.currentList?.let { currentList ->
                    if (currentList.id == listId) {
                        // Collect the first emission from the flow
                        todoItemDao.getItemsByListId(listId).collect { items ->
                            _todoState.update { currentState ->
                                currentState.copy(
                                    items = items,
                                    isLoading = false
                                )
                            }
                            // Break out of the flow collection after first emission
                            return@collect
                        }
                    } else {
                        _todoState.update { it.copy(isLoading = false) }
                    }
                } ?: _todoState.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                _todoState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }
}
