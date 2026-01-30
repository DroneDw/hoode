package com.example.mybalaka.data

import com.example.mybalaka.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object UserRepository {
    private const val USERS_COLLECTION = "users"
    private val db = FirebaseFirestore.getInstance()

    // Get all users for management
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val registration = db.collection(USERS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    User(
                        id = doc.id, // ✅ FIX
                        name = doc.getString("name") ?: "Unknown",
                        email = doc.getString("email") ?: "",
                        role = doc.getString("role") ?: "USER",
                        phone = doc.getString("phone") ?: "",
                        photoUrl = doc.getString("photoUrl")
                    )
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { registration.remove() }
    }

    // Get users by specific role (e.g., sellers, providers)
    fun getUsersByRole(role: String): Flow<List<User>> = callbackFlow {
        val registration = db.collection(USERS_COLLECTION)
            .whereEqualTo("role", role)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    User(
                        id = doc.id, // ✅ FIX
                        name = doc.getString("name") ?: "Unknown",
                        email = doc.getString("email") ?: "",
                        role = doc.getString("role") ?: "USER",
                        phone = doc.getString("phone") ?: "",
                        photoUrl = doc.getString("photoUrl")
                    )
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { registration.remove() }
    }

    // Update user role (e.g., make someone a seller/provider)
    suspend fun updateUserRole(userId: String, newRole: String) {
        try {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update("role", newRole)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Delete user from Firestore
    suspend fun deleteUser(userId: String) {
        try {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw e
        }
    }
}
