package com.example.mybalaka.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment  // ✅ FIXED: Add this import
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(navController: NavHostController) {
    val faqItems = listOf(
        FAQItem(
            question = "How do I order food?",
            answer = "Browse restaurants/shops, add items to cart, provide delivery details and payment info. Your order will be processed after payment verification."
        ),
        FAQItem(
            question = "How do I list a property?",
            answer = "Only admins can list properties. Contact the Balaka platform admin at +265 XXX XXX XXX to have your property listed."
        ),
        FAQItem(
            question = "How do I become a service provider?",
            answer = "Admins register service providers. Contact admin with your business details, certification, and service category."
        ),
        FAQItem(
            question = "How do I post an event?",
            answer = "Events can be posted by authorized users only. Contact the platform admin for event posting privileges."
        ),
        FAQItem(
            question = "What payment methods are accepted?",
            answer = "We accept Airtel Money and TNM Mpamba for all transactions. Cash on delivery is not available."
        ),
        FAQItem(
            question = "How do I track my orders?",
            answer = "Go to Profile > My Orders to see all your food orders with real-time status updates."
        ),
        FAQItem(
            question = "How do I report a problem?",
            answer = "Use the Contact Support button below to report any issues. Response time is within 24 hours."
        ),
        FAQItem(
            question = "Is my data secure?",
            answer = "Yes. We use Firebase authentication and encryption. Your personal information is never shared with third parties without consent."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help Center") },
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
            // Quick Actions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    HelpActionItem(
                        icon = Icons.Outlined.ContactSupport,
                        title = "Contact Support",
                        subtitle = "Get personalized help"
                    ) {
                        navController.navigate("contact_support")
                    }
                    Divider()
                    HelpActionItem(
                        icon = Icons.Outlined.PrivacyTip,
                        title = "Privacy Policy",
                        subtitle = "How we protect your data"
                    ) {
                        navController.navigate("privacy_policy")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // FAQ Section
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            FAQList(faqItems = faqItems)
        }
    }
}

data class FAQItem(
    val question: String,
    val answer: String
)

@Composable
private fun FAQList(faqItems: List<FAQItem>) {
    var expandedItem by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            faqItems.forEach { item ->
                FAQItem(
                    question = item.question,
                    answer = item.answer,
                    isExpanded = expandedItem == item.question,
                    onToggle = {
                        expandedItem = if (expandedItem == item.question) null else item.question
                    }
                )
                if (item != faqItems.last()) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun FAQItem(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand"
            )
        }
        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun HelpActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = title)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Go")
    }
}