package com.example.mybalaka.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.mybalaka.viewmodel.FoodViewModel
// Add this import at the top
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodCartScreen(
    navController: NavHostController,
    viewModel: FoodViewModel
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var deliveryAddress by remember { mutableStateOf("Office #, Building, Street") }
    var notes by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("") }
    var amountSent by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Mpamba") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearCart() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Clear All")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cartItems.isEmpty()) {
                EmptyCart()
            } else {
                // FIXED: Removed verticalScroll from parent Column
                // Cart items with limited height
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Use weight instead of fixed height
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemCard(
                            item = item,
                            onUpdateQuantity = { qty ->
                                viewModel.updateCartItemQuantity(item.itemId, qty)
                            },
                            onRemove = {
                                viewModel.removeFromCart(item.itemId)
                            }
                        )
                    }
                }

                // Delivery & Payment Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()) // Scrollable section
                        .padding(16.dp)
                ) {
                    // Payment Notice Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Payment Required",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your order will only be processed AFTER payment is confirmed by the cook. " +
                                        "Please send payment via Mpamba or Airtel Money to the cook's number.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Payment Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // FIXED: Use AssistChip instead of FilterChip for better compatibility
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { paymentMethod = "Mpamba" },
                            label = { Text("Mpamba") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )
                        AssistChip(
                            onClick = { paymentMethod = "Airtel Money" },
                            label = { Text("Airtel Money") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add this card after payment method selection
                    val context = LocalContext.current

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Pay to: ${viewModel.currentCartCookName ?: "Vendor"}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = viewModel.currentCartCookPhone ?: "Contact not available",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.currentCartCookPhone?.let { phone ->
                                        viewModel.copyToClipboard(context, phone)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Contact copied: $phone")
                                        }
                                    }
                                },
                                enabled = !viewModel.currentCartCookPhone.isNullOrEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sender's Name
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Sender's Name (as on mobile money)") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        maxLines = 1,
                        isError = senderName.isBlank()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Amount Sent
                    OutlinedTextField(
                        value = amountSent,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amountSent = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Amount Sent (MWK)") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null) // FIXED: Use existing icon
                        },
                        placeholder = { Text("15000") },
                        maxLines = 1,
                        isError = amountSent.isBlank()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Delivery Address
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Delivery Address") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                        },
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Order Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Order Notes (Optional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null) // FIXED: Use existing icon
                        },
                        maxLines = 3
                    )
                }

                // Checkout Button
                Surface(
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "K${cartItems.sumOf { it.totalPrice.toDouble() }}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "By placing order, you confirm payment details are correct. " +
                                    "Cook will verify before preparing food.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (senderName.isBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please enter sender's name")
                                    }
                                    return@Button
                                }

                                if (amountSent.isBlank() || amountSent.toFloatOrNull() == null) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please enter valid amount")
                                    }
                                    return@Button
                                }

                                scope.launch {
                                    val cookId = cartItems.firstOrNull()?.let { item ->
                                        viewModel.foodItems.value.find { it.id == item.itemId }?.cookId
                                    }

                                    if (cookId == null) {
                                        snackbarHostState.showSnackbar("Unable to process order")
                                        return@launch
                                    }

                                    viewModel.createOrder(
                                        cookId = cookId,
                                        deliveryAddress = deliveryAddress,
                                        notes = notes,
                                        senderName = senderName,
                                        amountSent = amountSent.toFloat(),
                                        paymentMethod = paymentMethod,
                                        onSuccess = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Order placed! Payment will be verified by cook.",
                                                    actionLabel = "Track Order",
                                                    duration = SnackbarDuration.Long
                                                ).let { result ->
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        navController.navigate("food_orders")
                                                    }
                                                }
                                            }
                                        },
                                        onError = { error ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(error)
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = cartItems.isNotEmpty(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null) // FIXED: Use existing icon
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Place Order & Send Payment")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: FoodViewModel.CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "K${item.price} each",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onUpdateQuantity(item.quantity - 1) },
                    enabled = item.quantity > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }

                Text(
                    text = item.quantity.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = { onUpdateQuantity(item.quantity + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onRemove,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }
        }
    }
}

@Composable
private fun EmptyCart() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your cart is empty",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}