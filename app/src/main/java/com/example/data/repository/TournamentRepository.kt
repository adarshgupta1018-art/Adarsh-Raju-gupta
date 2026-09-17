package com.example.data.repository

import com.example.data.local.NotificationDao
import com.example.data.local.NotificationEntity
import com.example.data.local.RegistrationDao
import com.example.data.local.RegistrationEntity
import com.example.data.local.TournamentDao
import com.example.data.local.TournamentEntity
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.model.RegistrationItem
import com.example.data.model.RegistrationStatus
import com.example.data.model.RoomCredentials
import com.example.data.model.TournamentItem
import com.example.data.model.TournamentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class TournamentRepository(
    private val tournamentDao: TournamentDao,
    private val registrationDao: RegistrationDao,
    private val notificationDao: NotificationDao
) {
    // Combine tournaments with registration counts reactively
    val allTournaments: Flow<List<TournamentItem>> = combine(
        tournamentDao.getAllTournaments(),
        registrationDao.getAllRegistrations()
    ) { tournaments, registrations ->
        tournaments.map { entity ->
            val tournamentRegistrations = registrations.filter { it.tournamentId == entity.id }
            val activeRegistrations = tournamentRegistrations.filter {
                it.status != RegistrationStatus.REJECTED && it.status != RegistrationStatus.CANCELLED
            }
            val confirmedCount = tournamentRegistrations.count { it.status == RegistrationStatus.CONFIRMED }
            
            // Auto update status if full
            val effectiveStatus = when {
                entity.status == TournamentStatus.CANCELLED -> TournamentStatus.CANCELLED
                entity.status == TournamentStatus.COMPLETED -> TournamentStatus.COMPLETED
                activeRegistrations.size >= entity.maxSlots -> TournamentStatus.SLOTS_FULL
                else -> TournamentStatus.OPEN
            }

            TournamentItem(
                id = entity.id,
                title = entity.title,
                date = entity.date,
                startTime = entity.startTime,
                startHour = entity.startHour,
                startMinute = entity.startMinute,
                entryFee = entity.entryFee,
                matchType = entity.matchType,
                map = entity.map,
                matches = entity.matches,
                maxSlots = entity.maxSlots,
                minSlots = entity.minSlots,
                status = effectiveStatus,
                roomId = entity.roomId,
                roomPassword = entity.roomPassword,
                revealMinutesBefore = entity.revealMinutesBefore,
                registeredCount = activeRegistrations.size,
                confirmedCount = confirmedCount
            )
        }
    }

    val allRegistrations: Flow<List<RegistrationItem>> = registrationDao.getAllRegistrations().map { list ->
        list.map { it.toModel() }
    }

    fun getRegistrationsForPlayer(ffUid: String): Flow<List<RegistrationItem>> =
        registrationDao.getRegistrationsForPlayer(ffUid).map { list -> list.map { it.toModel() } }

    fun getNotificationsForUser(userUid: String): Flow<List<NotificationItem>> =
        notificationDao.getNotificationsForUser(userUid).map { list -> list.map { it.toModel() } }

    /**
     * Creates a new registration with unique ID e.g., ACE-20260917-001
     */
    suspend fun registerPlayer(
        tournamentId: String,
        playerName: String,
        ffIgn: String,
        ffUid: String,
        contactNumber: String,
        paymentRef: String
    ): Result<RegistrationItem> {
        val tournament = tournamentDao.getTournamentById(tournamentId)
            ?: return Result.failure(Exception("Tournament session not found"))

        if (tournament.status == TournamentStatus.CANCELLED) {
            return Result.failure(Exception("This tournament session has been cancelled"))
        }

        val currentCount = registrationDao.getActiveCountForTournament(tournamentId)
        if (currentCount >= tournament.maxSlots) {
            return Result.failure(Exception("Tournament slots are completely full (20/20)"))
        }

        // Generate Registration ID: ACE-YYYYMMDD-XXX
        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val indexNumber = (currentCount + 1).toString().padStart(3, '0')
        val regId = "ACE-$dateCode-$indexNumber"

        val entity = RegistrationEntity(
            id = regId,
            tournamentId = tournamentId,
            tournamentTime = tournament.startTime,
            playerName = playerName.trim(),
            ffIgn = ffIgn.trim(),
            ffUid = ffUid.trim(),
            contactNumber = contactNumber.trim(),
            entryFee = tournament.entryFee,
            paymentRef = paymentRef.trim(),
            status = RegistrationStatus.PENDING,
            registeredAt = System.currentTimeMillis()
        )

        registrationDao.insertRegistration(entity)

        // Check if slots reached 20
        if (currentCount + 1 >= tournament.maxSlots) {
            tournamentDao.updateTournamentStatus(tournamentId, TournamentStatus.SLOTS_FULL)
        }

        // Emit Player Notification
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUid = ffUid.trim(),
                title = "Registration Submitted",
                message = "Registration #$regId for ${tournament.startTime} submitted. Status: PENDING CONFIRMATION.",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.REGISTRATION_SUBMITTED,
                isRead = false,
                tournamentId = tournamentId
            )
        )

        return Result.success(entity.toModel())
    }

    suspend fun confirmRegistration(regId: String) {
        val reg = registrationDao.getRegistrationById(regId) ?: return
        registrationDao.updateStatus(regId, RegistrationStatus.CONFIRMED)

        // Player notification
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUid = reg.ffUid,
                title = "Registration Confirmed!",
                message = "Your ACE ESPORTS registration has been confirmed! Match at ${reg.tournamentTime}.",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.REGISTRATION_CONFIRMED,
                isRead = false,
                tournamentId = reg.tournamentId
            )
        )
    }

    suspend fun rejectRegistration(regId: String, reason: String = "Verification failed") {
        val reg = registrationDao.getRegistrationById(regId) ?: return
        registrationDao.updateStatus(regId, RegistrationStatus.REJECTED)

        // Check if tournament was full and can now reopen
        val count = registrationDao.getActiveCountForTournament(reg.tournamentId)
        val t = tournamentDao.getTournamentById(reg.tournamentId)
        if (t != null && t.status == TournamentStatus.SLOTS_FULL && count < t.maxSlots) {
            tournamentDao.updateTournamentStatus(reg.tournamentId, TournamentStatus.OPEN)
        }

        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUid = reg.ffUid,
                title = "Registration Update",
                message = "Your registration #$regId for ${reg.tournamentTime} was rejected ($reason).",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.REGISTRATION_REJECTED,
                isRead = false,
                tournamentId = reg.tournamentId
            )
        )
    }

    suspend fun updateCustomRoomDetails(tournamentId: String, roomId: String, roomPassword: String) {
        tournamentDao.updateRoomDetails(tournamentId, roomId.trim(), roomPassword.trim())

        // Notify confirmed players that room credentials are saved and will auto-reveal 5 min prior
        val t = tournamentDao.getTournamentById(tournamentId) ?: return
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUid = "ALL",
                title = "Room Credentials Updated",
                message = "Custom Room details for ${t.startTime} tournament are set. Will reveal 5 minutes prior to match!",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.ADMIN_ANNOUNCEMENT,
                isRead = false,
                tournamentId = tournamentId
            )
        )
    }

    suspend fun cancelTournament(tournamentId: String, reason: String = "Not enough registrations") {
        val t = tournamentDao.getTournamentById(tournamentId) ?: return
        tournamentDao.updateTournamentStatus(tournamentId, TournamentStatus.CANCELLED)

        // Mark registrations as cancelled
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUid = "ALL",
                title = "Tournament Cancelled",
                message = "${t.startTime} tournament cancelled: $reason. Any entry fees will be refunded.",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.TOURNAMENT_CANCELLED,
                isRead = false,
                tournamentId = tournamentId
            )
        )
    }

    suspend fun reopenTournament(tournamentId: String) {
        val t = tournamentDao.getTournamentById(tournamentId) ?: return
        val count = registrationDao.getActiveCountForTournament(tournamentId)
        val newStatus = if (count >= t.maxSlots) TournamentStatus.SLOTS_FULL else TournamentStatus.OPEN
        tournamentDao.updateTournamentStatus(tournamentId, newStatus)
    }

    /**
     * Backend-enforced Room Credentials retrieval.
     * Prevents revealing Room ID / Password to unauthorized users or before (startTime - 5 mins).
     */
    suspend fun getSecureRoomCredentials(
        tournamentId: String,
        playerUid: String,
        simulatedTimeMillis: Long = System.currentTimeMillis()
    ): RoomCredentials {
        val tournament = tournamentDao.getTournamentById(tournamentId)
            ?: return RoomCredentials(
                tournamentId = tournamentId,
                roomId = "",
                roomPassword = "",
                isRevealed = false,
                revealTimeMillis = 0L,
                millisUntilReveal = 0L,
                message = "Tournament not found"
            )

        if (tournament.status == TournamentStatus.CANCELLED) {
            return RoomCredentials(
                tournamentId = tournamentId,
                roomId = "",
                roomPassword = "",
                isRevealed = false,
                revealTimeMillis = 0L,
                millisUntilReveal = 0L,
                message = "Tournament Cancelled"
            )
        }

        // Calculate reveal time for today
        val startCal = Calendar.getInstance().apply {
            timeInMillis = simulatedTimeMillis
            set(Calendar.HOUR_OF_DAY, tournament.startHour)
            set(Calendar.MINUTE, tournament.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTimeMillis = startCal.timeInMillis
        val revealTimeMillis = startTimeMillis - (tournament.revealMinutesBefore * 60 * 1000L)
        val millisUntilReveal = revealTimeMillis - simulatedTimeMillis

        if (simulatedTimeMillis < revealTimeMillis) {
            // SECURITY: Never return roomId or password before reveal time
            return RoomCredentials(
                tournamentId = tournamentId,
                roomId = "",
                roomPassword = "",
                isRevealed = false,
                revealTimeMillis = revealTimeMillis,
                millisUntilReveal = millisUntilReveal.coerceAtLeast(0L),
                message = "Room ID & Password will be revealed 5 minutes before the match."
            )
        }

        // At or after 5 minutes before start:
        // Return actual room credentials entered by admin
        val isReady = tournament.roomId.isNotBlank() && tournament.roomPassword.isNotBlank()
        return RoomCredentials(
            tournamentId = tournamentId,
            roomId = if (isReady) tournament.roomId else "WAITING_ADMIN",
            roomPassword = if (isReady) tournament.roomPassword else "WAITING_ADMIN",
            isRevealed = isReady,
            revealTimeMillis = revealTimeMillis,
            millisUntilReveal = 0L,
            message = if (isReady) "Custom Room details are now available!" else "Admin is entering Room details shortly..."
        )
    }

    suspend fun markNotificationAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(userUid: String) {
        notificationDao.markAllAsReadForUser(userUid)
    }

    /**
     * Seeds initial realistic tournament sessions:
     * 2:00 PM — Entry ₹1
     * 3:00 PM — Entry ₹5
     * 4:00 PM — Entry ₹10
     * 5:00 PM — Entry ₹15
     * 6:00 PM — Entry ₹20
     * 7:00 PM — Entry ₹30
     * 8:00 PM — Entry ₹50
     * 9:00 PM — Entry ₹100
     */
    suspend fun seedInitialDataIfEmpty() {
        val existing = tournamentDao.getTournamentById("ACE-20260917-7PM")
        if (existing != null) return

        val dateString = "Today, Sep 17, 2026"

        val initialTournaments = listOf(
            TournamentEntity("ACE-20260917-2PM", "SOLO BR MATCH", dateString, "2:00 PM", 14, 0, 1, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "102938475", "ACE2PM", 5),
            TournamentEntity("ACE-20260917-3PM", "SOLO BR MATCH", dateString, "3:00 PM", 15, 0, 5, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "293847561", "ACE3PM", 5),
            TournamentEntity("ACE-20260917-4PM", "SOLO BR MATCH", dateString, "4:00 PM", 16, 0, 10, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "384756102", "ACE4PM", 5),
            TournamentEntity("ACE-20260917-5PM", "SOLO BR MATCH", dateString, "5:00 PM", 17, 0, 15, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "475610293", "ACE5PM", 5),
            TournamentEntity("ACE-20260917-6PM", "SOLO BR MATCH", dateString, "6:00 PM", 18, 0, 20, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "561029384", "ACE6PM", 5),
            TournamentEntity("ACE-20260917-7PM", "SOLO BR MATCH", dateString, "7:00 PM", 19, 0, 30, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "672910384", "BOOYAH7", 5),
            TournamentEntity("ACE-20260917-8PM", "SOLO BR MATCH", dateString, "8:00 PM", 20, 0, 50, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "783021495", "ACE8PM", 5),
            TournamentEntity("ACE-20260917-9PM", "SOLO BR MATCH", dateString, "9:00 PM", 21, 0, 100, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "894132506", "ACE9PM", 5)
        )
        tournamentDao.insertTournaments(initialTournaments)

        // Realistic pre-existing player registrations for the 7:00 PM and 6:00 PM sessions
        val sampleRegistrations = listOf(
            RegistrationEntity("ACE-20260917-001", "ACE-20260917-7PM", "7:00 PM", "Aman Sharma", "AMAN_KILLER_99", "1049281723", "+91 9876543210", 30, "UPI-REF-998821", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 7200000),
            RegistrationEntity("ACE-20260917-002", "ACE-20260917-7PM", "7:00 PM", "Rahul Verma", "RAHUL_FF_SNIPER", "2918273645", "+91 9876543211", 30, "UPI-REF-998822", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 6500000),
            RegistrationEntity("ACE-20260917-003", "ACE-20260917-7PM", "7:00 PM", "Vikram Singh", "VIKRAM_HEADSHOT", "3827162534", "+91 9876543212", 30, "UPI-REF-998823", RegistrationStatus.PENDING, System.currentTimeMillis() - 5000000),
            RegistrationEntity("ACE-20260917-004", "ACE-20260917-7PM", "7:00 PM", "Dev Patel", "DEV_GODLIKE", "4738291048", "+91 9876543213", 30, "UPI-REF-998824", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 4000000),
            RegistrationEntity("ACE-20260917-005", "ACE-20260917-7PM", "7:00 PM", "Saurav Rao", "SAURAV_RUSHER", "5647382910", "+91 9876543214", 30, "UPI-REF-998825", RegistrationStatus.PENDING, System.currentTimeMillis() - 3600000),
            RegistrationEntity("ACE-20260917-006", "ACE-20260917-7PM", "7:00 PM", "Deepak Gupta", "DEEPAK_FIRE", "6558493021", "+91 9876543215", 30, "UPI-REF-998826", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 3200000),
            RegistrationEntity("ACE-20260917-007", "ACE-20260917-7PM", "7:00 PM", "Karan Malhotra", "KARAN_OP", "7469504132", "+91 9876543216", 30, "UPI-REF-998827", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 2800000),
            RegistrationEntity("ACE-20260917-008", "ACE-20260917-7PM", "7:00 PM", "Naveen Yadav", "NAVEEN_BOOYAH", "8370615243", "+91 9876543217", 30, "UPI-REF-998828", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 2500000),
            RegistrationEntity("ACE-20260917-009", "ACE-20260917-7PM", "7:00 PM", "Adarsh Gupta", "ACE_ADARSH_99", "1298471203", "+91 9811223344", 30, "UPI-REF-771122", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 2100000),
            RegistrationEntity("ACE-20260917-010", "ACE-20260917-7PM", "7:00 PM", "Rohit Rajput", "ROHIT_PRO_FF", "9281726354", "+91 9876543218", 30, "UPI-REF-998829", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 1900000),
            RegistrationEntity("ACE-20260917-011", "ACE-20260917-7PM", "7:00 PM", "Ajay Kumar", "AJAY_WARRIOR", "1928374650", "+91 9876543219", 30, "UPI-REF-998830", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 1500000),
            RegistrationEntity("ACE-20260917-012", "ACE-20260917-7PM", "7:00 PM", "Sandeep Rawat", "SANDEEP_FF", "2837465910", "+91 9876543220", 30, "UPI-REF-998831", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 1200000),
            RegistrationEntity("ACE-20260917-013", "ACE-20260917-7PM", "7:00 PM", "Manish Mehra", "MANISH_VIPER", "3748596021", "+91 9876543221", 30, "UPI-REF-998832", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 900000),
            RegistrationEntity("ACE-20260917-014", "ACE-20260917-7PM", "7:00 PM", "Ankit Joshi", "ANKIT_BEAST", "4659607132", "+91 9876543222", 30, "UPI-REF-998833", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 600000),
            RegistrationEntity("ACE-20260917-015", "ACE-20260917-6PM", "6:00 PM", "Adarsh Gupta", "ACE_ADARSH_99", "1298471203", "+91 9811223344", 20, "UPI-REF-662211", RegistrationStatus.CONFIRMED, System.currentTimeMillis() - 3600000)
        )
        registrationDao.insertRegistrations(sampleRegistrations)

        val sampleNotifications = listOf(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUid = "1298471203",
                title = "Registration Confirmed!",
                message = "Your ACE ESPORTS registration has been confirmed for 7:00 PM Solo BR!",
                timestamp = System.currentTimeMillis() - 2100000,
                type = NotificationType.REGISTRATION_CONFIRMED,
                isRead = false,
                tournamentId = "ACE-20260917-7PM"
            ),
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUid = "ALL",
                title = "Tournament Schedule Live",
                message = "Today's Free Fire Solo BR tournaments are open for registration. Minimum 12 players required per session.",
                timestamp = System.currentTimeMillis() - 10000000,
                type = NotificationType.ADMIN_ANNOUNCEMENT,
                isRead = true,
                tournamentId = null
            )
        )
        notificationDao.insertNotifications(sampleNotifications)
    }

    suspend fun resetAllData() {
        tournamentDao.clearAll()
        registrationDao.clearAll()
        notificationDao.clearAll()
        seedInitialDataIfEmpty()
    }
}

private fun RegistrationEntity.toModel(): RegistrationItem = RegistrationItem(
    id = id,
    tournamentId = tournamentId,
    tournamentTime = tournamentTime,
    playerName = playerName,
    ffIgn = ffIgn,
    ffUid = ffUid,
    contactNumber = contactNumber,
    entryFee = entryFee,
    paymentRef = paymentRef,
    status = status,
    registeredAt = registeredAt
)

private fun NotificationEntity.toModel(): NotificationItem = NotificationItem(
    id = id,
    targetUid = targetUid,
    title = title,
    message = message,
    timestamp = timestamp,
    type = type,
    isRead = isRead,
    tournamentId = tournamentId
)
