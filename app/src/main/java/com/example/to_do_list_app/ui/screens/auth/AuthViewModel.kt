package com.example.to_do_list_app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_do_list_app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Authentication state data class
 */
data class AuthState(
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for handling authentication related operations
 */
class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Check if user is already logged in
        auth.currentUser?.let { firebaseUser ->
            _authState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                try {
                    // Fetch the user data from the Realtime Database instead of using Auth data
                    val userSnapshot = database.child("users").child(firebaseUser.uid).get().await()
                    val userData = userSnapshot.getValue(User::class.java)

                    val user = if (userData != null && userData.displayName.isNotEmpty()) {
                        userData // Use data from database if available
                    } else {
                        User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "User"
                        )
                    }

                    _authState.update {
                        it.copy(
                            isLoggedIn = true,
                            user = user,
                            isLoading = false
                        )
                    }
                } catch (e: Exception) {
                    _authState.update {
                        it.copy(
                            error = e.message,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Register a new user with email and password
     */
    fun register(email: String, password: String, displayName: String, onSuccess: () -> Unit) {
        _authState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    val user = User(
                        id = firebaseUser.uid,
                        email = email,
                        displayName = displayName
                    )
                    // Save user to Realtime Database
                    database.child("users").child(firebaseUser.uid).setValue(user).await()
                    _authState.update {
                        it.copy(
                            isLoggedIn = true,
                            user = user,
                            isLoading = false
                        )
                    }
                    onSuccess()
                }
            } catch (e: Exception) {
                _authState.update {
                    it.copy(
                        error = e.message ?: "Registration failed",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Login with email and password
     */
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        _authState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    // Get user data from Realtime Database
                    val userSnapshot = database.child("users").child(firebaseUser.uid).get().await()
                    val user = userSnapshot.getValue(User::class.java) ?: User(
                        id = firebaseUser.uid,
                        email = email,
                        displayName = email.substringBefore('@')
                    )
                    _authState.update {
                        it.copy(
                            isLoggedIn = true,
                            user = user,
                            isLoading = false
                        )
                    }
                    onSuccess()
                }
            } catch (e: Exception) {
                _authState.update {
                    it.copy(
                        error = e.message ?: "Login failed",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Sign out the current user
     */
    fun signOut() {
        _authState.update { it.copy(isLoading = true, error = null) }
        auth.signOut()
        _authState.update {
            it.copy(
                isLoggedIn = false,
                user = null,
                isLoading = false
            )
        }
    }

    /**
     * Change the user's password
     */
    fun changePassword(currentPassword: String, newPassword: String, onSuccess: () -> Unit) {
        _authState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null && user.email != null) {
                    // Re-authenticate the user first
                    val credential = com.google.firebase.auth.EmailAuthProvider
                        .getCredential(user.email!!, currentPassword)

                    user.reauthenticate(credential).await()

                    // Change the password
                    user.updatePassword(newPassword).await()

                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    onSuccess()
                } else {
                    _authState.update {
                        it.copy(
                            error = "User not authenticated",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _authState.update {
                    it.copy(
                        error = e.message ?: "Failed to change password",
                        isLoading = false
                    )
                }
            }
        }
    }
}
