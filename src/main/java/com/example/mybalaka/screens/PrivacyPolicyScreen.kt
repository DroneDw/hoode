package com.example.mybalaka.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavHostController) {
    val lastUpdated = "22 December 2025"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Balaka District Platform",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Last Updated: $lastUpdated",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            PolicySection(
                title = "1. Introduction",
                content = "Welcome to Balaka District Platform. We are committed to protecting your personal information and your right to privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application."
            )

            PolicySection(
                title = "2. Information We Collect",
                content = "We collect personal information that you provide to us:\n\n• Account Information: Name, email, phone number, profile photo\n• Transaction Data: Orders, payments, delivery addresses\n• Content: Event postings, chat messages, reviews\n• Usage Data: App interactions, preferences, saved items\n• Device Information: Device ID, operating system for security\n\nWe do NOT collect sensitive personal data such as ID numbers, bank accounts, or location tracking without explicit consent."
            )

            PolicySection(
                title = "3. How We Use Your Information",
                content = "We use your information to:\n\n• Process and deliver your food orders\n• Connect you with service providers\n• Display relevant events and properties\n• Enable communication through chat\n• Personalize your app experience\n• Send important updates about your transactions\n• Improve our services and security\n\nWe never sell your personal data to third parties."
            )

            PolicySection(
                title = "4. Data Sharing",
                content = "We only share your information with:\n\n• Food vendors (for order fulfillment)\n• Service providers (when you book services)\n• Property sellers (when you inquire)\n• Payment processors (Airtel Money, TNM Mpamba)\n• Law enforcement (when legally required)\n\nAll data sharing is limited to what is necessary for transaction completion."
            )

            PolicySection(
                title = "5. Data Security",
                content = "We implement appropriate technical and organizational security measures:\n\n• Firebase authentication and encryption\n• Secure cloud storage\n• Regular security audits\n• Limited staff access to personal data\n• Encrypted data transmission\n\nHowever, no method of transmission over the internet is 100% secure. We strive to protect your data but cannot guarantee absolute security."
            )

            PolicySection(
                title = "6. Your Rights",
                content = "You have the right to:\n\n• Access and update your personal information\n• Delete your account and data\n• Export your data\n• Opt-out of non-essential communications\n• Request data usage explanation\n\nContact us at support@balakadistrict.mw to exercise these rights."
            )

            PolicySection(
                title = "7. Data Retention",
                content = "We retain your information only as long as necessary:\n\n• Active accounts: Until you delete your account\n• Order history: 2 years for tax/legal purposes\n• Chat messages: 6 months\n• Deleted accounts: 30 days before permanent deletion\n\nYou can request early deletion by contacting support."
            )

            PolicySection(
                title = "8. Updates to This Policy",
                content = "We may update this Privacy Policy from time to time. You will be notified of any changes via in-app notification. Continued use of the app after changes constitutes acceptance of the updated policy."
            )

            PolicySection(
                title = "9. Contact Us",
                content = "For privacy-related questions:\n\nEmail: privacy@balakadistrict.mw\nPhone: +265 123 456 789\nAddress: Balaka District Council Office, Balaka, Malawi\n\nWe respond to all privacy inquiries within 7 business days."
            )
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}