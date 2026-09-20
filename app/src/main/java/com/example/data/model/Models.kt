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

data class FeePrizeTier(
    val entryFee: Int,
    val winningPrize: String,
    val note: String = ""
)

object TournamentPrizeRules {
    val OFFICIAL_TIERS = listOf(
        FeePrizeTier(1, "₹10 (Only Booyah)", "Only Booyah"),
        FeePrizeTier(5, "₹45 (Only Booyah)", "Only Booyah"),
        FeePrizeTier(10, "₹50 (1st) / ₹40 (2nd) / ₹30 (3rd)", "Top 3"),
        FeePrizeTier(15, "₹70 (1st) / ₹50 (2nd) / ₹30 (3rd)", "Top 3"),
        FeePrizeTier(20, "₹100 (1st) / ₹70 (2nd) / ₹50 (3rd)", "Top 3"),
        FeePrizeTier(30, "₹150 (1st) / ₹100 (2nd) / ₹70 (3rd)", "Top 3"),
        FeePrizeTier(50, "₹200 (1st) / ₹150 (2nd) / ₹100 (3rd)", "Top 3"),
        FeePrizeTier(100, "₹400 (1st) / ₹300 (2nd) / ₹200 (3rd)", "Top 3")
    )

    val STANDARD_TIERS: List<Pair<Int, String>> = OFFICIAL_TIERS.map { it.entryFee to it.winningPrize }

    fun getDefaultPrizeForFee(fee: Int): String {
        return OFFICIAL_TIERS.firstOrNull { it.entryFee == fee }?.winningPrize
            ?: when {
                fee <= 5 -> "₹${fee * 9} (Only Booyah)"
                else -> "₹${fee * 4} (1st) / ₹${fee * 3} (2nd) / ₹${fee * 2} (3rd)"
            }
    }
}

data class TournamentItem(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String,
    val startHour: Int,
    val startMinute: Int,
    val entryFee: Int,
    val winningPrize: String = "",
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
    val confirmedCount: Int = 0,
    val pendingCount: Int = 0,
    val bookedSlotNumbers: List<Int> = emptyList(),
    val confirmedSlotNumbers: List<Int> = emptyList()
) {
    val totalSlots: Int get() = maxSlots
    val pendingSlots: Int get() = pendingCount
    val confirmedSlots: Int get() = confirmedCount
    val isFull: Boolean get() = registeredCount >= maxSlots
    val availableSlots: Int get() = (maxSlots - registeredCount).coerceAtLeast(0)
    val displayWinningPrize: String
        get() = if (winningPrize.isNotBlank()) winningPrize else TournamentPrizeRules.getDefaultPrizeForFee(entryFee)
}

data class RegistrationItem(
    val id: String,
    val tournamentId: String,
    val tournamentTime: String,
    val playerName: String,
    val ffIgn: String,
    val ffUid: String,
    val contactNumber: String,
    val teamName: String = "",
    val selectedSlot: Int = 1,
    val entryFee: Int,
    val winningPrize: String = "",
    val paymentRef: String = "",
    val paymentScreenshotUrl: String = "",
    val paymentStatus: String = "PENDING_VERIFICATION",
    val status: RegistrationStatus = RegistrationStatus.PENDING,
    val adminNotes: String = "",
    val registeredAt: Long = System.currentTimeMillis()
) {
    val displayWinningPrize: String
        get() = if (winningPrize.isNotBlank()) winningPrize else TournamentPrizeRules.getDefaultPrizeForFee(entryFee)
}

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
