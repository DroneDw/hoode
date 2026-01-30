package com.example.mybalaka.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// ---------- Services Section ----------
@Composable
fun ServicesSection(
    name: String,
    email: String,
    password: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    isLoading: Boolean,
    onRegisterClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Services Management", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            UserInputFields(name, email, password, phone, onNameChange, onEmailChange, onPasswordChange, onPhoneChange)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Register Provider")
                }
            }
        }
    }
}

// ---------- Organizers Section ----------
@Composable
fun OrganizersSection(
    name: String,
    email: String,
    password: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    isLoading: Boolean,
    onRegisterClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Event Organizers Management", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            UserInputFields(name, email, password, phone, onNameChange, onEmailChange, onPasswordChange, onPhoneChange)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Register Event Organizer")
                }
            }
        }
    }
}

// ---------- Food Section ----------
@Composable
fun FoodSection(
    name: String,
    email: String,
    password: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    isLoading: Boolean,
    onRegisterCookClick: () -> Unit,
    onAddFoodItemClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Food Management", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            UserInputFields(name, email, password, phone, onNameChange, onEmailChange, onPasswordChange, onPhoneChange)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRegisterCookClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Register Cook")
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAddFoodItemClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Food Item")
            }
        }
    }
}

// ---------- Marketplace Section ----------
@Composable
fun MarketplaceSection(
    navController: NavHostController,
    onAddMarketItemClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Marketplace Management", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate("manage_sellers") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Product Sellers")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onAddMarketItemClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Market Item")
            }
        }
    }
}

// ---------- Properties Section ----------
@Composable
fun PropertiesSection(onAddPropertyClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Properties Management", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAddPropertyClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("List New Property")
            }
        }
    }
}

// ---------- Events Section ----------
@Composable
fun EventsSection(onAddEventClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Events Management", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAddEventClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Event")
            }
        }
    }
}

// ---------- Announcements Section ----------
@Composable
fun AnnouncementsSection(navController: NavHostController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Announcements Management", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate("admin_add_announcement") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add New Announcement")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("announcements") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View All Announcements")
            }
        }
    }
}