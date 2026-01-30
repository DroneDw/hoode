package com.example.mybalaka.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.mybalaka.cloudinary.CloudinaryUploader
import com.example.mybalaka.model.FoodItem
import com.example.mybalaka.utils.FilePicker
import com.example.mybalaka.viewmodel.FoodViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailsScreen(
    itemId: String,
    navController: NavHostController,
    viewModel: FoodViewModel
) {
    val foodItems by viewModel.foodItems.collectAsStateWithLifecycle()
    val item = foodItems.find { it.id == itemId }
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var showEditDialog by remember { mutableStateOf(false) }

    var quantity by remember { mutableStateOf(1) }

    if (item == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val supportsCart = item.vendorType == "restaurant" || item.vendorType == "shop"
    val isOwner = item.cookId == currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwner) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                    IconButton(onClick = { navController.navigate("food_cart") }) {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge {
                                        Text(cartItems.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "K${item.price * quantity}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (supportsCart) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val error = viewModel.addToCart(item, quantity)
                                    if (error != null) {
                                        snackbarHostState.showSnackbar(error)
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            message = "${item.name} added to cart",
                                            actionLabel = "View Cart",
                                            duration = SnackbarDuration.Short
                                        ).let { result ->
                                            if (result == SnackbarResult.ActionPerformed) {
                                                navController.navigate("food_cart")
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to Cart")
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Contact ${item.cookName} at ${item.phone} to order",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call to Order")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                if (item.isNew) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ) {
                        Text("NEW")
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "K${item.price}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⏱ ${item.preparationTime}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Prepared by",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.cookName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Contact: ${item.phone}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (item.vendorType == "individual") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Individual seller - Call to order",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Quantity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        enabled = quantity > 1
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                    }

                    Text(
                        text = quantity.toString(),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { quantity++ }) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase")
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditFoodItemDialog(
            item = item,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedItem ->
                scope.launch {
                    viewModel.updateFoodItem(updatedItem)
                    Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show()
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditFoodItemDialog(
    item: FoodItem,
    onDismiss: () -> Unit,
    onConfirm: (FoodItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var description by remember { mutableStateOf(item.description) }
    var priceText by remember { mutableStateOf(item.price.toString()) }
    var category by remember { mutableStateOf(item.category) }
    var imageUrl by remember { mutableStateOf(item.imageUrl) }
    var preparationTime by remember { mutableStateOf(item.preparationTime) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = FilePicker.getImagePickerContract()
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
        title = { Text("Edit Food Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name") }
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
                    value = preparationTime,
                    onValueChange = { preparationTime = it },
                    label = { Text("Prep Time") }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Food Image", style = MaterialTheme.typography.labelLarge)
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    Text(if (imageUrl.isEmpty()) "Choose Image" else "Change Image")  // ✅ FIXED: Changed posterUrl to imageUrl
                }

                if (isUploading) {
                    LinearProgressIndicator(
                        progress = uploadProgress / 100f,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
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
                            name = name,
                            description = description,
                            price = priceValue,
                            category = category,
                            imageUrl = imageUrl,
                            preparationTime = preparationTime
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