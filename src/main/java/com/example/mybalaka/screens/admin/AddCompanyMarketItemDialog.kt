@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mybalaka.screens.admin

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.mybalaka.cloudinary.CloudinaryUploader
import com.example.mybalaka.model.MarketItem
import com.example.mybalaka.utils.FilePicker

@Composable
fun AddCompanyMarketItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (MarketItem) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Balaka") }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }

    // --- Image picker ---
    val imagePickerLauncher =
        rememberLauncherForActivityResult(FilePicker.getImagePickerContract()) { uri ->
            uri?.let {
                isUploading = true
                uploadProgress = 0
                CloudinaryUploader.uploadFile(
                    context,
                    it,
                    onProgress = { progress -> uploadProgress = progress },
                    onSuccess = { url ->
                        imageUrl = url
                        isUploading = false
                        uploadProgress = 0
                        Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
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

                Text("Add Company Market Item", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

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
                            Toast.makeText(context, "Please upload an image", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Create MarketItem
                        onConfirm(
                            MarketItem(
                                title = title,
                                description = description,
                                price = priceText.toFloatOrNull() ?: 0f,
                                category = category,
                                imageUrl = imageUrl,
                                sellerId = "COMPANY_ID", // hardcoded company id
                                sellerName = "Transit Wealth",
                                phone = "",
                                whatsapp = whatsapp,
                                location = location,
                                isAvailable = true,
                                approved = true // company items auto-approved
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
