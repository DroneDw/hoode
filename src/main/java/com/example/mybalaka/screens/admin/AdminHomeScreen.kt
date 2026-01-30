@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("OPT_IN_USAGE") // Suppresses the experimental API warning

package com.example.mybalaka.screens.admin

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mybalaka.model.*
import com.example.mybalaka.screens.admin.sections.*
import com.example.mybalaka.viewmodel.EventsViewModel
import com.example.mybalaka.viewmodel.FoodViewModel
import com.example.mybalaka.viewmodel.MarketViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

data class AdminDialogState(
    val showServiceDialog: Boolean = false,
    val showMarketDialog: Boolean = false,
    val showFoodDialog: Boolean = false,
    val showPropertyDialog: Boolean = false,
    val showEventDialog: Boolean = false,
    val showAnnouncementDialog: Boolean = false,
    val showCompanyMarketDialog: Boolean = false,
    val selectedProviderId: String = "",
    val selectedSellerId: String = "",
    val selectedOrganizerId: String = "",
    val selectedOrganizerName: String = ""
)

@Composable
fun AdminHomeScreen(navController: NavHostController) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var organizerName by remember { mutableStateOf("") }
    var organizerEmail by remember { mutableStateOf("") }
    var organizerPassword by remember { mutableStateOf("") }
    var organizerPhone by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    var dialogState by remember { mutableStateOf(AdminDialogState()) }

    var freshProviderId by remember { mutableStateOf("") }
    var selectedOrganizerId by remember { mutableStateOf("") }
    var selectedOrganizerName by remember { mutableStateOf("") }

    val marketViewModel: MarketViewModel = viewModel()
    val foodViewModel: FoodViewModel = viewModel()
    val eventsViewModel: EventsViewModel = viewModel()

    val allUsers = remember { mutableStateListOf<User>() }
    val allOrganizers = remember { mutableStateListOf<User>() }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        loadUsers(allUsers, allOrganizers, { isLoading = it }, { errorMessage = it })
    }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Admin Dashboard") }) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {

            /* ---------------- PROVIDERS ---------------- */
            ServicesSection(
                name, email, password, phone, isLoading,
                { name = it }, { email = it }, { password = it }, { phone = it }
            ) {
                if (validateInput(name, email, password, phone, { errorMessage = it })) {
                    registerUser(
                        "provider", name, email, password, phone, context,
                        { isLoading = it }, { errorMessage = it },
                        {
                            successMessage = "Provider registered - add service now"
                            resetForm({ name = "" }, { email = "" }, { password = "" }, { phone = "" })
                        },
                        {
                            freshProviderId = it
                            dialogState = dialogState.copy(showServiceDialog = true)
                        }
                    ) {
                        loadUsers(allUsers, allOrganizers, { isLoading = it }, { errorMessage = it })
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(24.dp))

            /* ---------------- ORGANIZERS ---------------- */
            OrganizersSection(
                organizerName, organizerEmail, organizerPassword, organizerPhone, isLoading,
                { organizerName = it },
                { organizerEmail = it },
                { organizerPassword = it },
                { organizerPhone = it }
            ) {
                if (validateInput(organizerName, organizerEmail, organizerPassword, organizerPhone, { errorMessage = it })) {
                    registerUser(
                        "organiser",
                        organizerName,
                        organizerEmail,
                        organizerPassword,
                        organizerPhone,
                        context,
                        { isLoading = it },
                        { errorMessage = it },
                        {
                            successMessage = "Organizer registered successfully!"
                            resetForm(
                                { organizerName = "" },
                                { organizerEmail = "" },
                                { organizerPassword = "" },
                                { organizerPhone = "" }
                            )
                        },
                        { }
                    ) {
                        loadUsers(allUsers, allOrganizers, { isLoading = it }, { errorMessage = it })
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(24.dp))

            /* ---------------- EVENTS ---------------- */
            EventsSection {
                dialogState = dialogState.copy(showEventDialog = true)
            }

            Spacer(Modifier.height(24.dp))

            /* ---------------- QUICK ADD BUTTONS ---------------- */
            Text("Quick Add Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { dialogState = dialogState.copy(showFoodDialog = true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Food")
                }
                Button(
                    onClick = { dialogState = dialogState.copy(showMarketDialog = true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Market")
                }
                Button(
                    onClick = { dialogState = dialogState.copy(showCompanyMarketDialog = true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Company Item")
                }
                Button(
                    onClick = { dialogState = dialogState.copy(showPropertyDialog = true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Property")
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { dialogState = dialogState.copy(showAnnouncementDialog = true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Announcement")
            }

            Spacer(Modifier.height(24.dp))

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            if (successMessage.isNotEmpty()) {
                Text(successMessage, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    /* ---------------- DIALOGS ---------------- */

    if (dialogState.showServiceDialog) {
        AddServiceDialog(
            onDismiss = { dialogState = dialogState.copy(showServiceDialog = false) },
            onConfirm = { service ->
                coroutineScope.launch {
                    try {
                        val serviceToSave = service.copy(providerId = freshProviderId)
                        FirebaseFirestore.getInstance().collection("services")
                            .add(serviceToSave)
                        Toast.makeText(context, "Service added!", Toast.LENGTH_SHORT).show()
                        successMessage = "Service added successfully!"
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to add service"
                    }
                }
                dialogState = dialogState.copy(showServiceDialog = false)
            }
        )
    }

    if (dialogState.showFoodDialog) {
        AddFoodItemDialog(
            cooks = allUsers.filter { it.role == "cook" || it.role == "provider" },
            onDismiss = { dialogState = dialogState.copy(showFoodDialog = false) },
            onConfirm = { foodItem ->
                coroutineScope.launch {
                    try {
                        foodViewModel.addFoodItem(foodItem)
                        Toast.makeText(context, "Food item added!", Toast.LENGTH_SHORT).show()
                        successMessage = "Food item added successfully!"
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to add food item"
                    }
                }
                dialogState = dialogState.copy(showFoodDialog = false)
            },
            showError = {
                Toast.makeText(context, "Please select a cook", Toast.LENGTH_SHORT).show()
            }
        )
    }
    if (dialogState.showMarketDialog) {
        AddMarketItemDialog(
            onDismiss = { dialogState = dialogState.copy(showMarketDialog = false) },
            onConfirm = { marketItem ->
                coroutineScope.launch {
                    try {
                        marketViewModel.addItem(marketItem)
                        Toast.makeText(context, "Market item added!", Toast.LENGTH_SHORT).show()
                        successMessage = "Market item added successfully!"
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to add market item"
                    }
                }
                dialogState = dialogState.copy(showMarketDialog = false)
            }
        )
    }
    /* ---------------- COMPANY MARKET DIALOG ---------------- */
    if (dialogState.showCompanyMarketDialog) {
        AddCompanyMarketItemDialog(
            onDismiss = { dialogState = dialogState.copy(showCompanyMarketDialog = false) },
            onConfirm = { marketItem ->
                coroutineScope.launch {
                    try {
                        marketViewModel.addItem(marketItem)
                        Toast.makeText(context, "Company item added!", Toast.LENGTH_SHORT).show()
                        successMessage = "Company item added successfully!"
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to add company item"
                    }
                }
                dialogState = dialogState.copy(showCompanyMarketDialog = false)
            }
        )
    }

    if (dialogState.showPropertyDialog) {
        AddPropertyDialog(
            users = allUsers,
            onDismiss = { dialogState = dialogState.copy(showPropertyDialog = false) },
            onConfirm = { property ->
                coroutineScope.launch {
                    try {
                        FirebaseFirestore.getInstance().collection("properties_balaka")
                            .add(property)
                        Toast.makeText(context, "Property listed!", Toast.LENGTH_SHORT).show()
                        successMessage = "Property listed successfully!"
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to list property"
                    }
                }
                dialogState = dialogState.copy(showPropertyDialog = false)
            }
        )
    }

    if (dialogState.showAnnouncementDialog) {
        AddAnnouncementDialog(
            onDismiss = { dialogState = dialogState.copy(showAnnouncementDialog = false) },
            onConfirm = { announcement ->
                coroutineScope.launch {
                    try {
                        FirebaseFirestore.getInstance().collection("announcements")
                            .add(announcement)
                        Toast.makeText(context, "Announcement added!", Toast.LENGTH_SHORT).show()
                        successMessage = "Announcement added successfully!"
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to add announcement"
                    }
                }
                dialogState = dialogState.copy(showAnnouncementDialog = false)
            }
        )
    }

    if (dialogState.showEventDialog) {
        AddEventDialog(
            organizerId = selectedOrganizerId,
            organizerName = selectedOrganizerName,
            allOrganizers = allOrganizers,
            onDismiss = { dialogState = dialogState.copy(showEventDialog = false) },
            onConfirm = { event ->
                coroutineScope.launch {
                    try {
                        eventsViewModel.addEvent(event)
                        Toast.makeText(context, "Event added!", Toast.LENGTH_SHORT).show()
                        successMessage = "Event added successfully!"
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to add event"
                    }
                }
                dialogState = dialogState.copy(showEventDialog = false)
            },
            onOrganizerSelected = { id, name ->
                selectedOrganizerId = id
                selectedOrganizerName = name
            }
        )
    }
}

/* ------------------- HELPER COMPOSABLES & FUNCTIONS ------------------- */

@Composable
fun UserInputFields(
    name: String,
    email: String,
    password: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Name") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChange,
        label = { Text("Phone") },
        modifier = Modifier.fillMaxWidth()
    )
}

fun validateInput(name: String, email: String, password: String, phone: String, setError: (String) -> Unit): Boolean {
    return when {
        name.isBlank() -> { setError("Name is required"); false }
        email.isBlank() -> { setError("Email is required"); false }
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { setError("Invalid email format"); false }
        password.length < 6 -> { setError("Password must be at least 6 characters"); false }
        phone.isBlank() -> { setError("Phone is required"); false }
        else -> true
    }
}

fun registerUser(
    role: String, name: String, email: String, password: String, phone: String,
    context: android.content.Context,
    isLoading: (Boolean) -> Unit,
    error: (String) -> Unit,
    success: () -> Unit,
    openDialog: (String) -> Unit,
    refreshUsers: () -> Unit
) {
    isLoading(true)
    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), password.trim())
        .addOnSuccessListener { authRes ->
            val uid = authRes.user?.uid ?: return@addOnSuccessListener
            val userMap = hashMapOf(
                "uid" to uid,
                "name" to name.trim(),
                "email" to email.trim(),
                "phone" to phone.trim(),
                "role" to role,
                "createdAt" to Timestamp.now()
            )
            FirebaseFirestore.getInstance().collection("users").document(uid).set(userMap)
                .addOnSuccessListener {
                    isLoading(false)
                    success()
                    refreshUsers()
                    openDialog(uid)
                }
                .addOnFailureListener { e ->
                    isLoading(false)
                    error(e.message ?: "Firestore error")
                }
        }
        .addOnFailureListener { e ->
            isLoading(false)
            error(e.message ?: "Auth error")
        }
}

fun loadUsers(
    allUsers: MutableList<User>,
    allOrganizers: MutableList<User>,
    setLoading: (Boolean) -> Unit,
    setError: (String) -> Unit
) {
    setLoading(true)
    FirebaseFirestore.getInstance().collection("users").get()
        .addOnSuccessListener { snapshot ->
            allUsers.clear()
            allOrganizers.clear()
            snapshot.documents.forEach { doc ->
                val user = User(
                    id = doc.id,
                    name = doc.getString("name") ?: "Unknown",
                    email = doc.getString("email") ?: "",
                    role = doc.getString("role") ?: "USER",
                    phone = doc.getString("phone") ?: "",
                    photoUrl = doc.getString("photoUrl")
                )
                allUsers.add(user)
                if (user.role.equals("organiser", ignoreCase = true)) {
                    allOrganizers.add(user)
                }
            }
            setLoading(false)
        }
        .addOnFailureListener { e ->
            setLoading(false)
            setError("Failed to load users: ${e.message}")
        }
}

fun resetForm(setName: () -> Unit, setEmail: () -> Unit, setPassword: () -> Unit, setPhone: () -> Unit) {
    setName()
    setEmail()
    setPassword()
    setPhone()
}