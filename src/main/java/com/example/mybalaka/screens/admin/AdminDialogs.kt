@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mybalaka.screens.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.mybalaka.cloudinary.CloudinaryUploader
import com.example.mybalaka.model.*
import com.example.mybalaka.utils.FilePicker
import com.google.firebase.Timestamp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import java.util.*


@Composable
fun AddFoodItemDialog(
    cooks: List<User>,
    onDismiss: () -> Unit,
    onConfirm: (FoodItem) -> Unit,
    showError: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var preparationTime by remember { mutableStateOf("15-20 min") }
    var selectedCook by remember { mutableStateOf<User?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var vendorType by remember { mutableStateOf("restaurant") }
    var isNew by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = FilePicker.getImagePickerContract()) { uri ->
        uri?.let {
            isUploading = true
            uploadProgress = 0
            CloudinaryUploader.uploadFile(context, it, { progress -> uploadProgress = progress }, { url ->
                imageUrl = url
                isUploading = false
                uploadProgress = 0
                Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
            }, { error ->
                isUploading = false
                uploadProgress = 0
                Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
            })
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 8.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                Text("Add Food Item", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                var vendorExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = vendorExpanded, onExpandedChange = { vendorExpanded = !vendorExpanded }) {
                    OutlinedTextField(value = vendorType.replaceFirstChar { it.uppercase() }, onValueChange = {}, label = { Text("Vendor Type") }, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorExpanded) })
                    ExposedDropdownMenu(expanded = vendorExpanded, onDismissRequest = { vendorExpanded = false }) {
                        listOf("restaurant", "shop", "individual").forEach { type ->
                            DropdownMenuItem(text = { Text(type.replaceFirstChar { it.uppercase() }) }, onClick = { vendorType = type; vendorExpanded = false; if (type == "individual") selectedCook = null })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (vendorType != "individual") {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(value = selectedCook?.name ?: "", onValueChange = {}, label = { Text("Select Cook") }, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) })
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            if (cooks.isEmpty()) {
                                DropdownMenuItem(text = { Text("No cooks available") }, onClick = { expanded = false })
                            } else {
                                cooks.forEach { cook ->
                                    DropdownMenuItem(text = { Text("${cook.name} (${cook.email})") }, onClick = { selectedCook = cook; expanded = false })
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Food Name") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price") })
                Spacer(Modifier.height(8.dp))
                Text("Food Image", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth(), enabled = !isUploading) {
                    Text(if (imageUrl.isEmpty()) "Choose Image" else "Change Image")
                }
                if (isUploading) LinearProgressIndicator(progress = uploadProgress / 100f, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                if (imageUrl.isNotEmpty()) Text(text = "✓ Image uploaded", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = preparationTime, onValueChange = { preparationTime = it }, label = { Text("Prep Time") })
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Mark as NEW item", modifier = Modifier.weight(1f))
                    Switch(checked = isNew, onCheckedChange = { isNew = it })
                }
                Spacer(Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    if (vendorType != "individual" && selectedCook == null) {
                        showError()
                        return@Button
                    }
                    if (imageUrl.isBlank()) {
                        Toast.makeText(context, "Please upload an image", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onConfirm(
                        FoodItem(
                            name = name,
                            description = description,
                            price = priceText.toFloatOrNull() ?: 0f,
                            category = category,
                            imageUrl = imageUrl,
                            cookId = if (vendorType == "individual") "" else selectedCook!!.id,
                            cookName = if (vendorType == "individual") "Individual Seller" else selectedCook!!.name,
                            phone = if (vendorType == "individual") "" else selectedCook!!.phone,
                            whatsapp = if (vendorType == "individual") "" else selectedCook!!.phone,
                            preparationTime = preparationTime,
                            isAvailable = true,
                            vendorType = vendorType,
                            isNew = isNew
                        )
                    )
                }, enabled = !isUploading) {
                    Text("Publish Food Item")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceDialog(onDismiss: () -> Unit, onConfirm: (Service) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var posterUrl by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = FilePicker.getImagePickerContract()) { uri ->
        uri?.let {
            isUploading = true
            uploadProgress = 0
            CloudinaryUploader.uploadFile(context, it, { progress -> uploadProgress = progress }, { url ->
                posterUrl = url
                isUploading = false
                uploadProgress = 0
                Toast.makeText(context, "Poster uploaded!", Toast.LENGTH_SHORT).show()
            }, { error ->
                isUploading = false
                uploadProgress = 0
                Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
            })
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(text = "Add New Service", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category *") }, modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) })
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        Service.ALL_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; categoryExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("Service Poster", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth(), enabled = !isUploading) {
                    Text(if (posterUrl.isEmpty()) "Choose Image" else "Change Image")
                }
                if (isUploading) LinearProgressIndicator(progress = uploadProgress / 100f, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                if (posterUrl.isNotEmpty()) Text(text = "✓ Poster uploaded", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(enabled = category.isNotBlank() && title.isNotBlank() && posterUrl.isNotBlank() && !isUploading, onClick = {
                        onConfirm(
                            Service(
                                id = "",
                                title = title.trim(),
                                description = description.trim(),
                                category = category,
                                price = price.trim(),
                                phone = phone.trim(),
                                whatsapp = whatsapp.trim(),
                                posterUrl = posterUrl.trim(),
                                providerId = "",
                                rating = 0f,
                                voteCount = 0,
                                isAvailable = true
                            )
                        )
                        onDismiss()
                    }) {
                        Text("Add Service")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMarketItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (MarketItem) -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser ?: return

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Balaka") }

    var sellerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }

    // Pre-fill current user info
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                sellerName = doc.getString("name") ?: ""
                phone = doc.getString("phone") ?: ""
                whatsapp = doc.getString("phone") ?: "" // Pre-fill WhatsApp with phone
            }
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(FilePicker.getImagePickerContract()) { uri ->
            uri?.let {
                isUploading = true
                uploadProgress = 0
                CloudinaryUploader.uploadFile(
                    context,
                    it,
                    { progress -> uploadProgress = progress },
                    { url ->
                        imageUrl = url
                        isUploading = false
                        uploadProgress = 0
                        Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show()
                    },
                    {
                        isUploading = false
                        uploadProgress = 0
                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Add Market Item", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                // Display seller info (not editable)
                Text("Seller: $sellerName", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price (MK)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text("Product Image", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))

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
                        "✓ Image uploaded",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading,
                    onClick = {
                        if (imageUrl.isBlank()) {
                            Toast.makeText(context, "Please upload an image", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }

                        onConfirm(
                            MarketItem(
                                title = title,
                                description = description,
                                price = priceText.toFloatOrNull() ?: 0f,
                                category = category,
                                imageUrl = imageUrl,
                                sellerId = currentUser.uid,
                                sellerName = sellerName,
                                phone = phone,
                                whatsapp = whatsapp,
                                location = location,
                                isAvailable = true,
                                approved = false
                            )
                        )

                        onDismiss()
                    }
                ) {
                    Text("Publish Item")
                }
            }
        }
    }
}


@Composable
fun AddPropertyDialog(users: List<User>, onDismiss: () -> Unit, onConfirm: (Property) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("house_rent") }
    var location by remember { mutableStateOf("Balaka") }
    var imageUrls by remember { mutableStateOf(listOf<String>()) }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = FilePicker.getImagePickerContract()) { uri ->
        uri?.let {
            isUploading = true
            uploadProgress = 0
            CloudinaryUploader.uploadFile(context, it, { progress -> uploadProgress = progress }, { url ->
                imageUrls = imageUrls + url
                isUploading = false
                uploadProgress = 0
                Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
            }, { error ->
                isUploading = false
                uploadProgress = 0
                Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
            })
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 8.dp) {
            Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text("List Property", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = selectedUser?.name ?: "", onValueChange = {}, label = { Text("Select Seller") }, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) })
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        users.forEach { user ->
                            DropdownMenuItem(text = { Text(user.name) }, onClick = { selectedUser = user; phone = user.phone; whatsapp = user.phone; expanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = !catExpanded }) {
                    OutlinedTextField(value = category.replace("_", " "), onValueChange = {}, label = { Text("Category") }, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) })
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        listOf("house_rent", "shop_rent", "plot_sale", "other").forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.replace("_", " ")) }, onClick = { category = cat; catExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Property Title") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                Spacer(Modifier.height(8.dp))
                Text("Property Images", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth(), enabled = !isUploading) {
                    Text("Add Image")
                }
                if (isUploading) LinearProgressIndicator(progress = uploadProgress / 100f, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                imageUrls.forEachIndexed { index, url ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(text = "Image ${index + 1}: ✓ Uploaded", color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { imageUrls = imageUrls.filterIndexed { i, _ -> i != index } }, enabled = !isUploading) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Image")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp") })
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    if (selectedUser == null) return@Button
                    if (imageUrls.isEmpty()) {
                        Toast.makeText(context, "Please upload at least one image", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onConfirm(
                        Property(
                            title = title,
                            description = description,
                            price = priceText.toFloatOrNull() ?: 0f,
                            category = category,
                            location = location,
                            imageUrls = imageUrls,
                            sellerId = selectedUser!!.id,
                            sellerName = selectedUser!!.name,
                            phone = phone,
                            whatsapp = whatsapp,
                            propertyType = if (category.contains("rent")) "rental" else "sale"
                        )
                    )
                    onDismiss()
                }, modifier = Modifier.fillMaxWidth(), enabled = !isUploading) {
                    Text("Publish Property")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnnouncementDialog(
    onDismiss: () -> Unit,
    onConfirm: (Announcement) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var downloadableFileUrl by remember { mutableStateOf("") }
    var showDownloadButton by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var expiryDays by remember { mutableStateOf("30") }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            uploadProgress = 0
            CloudinaryUploader.uploadFile(context, it, { progress ->
                uploadProgress = progress
            }, { url ->
                imageUrls = imageUrls + url
                isUploading = false
                uploadProgress = 0
                Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
            }, { error ->
                isUploading = false
                uploadProgress = 0
                Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
            })
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            uploadProgress = 0
            CloudinaryUploader.uploadFile(context, it, { progress ->
                uploadProgress = progress
            }, { url ->
                downloadableFileUrl = url
                showDownloadButton = true
                isUploading = false
                uploadProgress = 0
                Toast.makeText(context, "File uploaded!", Toast.LENGTH_SHORT).show()
            }, { error ->
                isUploading = false
                uploadProgress = 0
                Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
            })
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add New Announcement",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    maxLines = 4
                )
                Spacer(Modifier.height(16.dp))

                // Images Section
                Text("Announcement Images", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))

                // Uploaded images preview
                if (imageUrls.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(imageUrls) { url ->
                            Card(
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(4.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Uploaded image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Image upload button
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    Text(if (imageUrls.isEmpty()) "Add Image" else "Add More Images")
                }

                if (isUploading) {
                    LinearProgressIndicator(
                        progress = { uploadProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // File attachment section
                Text("Attachments", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))

                // File upload button
                Button(
                    onClick = { filePickerLauncher.launch("application/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    Text(if (downloadableFileUrl.isEmpty()) "Add File" else "Replace File")
                }

                if (showDownloadButton) {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Attachment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("File attached successfully")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Expiry settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = expiryDays,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                                expiryDays = newValue
                            }
                        },
                        label = { Text("Expiry (days)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("days", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || description.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Title and description are required",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val expiryDate = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, expiryDays.toIntOrNull() ?: 30)
                            }.time

                            onConfirm(
                                Announcement(
                                    title = title.trim(),
                                    description = description.trim(),
                                    imageUrls = imageUrls,
                                    createdBy = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                    createdAt = Timestamp.now(),
                                    expiresAt = Timestamp(expiryDate),
                                    showDownloadButton = showDownloadButton,
                                    downloadableFileUrl = if (showDownloadButton) downloadableFileUrl else null,
                                    readBy = emptyList()
                                )
                            )
                            onDismiss()
                        },
                        enabled = !isUploading && title.isNotBlank() && description.isNotBlank()
                    ) {
                        Text("Publish Announcement")
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    organizerId: String,
    organizerName: String,
    allOrganizers: List<User>,
    onDismiss: () -> Unit,
    onConfirm: (Event) -> Unit,
    onOrganizerSelected: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var posterUrl by remember { mutableStateOf("") }
    var startDateTime by remember { mutableStateOf<Calendar?>(null) }
    var endDateTime by remember { mutableStateOf<Calendar?>(null) }
    var isMultiDay by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("Other") }
    val categories = listOf("Music", "Football", "Church", "Wedding", "Other")
    var recurrence by remember { mutableStateOf("None") }
    val recurrenceOptions = listOf("None", "Daily", "Weekly", "Monthly")

    // Initialize from parameters so if dialog reopens it starts fresh
    var selectedOrganizer by remember(organizerName) { mutableStateOf(organizerName) }
    var selectedOrganizerId by remember(organizerId) { mutableStateOf(organizerId) }
    var expanded by remember { mutableStateOf(false) }

    /* -------- Image Upload State (CLOUDINARY PATTERN) -------- */
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // Image Picker Launcher using your FilePicker utility
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            uploadProgress = 0
            CloudinaryUploader.uploadFile(
                context = context,
                uri = it,
                onProgress = { progress -> uploadProgress = progress },
                onSuccess = { url ->
                    posterUrl = url
                    isUploading = false
                    uploadProgress = 0
                    Toast.makeText(context, "Poster uploaded!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    isUploading = false
                    uploadProgress = 0
                    Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
                }
            )

        }
    }

    /* -------- Ticketing -------- */
    var isTicketed by remember { mutableStateOf(false) }
    var ticketName by remember { mutableStateOf("") }
    var ticketPrice by remember { mutableStateOf("") }
    var ticketQuantity by remember { mutableStateOf("") }
    val ticketTypes = remember { mutableStateListOf<TicketType>() }

    val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())

    // Date/Time pickers
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var tempStartCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var tempEndCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    fun updateStartDateTime() {
        startDateTime = tempStartCalendar.clone() as Calendar
    }

    fun updateEndDateTime() {
        endDateTime = tempEndCalendar.clone() as Calendar
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Event", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(title, { title = it }, label = { Text("Event title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(desc, { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(venue, { venue = it }, label = { Text("Venue") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(phone, { phone = it }, label = { Text("Contact phone") }, modifier = Modifier.fillMaxWidth())

                /* -------- POSTER UPLOAD SECTION (MATCHES YOUR PATTERN) -------- */
                Text("Event Poster", style = MaterialTheme.typography.labelLarge)

                if (posterUrl.isNotEmpty()) {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = "Event Poster",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(vertical = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    Text(if (posterUrl.isEmpty()) "Upload Poster" else "Change Poster")
                }

                if (isUploading) {
                    LinearProgressIndicator(
                        progress = uploadProgress / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                if (posterUrl.isNotEmpty()) {
                    Text(
                        text = "✓ Poster uploaded",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Text("Category", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach {
                        FilterChip(
                            selected = category == it,
                            onClick = { category = it },
                            label = { Text(it) }
                        )
                    }
                }

                Text("Organizer", style = MaterialTheme.typography.labelLarge)

                // FIXED DROPDOWN - Added menuAnchor() modifier
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedOrganizer,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Organizer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()  // <-- CRITICAL FIX for dropdown
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.exposedDropdownSize()
                    ) {
                        if (allOrganizers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No organizers available", color = Color.Gray) },
                                onClick = { }
                            )
                        } else {
                            allOrganizers.forEach { organizer ->
                                DropdownMenuItem(
                                    text = { Text(organizer.name) },
                                    onClick = {
                                        selectedOrganizer = organizer.name
                                        selectedOrganizerId = organizer.id
                                        onOrganizerSelected(organizer.id, organizer.name)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Start Date/Time
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showStartDatePicker = true
                        }
                ) {
                    OutlinedTextField(
                        value = startDateTime?.let { sdf.format(it.time) } ?: "",
                        onValueChange = {},
                        label = { Text("Start date & time") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Multi-day event", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isMultiDay,
                        onCheckedChange = {
                            isMultiDay = it
                            if (!it) endDateTime = null
                        }
                    )
                }

                if (isMultiDay) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showEndDatePicker = true
                            }
                    ) {
                        OutlinedTextField(
                            value = endDateTime?.let { sdf.format(it.time) } ?: "",
                            onValueChange = {},
                            label = { Text("End date & time") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        )
                    }
                }

                Text("Recurrence", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recurrenceOptions.forEach {
                        FilterChip(
                            selected = recurrence == it,
                            onClick = { recurrence = it },
                            label = { Text(it) }
                        )
                    }
                }

                /* -------- Ticketing UI -------- */
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ticketed Event", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isTicketed,
                        onCheckedChange = { isTicketed = it }
                    )
                }

                if (isTicketed) {
                    OutlinedTextField(
                        value = ticketName,
                        onValueChange = { ticketName = it },
                        label = { Text("Ticket name (e.g. Regular, VIP)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ticketPrice,
                        onValueChange = { ticketPrice = it },
                        label = { Text("Ticket price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ticketQuantity,
                        onValueChange = { ticketQuantity = it },
                        label = { Text("Quantity available") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            ticketTypes.add(
                                TicketType(
                                    id = UUID.randomUUID().toString(),
                                    name = ticketName,
                                    price = ticketPrice.toDoubleOrNull() ?: 0.0,
                                    quantity = ticketQuantity.toIntOrNull() ?: 0,
                                    sold = 0,
                                    available = ticketQuantity.toIntOrNull() ?: 0
                                )
                            )
                            ticketName = ""
                            ticketPrice = ""
                            ticketQuantity = ""
                        },
                        enabled = ticketName.isNotBlank() && ticketPrice.isNotBlank() && ticketQuantity.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Ticket Type")
                    }

                    ticketTypes.forEach {
                        Text("• ${it.name} – MWK ${it.price} (${it.quantity} tickets)")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() &&
                        desc.isNotBlank() &&
                        venue.isNotBlank() &&
                        phone.isNotBlank() &&
                        startDateTime != null &&
                        selectedOrganizerId.isNotBlank() &&
                        !isUploading,

                onClick = {
                    onConfirm(
                        Event(
                            id = "",
                            title = title,
                            description = desc,
                            venue = venue,
                            contactPhone = phone,
                            posterUrl = posterUrl,
                            date = Timestamp(startDateTime!!.time),
                            endDate = endDateTime?.let { Timestamp(it.time) },
                            category = category,
                            recurrence = recurrence,
                            multiDay = isMultiDay,
                            organiserId = selectedOrganizerId,
                            organiserIds = listOf(selectedOrganizerId),
                            organizerId = selectedOrganizerId,
                            organizerName = selectedOrganizer,
                            isTicketed = isTicketed,  // This ensures Buy Ticket button shows
                            ticketTypes = if (isTicketed) ticketTypes.toList() else emptyList()
                        )
                    )
                    onDismiss()
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    // Date/Time Pickers (Material3)
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        tempStartCalendar.timeInMillis = millis
                        showStartDatePicker = false
                        showStartTimePicker = true
                    }
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState()
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tempStartCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    tempStartCalendar.set(Calendar.MINUTE, timePickerState.minute)
                    updateStartDateTime()
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        tempEndCalendar.timeInMillis = millis
                        showEndDatePicker = false
                        showEndTimePicker = true
                    }
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState()
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tempEndCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    tempEndCalendar.set(Calendar.MINUTE, timePickerState.minute)
                    updateEndDateTime()
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = "Select Time",
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    dismissButton()
                    confirmButton()
                }
            }
        }
    }
}
