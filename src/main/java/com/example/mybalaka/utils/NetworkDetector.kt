// com/example/mybalaka/utils/NetworkDetector.kt
package com.example.mybalaka.utils

object NetworkDetector {

    fun detectNetwork(phone: String): String? {
        val cleanPhone = phone.replace("\\s".toRegex(), "")

        return when {
            cleanPhone.startsWith("099") ||
                    cleanPhone.startsWith("0995") ||
                    cleanPhone.startsWith("088") -> "AIRTEL"

            cleanPhone.startsWith("088") ||
                    cleanPhone.startsWith("087") ||
                    cleanPhone.startsWith("089") -> "TNM"

            else -> null
        }
    }

    fun formatPhoneForPayChangu(phone: String): String {
        return phone.replace("\\s".toRegex(), "")
    }

    fun isValidTestNumber(phone: String): Boolean {
        val cleanPhone = formatPhoneForPayChangu(phone)
        // PayChangu test numbers
        return cleanPhone in listOf(
            "0999123456",
            "0888123456",
            "0995000000",
            "0777000000"
        )
    }
}