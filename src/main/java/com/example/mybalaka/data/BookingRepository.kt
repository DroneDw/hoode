package com.example.mybalaka.data

import com.example.mybalaka.model.Booking
import com.example.mybalaka.model.Service
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object BookingRepository {

    private const val BOOKINGS = "bookings_balaka"
    private const val SERVICES = "services_balaka"
    private val db = FirebaseFirestore.getInstance()

    // customer creates booking
    suspend fun createBooking(service: Service, customerId: String) {
        val booking = Booking(
            serviceId  = service.id,
            customerId = customerId,
            providerId = service.providerId,
            status     = "pending",
            createdAt  = Timestamp.now(),
            updatedAt  = Timestamp.now()
        )

        db.collection(BOOKINGS).add(booking)

        // mark service unavailable
        db.collection(SERVICES)
            .document(service.id)
            .update(
                mapOf(
                    "isAvailable" to false,
                    "bookedBy" to customerId
                )
            )
    }

    // provider accepts/rejects booking
    suspend fun updateBookingStatus(bookingId: String, newStatus: String) {
        db.collection(BOOKINGS)
            .document(bookingId)
            .update(
                "status", newStatus,
                "updatedAt", Timestamp.now()
            )

        // if rejected → make service available again
        if (newStatus == "rejected") {
            val bookingSnap = db.collection(BOOKINGS)
                .document(bookingId)
                .get()
                .await()

            val serviceId = bookingSnap.getString("serviceId") ?: return

            db.collection(SERVICES)
                .document(serviceId)
                .update(
                    "isAvailable", true,
                    "bookedBy", FieldValue.delete()
                )
        }
    }

    // listen to bookings for a specific customer
    fun getCustomerBookings(customerId: String): Flow<List<Booking>> = callbackFlow {
        val reg = db.collection(BOOKINGS)
            .whereEqualTo("customerId", customerId)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snap?.toObjects(Booking::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    // listen to bookings for a specific provider
    fun getProviderBookings(providerId: String): Flow<List<Booking>> = callbackFlow {
        val reg = db.collection(BOOKINGS)
            .whereEqualTo("providerId", providerId)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snap?.toObjects(Booking::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }
}
