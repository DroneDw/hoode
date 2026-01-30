package com.example.mybalaka.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybalaka.data.FoodRepository
import com.example.mybalaka.model.FoodItem
import com.example.mybalaka.model.FoodOrder
import com.example.mybalaka.model.OrderItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.ClipData
import android.content.ClipboardManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

class FoodViewModel : ViewModel() {
    private val repository = FoodRepository
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val db = FirebaseFirestore.getInstance()
    private val FOOD_ITEMS = "food_items_balaka"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedVendorType = MutableStateFlow("restaurant")
    val selectedVendorType: StateFlow<String> = _selectedVendorType.asStateFlow()

    val categories = listOf("All", "Fast Food", "Traditional", "Drinks", "Snacks", "Desserts")

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _cartValidationMessage = MutableStateFlow<String?>(null)
    val cartValidationMessage: StateFlow<String?> = _cartValidationMessage.asStateFlow()

    private val _customerOrders = MutableStateFlow<List<FoodOrder>>(emptyList())
    val customerOrders: StateFlow<List<FoodOrder>> = _customerOrders.asStateFlow()

    private val _cookOrders = MutableStateFlow<List<FoodOrder>>(emptyList())
    val cookOrders: StateFlow<List<FoodOrder>> = _cookOrders.asStateFlow()

    init {
        loadFoodItems()
    }

