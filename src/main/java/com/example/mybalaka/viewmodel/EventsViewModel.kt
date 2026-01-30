package com.example.mybalaka.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybalaka.data.EventRepository
import com.example.mybalaka.data.TicketRepository
import com.example.mybalaka.model.Event
import com.example.mybalaka.model.EventComment
import com.example.mybalaka.model.Ticket
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

class EventsViewModel : ViewModel() {

    private val repo = EventRepository
    private val storage = FirebaseStorage.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    /* ---------- filtering ---------- */
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    val categories = listOf("All", "Music", "Football", "Church", "Wedding", "Other")
    private val _selectedCat = MutableStateFlow("All")
    val selectedCat: StateFlow<String> = _selectedCat.asStateFlow()
    fun selectCat(cat: String) { _selectedCat.value = cat }

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getEvents().collect { list ->
                _events.value = list
            }
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(800)
            _isRefreshing.value = false
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch { repo.addEvent(event) }
    }

    fun hasLiked(event: Event, userId: String): Boolean =
        event.likedBy.contains(userId)

    fun toggleLikeEvent(eventId: String, userId: String) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)
            val eventRef = db.collection("events_balaka").document(eventId)

            db.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                val eventDoc = transaction.get(eventRef)

                val likedEvents =
                    (userDoc.get("likedEvents") as? List<String>)?.toMutableList() ?: mutableListOf()
                val likedBy =
                    (eventDoc.get("likedBy") as? List<String>)?.toMutableList() ?: mutableListOf()
                val likes = eventDoc.getLong("likes")?.toInt() ?: 0

                val isLiked = likedEvents.contains(eventId)

                if (isLiked) {
                    likedEvents.remove(eventId)
                    likedBy.remove(userId)
                    transaction.update(eventRef, "likes", likes - 1)
                } else {
                    likedEvents.add(eventId)
                    likedBy.add(userId)
                    transaction.update(eventRef, "likes", likes + 1)
                }

                transaction.update(userRef, "likedEvents", likedEvents)
                transaction.update(eventRef, "likedBy", likedBy)

                _events.update { list ->
                    list.map {
                        if (it.id == eventId)
                            it.copy(
                                likes = if (isLiked) likes - 1 else likes + 1,
                                likedBy = likedBy
                            )
                        else it
                    }
                }
            }
        }
    }

    fun addComment(eventId: String, text: String) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .get()
                .await()

            val comment = EventComment(
                authorId = user.uid,
                authorName = userDoc.getString("name") ?: "Anonymous",
                text = text.trim(),
                timestamp = Timestamp.now()
            )

            repo.commentOnEvent(eventId, comment)
        }
    }

    /* ---------- REAL-TIME EVENT (FIXED WITHOUT getEventStream) ---------- */

    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            events.collect { list ->
                _selectedEvent.value = list.find { it.id == eventId }
            }
        }
    }

    /* ---------- storage ---------- */
    fun uploadPoster(uri: Uri, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val ref = storage.reference.child("posters/${UUID.randomUUID()}.jpg")
            ref.putFile(uri)
                .continueWithTask { ref.downloadUrl }
                .addOnSuccessListener { onResult(it.toString()) }
        }
    }

    /* ---------- tickets ---------- */

    private val _myTickets = MutableStateFlow<List<Ticket>>(emptyList())
    val myTickets: StateFlow<List<Ticket>> = _myTickets.asStateFlow()

    fun loadMyTickets() {
        viewModelScope.launch {
            TicketRepository.getMyTickets().collect {
                _myTickets.value = it
            }
        }
    }

    fun checkPaymentStatus(paymentId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repeat(30) {
                when (TicketRepository.getPaymentStatus(paymentId)) {
                    "success" -> return@launch onComplete(true)
                    "failed" -> return@launch onComplete(false)
                }
                delay(2000)
            }
            onComplete(false)
        }
    }
}
