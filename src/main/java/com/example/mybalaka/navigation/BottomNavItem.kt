package com.example.mybalaka.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // Primary items (shown in bottom nav)
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Services : BottomNavItem("services", "Services", Icons.Default.Build)
    object Chat : BottomNavItem("chat_list", "Chat", Icons.Default.ChatBubbleOutline)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
    object More : BottomNavItem("more", "More", Icons.Default.Menu)

    // Secondary items (accessed via More screen)
    object Events : BottomNavItem("events", "Events", Icons.Default.Event)
    object Food : BottomNavItem("food", "Food", Icons.Default.Fastfood)
    object Market : BottomNavItem("market", "Market", Icons.Default.ShoppingBag)
    object Properties : BottomNavItem("properties", "Properties", Icons.Default.House)
    object Accommodation : BottomNavItem("accommodation", "Stay", Icons.Default.Hotel)

    // New Food routes
    object FoodOrders : BottomNavItem("food_orders", "My Orders", Icons.Default.ShoppingCart)
    object FoodCart : BottomNavItem("food_cart", "Cart", Icons.Default.ShoppingCart)
    object FoodDetails : BottomNavItem("food_details/{itemId}", "Food Details", Icons.Default.Fastfood)
    object FoodSellerOrders : BottomNavItem("food_seller_orders", "Food Orders", Icons.Default.RestaurantMenu)
}