    fun loadFoodItems() {
        viewModelScope.launch {
            repository.getAvailableFoodItems()
                .collect { items ->
                    _foodItems.value = items
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setVendorType(type: String) {
        _selectedVendorType.value = type
    }

    fun getVendorsByType(vendorType: String): Flow<List<FoodItem>> = callbackFlow {
        val registration = db.collection(FOOD_ITEMS)
            .whereEqualTo("vendorType", vendorType)
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        FoodItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price")?.toFloat() ?: 0f,
                            category = doc.getString("category") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            cookId = doc.getString("cookId") ?: "",
                            cookName = doc.getString("cookName") ?: "",
                            phone = doc.getString("phone") ?: "",
                            whatsapp = doc.getString("whatsapp") ?: "",
                            preparationTime = doc.getString("preparationTime") ?: "",
                            isAvailable = doc.getBoolean("isAvailable") ?: true,
                            createdAt = doc.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now(),
                            vendorType = doc.getString("vendorType") ?: "restaurant",
                            isNew = doc.getBoolean("isNew") ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                val vendors = items.groupBy { it.cookId }.map { it.value.first() }
                trySend(vendors)
            }
        awaitClose { registration.remove() }
    }

    fun getFoodByVendor(cookId: String, vendorType: String): Flow<List<FoodItem>> = callbackFlow {
        val registration = db.collection(FOOD_ITEMS)
            .whereEqualTo("cookId", cookId)
            .whereEqualTo("vendorType", vendorType)
            .whereEqualTo("isAvailable", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        FoodItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price")?.toFloat() ?: 0f,
                            category = doc.getString("category") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            cookId = doc.getString("cookId") ?: "",
                            cookName = doc.getString("cookName") ?: "",
                            phone = doc.getString("phone") ?: "",
                            whatsapp = doc.getString("whatsapp") ?: "",
                            preparationTime = doc.getString("preparationTime") ?: "",
                            isAvailable = doc.getBoolean("isAvailable") ?: true,
                            createdAt = doc.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now(),
                            vendorType = doc.getString("vendorType") ?: "restaurant",
                            isNew = doc.getBoolean("isNew") ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun loadCustomerOrders() {
        viewModelScope.launch {
            repository.getCustomerOrders(currentUserId)
                .collect { orders ->
                    _customerOrders.value = orders
                }
        }
    }

    fun loadCookOrders(cookId: String) {
        viewModelScope.launch {
            repository.getCookOrders(cookId)
                .collect { orders ->
                    _cookOrders.value = orders
                }
        }
    }

    fun addToCart(foodItem: FoodItem, quantity: Int = 1): String? {
        val currentCart = _cartItems.value.toMutableList()

        if (currentCart.isNotEmpty()) {
            val existingItem = _foodItems.value.find { it.id == currentCart.first().itemId }
            if (existingItem != null && foodItem.cookId != existingItem.cookId) {
                return "Cannot add from different vendor. Finish current order first."
            }
        }

        val index = currentCart.indexOfFirst { it.itemId == foodItem.id }

        if (index >= 0) {
            val existingItem = currentCart[index]
            currentCart[index] = existingItem.copy(
                quantity = existingItem.quantity + quantity
            )
        } else {
            currentCart.add(
                CartItem(
                    itemId = foodItem.id,
                    name = foodItem.name,
                    price = foodItem.price,
                    quantity = quantity
                )
            )
        }

        _cartItems.value = currentCart
        _cartValidationMessage.value = null
        return null
    }

    fun removeFromCart(itemId: String) {
        _cartItems.value = _cartItems.value.filter { it.itemId != itemId }
        _cartValidationMessage.value = null
    }

    fun updateCartItemQuantity(itemId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(itemId)
            return
        }

        _cartItems.value = _cartItems.value.map {
            if (it.itemId == itemId) it.copy(quantity = quantity) else it
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartValidationMessage.value = null
    }

    fun addFoodItem(item: FoodItem) {
        viewModelScope.launch {
            repository.addFoodItem(item)
        }
    }

    fun updateFoodItem(updatedItem: FoodItem) {
        viewModelScope.launch {
            try {
                db.collection(FOOD_ITEMS).document(updatedItem.id).set(updatedItem).await()
                loadFoodItems()
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    suspend fun createOrder(
        cookId: String,
        deliveryAddress: String,
        notes: String,
        senderName: String,
        amountSent: Float,
        paymentMethod: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val cart = _cartItems.value
            if (cart.isEmpty()) {
                onError("Cart is empty")
                return
            }

            val cartCookIds = cart.mapNotNull { item ->
                _foodItems.value.find { it.id == item.itemId }?.cookId
            }.distinct()

            if (cartCookIds.size > 1) {
                onError("Cart contains items from multiple vendors")
                return
            }

            val cookItem = _foodItems.value.find { it.id == cart.first().itemId }
            if (cookItem == null) {
                onError("Could not find vendor details")
                return
            }

            val orderItems = cart.map {
                OrderItem(
                    itemId = it.itemId,
                    name = it.name,
                    quantity = it.quantity,
                    price = it.price
                )
            }

            val totalAmount = cart.sumOf { it.totalPrice.toDouble() }.toFloat()

            val order = FoodOrder(
                customerId = currentUserId,
                customerName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Customer",
                customerPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "",
                cookId = cookItem.cookId,
                cookName = cookItem.cookName,
                items = orderItems,
                totalAmount = totalAmount,
                deliveryAddress = deliveryAddress,
                notes = notes,
                senderName = senderName,
                amountSent = amountSent,
                paymentMethod = paymentMethod,
                paymentReceived = false
            )

            repository.createOrder(order)
            clearCart()
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Failed to create order")
        }
    }

    fun markPaymentReceived(orderId: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderPaymentStatus(orderId, true)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Contact", text)
        clipboard.setPrimaryClip(clip)
    }

    fun showCartError(message: String) {
        _cartValidationMessage.value = message
    }

    val currentCartCookName: String?
        get() = _cartItems.value.firstOrNull()?.let { item ->
            _foodItems.value.find { it.id == item.itemId }?.cookName
        }

    val currentCartCookPhone: String?
        get() = _cartItems.value.firstOrNull()?.let { item ->
            _foodItems.value.find { it.id == item.itemId }?.phone
        }

    val filteredItemsByVendor: StateFlow<List<FoodItem>> = combine(
        _foodItems,
        _searchQuery,
        _selectedCategory,
        _selectedVendorType
    ) { items, query, category, vendorType ->
        items.filter { item ->
            (item.vendorType == vendorType) &&
                    (category == "All" || item.category == category) &&
                    (query.isBlank() || item.name.contains(query, ignoreCase = true) ||
                            item.description.contains(query, ignoreCase = true))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredItems: StateFlow<List<FoodItem>> = combine(
        _foodItems,
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            (category == "All" || item.category == category) &&
                    (query.isBlank() || item.name.contains(query, ignoreCase = true) ||
                            item.description.contains(query, ignoreCase = true))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    data class CartItem(
        val itemId: String,
        val name: String,
        val price: Float,
        val quantity: Int
    ) {
        val totalPrice: Float
            get() = price * quantity
    }
}