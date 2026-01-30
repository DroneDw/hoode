@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mybalaka.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.mybalaka.viewmodel.PropertyViewModel

@Composable
fun PropertyDetailsScreen(
    propertyId: String,
    navController: NavHostController,
    viewModel: PropertyViewModel = viewModel()
) {
    val property by viewModel.selectedProperty.collectAsState()
    val context = LocalContext.current
    var showFullScreenViewer by remember { mutableStateOf(false) }

    LaunchedEffect(propertyId) { viewModel.loadPropertyById(propertyId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(property?.title ?: "Property") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        property?.let { prop ->
            Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
                // Image Gallery
                if (prop.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = prop.imageUrls[0],
                        contentDescription = prop.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clickable { showFullScreenViewer = true },
                        contentScale = ContentScale.Crop
                    )
                    if (prop.imageUrls.size > 1) {
                        Text(
                            text = "+${prop.imageUrls.size - 1} more images",
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { showFullScreenViewer = true }
                        )
                    }
                }

                Column(Modifier.padding(16.dp)) {
                    Text(prop.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(prop.category.replace("_", " ").uppercase(), Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                        Text("K${"%,.0f".format(prop.price)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Location: ${prop.location}", style = MaterialTheme.typography.bodyMedium)
                        Text(prop.propertyType.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(prop.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(prop.sellerName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("Phone: ${prop.phone}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(Modifier.padding(16.dp)) {
                    Button(onClick = { navController.navigate("chat_room_direct/${prop.sellerId}/${prop.sellerName}") }, Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat")
                        Spacer(Modifier.width(8.dp))
                        Text("Chat Seller")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Button(onClick = { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/${prop.whatsapp}"))) }, Modifier.weight(1f)) {
                            Icon(Icons.Default.Send, contentDescription = "WhatsApp")
                            Spacer(Modifier.width(8.dp))
                            Text("WhatsApp")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { context.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${prop.phone}"))) }, Modifier.weight(1f)) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                            Spacer(Modifier.width(8.dp))
                            Text("Call")
                        }
                    }
                }
            }
        } ?: Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // Full-Screen Image Viewer
    if (showFullScreenViewer) {
        property?.let { prop ->
            FullScreenImageViewer(
                imageUrls = prop.imageUrls,
                onDismiss = { showFullScreenViewer = false }
            )
        }
    }
}

@Composable
fun FullScreenImageViewer(
    imageUrls: List<String>,
    onDismiss: () -> Unit
) {
    var currentImageIndex by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Display the current image
            AsyncImage(
                model = imageUrls[currentImageIndex],
                contentDescription = "Property image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Previous button (left arrow)
            IconButton(
                onClick = {
                    if (currentImageIndex > 0) {
                        currentImageIndex--
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Next button (right arrow)
            IconButton(
                onClick = {
                    if (currentImageIndex < imageUrls.size - 1) {
                        currentImageIndex++
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Image counter
            Text(
                text = "${currentImageIndex + 1}/${imageUrls.size}",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            )
        }
    }
}
