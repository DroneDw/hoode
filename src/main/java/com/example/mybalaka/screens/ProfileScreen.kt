package com.example.mybalaka.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.mybalaka.model.User
import com.example.mybalaka.viewmodel.EventsViewModel
import com.example.mybalaka.viewmodel.FoodViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mybalaka.screens.admin.AddMarketItemDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    foodViewModel: FoodViewModel = viewModel(),
    eventsViewModel: EventsViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var userData by remember { mutableStateOf<User?>(null) }
    var sellerStatus by remember { mutableStateOf<String?>(null) }

    var savedPropertiesCount by remember { mutableStateOf(0) }
    var savedFoodCount by remember { mutableStateOf(0) }
    val customerOrders by foodViewModel.customerOrders.collectAsStateWithLifecycle()

    // STATE: For showing AddMarketItemDialog
    var showAddItemDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current // ✅ Move LocalContext here

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    userData = User(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        role = doc.getString("role") ?: "user",
                        photoUrl = doc.getString("photoUrl")
                    )
                    val savedProps = doc.get("savedProperties") as? List<String> ?: emptyList()
                    val savedFd = doc.get("savedFood") as? List<String> ?: emptyList()
                    savedPropertiesCount = savedProps.size
                    savedFoodCount = savedFd.size
                }

            db.collection("seller_requests")
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .addOnSuccessListener { snap ->
                    sellerStatus = snap.documents.firstOrNull()?.getString("status")
                }
        }
    }

    LaunchedEffect(Unit) {
        foodViewModel.loadCustomerOrders()
        eventsViewModel.loadMyTickets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Settings, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            UserIdentityCard(
                user = userData,
                onEditClick = { navController.navigate("edit_profile") }
            )

            userData?.let { user ->
                SellerSection(
                    role = user.role,
                    sellerStatus = sellerStatus,
                    navController = navController,
                    onAddItemClick = { showAddItemDialog = true }
                )
            }

            userData?.role?.let { role ->
                RoleSpecificSection(
                    role = role,
                    navController = navController,
                    foodViewModel = foodViewModel
                )
            }

            ActivitySummaryCard(
                orderCount = customerOrders.size,
                savedCount = savedPropertiesCount + savedFoodCount,
                navController = navController
            )

            MyTicketsCard(navController)
            AccountManagementSection(navController)
            SupportSection(navController)
            LogoutButton(navController, context)

            // ✅ Show dialog inside Column
            if (showAddItemDialog) {
                AddMarketItemDialog(
                    onDismiss = { showAddItemDialog = false },
                    onConfirm = { item ->
                        FirebaseFirestore.getInstance()
                            .collection("market_items")
                            .add(item)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Item added successfully", Toast.LENGTH_SHORT).show()
                            }
                        showAddItemDialog = false
                    }
                )
            }
        }
    }
}

/* ---------------- SELLER SECTION ---------------- */
@Composable
private fun SellerSection(
    role: String,
    sellerStatus: String?,
    navController: NavHostController,
    onAddItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Marketplace",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            when {
                role == "admin" -> {
                    ListItem(
                        headlineContent = { Text("Seller Requests") },
                        leadingContent = { Icon(Icons.Outlined.VerifiedUser, null) },
                        trailingContent = { Icon(Icons.Outlined.ChevronRight, null) },
                        modifier = Modifier.clickable { navController.navigate("seller_requests") }
                    )
                }

                role == "seller" -> {
                    if (sellerStatus == "approved") {
                        Column {
                            Text(
                                "You are an approved seller ✅",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onAddItemClick) {
                                Text("Add Market Item")
                            }
                        }
                    } else if (sellerStatus == "pending") {
                        Text(
                            "Seller application pending approval ⏳",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        Button(
                            onClick = { navController.navigate("become_seller") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Store, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Become a Seller")
                        }
                    }
                }

                else -> {
                    Button(
                        onClick = { navController.navigate("become_seller") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Store, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Become a Seller")
                    }
                }
            }
        }
    }
}

/* MY TICKETS CARD */
@Composable
private fun MyTicketsCard(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { navController.navigate("my_tickets") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ConfirmationNumber,
                contentDescription = "My Tickets",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "My Tickets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = "Go to Tickets"
            )
        }
    }
}

