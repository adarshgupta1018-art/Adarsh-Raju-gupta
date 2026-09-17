package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.UserRole
import com.example.ui.components.SimulatedClockBanner
import com.example.ui.navigation.AdminBottomBar
import com.example.ui.navigation.PlayerBottomBar
import com.example.ui.navigation.TopEsportsBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminLoginDialog
import com.example.ui.screens.admin.AdminRegistrationsScreen
import com.example.ui.screens.admin.AdminRoomManagementScreen
import com.example.ui.screens.admin.AdminSettingsScreen
import com.example.ui.screens.admin.AdminSlotManagementScreen
import com.example.ui.screens.player.NotificationsScreen
import com.example.ui.screens.player.PlayerDashboardScreen
import com.example.ui.screens.player.PlayerHomeScreen
import com.example.ui.screens.player.PlayerTournamentsScreen
import com.example.ui.screens.player.ProfileScreen
import com.example.ui.screens.player.RegistrationDialog
import com.example.ui.theme.EsportsBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AdminNavTab
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.PlayerNavTab

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsState()
    val playerTab by viewModel.playerTab.collectAsState()
    val adminTab by viewModel.adminTab.collectAsState()

    val currentUser by viewModel.currentUser.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()
    val allRegistrations by viewModel.allRegistrations.collectAsState()
    val playerRegistrations by viewModel.playerRegistrations.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifsCount by viewModel.unreadNotificationsCount.collectAsState()
    val dashboardStats by viewModel.dashboardStats.collectAsState()

    val selectedTournamentForReg by viewModel.selectedTournamentForRegistration.collectAsState()
    val simulatedTimeOverride by viewModel.simulatedTimeOverrideMillis.collectAsState()
    val currentTickerMillis by viewModel.currentTickerTimeMillis.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()
    val activeRoomCredentials by viewModel.activeRoomCredentials.collectAsState()

    var showAdminLoginDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val isAdminMode = currentRole == UserRole.ADMIN && isAdminAuthenticated

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(EsportsBlack),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                TopEsportsBar(
                    title = "ACE ESPORTS",
                    isAdminMode = isAdminMode,
                    onAdminToggleClick = {
                        if (isAdminMode) {
                            viewModel.logoutAdmin()
                        } else {
                            if (isAdminAuthenticated) {
                                viewModel.requestAdminPanel()
                            } else {
                                showAdminLoginDialog = true
                            }
                        }
                    }
                )

                // Simulated Clock banner if test time is overridden
                SimulatedClockBanner(
                    simulatedTimeOverride = simulatedTimeOverride,
                    onResetClick = { viewModel.resetToRealTime() }
                )
            }
        },
        bottomBar = {
            if (isAdminMode) {
                AdminBottomBar(
                    currentTab = adminTab,
                    pendingCount = dashboardStats.pendingRegistrations,
                    onTabSelected = { viewModel.setAdminTab(it) }
                )
            } else {
                PlayerBottomBar(
                    currentTab = playerTab,
                    unreadCount = unreadNotifsCount,
                    onTabSelected = { viewModel.setPlayerTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(EsportsBlack)
        ) {
            if (isAdminMode) {
                // ADMIN PANEL SCREENS
                when (adminTab) {
                    AdminNavTab.DASHBOARD -> AdminDashboardScreen(
                        stats = dashboardStats,
                        tournaments = tournaments,
                        onNavigateToRegistrations = { viewModel.setAdminTab(AdminNavTab.REGISTRATIONS) },
                        onNavigateToRooms = { viewModel.setAdminTab(AdminNavTab.ROOM_MANAGEMENT) }
                    )
                    AdminNavTab.REGISTRATIONS -> AdminRegistrationsScreen(
                        registrations = allRegistrations,
                        onConfirm = { viewModel.confirmRegistration(it) },
                        onReject = { viewModel.rejectRegistration(it) }
                    )
                    AdminNavTab.ROOM_MANAGEMENT -> AdminRoomManagementScreen(
                        tournaments = tournaments,
                        currentTimeMillis = currentTickerMillis,
                        onSaveRoomDetails = { tourneyId, roomId, roomPass ->
                            viewModel.updateCustomRoom(tourneyId, roomId, roomPass)
                        }
                    )
                    AdminNavTab.TOURNAMENTS -> AdminSlotManagementScreen(
                        tournaments = tournaments,
                        onCancelTournament = { viewModel.cancelTournament(it) },
                        onReopenTournament = { viewModel.reopenTournament(it) }
                    )
                    AdminNavTab.SETTINGS, AdminNavTab.NOTIFICATIONS -> AdminSettingsScreen(
                        tournaments = tournaments,
                        currentTimeMillis = currentTickerMillis,
                        isSimulated = simulatedTimeOverride != null,
                        onResetRealTime = { viewModel.resetToRealTime() },
                        onSimulateTime = { tourney, mins ->
                            viewModel.setSimulatedTimeForTournament(tourney, mins)
                        },
                        onBroadcast = { title, msg ->
                            viewModel.sendAdminBroadcast(title, msg)
                        },
                        onResetData = { viewModel.resetAllDataToDefault() },
                        onLogoutAdmin = { viewModel.logoutAdmin() }
                    )
                }
            } else {
                // PLAYER PANEL SCREENS
                when (playerTab) {
                    PlayerNavTab.HOME -> PlayerHomeScreen(
                        tournaments = tournaments,
                        myRegistrations = playerRegistrations,
                        onRegisterClick = { viewModel.openRegistrationDialog(it) },
                        onViewMyRegistration = { viewModel.setPlayerTab(PlayerNavTab.MY_REGISTRATIONS) },
                        onSwitchToAdmin = {
                            if (isAdminAuthenticated) {
                                viewModel.requestAdminPanel()
                            } else {
                                showAdminLoginDialog = true
                            }
                        }
                    )
                    PlayerNavTab.TOURNAMENTS -> PlayerTournamentsScreen(
                        tournaments = tournaments,
                        myRegistrations = playerRegistrations,
                        onRegisterClick = { viewModel.openRegistrationDialog(it) },
                        onViewMyRegistration = { viewModel.setPlayerTab(PlayerNavTab.MY_REGISTRATIONS) }
                    )
                    PlayerNavTab.MY_REGISTRATIONS -> PlayerDashboardScreen(
                        registrations = playerRegistrations,
                        tournaments = tournaments,
                        activeRoomCredentials = activeRoomCredentials,
                        currentTickerMillis = currentTickerMillis,
                        onFetchRoomDetails = { viewModel.checkAndFetchRoomCredentials(it) },
                        onSimulateTestTime = { tourney, mins ->
                            viewModel.setSimulatedTimeForTournament(tourney, mins)
                        },
                        onToast = { viewModel.showSnackbar(it) }
                    )
                    PlayerNavTab.NOTIFICATIONS -> NotificationsScreen(
                        notifications = notifications,
                        onMarkRead = { viewModel.markNotificationRead(it) },
                        onMarkAllRead = { viewModel.markAllNotificationsRead() }
                    )
                    PlayerNavTab.PROFILE -> ProfileScreen(
                        user = currentUser,
                        onSaveProfile = { name, ign, uid, phone ->
                            viewModel.updateUserProfile(name, ign, uid, phone)
                        },
                        onOpenAdminPanel = {
                            if (isAdminAuthenticated) {
                                viewModel.requestAdminPanel()
                            } else {
                                showAdminLoginDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    // Modal Registration Form Dialog
    selectedTournamentForReg?.let { tournament ->
        RegistrationDialog(
            tournament = tournament,
            currentUser = currentUser,
            onDismiss = { viewModel.closeRegistrationDialog() },
            onSubmit = { fullName, ffIgn, ffUid, phone, paymentRef ->
                viewModel.submitRegistration(fullName, ffIgn, ffUid, phone, paymentRef)
            }
        )
    }

    // Admin Login Dialog
    if (showAdminLoginDialog) {
        AdminLoginDialog(
            onDismiss = { showAdminLoginDialog = false },
            onLogin = { pass ->
                val success = viewModel.authenticateAdmin(pass)
                if (success) {
                    showAdminLoginDialog = false
                }
                success
            }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
