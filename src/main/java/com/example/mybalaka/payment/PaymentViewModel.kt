package com.example.mybalaka.payment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {

    private val repo = PaymentRepository()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    // ✅ NEW: Public method to set errors from UI
    fun setError(message: String) {
        errorMessage = message
    }

    fun pay(
        req: PayRequest,
        onCheckout: (String) -> Unit
    ) {
        // prevent double taps
        if (isLoading) return

        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null   // ✅ CLEAR OLD ERRORS

                val res = repo.startPayment(req)

                // success
                errorMessage = null  // ✅ EXTRA SAFETY
                onCheckout(res.checkoutUrl)

            } catch (e: Exception) {
                errorMessage = e.message ?: "Payment failed"
            } finally {
                isLoading = false
            }
        }
    }
}