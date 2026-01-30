package com.example.mybalaka.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybalaka.data.MarketRepository
import com.example.mybalaka.model.MarketItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.content.ClipData
import android.content.ClipboardManager
import kotlinx.coroutines.channels.awaitClose

class MarketViewModel : ViewModel() {
    private val repository = MarketRepository
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val db = FirebaseFirestore.getInstance()
    private val MARKET_ITEMS = "market_items"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val categories = listOf("All", "Electronics", "Clothing", "Food", "Other")

    private val _marketItems = MutableStateFlow<List<MarketItem>>(emptyList())
    val marketItems: StateFlow<List<MarketItem>> = _marketItems.asStateFlow()

    init {
        loadMarketItems()
    }

    fun loadMarketItems() {
        viewModelScope.launch {
            repository.getAvailableMarketItems()
                .collect { items ->
                    _marketItems.value = items
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    // ✅ FIXED: Real-time updates for detail screen
    fun getMarketItemById(itemId: String): Flow<MarketItem?> = callbackFlow {
        val registration = db.collection(MARKET_ITEMS).document(itemId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val item = snapshot?.toObject(MarketItem::class.java)
                trySend(item)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addMarketItem(item: MarketItem): String {
        return repository.addMarketItem(item)
    }

    // ✅ NEW: Convenience method for Admin screen (FIXES YOUR ERROR)
    fun addItem(item: MarketItem) {
        viewModelScope.launch {
            repository.addMarketItem(item)
            loadMarketItems() // Refresh the list
        }
    }

    // ✅ NEW: Update existing market item (FIXED to use HashMap)
    fun updateMarketItem(updatedItem: MarketItem) {
        viewModelScope.launch {
            try {
                val data = hashMapOf(
                    "title" to updatedItem.title,
                    "description" to updatedItem.description,
                    "price" to updatedItem.price,
                    "category" to updatedItem.category,
                    "imageUrl" to updatedItem.imageUrl,
                    "sellerId" to updatedItem.sellerId,
                    "sellerName" to updatedItem.sellerName,
                    "phone" to updatedItem.phone,
                    "whatsapp" to updatedItem.whatsapp,
                    "location" to updatedItem.location,
                    "isAvailable" to updatedItem.isAvailable,
                    "approved" to updatedItem.approved,
                    "createdAt" to updatedItem.createdAt
                )

                db.collection(MARKET_ITEMS).document(updatedItem.id).update(data.toMap()).await()
                loadMarketItems() // Refresh the list
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Contact", text)
        clipboard.setPrimaryClip(clip)
    }

    val filteredItems: StateFlow<List<MarketItem>> = combine(
        _marketItems,
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            (category == "All" || item.category == category) &&
                    (query.isBlank() || item.title.contains(query, ignoreCase = true) ||
                            item.description.contains(query, ignoreCase = true))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ✅ HELPER: Format price in MK
    fun formatPriceMK(price: Float): String = "MK${price.toInt()}"
}
