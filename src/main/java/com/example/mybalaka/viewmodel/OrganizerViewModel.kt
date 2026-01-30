package com.example.mybalaka.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mybalaka.model.Event
import com.example.mybalaka.data.TicketRepository.TicketStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ✅ DEFINE ONCE — THIS IS THE ONLY PLACE
data class OrganizerEventWithStats(
    val event: Event,
    val stats: TicketStats
)

class OrganizerViewModel : ViewModel() {

    private val _eventsWithStats =
        MutableStateFlow<List<OrganizerEventWithStats>>(emptyList())
    val eventsWithStats: StateFlow<List<OrganizerEventWithStats>> =
        _eventsWithStats

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadOrganizerEvents()
    }

    private fun loadOrganizerEvents() {
        val organizerId = auth.currentUser?.uid ?: return

        db.collection("events_balaka")
            .whereArrayContains("organiserIds", organizerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val result = snapshot.documents.mapNotNull { doc ->
                    val event = doc.toObject(Event::class.java)
                        ?.copy(id = doc.id)
                        ?: return@mapNotNull null

                    val total = event.ticketTypes.sumOf { it.quantity }
                    val sold = event.ticketTypes.sumOf { it.sold }
                    val remaining = total - sold

                    OrganizerEventWithStats(
                        event = event,
                        stats = TicketStats(
                            eventId = event.id,
                            total = total,
                            sold = sold,
                            remaining = remaining
                        )
                    )
                }

                _eventsWithStats.value = result
                _isLoading.value = false
            }
    }
}
