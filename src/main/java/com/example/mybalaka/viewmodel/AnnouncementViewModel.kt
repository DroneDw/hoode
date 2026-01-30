package com.example.mybalaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybalaka.data.AnnouncementRepository
import com.example.mybalaka.model.Announcement
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.mybalaka.utils.isExpired

class AnnouncementViewModel : ViewModel() {

    private val repository = AnnouncementRepository
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        viewModelScope.launch {
            repository.getAnnouncements(currentUserId).collect { list ->
                _announcements.value = list.sortedWith(
                    compareBy<Announcement> { isExpired(it.expiresAt) }
                )
                _unreadCount.value = list.count { currentUserId !in it.readBy }
            }
        }
    }

    fun markAsRead(announcementId: String) {
        viewModelScope.launch {
            repository.markAsRead(announcementId, currentUserId)
        }
    }

    // ------------------ NEW FUNCTION ------------------
    fun addAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            repository.addAnnouncement(announcement)
        }
    }
}
