package com.example.mybalaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybalaka.data.PropertyRepository
import com.example.mybalaka.model.Property
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PropertyViewModel : ViewModel() {
    private val repository = PropertyRepository

    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    private val _filteredProperties = MutableStateFlow<List<Property>>(emptyList())
    val filteredProperties: StateFlow<List<Property>> = _filteredProperties.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedProperty = MutableStateFlow<Property?>(null)
    val selectedProperty: StateFlow<Property?> = _selectedProperty.asStateFlow()

    init {
        loadProperties()
    }

    private fun loadProperties() {
        viewModelScope.launch {
            repository.getAvailableProperties().collect { props ->
                _properties.value = props
                filterProperties(_selectedCategory.value)
            }
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        filterProperties(category)
    }

    private fun filterProperties(category: String) {
        _filteredProperties.value = if (category == "All") {
            _properties.value
        } else {
            _properties.value.filter { it.category == category }
        }
    }

    fun loadPropertyById(propertyId: String) {
        viewModelScope.launch {
            _selectedProperty.value = repository.getPropertyById(propertyId)
        }
    }

    fun addProperty(property: Property) {
        viewModelScope.launch { repository.addProperty(property) }
    }
}
