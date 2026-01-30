package com.example.mybalaka.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mybalaka.model.Event
import com.example.mybalaka.model.Property
import com.example.mybalaka.viewmodel.HomeViewModel
import com.example.mybalaka.viewmodel.AnnouncementViewModel
import java.text.SimpleDateFormat
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

@Composable
fun HomeScreen(
    vm: HomeViewModel = viewModel { HomeViewModel() },
    onNavigateToEvents: () -> Unit,
    onNavigateToTickets: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    navController: androidx.navigation.NavHostController
) {
    val upcoming by vm.upcomingEvents.collectAsStateWithLifecycle()
    val marketList by vm.marketItems.collectAsStateWithLifecycle()
    val foodList by vm.foodItems.collectAsStateWithLifecycle()
    val section by vm.section.collectAsStateWithLifecycle()
    val properties by vm.properties.collectAsStateWithLifecycle()

    // Get current user ID from FirebaseAuth
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val announcementViewModel: AnnouncementViewModel = viewModel()
    val unreadCount by announcementViewModel.unreadCount.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Section with Weather
        HeaderSection(vm = vm)

        // Quick Actions
        QuickActionsSection(
            onNavigateToEvents = onNavigateToEvents,
            onNavigateToEmergency = onNavigateToEmergency,
            navController = navController,
            unreadCount = unreadCount
        )

        // Featured Content Billboard
        FeaturedContentSection(
            section = section,
            upcoming = upcoming,
            marketList = marketList,
            foodList = foodList,
            properties = properties,
            onNavigateToEvents = onNavigateToEvents,
            navController = navController
        )
    }
}

@Composable
private fun HeaderSection(vm: HomeViewModel) {
    val weather by vm.weather.collectAsStateWithLifecycle()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var photoUrl by remember { mutableStateOf<String?>(null) }
    var showFullImage by remember { mutableStateOf(false) }

    // 🔹 LOAD PHOTO FROM FIRESTORE
    LaunchedEffect(uid) {
        uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                photoUrl = doc.getString("photoUrl")
            }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {

            // 🔹 TOP ROW: Profile picture + welcome text
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // PROFILE IMAGE
                if (!photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { showFullImage = true },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(50)),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Welcome, ${vm.userName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Balaka District Platform",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // WEATHER CARD (unchanged)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = weather.icon,
                        contentDescription = "Weather icon",
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(weather.desc, fontWeight = FontWeight.Medium)
                        Text(
                            "Balaka District",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        weather.temp,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // 🔹 FULLSCREEN IMAGE PREVIEW
    if (showFullImage && !photoUrl.isNullOrEmpty()) {
        Dialog(onDismissRequest = { showFullImage = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullImage = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Full profile image",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToEvents: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    navController: androidx.navigation.NavHostController,
    unreadCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Outlined.Event,
            label = "Events",
            onClick = onNavigateToEvents,
            modifier = Modifier.weight(1f)
        )

        // ✅ PROFESSIONAL ANNOUNCEMENTS BUTTON
        Box(modifier = Modifier.weight(1f)) {
            QuickActionButton(
                icon = Icons.Outlined.Campaign,
                label = "Announcements",
                onClick = {
                    navController.navigate("announcements")
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (unreadCount > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = 6.dp)
                ) {
                    Text(unreadCount.toString())
                }
            }
        }

        QuickActionButton(
            icon = Icons.Outlined.ExitToApp,
            label = "Logout",
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("user_home") { inclusive = true }
                }
            },
            modifier = Modifier.weight(1f),
            isDestructive = true
        )
    }
}
@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    val containerColor = if (isDestructive) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun FeaturedContentSection(
    section: HomeViewModel.Section,
    upcoming: List<Event>,
    marketList: List<HomeViewModel.MarketItem>,
    foodList: List<HomeViewModel.FoodItem>,
    properties: List<Property>,
    onNavigateToEvents: () -> Unit,
    navController: androidx.navigation.NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getSectionTitle(section),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { onSectionViewAll(section, onNavigateToEvents, navController) }) {
                Text("View All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Animated Content
        AnimatedContent(
            targetState = section,
            label = "featured_content",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { targetSection ->
            when (targetSection) {
                HomeViewModel.Section.EVENTS -> EventCarousel(
                    events = upcoming,
                    onEventClick = { /* Handle click */ }
                )
                HomeViewModel.Section.MARKET -> MarketCarousel(marketList)
                HomeViewModel.Section.FOOD -> FoodCarousel(foodList)
                HomeViewModel.Section.PROPERTIES -> PropertiesCarousel(
                    properties = properties,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun getSectionTitle(section: HomeViewModel.Section): String {
    return when (section) {
        HomeViewModel.Section.EVENTS -> "Featured Events"
        HomeViewModel.Section.MARKET -> "Market Highlights"
        HomeViewModel.Section.FOOD -> "Food Specials"
        HomeViewModel.Section.PROPERTIES -> "Property Listings"
    }
}

private fun onSectionViewAll(
    section: HomeViewModel.Section,
    onNavigateToEvents: () -> Unit,
    navController: androidx.navigation.NavHostController
) {
    when (section) {
        HomeViewModel.Section.EVENTS -> onNavigateToEvents()
        HomeViewModel.Section.MARKET -> navController.navigate("market")
        HomeViewModel.Section.FOOD -> navController.navigate("food")
        HomeViewModel.Section.PROPERTIES -> navController.navigate("properties")
    }
}

@Composable
private fun EventCarousel(
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(events) { event ->
            EventCard(
                event = event,
                onClick = { onEventClick(event) }
            )
        }
    }
}

@Composable
private fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            if (event.posterUrl.isNotEmpty()) {
                AsyncImage(
                    model = event.posterUrl,
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 100f
                            )
                        )
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(event.date.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun MarketCarousel(list: List<HomeViewModel.MarketItem>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(list) { item ->
            MarketCard(item = item)
        }
    }
}

@Composable
private fun MarketCard(item: HomeViewModel.MarketItem) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FoodCarousel(list: List<HomeViewModel.FoodItem>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(list) { item ->
            FoodCard(item = item)
        }
    }
}

@Composable
private fun FoodCard(item: HomeViewModel.FoodItem) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.dish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.dish,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PropertiesCarousel(
    properties: List<Property>,
    navController: androidx.navigation.NavHostController
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(properties) { property ->
            PropertyCard(
                property = property,
                onClick = { navController.navigate("property_details/${property.id}") }
            )
        }
    }
}

@Composable
private fun PropertyCard(property: Property, onClick: () -> Unit) {
    val cardHeight = 250.dp

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(cardHeight),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (property.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = property.imageUrls[0],
                    contentDescription = property.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.House,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "K${"%,.0f".format(property.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = property.location,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}