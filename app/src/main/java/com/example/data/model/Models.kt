package com.example.data.model

enum class RegistrationStatus(val label: String, val emoji: String) {
    PENDING("PENDING", "🟡"),
    CONFIRMED("CONFIRMED", "🟢"),
    REJECTED("REJECTED", "🔴"),
    CANCELLED("CANCELLED", "⚪")
}

enum class TournamentStatus(val label: String) {
    OPEN("OPEN"),
    SLOTS_FULL("SLOTS FULL"),
    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED")
}

enum class NotificationType {
    REGISTRATION_SUBMITTED,
    REGISTRATION_CONFIRMED,
    REGISTRATION_REJECTED,
    ROOM_DETAILS_UNLOCKED,
    TOURNAMENT_STARTING_SOON,
    TOURNAMENT_CANCELLED,
    ADMIN_ANNOUNCEMENT
}

enum class UserRole {
    PLAYER,
    ADMIN
}

data class TournamentItem(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String,
    val startHour: Int,
    val startMinute: Int,
    val entryFee: Int,
    val matchType: String = "SOLO BR",
    val map: String = "BR (Bermuda)",
    val matches: Int = 1,
    val maxSlots: Int = 20,
    val minSlots: Int = 12,
    val status: TournamentStatus = TournamentStatus.OPEN,
    val roomId: String = "",
    val roomPassword: String = "",
    val revealMinutesBefore: Int = 5,
    val registeredCount: Int = 0,
    val confirmedCount: Int = 0
) {
    val isFull: Boolean get() = registeredCount >= maxSlots
    val availableSlots: Int get() = (maxSlots - registeredCount).coerceAtLeast(0)
}

data class RegistrationItem(
    val id: String,
    val tournamentId: String,
    val tournamentTime: String,
    val playerName: String,
    val ffIgn: String,
    val ffUid: String,
    val contactNumber: String,
    val entryFee: Int,
    val paymentRef: String = "",
    val status: RegistrationStatus = RegistrationStatus.PENDING,
    val registeredAt: Long = System.currentTimeMillis()
)

data class NotificationItem(
    val id: String,
    val targetUid: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: NotificationType,
    val isRead: Boolean = false,
    val tournamentId: String? = null
)

data class UserProfile(
    val id: String,
    val name: String,
    val ffIgn: String,
    val ffUid: String,
    val contactNumber: String,
    val role: UserRole = UserRole.PLAYER
)

data class RoomCredentials(
    val tournamentId: String,
    val roomId: String,
    val roomPassword: String,
    val isRevealed: Boolean,
    val revealTimeMillis: Long,
    val millisUntilReveal: Long,
    val message: String
)
