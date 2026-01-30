package com.example.mybalaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybalaka.data.BookingRepository
import com.example.mybalaka.data.ChatRepository
import com.example.mybalaka.data.ServiceRepository
import com.example.mybalaka.model.Service
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServicesViewModel : ViewModel() {

    private val repo = ServiceRepository

    // Firebase current user
    private val currentUserId =
        FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"

    // All services (from Firestore)
    private val allServices: Flow<List<Service>> =
        repo.getServicesSortedByRating()

    /* -------------------- FIXED STATE -------------------- */

    // These MUST be StateFlow (not mutableStateOf)
    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow("All")

    /* -------------------- UI STATE -------------------- */

    val uiState: StateFlow<ServicesUiState> =
        combine(
            allServices,
            searchQuery,
            selectedCategory
        ) { services, query, category ->

            val filtered = services.filter { svc ->
                svc.isAvailable &&
                        (category == "All" || svc.category == category) &&
                        (
                                query.isBlank() ||
                                        svc.title.contains(query, ignoreCase = true) ||
                                        svc.description.contains(query, ignoreCase = true)
                                )
            }

            ServicesUiState(
                services = filtered,
                selectedCategory = category
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ServicesUiState()
        )

    /* -------------------- UI ACTIONS -------------------- */

    fun setSearch(q: String) {
        searchQuery.value = q
    }

    fun setCategory(c: String) {
        selectedCategory.value = c
    }

    fun addService(service: Service) {
        viewModelScope.launch {
            repo.addService(service)
        }
    }

    fun rateService(serviceId: String, userId: String, stars: Float) {
        viewModelScope.launch {
            repo.rateService(serviceId, userId, stars)
        }
    }

    /* -------------------- BOOKING -------------------- */

    fun createBooking(service: Service) {
        viewModelScope.launch {
            BookingRepository.createBooking(service, currentUserId)
        }
    }

    /* -------------------- CHAT -------------------- */

    fun startChatWithProvider(providerId: String, providerName: String) {
        viewModelScope.launch {
            val chatRoomId = ChatRepository.createOrGetChatRoom(
                currentUserId,
                providerId
            )
            println("✅ Chat room created with provider $providerName: $chatRoomId")
        }
    }
}

/* -------------------- UI STATE HOLDER -------------------- */

data class ServicesUiState(
    val services: List<Service> = emptyList(),
    val selectedCategory: String = "All"
)
