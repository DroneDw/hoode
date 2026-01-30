@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mybalaka.screens.market

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.mybalaka.cloudinary.CloudinaryUploader
import com.example.mybalaka.model.MarketItem
import com.example.mybalaka.utils.FilePicker
import com.example.mybalaka.viewmodel.MarketViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.widget.Toast

@Composable
fun MarketDetailsScreen(
    itemId: String,
    navController: NavHostController,
    viewModel: MarketViewModel = viewModel()
) {
    val item by viewModel.getMarketItemById(itemId).collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var showEditDialog by remember { mutableStateOf(false) }

    // ✅ REMOVED the problematic LaunchedEffect - it's not needed!

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(item?.title ?: "Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    item?.let { marketItem ->
                        if (marketItem.sellerId == currentUserId) {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        item?.let { marketItem ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = marketItem.imageUrl,
                    contentDescription = marketItem.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(marketItem.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("K${marketItem.price}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(marketItem.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Seller: ${marketItem.sellerName}", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Location: ${marketItem.location}", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            navController.navigate(
                                "chat_room_direct/${marketItem.sellerId}/${marketItem.sellerName}"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat Seller")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val url = "https://wa.me/${marketItem.whatsapp}"
                                android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse(url)
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }.also { context.startActivity(it) }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "WhatsApp")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WhatsApp")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:${marketItem.phone}")
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call")
                        }
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (showEditDialog) {
        item?.let { marketItem ->
            EditMarketItemDialog(
                item = marketItem,
                onDismiss = { showEditDialog = false },
                onConfirm = { updatedItem ->
                    scope.launch {
                        viewModel.updateMarketItem(updatedItem)
                        Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun EditMarketItemDialog(
    item: MarketItem,
    onDismiss: () -> Unit,
    onConfirm: (MarketItem) -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var description by remember { mutableStateOf(item.description) }
    var priceText by remember { mutableStateOf(item.price.toString()) }
    var category by remember { mutableStateOf(item.category) }
    var imageUrl by remember { mutableStateOf(item.imageUrl) }
    var phone by remember { mutableStateOf(item.phone) }
    var whatsapp by remember { mutableStateOf(item.whatsapp) }
    var location by remember { mutableStateOf(item.location) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // ✅ UPDATED: Use standard GetContent launcher instead of custom FilePicker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isUploading = true
            uploadProgress = 0
            CloudinaryUploader.uploadFile(
                context = context,
                uri = it,
                onProgress = { progress -> uploadProgress = progress },
                onSuccess = { url ->
                    imageUrl = url
                    isUploading = false
                    uploadProgress = 0
                    Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    isUploading = false
                    uploadProgress = 0
                    Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Market Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") }
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price") }
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") }
                )
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp") }
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Product Image", style = MaterialTheme.typography.labelLarge)
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    Text(if (imageUrl.isEmpty()) "Choose Image" else "Change Image")
                }

                if (isUploading) {
                    LinearProgressIndicator(
                        progress = uploadProgress / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                if (imageUrl.isNotEmpty()) {
                    Text(
                        text = "✓ Image uploaded",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceValue = priceText.toFloatOrNull() ?: 0f
                    onConfirm(
                        item.copy(
                            title = title,
                            description = description,
                            price = priceValue,
                            category = category,
                            imageUrl = imageUrl,
                            phone = phone,
                            whatsapp = whatsapp,
                            location = location
                        )
                    )
                },
                enabled = !isUploading
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}