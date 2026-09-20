package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.RegistrationItem
import com.example.data.model.RegistrationStatus
import com.example.data.model.TournamentItem
import com.example.data.model.TournamentStatus
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private const val COLLECTION_TOURNAMENTS = "tournaments"
    private const val COLLECTION_REGISTRATIONS = "registrations"

    /**
     * Checks if Firebase is properly initialized with google-services.json configuration.
     */
    fun isFirebaseConfigured(context: Context): Boolean {
        return try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context) != null
            } else {
                true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not initialized: ${e.message}")
            false
        }
    }

    fun getAuth(context: Context): FirebaseAuth? {
        return if (isFirebaseConfigured(context)) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FirebaseAuth", e)
                null
            }
        } else {
            null
        }
    }

    fun getFirestore(context: Context): FirebaseFirestore? {
        return if (isFirebaseConfigured(context)) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FirebaseFirestore", e)
                null
            }
        } else {
            null
        }
    }

    /**
     * Authenticate Admin securely using Firebase Authentication (Email/Password).
     * No admin passwords or secret credentials are ever hardcoded in the client.
     */
    suspend fun signInAdmin(context: Context, email: String, pass: String): Result<FirebaseUser> {
        val auth = getAuth(context)
            ?: return Result.failure(
                Exception(
                    "Firebase Authentication is not yet configured. Please add your google-services.json to the app/ directory and enable Email/Password Authentication in the Firebase Console."
                )
            )

        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication succeeded but no user details returned."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Admin sign-in failed", e)
            Result.failure(e)
        }
    }

    fun signOutAdmin(context: Context) {
        try {
            getAuth(context)?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out", e)
        }
    }

    fun getCurrentAdmin(context: Context): FirebaseUser? {
        return try {
            getAuth(context)?.currentUser
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save registration document to Firestore.
     */
    suspend fun saveRegistration(context: Context, item: RegistrationItem): Result<Unit> {
        val firestore = getFirestore(context) ?: return Result.success(Unit) // Offline/Local-first fallback
        return try {
            val docRef = firestore.collection(COLLECTION_REGISTRATIONS).document(item.id)
            val data = mapOf(
                "id" to item.id,
                "tournamentId" to item.tournamentId,
                "tournamentTime" to item.tournamentTime,
                "playerName" to item.playerName,
                "ffIgn" to item.ffIgn,
                "ffUid" to item.ffUid,
                "contactNumber" to item.contactNumber,
                "teamName" to item.teamName,
                "selectedSlot" to item.selectedSlot,
                "entryFee" to item.entryFee,
                "paymentRef" to item.paymentRef,
                "paymentScreenshotUrl" to item.paymentScreenshotUrl,
                "paymentStatus" to item.paymentStatus,
                "status" to item.status.name,
                "adminNotes" to item.adminNotes,
                "registeredAt" to item.registeredAt
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving registration to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Updates registration in Firestore (status, payment verification, notes).
     */
    suspend fun updateRegistrationFields(
        context: Context,
        regId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        val firestore = getFirestore(context) ?: return Result.success(Unit)
        return try {
            firestore.collection(COLLECTION_REGISTRATIONS).document(regId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating registration in Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Updates or creates tournament session in Firestore.
     */
    suspend fun saveTournament(context: Context, item: TournamentItem): Result<Unit> {
        val firestore = getFirestore(context) ?: return Result.success(Unit)
        return try {
            val data = mapOf(
                "id" to item.id,
                "title" to item.title,
                "date" to item.date,
                "startTime" to item.startTime,
                "startHour" to item.startHour,
                "startMinute" to item.startMinute,
                "entryFee" to item.entryFee,
                "matchType" to item.matchType,
                "map" to item.map,
                "matches" to item.matches,
                "maxSlots" to item.maxSlots,
                "minSlots" to item.minSlots,
                "status" to item.status.name,
                "roomId" to item.roomId,
                "roomPassword" to item.roomPassword,
                "revealMinutesBefore" to item.revealMinutesBefore
            )
            firestore.collection(COLLECTION_TOURNAMENTS).document(item.id)
                .set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving tournament to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time listener for registrations collection in Firestore.
     */
    fun attachRegistrationsListener(
        context: Context,
        onUpdate: (List<RegistrationItem>) -> Unit
    ): ListenerRegistration? {
        val firestore = getFirestore(context) ?: return null
        return try {
            firestore.collection(COLLECTION_REGISTRATIONS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen error on registrations", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { doc ->
                            try {
                                RegistrationItem(
                                    id = doc.getString("id") ?: doc.id,
                                    tournamentId = doc.getString("tournamentId") ?: "",
                                    tournamentTime = doc.getString("tournamentTime") ?: "",
                                    playerName = doc.getString("playerName") ?: "",
                                    ffIgn = doc.getString("ffIgn") ?: "",
                                    ffUid = doc.getString("ffUid") ?: "",
                                    contactNumber = doc.getString("contactNumber") ?: "",
                                    teamName = doc.getString("teamName") ?: "",
                                    selectedSlot = (doc.getLong("selectedSlot") ?: 1L).toInt(),
                                    entryFee = (doc.getLong("entryFee") ?: 0L).toInt(),
                                    paymentRef = doc.getString("paymentRef") ?: "",
                                    paymentScreenshotUrl = doc.getString("paymentScreenshotUrl") ?: "",
                                    paymentStatus = doc.getString("paymentStatus") ?: "PENDING_VERIFICATION",
                                    status = try {
                                        RegistrationStatus.valueOf(doc.getString("status") ?: "PENDING")
                                    } catch (e: Exception) {
                                        RegistrationStatus.PENDING
                                    },
                                    adminNotes = doc.getString("adminNotes") ?: "",
                                    registeredAt = doc.getLong("registeredAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        onUpdate(items)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach registrations listener", e)
            null
        }
    }
}
