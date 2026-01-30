@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mybalaka.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.mybalaka.model.Service
import com.example.mybalaka.viewmodel.ServicesViewModel

@Composable
fun ServicesScreen(
    viewModel: ServicesViewModel = viewModel(),
    navController: androidx.navigation.NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAllFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { /* unchanged */ },
        floatingActionButton = { /* unchanged */ }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            PopularCategoryFilters(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.setCategory(it) },
                onMoreClick = { showAllFilters = true } // ✅ Wired properly
            )

            if (uiState.services.isEmpty()) {
                EmptyServices()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.services,
                        key = { it.id }
                    ) { service ->
                        ServiceCard(service, viewModel, navController)
                    }
                }
            }
        }
    }

    if (showAllFilters) {
        AllCategoriesDialog(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = {
                viewModel.setCategory(it)
                showAllFilters = false
            },
            onDismiss = { showAllFilters = false }
        )
    }
}

@Composable
fun PopularCategoryFilters(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onMoreClick: () -> Unit // ✅ Added callback
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            "Filter by Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == "All",
                onClick = { onCategorySelected("All") },
                label = { Text("All") }
            )

            Service.POPULAR_CATEGORIES.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) }
                )
            }

            FilterChip(
                selected = Service.ALL_CATEGORIES.contains(selectedCategory) &&
                        !Service.POPULAR_CATEGORIES.contains(selectedCategory) &&
                        selectedCategory != "All",
                onClick = onMoreClick, // ✅ Open dialog
                label = { Text("More") },
                trailingIcon = {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More filters",
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun AllCategoriesDialog(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "All Categories",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(Service.ALL_CATEGORIES) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category, style = MaterialTheme.typography.bodyLarge)
                        if (selectedCategory == category) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyServices() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Handyman, contentDescription = null, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("No services found", style = MaterialTheme.typography.headlineSmall)
        Text("Add one using the + button", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ServiceCard(
    service: Service,
    viewModel: ServicesViewModel,
    navController: androidx.navigation.NavHostController
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            if (service.posterUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(service.posterUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.padding(16.dp)) {

                if (service.category.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            service.category,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    service.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    service.description,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                    Text(service.price, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${service.phone}"))
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Call")
                    }

                    Button(
                        onClick = {
                            val number = service.whatsapp.ifEmpty { service.phone }
                                .replace("+", "")
                                .replace(" ", "")
                            val uri = Uri.parse("https://wa.me/$number")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("WhatsApp")
                    }

                    Button(
                        onClick = {
                            viewModel.startChatWithProvider(
                                service.providerId,
                                service.title
                            )
                            navController.navigate(
                                "chat_room_direct/${service.providerId}/${service.title}"
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Chat")
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
