package com.example.to_do_list_app.ui.screens.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.to_do_list_app.R
import com.example.to_do_list_app.model.TodoList
import com.example.to_do_list_app.ui.components.TaskBottomSheet
import com.example.to_do_list_app.ui.screens.auth.AuthViewModel
import kotlin.random.Random

/**
 * Screen for displaying and managing todo lists
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    onNavigateToListDetails: (Long) -> Unit,
    todoViewModel: TodoViewModel,
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val todoState by todoViewModel.todoState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    var showNewListDialog by remember { mutableStateOf(false) }
    var newListTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Personal") }
    var searchQuery by remember { mutableStateOf("") }
    var showTaskBottomSheet by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    // Enhanced category system with modern color palette
    val categories = listOf("Personal", "Work", "Shopping", "Health", "Travel", "Finance")
    val categoryColors = mapOf(
        "Personal" to Color(0xFF5B8DEF), // Modern blue
        "Work" to Color(0xFFFF8355),     // Warm orange
        "Shopping" to Color(0xFF4CD97B), // Fresh green
        "Health" to Color(0xFFFF5A5A),   // Energetic red
        "Travel" to Color(0xFF9772FB),   // Calming purple
        "Finance" to Color(0xFFFFCB44)   // Bright yellow
    )

    // Secondary colors for gradients and backgrounds
    val categorySecondaryColors = mapOf(
        "Personal" to Color(0xFF3A7BFA),
        "Work" to Color(0xFFFF6937),
        "Shopping" to Color(0xFF20BD5F),
        "Health" to Color(0xFFFF3B3B),
        "Travel" to Color(0xFF7B4BFF),
        "Finance" to Color(0xFFFFBB22)
    )

    val categoryIcons = mapOf(
        "Personal" to Icons.Outlined.Notifications,
        "Work" to Icons.Rounded.List,
        "Shopping" to Icons.Rounded.CheckCircle,
        "Health" to Icons.Rounded.CheckCircle,
        "Travel" to Icons.Rounded.CheckCircle,
        "Finance" to Icons.Rounded.CheckCircle
    )

    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    val topBarElevation by animateDpAsState(
        targetValue = if (isScrolled) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "topBarElevation"
    )

    LaunchedEffect(authState.user) {
        authState.user?.let { user ->
            todoViewModel.loadLists(user.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search lists...",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (searchQuery.isNotEmpty()) {
                                        searchQuery = ""
                                    } else {
                                        isSearchActive = false
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                    } else {
                        Text(
                            "My Lists",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.headlineSmall,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = { onNavigateToProfile() },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .shadow(elevation = topBarElevation)
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // New Task FAB (left-aligned) - with added text label
                ExtendedFloatingActionButton(
                    onClick = { showTaskBottomSheet = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    icon = {
                        Icon(
                            Icons.Default.Create,
                            contentDescription = "New Task",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            "New Task",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                )

                // Add List FAB (right-aligned) - with added text label
                ExtendedFloatingActionButton(
                    onClick = { showNewListDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add List",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            "New List",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = Color.Transparent // Changed from MaterialTheme.colorScheme.background to transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.listscreen),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            if (todoState.isLoading) {
                LoadingView()
            } else if (todoState.lists.isEmpty()) {
                EmptyListsView(onCreateList = { showNewListDialog = true })
            } else {
                Column {
                    // Greeting section with username
                    authState.user?.let { user ->
                        GreetingSection(user.displayName)
                    }

                    // Category quick filter
                    CategoryFilterSection(
                        categories = categories,
                        categoryColors = categoryColors,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category ->
                            selectedCategory = if (selectedCategory == category) "" else category
                            // Filter by category
                        }
                    )

                    // Lists content
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val filteredLists = todoState.lists.filter {
                            val matchesSearch = if (searchQuery.isEmpty()) true else {
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.category.contains(searchQuery, ignoreCase = true)
                            }

                            val matchesCategory = if (selectedCategory.isEmpty()) true else {
                                it.category == selectedCategory
                            }

                            matchesSearch && matchesCategory
                        }

                        if (filteredLists.isEmpty() && (searchQuery.isNotEmpty() || selectedCategory.isNotEmpty())) {
                            item {
                                NoResultsView(
                                    query = searchQuery,
                                    category = selectedCategory,
                                    onClearSearch = {
                                        searchQuery = ""
                                        selectedCategory = ""
                                    }
                                )
                            }
                        } else {
                            // Group lists by category if no category filter is applied
                            if (selectedCategory.isEmpty()) {
                                val groupedLists = filteredLists.groupBy { it.category }

                                groupedLists.forEach { (category, lists) ->
                                    item {
                                        CategoryHeader(
                                            category = category,
                                            color = categoryColors[category] ?: MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    itemsIndexed(lists) { index, list ->
                                        val delay = index * 50 // Staggered animation delay

                                        AnimatedVisibility(
                                            visible = true,
                                            enter = fadeIn(tween(durationMillis = 300, delayMillis = delay)) +
                                                    slideInVertically(
                                                        initialOffsetY = { 40 },
                                                        animationSpec = tween(durationMillis = 300, delayMillis = delay)
                                                    )
                                        ) {
                                            val categoryColor = categoryColors[list.category]
                                                ?: MaterialTheme.colorScheme.primary
                                            val secondaryColor = categorySecondaryColors[list.category]
                                                ?: MaterialTheme.colorScheme.primaryContainer

                                            TodoListItem(
                                                list = list,
                                                onClick = { onNavigateToListDetails(list.id) },
                                                categoryColor = categoryColor,
                                                secondaryColor = secondaryColor,
                                                todoViewModel = todoViewModel,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // If category filter is applied, show lists without grouping
                                itemsIndexed(filteredLists) { index, list ->
                                    val delay = index * 50 // Staggered animation delay

                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(tween(durationMillis = 300, delayMillis = delay)) +
                                                slideInVertically(
                                                    initialOffsetY = { 40 },
                                                    animationSpec = tween(durationMillis = 300, delayMillis = delay)
                                                )
                                    ) {
                                        val categoryColor = categoryColors[list.category]
                                            ?: MaterialTheme.colorScheme.primary
                                        val secondaryColor = categorySecondaryColors[list.category]
                                            ?: MaterialTheme.colorScheme.primaryContainer

                                        TodoListItem(
                                            list = list,
                                            onClick = { onNavigateToListDetails(list.id) },
                                            categoryColor = categoryColor,
                                            secondaryColor = secondaryColor,
                                            todoViewModel = todoViewModel,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // New List Dialog
            if (showNewListDialog) {
                CreateListDialog(
                    newListTitle = newListTitle,
                    onNewListTitleChange = { newListTitle = it },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    categories = categories,
                    categoryColors = categoryColors,
                    categoryIcons = categoryIcons,
                    onDismiss = {
                        showNewListDialog = false
                        newListTitle = ""
                    },
                    onCreateList = {
                        authState.user?.let { user ->
                            todoViewModel.createList(
                                title = newListTitle,
                                userId = user.id,
                                category = selectedCategory
                            )
                        }
                        showNewListDialog = false
                        newListTitle = ""
                    }
                )
            }

            // Task Bottom Sheet
            if (showTaskBottomSheet) {
                Dialog(
                    onDismissRequest = { showTaskBottomSheet = false },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false,
                        decorFitsSystemWindows = false
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 64.dp) // Leave space for the top content to be visible
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    ) {
                        TaskBottomSheet(
                            lists = todoState.lists,
                            onDismiss = { showTaskBottomSheet = false },
                            onCreateTask = { description, listId ->
                                todoViewModel.createTask(description, listId)
                                showTaskBottomSheet = false
                            }
                        )
                    }
                }
            }

            // Animated error snackbar
            AnimatedVisibility(
                visible = todoState.error != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                todoState.error?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .padding(bottom = 80.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp)),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text(error)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 5.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Loading your lists...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryFilterSection(
    categories: List<String>,
    categoryColors: Map<String, Color>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add "All" option
        item {
            CategoryChip(
                category = "All",
                isSelected = selectedCategory.isEmpty(),
                color = MaterialTheme.colorScheme.primary,
                onSelected = { onCategorySelected("") }
            )
        }

        items(categories) { category ->
            CategoryChip(
                category = category,
                isSelected = selectedCategory == category,
                color = categoryColors[category] ?: MaterialTheme.colorScheme.primary,
                onSelected = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: String,
    isSelected: Boolean,
    color: Color,
    onSelected: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Surface(
        onClick = onSelected,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .height(36.dp)
            .scale(scale)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )

                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryHeader(
    category: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Modern gradient indicator
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = category,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        // Horizontal line that fills the rest of the space
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .padding(start = 12.dp, end = 4.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f),
                            color.copy(alpha = 0.0f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun GreetingSection(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        val greeting = buildAnnotatedString {
            withStyle(style = SpanStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )) {
                append("Hello, ")
            }
            withStyle(style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )) {
                append(userName)
            }
        }

        Text(greeting)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Organize your day with ease",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Modern gradient divider
        Box(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun NoResultsView(
    query: String,
    category: String,
    onClearSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Text(
                text = when {
                    query.isNotEmpty() && category.isNotEmpty() ->
                        "No lists found for \"$query\" in $category category"
                    query.isNotEmpty() ->
                        "No lists found for \"$query\""
                    category.isNotEmpty() ->
                        "No lists found in $category category"
                    else ->
                        "No lists found"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClearSearch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Filters")
            }
        }
    }
}

@Composable
private fun EmptyListsView(onCreateList: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated empty state illustration
        val pulseAnimation by animateFloatAsState(
            targetValue = 1.05f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "pulse"
        )

        // Layered circles for a more modern look
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(pulseAnimation)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background circles
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.List,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your list is empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create your first list to get started organizing your tasks in a beautiful way",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onCreateList,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Create Your First List",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoListItem(
    list: TodoList,
    onClick: () -> Unit,
    categoryColor: Color,
    secondaryColor: Color,
    todoViewModel: TodoViewModel,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 2f else 5f,
        label = "elevation"
    )

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.shadowElevation = elevation
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        // Modern layout with visual hierarchy
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background elements
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .drawBehind {
                        // Add curved pattern for visual interest
                        val path = androidx.compose.ui.graphics.Path()
                        val width = size.width
                        val height = size.height

                        path.moveTo(width * 0.7f, 0f)
                        path.cubicTo(
                            width * 0.9f, height * 0.1f,
                            width * 0.8f, height * 0.5f,
                            width * 1.1f, height * 0.9f
                        )
                        path.lineTo(width, 0f)
                        path.close()

                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    categoryColor.copy(alpha = 0.1f),
                                    categoryColor.copy(alpha = 0.05f)
                                )
                            )
                        )

                        // Add a few dots for texture
                        for (i in 0..5) {
                            val x = width * (0.7f + Random.nextFloat() * 0.25f)
                            val y = height * Random.nextFloat()
                            val size = 4.dp.toPx() * (0.5f + Random.nextFloat() * 0.5f)

                            drawCircle(
                                color = categoryColor.copy(alpha = 0.1f),
                                radius = size,
                                center = Offset(x, y)
                            )
                        }
                    }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Category tag and task count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = categoryColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = list.category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = categoryColor
                            )
                        }
                    }

                    // Task count indicator
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "${list.taskCount ?: 0}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List title with better typography
                Text(
                    text = list.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress indicator - more visual and engaging
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Progress bar
                    val progress = list.completedTaskCount?.toFloat()?.div((list.taskCount ?: 1).toFloat()) ?: 0f

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(categoryColor, secondaryColor)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Percentage text
                    val percentage = (progress * 100).toInt()
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
            }

            // Quick action buttons - subtle but accessible
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete List") },
            text = {
                Text("Are you sure you want to delete \"${list.title}\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        todoViewModel.deleteList(list.id)
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CreateListDialog(
    newListTitle: String,
    onNewListTitleChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categories: List<String>,
    categoryColors: Map<String, Color>,
    categoryIcons: Map<String, Any>,
    onDismiss: () -> Unit,
    onCreateList: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 16.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Header with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                "Create New List",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )

                            Text(
                                "Organize your tasks effectively",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // List name input
                    OutlinedTextField(
                        value = newListTitle,
                        onValueChange = onNewListTitleChange,
                        label = { Text("List Name") },
                        placeholder = { Text("Enter a descriptive name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = categoryColors[selectedCategory] ?: MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = categoryColors[selectedCategory] ?: MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Choose Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Modern category selection grid
                    LazyColumn {
                        items(categories.chunked(2)) { rowCategories ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowCategories.forEach { category ->
                                    val isSelected = selectedCategory == category
                                    val categoryColor = categoryColors[category] ?: MaterialTheme.colorScheme.primary

                                    Surface(
                                        onClick = { onCategorySelected(category) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(64.dp)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) categoryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) categoryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Color indicator
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(
                                                                categoryColor,
                                                                categoryColor.copy(alpha = 0.7f)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                // Add empty space if odd number of categories
                                if (rowCategories.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action buttons in a more modern layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Cancel",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Create button
                        Button(
                            onClick = onCreateList,
                            enabled = newListTitle.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = categoryColors[selectedCategory] ?: MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                "Create",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}