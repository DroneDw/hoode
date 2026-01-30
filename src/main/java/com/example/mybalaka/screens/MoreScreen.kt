package com.example.mybalaka.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavHostController) {
    val moreItems = listOf(
        Triple("Events", Icons.Default.Event, "events"),
        Triple("Food", Icons.Default.Fastfood, "food"),
        Triple("Market", Icons.Default.ShoppingBag, "market"),
        Triple("Properties", Icons.Default.House, "properties"),
        Triple("Accommodation", Icons.Default.Hotel, "accommodation"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More Options") },
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
            items(moreItems.size) { index ->
                val (title, icon, route) = moreItems[index]
                ListItem(
                    headlineContent = { Text(title) },
                    leadingContent = {
                        Icon(icon, contentDescription = title)
                    },
                    modifier = Modifier.clickable {
                        navController.navigate(route)
                    }
                )
            }
        }
    }
}