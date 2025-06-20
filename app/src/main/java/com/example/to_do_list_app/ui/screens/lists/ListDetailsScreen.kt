package com.example.to_do_list_app.ui.screens.lists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.to_do_list_app.model.TodoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailsScreen(
    listId: Long,
    todoViewModel: TodoViewModel,
    onBack: () -> Unit
) {
    val todoState by todoViewModel.todoState.collectAsState()
    var showAddItemDialog by remember { mutableStateOf(false) }
    var newItemDescription by remember { mutableStateOf("") }

    LaunchedEffect(listId) {
        // Load items for this list
        todoViewModel.loadItems(listId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(todoState.currentList?.title ?: "List Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddItemDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add new item")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (todoState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (todoState.items.isEmpty()) {
                Text(
                    text = "No items in this list. Add your first item!",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = todoState.items,
                        key = { it.id }
                    ) { item ->
                        TodoItemCard(
                            item = item,
                            onToggleComplete = {
                                todoViewModel.updateItem(item.copy(isCompleted = !item.isCompleted))
                            },
                            onDelete = { todoViewModel.deleteItem(item) }
                        )
                    }
                }
            }

            if (showAddItemDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showAddItemDialog = false
                        newItemDescription = ""
                    },
                    title = { Text("Add New Item") },
                    text = {
                        OutlinedTextField(
                            value = newItemDescription,
                            onValueChange = { newItemDescription = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                todoViewModel.addItem(listId, newItemDescription)
                                showAddItemDialog = false
                                newItemDescription = ""
                            },
                            enabled = newItemDescription.isNotEmpty()
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showAddItemDialog = false
                                newItemDescription = ""
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            todoState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoItemCard(
    item: TodoItem,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggleComplete() }
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
