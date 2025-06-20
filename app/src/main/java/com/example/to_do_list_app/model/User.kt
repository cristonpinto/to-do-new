package com.example.to_do_list_app.model

/**
 * Data class representing a user in the app
 */
data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = ""
) {
    // Required empty constructor for Firebase
    constructor() : this("", "", "")
}
