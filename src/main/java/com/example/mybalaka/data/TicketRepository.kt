package com.example.mybalaka.data

import com.example.mybalaka.model.Ticket
import com.google.firebase.Timestamp
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import java.util.UUID

object TicketRepository {

    private const val TICKETS_COLLECTION = "tickets"
    private const val PAYMENTS_COLLECTION = "payments"
    private val db = FirebaseFirestore.getInstance()

    // Data class for ticket statistics
    data class TicketStats(
        val eventId: String,
        val total: Int,
        val sold: Int,
        val remaining: Int
    )

    // Get all tickets for current user
    fun getMyTickets(): Flow<List<Ticket>> = callbackFlow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            close(Exception("User not logged in"))
            return@callbackFlow
        }

        val registration = db.collection(TICKETS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Ticket::class.java)
                } ?: emptyList()

                trySend(tickets)
            }

        awaitClose { registration.remove() }
    }

    // Get a specific ticket
    suspend fun getTicket(ticketId: String): Ticket? {
        val doc = db.collection(TICKETS_COLLECTION).document(ticketId).get().await()
        return doc.toObject(Ticket::class.java)
    }

    // Track payment status
    suspend fun getPaymentStatus(paymentId: String): String? {
        val doc = db.collection(PAYMENTS_COLLECTION).document(paymentId).get().await()
        return doc.getString("status")
    }

    // Create a ticket after successful payment
    suspend fun createTicketForPayment(
        paymentId: String,
        eventId: String,
        ticketTypeId: String,
        userId: String
    ): String {
        val ticketId = db.collection(TICKETS_COLLECTION).document().id
        val ticket = Ticket(
            id = ticketId,
            eventId = eventId,
            userId = userId,
            ticketTypeId = ticketTypeId,
            qrCode = "QR_${UUID.randomUUID()}",
            status = "active",
            createdAt = Timestamp.now()
        )
        db.collection(TICKETS_COLLECTION).document(ticketId).set(ticket).await()
        return ticketId
    }

    // Get ticket statistics for a specific event as Flow
    fun getTicketStatsFlow(eventId: String): Flow<TicketStats> = callbackFlow {
        val eventsRef = db.collection("events_balaka").document(eventId)
        val ticketsRef = db.collection(TICKETS_COLLECTION)
            .whereEqualTo("eventId", eventId)

        val eventListener = eventsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val ticketTypes = snapshot?.get("ticketTypes") as? List<Map<String, Any>>
            val total = ticketTypes?.sumOf { (it["quantity"] as? Long)?.toInt() ?: 0 } ?: 0

            ticketsRef.count().get(AggregateSource.SERVER)
                .addOnSuccessListener { countResult ->
                    val sold = countResult.count.toInt()
                    trySend(
                        TicketStats(
                            eventId = eventId,
                            total = total,
                            sold = sold,
                            remaining = total - sold
                        )
                    )
                }
                .addOnFailureListener {
                    trySend(TicketStats(eventId, total, 0, total))
                }
        }

        awaitClose { eventListener.remove() }
    }
}
