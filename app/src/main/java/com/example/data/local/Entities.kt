package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.NotificationType
import com.example.data.model.RegistrationStatus
import com.example.data.model.TournamentStatus

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val date: String,
    val startTime: String,
    val startHour: Int,
    val startMinute: Int,
    val entryFee: Int,
    val matchType: String,
    val map: String,
    val matches: Int,
    val maxSlots: Int,
    val minSlots: Int,
    val status: TournamentStatus,
    val roomId: String,
    val roomPassword: String,
    val revealMinutesBefore: Int,
    val winningPrize: String = ""
)

@Entity(tableName = "registrations")
data class RegistrationEntity(
    @PrimaryKey val id: String,
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
    val paymentStatus: String = "PENDING",
    val status: RegistrationStatus,
    val adminNotes: String = "",
    val registeredAt: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val targetUid: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType,
    val isRead: Boolean,
    val tournamentId: String?
)
