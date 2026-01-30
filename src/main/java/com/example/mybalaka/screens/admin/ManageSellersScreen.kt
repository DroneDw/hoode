@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mybalaka.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.mybalaka.data.UserRepository
import com.example.mybalaka.model.User
import kotlinx.coroutines.launch

@Composable
fun ManageSellersScreen(
    navController: NavHostController,
    userRepository: UserRepository = UserRepository
) {
    val users by userRepository.getAllUsers().collectAsStateWithLifecycle(initialValue = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Sellers") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(users) { user ->
                UserManagementItem(
                    user = user,
                    onMakeSeller = {
                        coroutineScope.launch {
                            try {
                                userRepository.updateUserRole(user.id, "seller")
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to update role"
                                showError = true
                            }
                        }
                    },
                    onMakeProvider = {
                        coroutineScope.launch {
                            try {
                                userRepository.updateUserRole(user.id, "provider")
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to update role"
                                showError = true
                            }
                        }
                    },
                    onDelete = {
                        coroutineScope.launch {
                            try {
                                userRepository.deleteUser(user.id)
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to delete user"
                                showError = true
                            }
                        }
                    }
                )
            }
        }

        // Show error if any operation fails
        if (showError) {
            LaunchedEffect(showError) {
                // You can log the error or show a toast here
                // For now, we just reset the flag
                showError = false
            }
        }
    }
}

@Composable
private fun UserManagementItem(
    user: User,
    onMakeSeller: () -> Unit,
    onMakeProvider: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.name, style = MaterialTheme.typography.titleMedium)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium)
                    Text("Role: ${user.role}", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (user.role != "seller") {
                    TextButton(onClick = onMakeSeller) {
                        Text("Make Seller")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                if (user.role != "provider") {
                    TextButton(onClick = onMakeProvider) {
                        Text("Make Provider")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}