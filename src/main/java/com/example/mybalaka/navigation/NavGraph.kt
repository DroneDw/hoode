package com.example.mybalaka.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mybalaka.screens.*
import com.example.mybalaka.screens.auth.LoginScreen
import com.example.mybalaka.screens.auth.SignupScreen
import com.example.mybalaka.screens.admin.AdminHomeScreen
import com.example.mybalaka.screens.admin.ManageSellersScreen
import com.example.mybalaka.screens.admin.SellerRequestsScreen
import com.example.mybalaka.screens.home.HomeScreen
import com.example.mybalaka.screens.chat.*
import com.example.mybalaka.screens.market.*
import com.example.mybalaka.screens.organizer.OrganizerDashboardScreen
import com.example.mybalaka.screens.organizer.TicketScannerScreen
import com.example.mybalaka.screens.seller.BecomeSellerScreen
import com.example.mybalaka.viewmodel.FoodViewModel
import com.example.mybalaka.viewmodel.AnnouncementViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    val startDestination = if (currentUser == null) {
        "login"
    } else {
        "home"
    }

    val foodViewModel: FoodViewModel = viewModel()
    val announcementViewModel: AnnouncementViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        /* ---------- AUTH ---------- */
        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("signup") {
            SignupScreen(navController = navController)
        }

        /* ---------- MAIN HOME ---------- */
        composable("home") {
            HomeScreen(
                navController = navController,
                onNavigateToEvents = {
                    navController.navigate(BottomNavItem.Events.route)
                },
                onNavigateToTickets = {
                    navController.navigate("tickets")
                },
                onNavigateToEmergency = {
                    navController.navigate("emergency")
                }
            )
        }

        /* ---------- ROLE-BASED HOMES ---------- */
        composable("user_home") {
            ServicesScreen(navController = navController)
        }

        composable("admin_home") {
            AdminHomeScreen(navController = navController)
        }

        /* ---------- ORGANIZER ---------- */
        composable("organiser_dashboard") {
            OrganizerDashboardScreen(navController = navController)
        }

        composable("scanner") {
            TicketScannerScreen()
        }

        /* ---------- ANNOUNCEMENTS ---------- */
        composable("announcements") {
            AnnouncementsScreen(navController = navController)
        }

        composable("announcement_detail/{announcementId}") { backStackEntry ->
            val announcementId = backStackEntry.arguments?.getString("announcementId") ?: ""
            val viewModel: AnnouncementViewModel = viewModel()
            val announcements =
                viewModel.announcements.collectAsStateWithLifecycle(initialValue = emptyList())
            val announcement = announcements.value.find { it.id == announcementId }

            if (announcement != null) {
                AnnouncementDetailScreen(
                    navController = navController,
                    announcement = announcement,
                    onMarkAsRead = { viewModel.markAsRead(announcementId) }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Announcement not found")
                }
            }
        }

        /* ---------- BOTTOM NAV ---------- */
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                navController = navController,
                onNavigateToEvents = {
                    navController.navigate(BottomNavItem.Events.route)
                },
                onNavigateToTickets = {
                    navController.navigate("tickets")
                },
                onNavigateToEmergency = {
                    navController.navigate("emergency")
                }
            )
        }

        composable(BottomNavItem.Services.route) {
            ServicesScreen(navController = navController)
        }

        composable(BottomNavItem.Chat.route) {
            ChatListScreen(
                onNavigateToChatRoom = { chatRoom ->
                    val currentUserId =
                        FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val otherParticipantId =
                        chatRoom.getOtherParticipantId(currentUserId)
                    val otherParticipantName =
                        chatRoom.getOtherParticipantName(currentUserId)

                    navController.navigate(
                        "chat_room/${chatRoom.id}/$otherParticipantId/$otherParticipantName"
                    )
                },
                onNavigateToUserList = {
                    navController.navigate("user_list")
                }
            )
        }

        composable(BottomNavItem.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(BottomNavItem.More.route) {
            MoreScreen(navController = navController)
        }

        /* ---------- EVENTS / FOOD / MARKET ---------- */
        composable(BottomNavItem.Events.route) {
            EventsScreen()
        }

        composable(BottomNavItem.Food.route) {
            FoodScreen(
                navController = navController,
                viewModel = foodViewModel
            )
        }

        composable(BottomNavItem.Market.route) {
            MarketScreen(navController = navController)
        }

        composable(BottomNavItem.Properties.route) {
            PropertiesScreen(navController = navController)
        }

        /* ---------- PROFILE ---------- */
        composable("edit_profile") {
            EditProfileScreen(navController = navController)
        }

        composable("notification_settings") {
            NotificationSettingsScreen(navController = navController)
        }

        composable("change_password") {
            ChangePasswordScreen(navController = navController)
        }

        composable(BottomNavItem.Accommodation.route) {
            AccommodationScreen()
        }

        /* ---------- PROPERTY DETAILS ---------- */
        composable("property_details/{propertyId}") { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
            PropertyDetailsScreen(
                propertyId = propertyId,
                navController = navController
            )
        }

        /* ---------- HELP ---------- */
        composable("help_center") {
            HelpCenterScreen(navController = navController)
        }

        composable("privacy_policy") {
            PrivacyPolicyScreen(navController = navController)
        }

        composable("contact_support") {
            ContactSupportScreen(navController = navController)
        }

        /* ---------- FOOD FLOWS ---------- */
        composable("food_details/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            FoodDetailsScreen(
                itemId = itemId,
                navController = navController,
                viewModel = foodViewModel
            )
        }

        composable("food_cart") {
            FoodCartScreen(
                navController = navController,
                viewModel = foodViewModel
            )
        }

        composable("food_orders") {
            FoodOrdersScreen(
                navController = navController,
                viewModel = foodViewModel
            )
        }

        composable("food_seller_orders") {
            FoodSellerOrdersScreen(
                navController = navController,
                viewModel = foodViewModel
            )
        }

        composable("restaurant_list") {
            RestaurantListScreen(
                navController = navController,
                viewModel = foodViewModel
            )
        }

        /* ---------- SHOPS ---------- */
        composable("shop_list") {
            ShopListScreen(
                navController = navController,
                viewModel = foodViewModel
            )
        }

        composable("vendor_food/{cookId}/{vendorType}") { backStackEntry ->
            val cookId = backStackEntry.arguments?.getString("cookId") ?: ""
            val vendorType =
                backStackEntry.arguments?.getString("vendorType") ?: ""
            VendorFoodScreen(
                cookId = cookId,
                vendorType = vendorType,
                navController = navController,
                viewModel = foodViewModel
            )
        }

        /* ---------- CHAT ---------- */
        composable("user_list") {
            UserListScreen(
                onNavigateToChatRoom = { userId, userName ->
                    navController.navigate("chat_room_direct/$userId/$userName")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("chat_room/{chatRoomId}/{receiverId}/{receiverName}") { backStackEntry ->
            val chatRoomId =
                backStackEntry.arguments?.getString("chatRoomId") ?: ""
            val receiverId =
                backStackEntry.arguments?.getString("receiverId") ?: ""
            val receiverName =
                backStackEntry.arguments?.getString("receiverName") ?: ""
            ChatRoomScreen(
                chatRoomId = chatRoomId,
                receiverId = receiverId,
                receiverName = receiverName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("chat_room_direct/{receiverId}/{receiverName}") { backStackEntry ->
            val receiverId =
                backStackEntry.arguments?.getString("receiverId") ?: ""
            val receiverName =
                backStackEntry.arguments?.getString("receiverName") ?: ""
            val currentUserId =
                FirebaseAuth.getInstance().currentUser?.uid ?: ""

            val (firstId, secondId) =
                if (currentUserId < receiverId) currentUserId to receiverId
                else receiverId to currentUserId
            val chatRoomId = "${firstId}_${secondId}"

            ChatRoomScreen(
                chatRoomId = chatRoomId,
                receiverId = receiverId,
                receiverName = receiverName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        /* ---------- PROVIDER CHAT ---------- */
        composable("provider_chat_list") {
            ProviderChatListScreen(
                navController = navController
            )
        }

        /* ---------- MARKETPLACE DETAILS ---------- */
        composable("market_details/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            MarketDetailsScreen(
                itemId = itemId,
                navController = navController
            )
        }

        /* ---------- SELLER MANAGEMENT ---------- */
        composable("manage_sellers") {
            ManageSellersScreen(navController = navController)
        }

        /* ---------- NEW: SELLER FLOW ---------- */
        composable("become_seller") {
            BecomeSellerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("seller_requests") {
            SellerRequestsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        /* ---------- TICKETS ---------- */
        composable("my_tickets") {
            MyTicketsScreen()
        }
    }
}