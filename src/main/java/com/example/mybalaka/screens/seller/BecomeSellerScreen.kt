@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mybalaka.screens.seller

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
import coil.compose.AsyncImage
import com.example.mybalaka.cloudinary.CloudinaryUploader
import com.example.mybalaka.model.SellerRequest
import com.example.mybalaka.utils.FilePicker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import android.net.Uri

@Composable
fun BecomeSellerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser ?: return

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var idFront by remember { mutableStateOf("") }
    var idBack by remember { mutableStateOf("") }
    var selfie by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }

    // --- Image picker launcher ---
    val frontPicker = rememberLauncherForActivityResult(
        contract = FilePicker.getImagePickerContract()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            CloudinaryUploader.uploadFile(
                context = context,
                uri = it, // <-- Fixed parameter name here
                onProgress = { progress -> uploadProgress = progress },
                onSuccess = { url ->
                    isUploading = false
                    uploadProgress = 0
                    idFront = url
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

    val backPicker = rememberLauncherForActivityResult(
        contract = FilePicker.getImagePickerContract()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            CloudinaryUploader.uploadFile(
                context = context,
                uri = it, // <-- Fixed parameter
                onProgress = { progress -> uploadProgress = progress },
                onSuccess = { url ->
                    isUploading = false
                    uploadProgress = 0
                    idBack = url
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

    val selfiePicker = rememberLauncherForActivityResult(
        contract = FilePicker.getImagePickerContract()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            CloudinaryUploader.uploadFile(
                context = context,
                uri = it, // <-- Fixed parameter
                onProgress = { progress -> uploadProgress = progress },
                onSuccess = { url ->
                    isUploading = false
                    uploadProgress = 0
                    selfie = url
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

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Become a Seller") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name (as on ID)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("National ID - Front")
            Button(
                onClick = { frontPicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) { Text(if (idFront.isEmpty()) "Upload Front" else "Change Front") }
            if (idFront.isNotEmpty()) AsyncImage(model = idFront, contentDescription = "ID Front", modifier = Modifier.height(120.dp))

            Spacer(Modifier.height(12.dp))

            Text("National ID - Back")
            Button(
                onClick = { backPicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) { Text(if (idBack.isEmpty()) "Upload Back" else "Change Back") }
            if (idBack.isNotEmpty()) AsyncImage(model = idBack, contentDescription = "ID Back", modifier = Modifier.height(120.dp))

            Spacer(Modifier.height(12.dp))

            Text("Selfie (Clear Face)")
            Button(
                onClick = { selfiePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) { Text(if (selfie.isEmpty()) "Upload Selfie" else "Change Selfie") }
            if (selfie.isNotEmpty()) AsyncImage(model = selfie, contentDescription = "Selfie", modifier = Modifier.height(120.dp))

            if (isUploading) {
                LinearProgressIndicator(
                    progress = uploadProgress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = fullName.isNotBlank() && phone.isNotBlank() && idFront.isNotBlank() && idBack.isNotBlank() && selfie.isNotBlank() && !isUploading,
                onClick = {
                    val request = SellerRequest(
                        userId = user.uid,
                        fullName = fullName.trim(),
                        phone = phone.trim(),
                        nationalIdFront = idFront,
                        nationalIdBack = idBack,
                        selfieImage = selfie,
                        status = "pending",
                        createdAt = Timestamp.now()
                    )

                    FirebaseFirestore.getInstance()
                        .collection("seller_requests")
                        .add(request)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Application submitted. Await approval.", Toast.LENGTH_LONG).show()
                            onBack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to submit", Toast.LENGTH_SHORT).show()
                        }
                }
            ) {
                Text("Submit for Review")
            }
        }
    }
}
