package com.example.mybalaka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.mybalaka.navigation.BottomNavigationBar
import com.example.mybalaka.navigation.NavGraph
import com.example.mybalaka.ui.theme.MyBalakaTheme
import com.example.mybalaka.viewmodel.EventsViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EventsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paymentIdFromDeepLink =
            intent?.data
                ?.takeIf { it.scheme == "mybalaka" && it.host == "payment-success" }
                ?.getQueryParameter("paymentId")

        setContent {
            MyBalakaTheme {
                MainScreen(
                    viewModel = viewModel,
                    paymentId = paymentIdFromDeepLink
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: EventsViewModel,
    paymentId: String?
) {
    val navController = rememberNavController()

    var showLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showFailure by remember { mutableStateOf(false) }

    // Handle payment check safely in Compose
    LaunchedEffect(paymentId) {
        if (paymentId != null) {
            showLoading = true
            viewModel.checkPaymentStatus(paymentId) { success: Boolean ->
                showLoading = false
                if (success) showSuccess = true else showFailure = true
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavGraph(navController = navController)
        }
    }

    /* ---------- DIALOGS ---------- */
    if (showLoading) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Processing Payment") },
            text = { Text("Please wait while we confirm your payment…") }
        )
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            confirmButton = {
                TextButton(onClick = { showSuccess = false }) {
                    Text("OK")
                }
            },
            title = { Text("Payment Successful") },
            text = { Text("Your tickets have been purchased successfully.") }
        )
    }

    if (showFailure) {
        AlertDialog(
            onDismissRequest = { showFailure = false },
            confirmButton = {
                TextButton(onClick = { showFailure = false }) {
                    Text("OK")
                }
            },
            title = { Text("Payment Failed") },
            text = { Text("Your payment could not be processed. Please try again.") }
        )
    }
}
