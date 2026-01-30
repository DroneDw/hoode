package com.example.mybalaka.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybalaka.data.EventRepository
import com.example.mybalaka.model.Event
import com.example.mybalaka.model.Property
import com.example.mybalaka.data.PropertyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.example.mybalaka.data.WeatherService

// ✅ NEW: Import repositories for real data
import com.example.mybalaka.data.MarketRepository
import com.example.mybalaka.data.FoodRepository

class HomeViewModel : ViewModel() {

    private val eventRepo = EventRepository
    private val propertyRepo = PropertyRepository

    // ✅ NEW: Real repositories
    private val marketRepo = MarketRepository
    private val foodRepo = FoodRepository

    data class WeatherUi(
        val temp: String = "--",
        val icon: String = "",
        val desc: String = ""
    )

    private val _weather = MutableStateFlow(WeatherUi())
    val weather: StateFlow<WeatherUi> = _weather.asStateFlow()

    /* ----------  properties  ---------- */
    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    /* ----------  user greeting  ---------- */
    var userName by mutableStateOf("")
        private set

    /* ----------  rotating billboard  ---------- */
    enum class Section { EVENTS, MARKET, FOOD, PROPERTIES }

    private val _section = MutableStateFlow(Section.EVENTS)
    val section: StateFlow<Section> = _section.asStateFlow()

    // ✅ NEW: Real data flows
    private val _marketItems = MutableStateFlow<List<MarketItem>>(emptyList())
    val marketItems: StateFlow<List<MarketItem>> = _marketItems.asStateFlow()

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    init {
        loadWeather()
        loadProperties()

        // ✅ NEW: Load real data
        loadMarketItems()
        loadFoodItems()

        val user = FirebaseAuth.getInstance().currentUser
        userName = user?.displayName ?: user?.email ?: "Balaka friend"
    }

    private fun loadWeather() = viewModelScope.launch {
        try {
            val dto = WeatherService.api.getBalakaNow()
            _weather.value = WeatherUi(
                temp = "${dto.current.temperature.toInt()}°C",
                icon = WeatherService.iconUrl(dto.current.weather_code),
                desc = shortDesc(dto.current.weather_code)
            )
        } catch (e: Exception) {
            _weather.value = WeatherUi("⚠️", "", "offline")
        }
    }

    private fun loadProperties() = viewModelScope.launch {
        propertyRepo.getAvailableProperties().collect { _properties.value = it }
    }

    // ✅ NEW: Load real market items
    private fun loadMarketItems() = viewModelScope.launch {
        marketRepo.getAvailableMarketItems()
            .map { items ->
                items.take(6).map { market ->
                    MarketItem(
                        name = market.title,
                        price = "K${market.price}",
                        imageUrl = market.imageUrl,
                        itemId = market.id
                    )
                }
            }
            .collect { _marketItems.value = it }
    }

    // ✅ NEW: Load real food items
    private fun loadFoodItems() = viewModelScope.launch {
        foodRepo.getAvailableFoodItems()
            .map { items ->
                items.take(6).map { food ->
                    FoodItem(
                        dish = food.name,
                        price = "K${food.price}",
                        imageUrl = food.imageUrl,
                        itemId = food.id
                    )
                }
            }
            .collect { _foodItems.value = it }
    }

    private fun shortDesc(code: Int): String = when (code) {
        0  -> "Clear sky"
        1,2,3 -> "Partly cloudy"
        45,48 -> "Foggy"
        51,53,55 -> "Drizzle"
        61,63,65 -> "Rain"
        71,73,75 -> "Snow"
        95,96,99 -> "Thunderstorm"
        else -> "Cloudy"
    }

    // Rotating section billboard
    init {
        viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                _section.value = when (_section.value) {
                    Section.EVENTS -> Section.MARKET
                    Section.MARKET -> Section.FOOD
                    Section.FOOD   -> Section.PROPERTIES
                    Section.PROPERTIES -> Section.EVENTS
                }
            }
        }
    }

    /* ----------  events (upcoming 3)  ---------- */
    val upcomingEvents: StateFlow<List<Event>> =
        eventRepo.getEvents()
            .map { it.sortedBy { ev -> ev.date }.take(3) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun refresh() {
        viewModelScope.launch {
            // Reload all data
            loadProperties()
            loadMarketItems()
            loadFoodItems()
        }
    }

    // ✅ UPDATED: Data classes for carousel display (simplified)
    data class MarketItem(
        val name: String,
        val price: String,
        val imageUrl: String,
        val itemId: String // For navigation
    )

    data class FoodItem(
        val dish: String,
        val price: String,
        val imageUrl: String,
        val itemId: String // For navigation
    )
}