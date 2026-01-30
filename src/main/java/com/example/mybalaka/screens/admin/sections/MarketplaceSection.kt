package com.example.mybalaka.screens.admin.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MarketplaceSection(
    onManageSellers: () -> Unit,
    onAddMarketItem: () -> Unit
) {
    Text("Marketplace Management", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(16.dp))
    Button(onClick = onManageSellers, modifier = Modifier.fillMaxWidth()) {
        Text("Manage Product Sellers")
    }
    Spacer(Modifier.height(16.dp))
    Button(onClick = onAddMarketItem, modifier = Modifier.fillMaxWidth()) {
        Text("Add Market Item")
    }
}