package com.example.to_do_list_app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.to_do_list_app.R
import com.example.to_do_list_app.ui.screens.auth.AuthViewModel
import com.example.to_do_list_app.ui.theme.ToDoListAppTheme

/**
 * Home screen of the app showing a welcome message and "Go to Lists" button
 *
 * This screen was created with assistance from GitHub Copilot
 */
@Composable
fun HomeScreen(
    onNavigateToLists: () -> Unit,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val userName = authState.user?.displayName ?: "User"

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.homepic),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content overlay
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom // Changed from Center to Bottom
            ) {
                // Welcome text and developer info removed

                // Added more space at top to push button toward bottom
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onNavigateToLists,
                    modifier = Modifier.size(width = 200.dp, height = 60.dp)
                ) {
                    Text(
                        text = "Go to Lists",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Add some space at the bottom for better positioning
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ToDoListAppTheme {
        // Preview implementation
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom // Changed from Center to Bottom
            ) {
                // Welcome text and developer info removed

                // Added more space at top to push button toward bottom
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { },
                    modifier = Modifier.size(width = 200.dp, height = 60.dp)
                ) {
                    Text(
                        text = "Go to Lists",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Add some space at the bottom for better positioning
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
