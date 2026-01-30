package com.example.mybalaka.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybalaka.data.UserRepository
import com.example.mybalaka.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ManageSellersViewModel : ViewModel() {
    private val userRepository = UserRepository
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers()
                .onStart { _isLoading.value = true }
                .catch { error ->
                    println("Error loading users: ${error.message}")
                    _isLoading.value = false
                }
                .collect { users ->
                    _allUsers.value = users
                    _isLoading.value = false
                }
        }
    }

    fun promoteUser(userId: String, newRole: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.updateUserRole(userId, newRole)
                onComplete(true)
            } catch (e: Exception) {
                println("Error promoting user: ${e.message}")
                onComplete(false)
            }
        }
    }
}
