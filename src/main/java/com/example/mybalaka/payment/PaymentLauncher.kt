package com.example.mybalaka.payment

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object PaymentLauncher {

    fun openCheckout(context: Context, checkoutUrl: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(
            context,
            Uri.parse(checkoutUrl)
        )
    }
}
