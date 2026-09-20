package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirebaseManager
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
import com.example.data.model.TournamentPrizeRules
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
    private val context: Context,
    private val tournamentDao: TournamentDao,
    private val registrationDao: RegistrationDao,
    private val notificationDao: NotificationDao
) {
    // Combine tournaments with registration counts and slots reactively
    val allTournaments: Flow<List<TournamentItem>> = combine(
        tournamentDao.getAllTournaments(),
        registrationDao.getAllRegistrations()
    ) { tournaments, registrations ->
        tournaments.map { entity ->
            val tournamentRegistrations = registrations.filter { it.tournamentId == entity.id }
            val activeRegistrations = tournamentRegistrations.filter {
                it.status != RegistrationStatus.REJECTED && it.status != RegistrationStatus.CANCELLED
            }
            val confirmedList = tournamentRegistrations.filter { it.status == RegistrationStatus.CONFIRMED }
            val pendingList = tournamentRegistrations.filter { it.status == RegistrationStatus.PENDING }

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
                confirmedCount = confirmedList.size,
                pendingCount = pendingList.size,
                bookedSlotNumbers = activeRegistrations.map { it.selectedSlot },
                confirmedSlotNumbers = confirmedList.map { it.selectedSlot }
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
     * Creates a new real player registration with validation and duplicate slot prevention.
     */
    suspend fun registerPlayer(
        tournamentId: String,
        playerName: String,
        ffIgn: String,
        ffUid: String,
        contactNumber: String,
        teamName: String = "",
        selectedSlot: Int,
        paymentRef: String,
        paymentScreenshotUrl: String = "",
        chosenFee: Int? = null,
        winningPrize: String? = null
    ): Result<RegistrationItem> {
        val tournament = tournamentDao.getTournamentById(tournamentId)
            ?: return Result.failure(Exception("Tournament session not found."))

        if (tournament.status == TournamentStatus.CANCELLED) {
            return Result.failure(Exception("This tournament session has been cancelled."))
        }

        if (selectedSlot < 1 || selectedSlot > tournament.maxSlots) {
            return Result.failure(Exception("Selected slot must be between 1 and ${tournament.maxSlots}."))
        }

        // COMPULSORY PAYMENT ENFORCEMENT
        val cleanPaymentRef = paymentRef.trim()
        if (cleanPaymentRef.isBlank()) {
            return Result.failure(Exception("Payment is compulsory. Please scan the QR code and enter your Transaction ID/UTR."))
        }

        // 1. PREVENT DUPLICATE SLOT: Check if selected slot is already taken by an active player
        val existingSlotBooking = registrationDao.getRegistrationBySlot(tournamentId, selectedSlot)
        if (existingSlotBooking != null) {
            return Result.failure(
                Exception("Slot #$selectedSlot is already booked. Please choose another available slot.")
            )
        }

        // 2. CHECK LOBBY CAPACITY
        val currentCount = registrationDao.getActiveCountForTournament(tournamentId)
        if (currentCount >= tournament.maxSlots) {
            return Result.failure(Exception("Tournament slots are completely full (20/20)."))
        }

        val effectiveFee = chosenFee ?: tournament.entryFee
        val effectivePrize = winningPrize ?: if (tournament.winningPrize.isNotBlank()) tournament.winningPrize else TournamentPrizeRules.getDefaultPrizeForFee(effectiveFee)

        // 3. Generate Unique Registration ID: ACE-YYYYMMDD-XXX
        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        var nextSeq = registrationDao.getTotalRegistrationCount() + 1
        var regId = "ACE-$dateCode-${nextSeq.toString().padStart(3, '0')}"
        while (registrationDao.getRegistrationById(regId) != null) {
            nextSeq++
            regId = "ACE-$dateCode-${nextSeq.toString().padStart(3, '0')}"
        }

        val entity = RegistrationEntity(
            id = regId,
            tournamentId = tournamentId,
            tournamentTime = tournament.startTime,
            playerName = playerName.trim(),
            ffIgn = ffIgn.trim(),
            ffUid = ffUid.trim(),
            contactNumber = contactNumber.trim(),
            teamName = teamName.trim(),
            selectedSlot = selectedSlot,
            entryFee = effectiveFee,
            winningPrize = effectivePrize,
            paymentRef = cleanPaymentRef,
            paymentScreenshotUrl = paymentScreenshotUrl.trim(),
            paymentStatus = "PENDING",
            status = RegistrationStatus.PENDING,
            adminNotes = "",
            registeredAt = System.currentTimeMillis()
        )

        // Save locally in Room
        registrationDao.insertRegistration(entity)

        // Save in Firebase Firestore
        val model = entity.toModel()
        FirebaseManager.saveRegistration(context, model)

        // Check if slots reached max
        if (currentCount + 1 >= tournament.maxSlots) {
            tournamentDao.updateTournamentStatus(tournamentId, TournamentStatus.SLOTS_FULL)
        }

        // Emit Player Notification
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUid = ffUid.trim(),
                title = "Slot #$selectedSlot Booked (Payment Pending)",
                message = "Registration #$regId for ${tournament.startTime} (Slot #$selectedSlot) is pending Admin payment verification.",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.REGISTRATION_SUBMITTED,
                isRead = false,
                tournamentId = tournamentId
            )
        )

        return Result.success(model)
    }

    suspend fun updateTournamentFeeAndPrize(tournamentId: String, newEntryFee: Int, newWinningPrize: String) {
        val existing = tournamentDao.getTournamentById(tournamentId) ?: return
        val updated = existing.copy(
            entryFee = newEntryFee,
            winningPrize = newWinningPrize.trim().ifBlank { TournamentPrizeRules.getDefaultPrizeForFee(newEntryFee) }
        )
        tournamentDao.insertTournaments(listOf(updated))
    }

    suspend fun confirmRegistration(regId: String) {
        val reg = registrationDao.getRegistrationById(regId) ?: return
        registrationDao.updateBookingDetails(
            id = regId,
            status = RegistrationStatus.CONFIRMED,
            paymentStatus = "VERIFIED",
            adminNotes = reg.adminNotes
        )

        // Sync with Firebase Firestore
        FirebaseManager.updateRegistrationFields(
            context = context,
            regId = regId,
            updates = mapOf(
                "status" to RegistrationStatus.CONFIRMED.name,
                "paymentStatus" to "VERIFIED"
            )
        )

        // Player notification
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUid = reg.ffUid,
                title = "Slot #${reg.selectedSlot} Confirmed!",
                message = "Your registration #$regId for ${reg.tournamentTime} (Slot #${reg.selectedSlot}) has been verified & confirmed!",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.REGISTRATION_CONFIRMED,
                isRead = false,
                tournamentId = reg.tournamentId
            )
        )
    }

    suspend fun rejectRegistration(regId: String, reason: String = "Verification failed") {
        val reg = registrationDao.getRegistrationById(regId) ?: return
        registrationDao.updateBookingDetails(
            id = regId,
            status = RegistrationStatus.REJECTED,
            paymentStatus = "REJECTED",
            adminNotes = "Rejected: $reason"
        )

        // Sync with Firebase Firestore
        FirebaseManager.updateRegistrationFields(
            context = context,
            regId = regId,
            updates = mapOf(
                "status" to RegistrationStatus.REJECTED.name,
                "paymentStatus" to "REJECTED",
                "adminNotes" to "Rejected: $reason"
            )
        )

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
                title = "Registration Rejected",
                message = "Your registration #$regId (Slot #${reg.selectedSlot}) was rejected: $reason.",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.REGISTRATION_REJECTED,
                isRead = false,
                tournamentId = reg.tournamentId
            )
        )
    }

    suspend fun updateRegistrationStatusAndNotes(
        regId: String,
        newStatus: RegistrationStatus,
        newPaymentStatus: String,
        adminNotes: String
    ) {
        val reg = registrationDao.getRegistrationById(regId) ?: return
        registrationDao.updateBookingDetails(
            id = regId,
            status = newStatus,
            paymentStatus = newPaymentStatus,
            adminNotes = adminNotes
        )

        FirebaseManager.updateRegistrationFields(
            context = context,
            regId = regId,
            updates = mapOf(
                "status" to newStatus.name,
                "paymentStatus" to newPaymentStatus,
                "adminNotes" to adminNotes
            )
        )
    }

    suspend fun updateCustomRoomDetails(tournamentId: String, roomId: String, roomPassword: String) {
        tournamentDao.updateRoomDetails(tournamentId, roomId.trim(), roomPassword.trim())

        val t = tournamentDao.getTournamentById(tournamentId) ?: return
        FirebaseManager.saveTournament(context, t.toModel())

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

    suspend fun updateTournament(item: TournamentItem) {
        tournamentDao.updateTournament(item.toEntity())
        FirebaseManager.saveTournament(context, item)
    }

    suspend fun addTournament(item: TournamentItem) {
        tournamentDao.insertTournament(item.toEntity())
        FirebaseManager.saveTournament(context, item)
    }

    suspend fun cancelTournament(tournamentId: String, reason: String = "Not enough registrations") {
        val t = tournamentDao.getTournamentById(tournamentId) ?: return
        tournamentDao.updateTournamentStatus(tournamentId, TournamentStatus.CANCELLED)
        FirebaseManager.saveTournament(context, t.copy(status = TournamentStatus.CANCELLED).toModel())

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
        FirebaseManager.saveTournament(context, t.copy(status = newStatus).toModel())
    }

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
     * Seeds initial real tournament schedule.
     * All mock/sample registrations are completely removed. All slots start available!
     */
    suspend fun seedInitialDataIfEmpty() {
        val existing = tournamentDao.getTournamentById("ACE-20260917-7PM")
        val dateString = "Today, Sep 18, 2026"

        val officialTournaments = listOf(
            TournamentEntity("ACE-20260917-2PM", "SOLO BR MATCH", dateString, "2:00 PM", 14, 0, 1, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "", "", 5, "₹10 (Only Booyah)"),
            TournamentEntity("ACE-20260917-3PM", "SOLO BR MATCH", dateString, "3:00 PM", 15, 0, 5, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "", "", 5, "₹45 (Only Booyah)"),
            TournamentEntity("ACE-20260917-4PM", "SOLO BR MATCH", dateString, "4:00 PM", 16, 0, 10, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "", "", 5, "₹50 (1st) / ₹40 (2nd) / ₹30 (3rd)"),
            TournamentEntity("ACE-20260917-5PM", "SOLO BR MATCH", dateString, "5:00 PM", 17, 0, 15, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "", "", 5, "₹70 (1st) / ₹50 (2nd) / ₹30 (3rd)"),
            TournamentEntity("ACE-20260917-6PM", "SOLO BR MATCH", dateString, "6:00 PM", 18, 0, 20, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "", "", 5, "₹100 (1st) / ₹70 (2nd) / ₹50 (3rd)"),
            TournamentEntity("ACE-20260917-7PM", "SOLO BR MATCH", dateString, "7:00 PM", 19, 0, 30, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "", "", 5, "₹150 (1st) / ₹100 (2nd) / ₹70 (3rd)"),
            TournamentEntity("ACE-20260917-8PM", "SOLO BR MATCH", dateString, "8:00 PM", 20, 0, 50, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "", "", 5, "₹200 (1st) / ₹150 (2nd) / ₹100 (3rd)"),
            TournamentEntity("ACE-20260917-9PM", "SOLO BR MATCH", dateString, "9:00 PM", 21, 0, 100, "SOLO BR", "BR (Bermuda)", 1, 20, 12, TournamentStatus.OPEN, "", "", 5, "₹400 (1st) / ₹300 (2nd) / ₹200 (3rd)")
        )

        if (existing == null) {
            tournamentDao.insertTournaments(officialTournaments)
        } else {
            // Ensure any existing tournaments have accurate prize information populated
            val currentList = tournamentDao.getAllTournamentsSync()
            val needUpdate = currentList.any { it.winningPrize.isBlank() }
            if (needUpdate) {
                val updatedList = currentList.map { entity ->
                    val officialMatch = officialTournaments.firstOrNull { it.id == entity.id }
                    val prize = if (officialMatch != null) {
                        officialMatch.winningPrize
                    } else if (entity.winningPrize.isNotBlank()) {
                        entity.winningPrize
                    } else {
                        com.example.data.model.TournamentPrizeRules.getDefaultPrizeForFee(entity.entryFee)
                    }
                    entity.copy(winningPrize = prize)
                }
                tournamentDao.insertTournaments(updatedList)
            }
        }

        // PURGE ALL DEMO/MOCK/SAMPLE REGISTRATIONS so all slots start 100% available!
        val legacyDemoIds = listOf(
            "ACE-20260917-001", "ACE-20260917-002", "ACE-20260917-003",
            "ACE-20260917-004", "ACE-20260917-005", "ACE-20260917-006",
            "ACE-20260917-007", "ACE-20260917-008", "ACE-20260917-009",
            "ACE-20260917-010", "ACE-20260917-011", "ACE-20260917-012",
            "ACE-20260917-013", "ACE-20260917-014", "ACE-20260917-015"
        )
        for (demoId in legacyDemoIds) {
            val reg = registrationDao.getRegistrationById(demoId)
            if (reg != null && reg.playerName in listOf("Aman Sharma", "Rahul Verma", "Vikram Singh", "Dev Patel", "Saurav Rao", "Deepak Gupta", "Karan Malhotra", "Naveen Yadav", "Vikrant Joshi", "Rohit Rajput", "Ajay Kumar", "Sandeep Rawat", "Manish Mehra", "Ankit Joshi", "Ramesh Rao")) {
                registrationDao.deleteRegistrationsByPlayer(reg.ffUid, reg.playerName)
            }
        }
        registrationDao.deleteRegistrationsByPlayer("1298471203", "Adarsh Gupta")
    }

    suspend fun resetAllData() {
        tournamentDao.clearAll()
        registrationDao.clearAll()
        notificationDao.clearAll()
        seedInitialDataIfEmpty()
    }
}

