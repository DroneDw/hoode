package com.example.mybalaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class SavedItemsViewModel : ViewModel() {

    fun toggleSavedProperty(propertyId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userRef.get().addOnSuccessListener { doc ->
                val saved = (doc.get("savedProperties") as? List<String>)?.toMutableList() ?: mutableListOf()
                if (saved.contains(propertyId)) {
                    saved.remove(propertyId)
                } else {
                    saved.add(propertyId)
                }

                userRef.update("savedProperties", saved)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
        }
    }

    fun toggleSavedFood(foodId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userRef.get().addOnSuccessListener { doc ->
                val saved = (doc.get("savedFood") as? List<String>)?.toMutableList() ?: mutableListOf()
                if (saved.contains(foodId)) {
                    saved.remove(foodId)
                } else {
                    saved.add(foodId)
                }

                userRef.update("savedFood", saved)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
        }
    }
}