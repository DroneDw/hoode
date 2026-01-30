@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mybalaka.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mybalaka.model.SellerRequest
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun SellerRequestsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var requests by remember { mutableStateOf<List<SellerRequest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("seller_requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load requests", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                requests = snapshot?.toObjects(SellerRequest::class.java) ?: emptyList()
                loading = false
            }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Seller Requests") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No pending seller requests")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(requests) { request ->
                    SellerRequestCard(
                        request = request,
                        onApprove = {
                            approveSeller(request, context)
                        },
                        onReject = {
                            rejectSeller(request, context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SellerRequestCard(
    request: SellerRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Name: ${request.fullName}", style = MaterialTheme.typography.titleMedium)
            Text("Phone: ${request.phone}")

            Spacer(Modifier.height(12.dp))

            Text("National ID - Front")
            AsyncImage(
                model = request.nationalIdFront,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text("National ID - Back")
            AsyncImage(
                model = request.nationalIdBack,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text("Selfie")
            AsyncImage(
                model = request.selfieImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reject")
                }

                Button(onClick = onApprove) {
                    Text("Approve")
                }
            }
        }
    }
}

private fun approveSeller(request: SellerRequest, context: android.content.Context) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .document(request.userId)
        .update(
            mapOf(
                "role" to "seller",
                "sellerApproved" to true
            )
        )
        .addOnSuccessListener {
            db.collection("seller_requests")
                .document(request.id)
                .update("status", "approved")

            Toast.makeText(context, "Seller approved", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Approval failed", Toast.LENGTH_SHORT).show()
        }
}

private fun rejectSeller(request: SellerRequest, context: android.content.Context) {
    val db = FirebaseFirestore.getInstance()

    db.collection("seller_requests")
        .document(request.id)
        .update("status", "rejected")
        .addOnSuccessListener {
            Toast.makeText(context, "Seller rejected", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Rejection failed", Toast.LENGTH_SHORT).show()
        }
}
