package com.example.mybalaka.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybalaka.model.Ticket
import com.example.mybalaka.viewmodel.EventsViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    viewModel: EventsViewModel = viewModel()
) {
    val tickets by viewModel.myTickets.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadMyTickets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tickets") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (tickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No tickets purchased yet",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tickets) { ticket ->
                    TicketItem(ticket = ticket)
                }
            }
        }
    }
}

@Composable
fun TicketItem(ticket: Ticket) {
    var showQrCode by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    // ✅ CHANGED: Show beautiful Event Name instead of ID
                    Text(
                        text = ticket.eventName.ifEmpty { "Event" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    // ✅ CHANGED: Show beautiful Ticket Type Name instead of ID
                    Text(
                        text = ticket.ticketTypeName.ifEmpty { "Standard Ticket" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Small reference number (optional, less prominent)
                    Text(
                        text = "Ref: #${ticket.id.takeLast(6).uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    TicketStatusChip(status = ticket.status)
                }

                IconButton(onClick = { showQrCode = true }) {
                    Icon(Icons.Default.QrCode, contentDescription = "Show QR Code")
                }
            }
        }
    }

    if (showQrCode) {

        // ✅ SAFE QR GENERATION (ONCE, GUARDED)
        val qrBitmap by remember(ticket.qrCode) {
            mutableStateOf(
                runCatching {
                    if (ticket.qrCode.isBlank()) null
                    else {
                        BarcodeEncoder().encodeBitmap(
                            ticket.qrCode,
                            BarcodeFormat.QR_CODE,
                            400,
                            400
                        )
                    }
                }.getOrNull()
            )
        }

        AlertDialog(
            onDismissRequest = { showQrCode = false },
            title = { Text("Ticket QR Code") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Ticket QR Code",
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        Text(
                            "Invalid QR Code",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Show this at the event entrance",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrCode = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun TicketStatusChip(status: String) {
    val (color, label) = when (status.lowercase()) {
        "active" -> MaterialTheme.colorScheme.primary to "ACTIVE"
        "used" -> MaterialTheme.colorScheme.error to "USED"
        "expired" -> MaterialTheme.colorScheme.outline to "EXPIRED"
        else -> MaterialTheme.colorScheme.outline to status.uppercase()
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                label,
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}