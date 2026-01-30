package com.example.mybalaka.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mybalaka.model.Event
import com.example.mybalaka.model.EventComment
import com.example.mybalaka.viewmodel.EventsViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventsViewModel = viewModel()
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var showTicketPurchase by remember { mutableStateOf<Event?>(null) }
    val tabIndex by viewModel.selectedTab.collectAsStateWithLifecycle()

    // Add Live tab
    val tabs = listOf("Live", "Today", "Upcoming", "Past")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Events") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Tabs
            ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 12.dp) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = tabIndex == i,
                        onClick = { viewModel.selectTab(i) },
                        text = { Text(title) }
                    )
                }
            }

            // Category Chips
            val selectedCat by viewModel.selectedCat.collectAsStateWithLifecycle()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCat == cat,
                        onClick = { viewModel.selectCat(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            Box(Modifier.fillMaxSize()) {
                if (events.isEmpty()) EmptyEvents()
                else LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filteredEvents = events.filter { event ->
                        val now = System.currentTimeMillis()
                        val start = event.date.toDate().time
                        val end = event.endDate?.toDate()?.time

                        val tabMatch = when (tabIndex) {
                            0 -> start <= now && (end == null || now <= end) // Live
                            1 -> {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = now
                                val todayStart = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                                val todayEnd = cal.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis
                                start in todayStart..todayEnd
                            }
                            2 -> start > now // Upcoming
                            3 -> end != null && end < now // Past
                            else -> true
                        }

                        val catMatch = selectedCat == "All" || event.category == selectedCat

                        tabMatch && catMatch
                    }

                    items(filteredEvents) { event ->
                        EventCard(
                            event = event,
                            viewModel = viewModel,
                            context = context,
                            currentUserId = currentUserId,
                            onClick = { selectedEvent = event },
                            onBuyTicket = { showTicketPurchase = it }
                        )
                    }
                }
            }
        }
    }

    selectedEvent?.let { event ->
        EventDetailsScreen(
            event = event,
            onClose = { selectedEvent = null },
            viewModel = viewModel
        )
    }

    showTicketPurchase?.let { event ->
        TicketPurchaseDialog(
            event = event,
            userId = currentUserId,
            onDismiss = { showTicketPurchase = null },
            onPurchaseSuccess = { }
        )
    }
}

@Composable
private fun EmptyEvents() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Event, null, Modifier.size(64.dp))
        Spacer(Modifier.height(12.dp))
        Text("No events", style = MaterialTheme.typography.bodyLarge)
    }
}

/*------------------------------------------------------
   EventDetailsScreen
--------------------------------------------------------*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDetailsScreen(
    event: Event,
    onClose: () -> Unit,
    viewModel: EventsViewModel
) {
    val liveEvent by viewModel.selectedEvent.collectAsStateWithLifecycle()
    var commentText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    LaunchedEffect(event.id) { viewModel.loadEvent(event.id) }

    val displayEvent = liveEvent ?: event
    val normalizedComments = displayEvent.getNormalizedComments()

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(displayEvent.title) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .weight(1f)
            ) {
                item {
                    if (displayEvent.posterUrl.isNotEmpty()) {
                        AsyncImage(
                            model = displayEvent.posterUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(MaterialTheme.shapes.large),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    Text(displayEvent.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(20.dp))
                    Text("Comments", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }

                items(normalizedComments) { comment ->
                    CommentBubble(comment)
                    Spacer(Modifier.height(6.dp))
                }

                if (normalizedComments.isEmpty()) {
                    item {
                        Text(
                            "No comments yet. Be the first!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }

            Row(Modifier.fillMaxWidth().padding(10.dp)) {
                TextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Write a comment...") },
                    enabled = !isPosting
                )
                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            isPosting = true
                            viewModel.addComment(displayEvent.id, commentText.trim())
                            commentText = ""
                            isPosting = false
                        }
                    },
                    enabled = !isPosting
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentBubble(comment: EventComment) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatCommentTime(comment.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatCommentTime(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
}

/*------------------------------------------------------
   EventCard
--------------------------------------------------------*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(
    event: Event,
    viewModel: EventsViewModel,
    context: android.content.Context,
    currentUserId: String,
    onClick: () -> Unit,
    onBuyTicket: (Event) -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val isLiked = event.likedBy.contains(currentUserId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (event.posterUrl.isNotEmpty()) {
                AsyncImage(
                    model = event.posterUrl,
                    contentDescription = "Event Poster",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )
            }
            Text(event.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                event.description,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(event.venue, style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Date: ${sdf.format(event.date.toDate())}")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = event.contactPhone,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${event.contactPhone}"))
                        )
                    }
                )
            }

            val (statusText, statusColor) = getEventStatusText(event)
            Text(
                text = statusText,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

// Inside EventCard composable
            if (event.isTicketed) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onBuyTicket(event) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Buy Tickets")
                }
            }

            Divider()
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = { viewModel.toggleLikeEvent(event.id, currentUserId) },
                    colors = if (isLiked)
                        ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFBBDEFB))
                    else
                        ButtonDefaults.filledTonalButtonColors(),
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("${event.likes} Likes")
                }
            }
        }
    }
}

private fun getEventStatusText(event: Event): Pair<String, Color> {
    val now = System.currentTimeMillis()
    val start = event.date.toDate().time
    val end = event.endDate?.toDate()?.time

    return when {
        now < start -> {
            val diff = start - now
            val hours = diff / (1000 * 60 * 60)
            val minutes = (diff / (1000 * 60)) % 60
            "Starts in ${hours}h ${minutes}m" to Color(0xFF1565C0)
        }
        end != null && now in start..end -> {
            "LIVE NOW" to Color(0xFFD32F2F)
        }
        else -> {
            "ENDED" to Color.Gray
        }
    }
}