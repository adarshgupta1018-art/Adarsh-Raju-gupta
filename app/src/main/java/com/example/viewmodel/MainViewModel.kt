package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.NotificationItem
import com.example.data.model.RegistrationItem
import com.example.data.model.RegistrationStatus
import com.example.data.model.RoomCredentials
import com.example.data.model.TournamentItem
import com.example.data.model.TournamentStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.TournamentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class PlayerNavTab {
    HOME,
    TOURNAMENTS,
    MY_REGISTRATIONS,
    NOTIFICATIONS,
    PROFILE
}

enum class AdminNavTab {
    DASHBOARD,
    TOURNAMENTS,
    REGISTRATIONS,
    ROOM_MANAGEMENT,
    NOTIFICATIONS,
    SETTINGS
}

data class DashboardStats(
    val todayTournamentsCount: Int = 8,
    val totalRegistrations: Int = 0,
    val pendingRegistrations: Int = 0,
    val confirmedPlayers: Int = 0,
    val totalAvailableSlots: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TournamentRepository(
        tournamentDao = database.tournamentDao(),
        registrationDao = database.registrationDao(),
        notificationDao = database.notificationDao()
    )

    // Current User
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "usr_001",
            name = "Adarsh Gupta",
            ffIgn = "ACE_ADARSH_99",
            ffUid = "1298471203",
            contactNumber = "+91 9811223344",
            role = UserRole.PLAYER
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Panel State (Player vs Admin)
    private val _currentRole = MutableStateFlow(UserRole.PLAYER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    // Nav Tabs
    private val _playerTab = MutableStateFlow(PlayerNavTab.HOME)
    val playerTab: StateFlow<PlayerNavTab> = _playerTab.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminNavTab.DASHBOARD)
    val adminTab: StateFlow<AdminNavTab> = _adminTab.asStateFlow()

    // Selected Tournament for Registration dialog
    private val _selectedTournamentForRegistration = MutableStateFlow<TournamentItem?>(null)
    val selectedTournamentForRegistration: StateFlow<TournamentItem?> = _selectedTournamentForRegistration.asStateFlow()

    // Selected Registration for detail dialog
    private val _selectedRegistrationDetail = MutableStateFlow<RegistrationItem?>(null)
    val selectedRegistrationDetail: StateFlow<RegistrationItem?> = _selectedRegistrationDetail.asStateFlow()

    // Simulated Clock / Time-Travel (to test 5-min reveal immediately)
    // 0L means real system time
    private val _simulatedTimeOverrideMillis = MutableStateFlow<Long?>(null)
    val simulatedTimeOverrideMillis: StateFlow<Long?> = _simulatedTimeOverrideMillis.asStateFlow()

    // Live clock ticker
    private val _currentTickerTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTickerTimeMillis: StateFlow<Long> = _currentTickerTimeMillis.asStateFlow()

    // UI Message / Toast banner
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Room Credentials for the selected active match
    private val _activeRoomCredentials = MutableStateFlow<RoomCredentials?>(null)
    val activeRoomCredentials: StateFlow<RoomCredentials?> = _activeRoomCredentials.asStateFlow()

    // Data Streams from Room
    val tournaments: StateFlow<List<TournamentItem>> = repository.allTournaments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allRegistrations: StateFlow<List<RegistrationItem>> = repository.allRegistrations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val playerRegistrations: StateFlow<List<RegistrationItem>> = combine(
        repository.allRegistrations,
        _currentUser
    ) { regs, user ->
        regs.filter { it.ffUid == user.ffUid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<NotificationItem>> = combine(
        database.notificationDao().getAllNotifications(),
        _currentUser,
        _currentRole
    ) { notifs, user, role ->
        if (role == UserRole.ADMIN) {
            notifs.map { NotificationItem(it.id, it.targetUid, it.title, it.message, it.timestamp, it.type, it.isRead, it.tournamentId) }
        } else {
            notifs.filter { it.targetUid == user.ffUid || it.targetUid == "ALL" }
                .map { NotificationItem(it.id, it.targetUid, it.title, it.message, it.timestamp, it.type, it.isRead, it.tournamentId) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationsCount: StateFlow<Int> = notifications.combine(_currentUser) { list, _ ->
        list.count { !it.isRead }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Admin Dashboard Stats
    val dashboardStats: StateFlow<DashboardStats> = combine(
        tournaments,
        allRegistrations
    ) { tourneys, regs ->
        val totalRegs = regs.size
        val pending = regs.count { it.status == RegistrationStatus.PENDING }
        val confirmed = regs.count { it.status == RegistrationStatus.CONFIRMED }
        val available = tourneys.sumOf { it.availableSlots }

        DashboardStats(
            todayTournamentsCount = tourneys.size,
            totalRegistrations = totalRegs,
            pendingRegistrations = pending,
            confirmedPlayers = confirmed,
            totalAvailableSlots = available
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        // Clock ticker every 1 second
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTickerTimeMillis.value = _simulatedTimeOverrideMillis.value ?: System.currentTimeMillis()
            }
        }
    }

    fun getEffectiveCurrentTime(): Long {
        return _simulatedTimeOverrideMillis.value ?: System.currentTimeMillis()
    }

    // Role Switching
    fun switchToPlayerPanel() {
        _currentRole.value = UserRole.PLAYER
    }

    fun requestAdminPanel() {
        if (_isAdminAuthenticated.value) {
            _currentRole.value = UserRole.ADMIN
        } else {
            // Needs authentication
            _currentRole.value = UserRole.ADMIN
        }
    }

    fun authenticateAdmin(password: String): Boolean {
        // Preset admin credentials: admin123, 7777, or aceadmin
        val isValid = password == "admin123" || password == "7777" || password == "aceadmin"
        if (isValid) {
            _isAdminAuthenticated.value = true
            _currentRole.value = UserRole.ADMIN
            showSnackbar("Admin logged in successfully")
            return true
        } else {
            showSnackbar("Incorrect admin passcode. Try 'admin123' or '7777'")
            return false
        }
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
        _currentRole.value = UserRole.PLAYER
        _playerTab.value = PlayerNavTab.HOME
        showSnackbar("Logged out of Admin Panel")
    }

    fun setPlayerTab(tab: PlayerNavTab) {
        _playerTab.value = tab
    }

    fun setAdminTab(tab: AdminNavTab) {
        _adminTab.value = tab
    }

    fun openRegistrationDialog(tournament: TournamentItem) {
        if (tournament.status == TournamentStatus.CANCELLED) {
            showSnackbar("This tournament session has been cancelled")
            return
        }
        if (tournament.isFull) {
            showSnackbar("Slots are full for ${tournament.startTime}")
            return
        }
        _selectedTournamentForRegistration.value = tournament
    }

    fun closeRegistrationDialog() {
        _selectedTournamentForRegistration.value = null
    }

    fun openRegistrationDetail(registration: RegistrationItem) {
        _selectedRegistrationDetail.value = registration
    }

    fun closeRegistrationDetail() {
        _selectedRegistrationDetail.value = null
    }

    fun submitRegistration(
        fullName: String,
        ffIgn: String,
        ffUid: String,
        phone: String,
        paymentRef: String
    ) {
        val tournament = _selectedTournamentForRegistration.value ?: return

        if (fullName.isBlank() || ffIgn.isBlank() || ffUid.isBlank() || phone.isBlank()) {
            showSnackbar("Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            val result = repository.registerPlayer(
                tournamentId = tournament.id,
                playerName = fullName,
                ffIgn = ffIgn,
                ffUid = ffUid,
                contactNumber = phone,
                paymentRef = paymentRef
            )

            result.onSuccess { reg ->
                // Update current user profile with entered details
                _currentUser.value = _currentUser.value.copy(
                    name = fullName,
                    ffIgn = ffIgn,
                    ffUid = ffUid,
                    contactNumber = phone
                )
                _selectedTournamentForRegistration.value = null
                showSnackbar("Registration Submitted! ID: ${reg.id} (Status: PENDING)")
                _playerTab.value = PlayerNavTab.MY_REGISTRATIONS
            }.onFailure { err ->
                showSnackbar(err.message ?: "Failed to register")
            }
        }
    }

    fun confirmRegistration(regId: String) {
        viewModelScope.launch {
            repository.confirmRegistration(regId)
            showSnackbar("Registration confirmed! Player notified.")
        }
    }

    fun rejectRegistration(regId: String, reason: String = "Verification failed") {
        viewModelScope.launch {
            repository.rejectRegistration(regId, reason)
            showSnackbar("Registration rejected.")
        }
    }

    fun updateCustomRoom(tournamentId: String, roomId: String, roomPassword: String) {
        if (roomId.isBlank() || roomPassword.isBlank()) {
            showSnackbar("Room ID and Password cannot be blank")
            return
        }
        viewModelScope.launch {
            repository.updateCustomRoomDetails(tournamentId, roomId, roomPassword)
            showSnackbar("Room details saved! 5-min automatic unlock active.")
        }
    }

    fun cancelTournament(tournamentId: String, reason: String = "Not enough registrations") {
        viewModelScope.launch {
            repository.cancelTournament(tournamentId, reason)
            showSnackbar("Tournament cancelled. Players notified.")
        }
    }

    fun reopenTournament(tournamentId: String) {
        viewModelScope.launch {
            repository.reopenTournament(tournamentId)
            showSnackbar("Tournament reopened for registrations.")
        }
    }

    fun checkAndFetchRoomCredentials(tournamentId: String) {
        viewModelScope.launch {
            val creds = repository.getSecureRoomCredentials(
                tournamentId = tournamentId,
                playerUid = _currentUser.value.ffUid,
                simulatedTimeMillis = getEffectiveCurrentTime()
            )
            _activeRoomCredentials.value = creds
        }
    }

    fun clearActiveRoomCredentials() {
        _activeRoomCredentials.value = null
    }

    // Time Simulation controls for easy testing of the 5-minute reveal
    fun setSimulatedTimeForTournament(tournament: TournamentItem, minutesBefore: Int) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, tournament.startHour)
            set(Calendar.MINUTE, tournament.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -minutesBefore)
        }
        _simulatedTimeOverrideMillis.value = cal.timeInMillis
        _currentTickerTimeMillis.value = cal.timeInMillis
        showSnackbar("Simulated Time set to ${cal.get(Calendar.HOUR)}:${cal.get(Calendar.MINUTE).toString().padStart(2, '0')} ($minutesBefore min before ${tournament.startTime})")
        checkAndFetchRoomCredentials(tournament.id)
    }

    fun resetToRealTime() {
        _simulatedTimeOverrideMillis.value = null
        _currentTickerTimeMillis.value = System.currentTimeMillis()
        showSnackbar("Clock reset to Real-Time")
    }

    fun updateUserProfile(name: String, ffIgn: String, ffUid: String, phone: String) {
        _currentUser.value = _currentUser.value.copy(
            name = name,
            ffIgn = ffIgn,
            ffUid = ffUid,
            contactNumber = phone
        )
        showSnackbar("Profile updated successfully")
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(_currentUser.value.ffUid)
            showSnackbar("All notifications marked as read")
        }
    }

    fun sendAdminBroadcast(title: String, message: String) {
        if (title.isBlank() || message.isBlank()) return
        viewModelScope.launch {
            database.notificationDao().insertNotification(
                com.example.data.local.NotificationEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    targetUid = "ALL",
                    title = title.trim(),
                    message = message.trim(),
                    timestamp = System.currentTimeMillis(),
                    type = com.example.data.model.NotificationType.ADMIN_ANNOUNCEMENT,
                    isRead = false,
                    tournamentId = null
                )
            )
            showSnackbar("Announcement broadcasted to all players!")
        }
    }

    fun resetAllDataToDefault() {
        viewModelScope.launch {
            repository.resetAllData()
            showSnackbar("Database reset to default realistic seed data")
        }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