/* USER IDENTITY CARD */
@Composable
private fun UserIdentityCard(
    user: User?,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = user?.phone ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = (user?.role ?: "user").uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit Profile")
            }
        }
    }
}

/* ROLE SPECIFIC SECTION */
@Composable
private fun RoleSpecificSection(
    role: String,
    navController: NavHostController,
    foodViewModel: FoodViewModel
) {
    when (role.lowercase()) {
        "cook" -> CookManagementCard(navController, foodViewModel)
        "provider" -> ProviderManagementCard(navController)
        "admin" -> AdminQuickActionsCard(navController)
        else -> {}
    }
}

/* COOK MANAGEMENT CARD */
@Composable
private fun CookManagementCard(
    navController: NavHostController,
    foodViewModel: FoodViewModel
) {
    val cookOrders by foodViewModel.cookOrders.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            foodViewModel.loadCookOrders(it)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cook Dashboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge {
                    Text(cookOrders.size.toString())
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("food_seller_orders") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.RestaurantMenu, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Orders")
                }
                OutlinedButton(
                    onClick = { navController.navigate("restaurant_list") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Menu")
                }
            }
        }
    }
}

/* PROVIDER MANAGEMENT CARD */
@Composable
private fun ProviderManagementCard(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Provider Dashboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("provider_chat_list") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Message, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chats")
                }
                OutlinedButton(
                    onClick = { /* Navigate to bookings */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bookings")
                }
            }
        }
    }
}

/* ADMIN QUICK ACTIONS CARD */
@Composable
private fun AdminQuickActionsCard(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Admin Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { navController.navigate("admin_home") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Admin Panel", color = MaterialTheme.colorScheme.primaryContainer)
                }
            }
        }
    }
}

/* ACTIVITY SUMMARY CARD */
@Composable
private fun ActivitySummaryCard(
    orderCount: Int,
    savedCount: Int,
    navController: NavHostController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActivityStat(
                    icon = Icons.Outlined.ShoppingBag,
                    count = orderCount.toString(),
                    label = "Orders",
                    onClick = { navController.navigate("food_orders") }
                )
                ActivityStat(
                    icon = Icons.Outlined.FavoriteBorder,
                    count = "0",
                    label = "Likes",
                    onClick = { }
                )
                ActivityStat(
                    icon = Icons.Outlined.BookmarkBorder,
                    count = savedCount.toString(),
                    label = "Saved",
                    onClick = { navController.navigate("saved_items") }
                )
            }
        }
    }
}

/* ACTIVITY STAT */
@Composable
private fun ActivityStat(
    icon: ImageVector,
    count: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/* ACCOUNT MANAGEMENT SECTION */
@Composable
private fun AccountManagementSection(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            ListItem(
                headlineContent = { Text("Edit Profile") },
                leadingContent = {
                    Icon(Icons.Outlined.Person, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable { navController.navigate("edit_profile") }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            ListItem(
                headlineContent = { Text("Change Password") },
                leadingContent = {
                    Icon(Icons.Outlined.Lock, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable { navController.navigate("change_password") }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            ListItem(
                headlineContent = { Text("Notifications") },
                leadingContent = {
                    Icon(Icons.Outlined.Notifications, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = true,
                        onCheckedChange = { },
                        modifier = Modifier.scale(0.8f)
                    )
                }
            )
        }
    }
}

/* SUPPORT SECTION */
@Composable
private fun SupportSection(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            ListItem(
                headlineContent = { Text("Help Center") },
                leadingContent = {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable { navController.navigate("help_center") }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            ListItem(
                headlineContent = { Text("Privacy Policy") },
                leadingContent = {
                    Icon(Icons.Outlined.PrivacyTip, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable { navController.navigate("privacy_policy") }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            ListItem(
                headlineContent = { Text("Contact Support") },
                leadingContent = {
                    Icon(Icons.Outlined.ContactSupport, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable { navController.navigate("contact_support") }
            )
        }
    }
}

@Composable
private fun LogoutButton(navController: NavHostController, context: android.content.Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        TextButton(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("user_home") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Outlined.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Logout",
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}