fun RegistrationEntity.toModel(): RegistrationItem = RegistrationItem(
    id = id,
    tournamentId = tournamentId,
    tournamentTime = tournamentTime,
    playerName = playerName,
    ffIgn = ffIgn,
    ffUid = ffUid,
    contactNumber = contactNumber,
    teamName = teamName,
    selectedSlot = selectedSlot,
    entryFee = entryFee,
    winningPrize = winningPrize,
    paymentRef = paymentRef,
    paymentScreenshotUrl = paymentScreenshotUrl,
    paymentStatus = paymentStatus,
    status = status,
    adminNotes = adminNotes,
    registeredAt = registeredAt
)

fun TournamentEntity.toModel(): TournamentItem = TournamentItem(
    id = id,
    title = title,
    date = date,
    startTime = startTime,
    startHour = startHour,
    startMinute = startMinute,
    entryFee = entryFee,
    winningPrize = winningPrize,
    matchType = matchType,
    map = map,
    matches = matches,
    maxSlots = maxSlots,
    minSlots = minSlots,
    status = status,
    roomId = roomId,
    roomPassword = roomPassword,
    revealMinutesBefore = revealMinutesBefore,
    registeredCount = 0,
    confirmedCount = 0
)

fun TournamentItem.toEntity(): TournamentEntity = TournamentEntity(
    id = id,
    title = title,
    date = date,
    startTime = startTime,
    startHour = startHour,
    startMinute = startMinute,
    entryFee = entryFee,
    winningPrize = winningPrize,
    matchType = matchType,
    map = map,
    matches = matches,
    maxSlots = maxSlots,
    minSlots = minSlots,
    status = status,
    roomId = roomId,
    roomPassword = roomPassword,
    revealMinutesBefore = revealMinutesBefore
)

fun NotificationEntity.toModel(): NotificationItem = NotificationItem(
    id = id,
    targetUid = targetUid,
    title = title,
    message = message,
    timestamp = timestamp,
    type = type,
    isRead = isRead,
    tournamentId = tournamentId
)

