package com.example.mybalaka.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybalaka.model.Event
import com.example.mybalaka.model.TicketType
import com.example.mybalaka.payment.PayRequest
import com.example.mybalaka.payment.PaymentLauncher
import com.example.mybalaka.payment.PaymentViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketPurchaseDialog(
    event: Event,
    userId: String,
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit
) {
    var selectedTicketType by remember { mutableStateOf<TicketType?>(null) }
    var quantity by remember { mutableStateOf(1) }

    val context = LocalContext.current
    val paymentViewModel: PaymentViewModel = viewModel()

    val ticketAvailability: (TicketType) -> Int = { it.quantity - it.sold }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                Text(
                    "Buy Tickets - ${event.title}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer16()

                // Ticket Types
                event.ticketTypes.forEach { ticketType ->
                    val available = ticketAvailability(ticketType)
                    val isSelected = selectedTicketType == ticketType

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = if (isSelected) {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        } else {
                            CardDefaults.cardColors(
                                containerColor =
                                if (available > 0)
                                    MaterialTheme.colorScheme.surface
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        },
                        onClick = {
                            if (available > 0) {
                                selectedTicketType = ticketType
                                quantity = 1
                                paymentViewModel.clearError()
                            }
                        },
                        enabled = available > 0
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(ticketType.name, fontWeight = FontWeight.Bold)
                                Text(
                                    if (available > 0) "$available available" else "Sold Out",
                                    color = if (available > 0)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                "MWK ${NumberFormat.getInstance().format(ticketType.price.toInt())}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer16()

                // Quantity + Total
                selectedTicketType?.let { ticket ->
                    val available = ticketAvailability(ticket)

                    Text("Quantity", fontWeight = FontWeight.Bold)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                        }

                        Text(quantity.toString(), fontWeight = FontWeight.Bold)

                        IconButton(
                            onClick = { if (quantity < available) quantity++ },
                            enabled = quantity < available
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                        }
                    }

                    Spacer16()

                    val totalPrice = ticket.price * quantity
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total:")
                            Text(
                                "MWK ${NumberFormat.getInstance().format(totalPrice.toInt())}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Error
                paymentViewModel.errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer16()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        enabled = selectedTicketType != null && !paymentViewModel.isLoading,
                        onClick = {
                            val ticket = selectedTicketType ?: return@Button
                            val totalPrice = ticket.price * quantity

                            paymentViewModel.pay(
                                PayRequest(
                                    amount = totalPrice,
                                    phone = "0000000000",   // placeholder ONLY
                                    network = "UNKNOWN",    // confirmed AFTER payment
                                    userId = userId,
                                    itemId = "${event.id}_${ticket.id}"
                                )
                            ) { checkoutUrl ->

                                // Store ONLY reference info (no phone/network)
                                val prefs = context.getSharedPreferences("payments", Context.MODE_PRIVATE)
                                prefs.edit()
                                    .putString("pending_payment_item", ticket.id)
                                    .apply()

                                PaymentLauncher.openCheckout(context, checkoutUrl)

                                onPurchaseSuccess()
                                onDismiss()
                            }
                        }
                    ) {
                        if (paymentViewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Proceed to Payment")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Spacer16() = Spacer(modifier = Modifier.height(16.dp))
