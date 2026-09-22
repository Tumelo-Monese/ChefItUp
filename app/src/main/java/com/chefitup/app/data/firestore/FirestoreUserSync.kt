package com.chefitup.app.data.firestore

import com.chefitup.app.domain.model.UserGamification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud backup for profile preferences and gamification.
 * Local Room remains source of truth while offline; this pushes when online.
 */
@Singleton
class FirestoreUserSync @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun pushProfile(
        name: String,
        surname: String,
        username: String,
        phone: String,
        diet: String,
        allergies: List<String>,
        languageCode: String,
        theme: String
    ) {
        val user = firebaseAuth.currentUser ?: return
        val data = mapOf(
            "name" to name,
            "surname" to surname,
            "username" to username,
            "phone" to phone,
            "email" to (user.email.orEmpty()),
            "diet" to diet,
            "allergies" to allergies,
            "languageCode" to languageCode,
            "theme" to theme,
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(user.uid)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun pushGamification(state: UserGamification) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val data = mapOf(
            "xp" to state.xp,
            "level" to state.level.name,
            "recipesCompleted" to state.recipesCompleted,
            "unlockedBadges" to state.earnedBadgeIds,
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(uid)
            .collection("meta").document("gamification")
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun pushFavourite(recipeId: String, isFavourite: Boolean) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val doc = firestore.collection("users").document(uid)
            .collection("favourites").document(recipeId)
        if (isFavourite) {
            doc.set(
                mapOf(
                    "recipeId" to recipeId,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        } else {
            doc.delete().await()
        }
    }
}
