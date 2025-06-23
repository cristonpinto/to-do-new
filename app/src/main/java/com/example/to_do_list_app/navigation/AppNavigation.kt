package com.example.to_do_list_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.to_do_list_app.ui.screens.auth.AuthViewModel
import com.example.to_do_list_app.ui.screens.auth.LoginScreen
import com.example.to_do_list_app.ui.screens.auth.RegisterScreen
import com.example.to_do_list_app.ui.screens.home.HomeScreen
import com.example.to_do_list_app.ui.screens.lists.ListDetailsScreen
import com.example.to_do_list_app.ui.screens.lists.ListsScreen
import com.example.to_do_list_app.ui.screens.lists.TodoViewModel
import com.example.to_do_list_app.ui.screens.profile.ProfileScreen
import com.example.to_do_list_app.ui.screens.tasks.TaskDetailsScreen

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object Register : Screen("register")
    object Lists : Screen("lists")
    object Profile : Screen("profile") // Adding Profile screen route
    object ListDetails : Screen("list/{listId}") {
        fun createRoute(listId: Long) = "list/$listId"
    }
    object TaskDetails : Screen("taskDetails") // Add new TaskDetails route
}

/**
 * Main navigation component for the app
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    todoViewModel: TodoViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState.isLoggedIn) Screen.Home.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { navController.navigate(Screen.Home.route) },
                authViewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onRegisterSuccess = { navController.navigate(Screen.Home.route) },
                authViewModel = authViewModel
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLists = { navController.navigate(Screen.Lists.route) },
                authViewModel = authViewModel
            )
        }

        composable(Screen.Lists.route) {
            ListsScreen(
                onNavigateToListDetails = { listId ->
                    navController.navigate(Screen.ListDetails.createRoute(listId))
                },
                todoViewModel = todoViewModel,
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Lists.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                navController = navController
            )
        }

        composable(
            Screen.ListDetails.route,
            arguments = listOf(navArgument("listId") { type = NavType.LongType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong("listId") ?: -1L
            ListDetailsScreen(
                listId = listId,
                todoViewModel = todoViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToTaskDetails = {
                    navController.navigate(Screen.TaskDetails.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.navigateUp() },
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TaskDetails.route) {
            android.util.Log.d("Navigation", "Composing TaskDetailsScreen")
            TaskDetailsScreen(
                onBack = { navController.navigateUp() },
                onAddTask = { hasPriority, date, time ->
                    // Handle task creation here
                    navController.navigateUp()
                }
            )
        }
    }
}
