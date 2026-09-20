package com.example.ui.screens.player

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegistrationItem
import com.example.data.model.RegistrationStatus
import com.example.data.model.RoomCredentials
import com.example.data.model.TournamentItem
import com.example.data.model.TournamentStatus
import com.example.ui.components.EsportsCard
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldConfirmed
import com.example.ui.theme.EsportsBlack
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceElevated
import com.example.ui.theme.EsportsSurfaceVariant
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RedRejected
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.concurrent.TimeUnit

@Composable
fun PlayerDashboardScreen(
    registrations: List<RegistrationItem>,
    tournaments: List<TournamentItem>,
    activeRoomCredentials: RoomCredentials?,
    currentTickerMillis: Long,
    onFetchRoomDetails: (String) -> Unit,
    onSimulateTestTime: (TournamentItem, Int) -> Unit,
    onToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EsportsBlack),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp)
    ) {
        item {
            // Header
            Column {
                Text(
                    text = "PLAYER DASHBOARD",
                    color = GoldPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Track your registrations, confirmation status, and live match room credentials.",
                    color = TextGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (registrations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EsportsSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Registrations Found",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Go to the Tournaments tab and register for today's matches to get your entry confirmed.",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(registrations, key = { it.id }) { reg ->
                val tournament = tournaments.firstOrNull { it.id == reg.tournamentId }
                RegistrationCard(
                    registration = reg,
                    tournament = tournament,
                    activeRoomCredentials = if (activeRoomCredentials?.tournamentId == reg.tournamentId) activeRoomCredentials else null,
                    currentTickerMillis = currentTickerMillis,
                    onFetchRoom = { onFetchRoomDetails(reg.tournamentId) },
                    onSimulateUnlock = {
                        tournament?.let { onSimulateTestTime(it, 5) }
                    },
                    onCopy = { label, value ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(label, value)
                        clipboard.setPrimaryClip(clip)
                        onToast("$label copied to clipboard!")
                    },
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }
        }
    }
}

@Composable
fun RegistrationCard(
    registration: RegistrationItem,
    tournament: TournamentItem?,
    activeRoomCredentials: RoomCredentials?,
    currentTickerMillis: Long,
    onFetchRoom: () -> Unit,
    onSimulateUnlock: () -> Unit,
    onCopy: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Poll room credentials when card is rendered
    LaunchedEffect(registration.tournamentId, currentTickerMillis) {
        if (registration.status == RegistrationStatus.CONFIRMED) {
            onFetchRoom()
        }
    }

    val isConfirmed = registration.status == RegistrationStatus.CONFIRMED
    val isCancelled = registration.status == RegistrationStatus.CANCELLED || tournament?.status == TournamentStatus.CANCELLED

    EsportsCard(
        modifier = modifier,
        hasGlow = isConfirmed
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Registration ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REGISTRATION ID",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = registration.id,
                        color = GoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                StatusBadge(status = registration.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player & Tournament Details Grid
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = EsportsSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "PLAYER NAME", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = registration.playerName, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "FREE FIRE IGN", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = registration.ffIgn, color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "FREE FIRE UID", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = registration.ffUid, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "TOURNAMENT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = tournament?.title ?: "SOLO BR MATCH", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "MATCH TIME", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = registration.tournamentTime, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "ENTRY FEE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "₹${registration.entryFee}", color = EmeraldConfirmed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status message
            when {
                isCancelled -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x26EF4444),
                        border = BorderStroke(1.dp, RedRejected.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tournament Cancelled — (Fewer than 12 registrations or match called off).",
                            color = RedRejected,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                isConfirmed -> {
                    // Confirmed Banner
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x2610B981),
                        border = BorderStroke(1.dp, EmeraldConfirmed.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldConfirmed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "YOU ARE CONFIRMED FOR THIS MATCH",
                                color = EmeraldConfirmed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // CUSTOM ROOM CREDENTIALS SECTION
                    CustomRoomDetailsBox(
                        credentials = activeRoomCredentials,
                        onCopy = onCopy,
                        onSimulateTest = onSimulateUnlock
                    )
                }

                registration.status == RegistrationStatus.PENDING -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x1AFBBF24),
                        border = BorderStroke(1.dp, Color(0x40FBBF24)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⏳ Awaiting Admin Confirmation. Room details will be revealed once confirmed by the ACE ESPORTS admin team.",
                            color = Color(0xFFFDE68A),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                registration.status == RegistrationStatus.REJECTED -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x26EF4444),
                        border = BorderStroke(1.dp, RedRejected.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "❌ Registration Rejected. Please contact Admin or check payment details.",
                            color = RedRejected,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomRoomDetailsBox(
    credentials: RoomCredentials?,
    onCopy: (String, String) -> Unit,
    onSimulateTest: () -> Unit
) {
    val isRevealed = credentials?.isRevealed == true && credentials.roomId.isNotBlank() && credentials.roomId != "WAITING_ADMIN"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRevealed) Color(0xFF142217) else EsportsSurfaceElevated
        ),
        border = BorderStroke(
            1.2.dp,
            if (isRevealed) EmeraldConfirmed else Color(0x4DFFD700)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (isRevealed) {
                // ROOM OPEN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Room Open",
                            tint = EmeraldConfirmed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎮 CUSTOM ROOM OPEN",
                            color = EmeraldConfirmed,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x3310B981)
                    ) {
                        Text(
                            text = "LIVE NOW",
                            color = EmeraldConfirmed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ROOM ID
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EsportsSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "ROOM ID", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = credentials?.roomId ?: "",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    GoldOutlinedButton(
                        text = "COPY ROOM ID",
                        onClick = { onCopy("Room ID", credentials?.roomId ?: "") },
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("copy_room_id_btn"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // PASSWORD
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EsportsSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "PASSWORD", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = credentials?.roomPassword ?: "",
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    GoldOutlinedButton(
                        text = "COPY PASSWORD",
                        onClick = { onCopy("Room Password", credentials?.roomPassword ?: "") },
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("copy_password_btn"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "🔥 Join the room in Free Fire immediately before match start!",
                    color = EmeraldConfirmed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

            } else {
                // ROOM LOCKED
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Room Locked",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔒 ROOM DETAILS LOCKED",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "“Room ID & Password will be revealed 5 minutes before the match.”",
                    color = TextGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                if (credentials != null && credentials.millisUntilReveal > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(credentials.millisUntilReveal)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(credentials.millisUntilReveal) % 60
                    Text(
                        text = "⏳ Unlocks in: ${minutes}m ${seconds}s",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Convenient test button so user can test the 5-min unlock instantly
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x337C3AED),
                    border = BorderStroke(1.dp, Color(0x66A78BFA)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSimulateTest() }
                ) {
                    Text(
                        text = "⚡ QUICK TEST: Simulate Clock to 5 Min Before Match",
                        color = Color(0xFFDDD6FE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
