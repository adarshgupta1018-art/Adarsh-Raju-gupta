package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TournamentItem
import com.example.ui.components.EsportsCard
import com.example.ui.components.GoldButton
import com.example.ui.theme.AmberPending
import com.example.ui.theme.EmeraldConfirmed
import com.example.ui.theme.EsportsBlack
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceVariant
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RedRejected
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.Calendar

@Composable
fun AdminRoomManagementScreen(
    tournaments: List<TournamentItem>,
    currentTimeMillis: Long,
    onSaveRoomDetails: (tournamentId: String, roomId: String, roomPassword: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EsportsBlack),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp)
    ) {
        item {
            Column {
                Text(
                    text = "CUSTOM ROOM ADMIN SYSTEM",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Manually enter the Free Fire Custom Room ID and Password before each tournament session.",
                    color = TextGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            // Security Feature banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x1AFFD700),
                border = BorderStroke(1.dp, Color(0x40FFD700)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Notice",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AUTOMATIC 5-MINUTE REVEAL RULE",
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Room credentials saved here remain strictly locked on the backend. They become visible to confirmed players exactly 5 minutes before scheduled match start time.",
                            color = TextWhite,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        items(tournaments, key = { it.id }) { tournament ->
            TournamentRoomEntryCard(
                tournament = tournament,
                currentTimeMillis = currentTimeMillis,
                onSave = { roomId, roomPassword ->
                    onSaveRoomDetails(tournament.id, roomId, roomPassword)
                },
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }
    }
}

@Composable
fun TournamentRoomEntryCard(
    tournament: TournamentItem,
    currentTimeMillis: Long,
    onSave: (roomId: String, roomPassword: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var roomIdInput by remember(tournament.roomId) { mutableStateOf(tournament.roomId) }
    var passwordInput by remember(tournament.roomPassword) { mutableStateOf(tournament.roomPassword) }
    var isSavedLocally by remember { mutableStateOf(false) }

    // Calculate reveal time
    val revealMinute = if (tournament.startMinute >= 5) tournament.startMinute - 5 else 60 + (tournament.startMinute - 5)
    val revealHour = if (tournament.startMinute >= 5) tournament.startHour else tournament.startHour - 1
    val revealTimeString = String.format("%d:%02d %s", if (revealHour > 12) revealHour - 12 else revealHour, revealMinute, if (revealHour >= 12) "PM" else "AM")

    // Check if time has crossed reveal time
    val cal = Calendar.getInstance().apply {
        timeInMillis = currentTimeMillis
        set(Calendar.HOUR_OF_DAY, tournament.startHour)
        set(Calendar.MINUTE, tournament.startMinute)
        set(Calendar.SECOND, 0)
        add(Calendar.MINUTE, -5)
    }
    val isRevealedNow = currentTimeMillis >= cal.timeInMillis

    EsportsCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Session & Reveal Timing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOURNAMENT: ${tournament.id}",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Start: ${tournament.startTime} • Entry: ₹${tournament.entryFee} • ${tournament.map}",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isRevealedNow) Color(0x3310B981) else Color(0x33F59E0B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isRevealedNow) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isRevealedNow) EmeraldConfirmed else AmberPending,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRevealedNow) "UNLOCKED" else "LOCKED",
                            color = if (isRevealedNow) EmeraldConfirmed else AmberPending,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reveal calculation info
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = EsportsSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Auto-Reveal Scheduled At:",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$revealTimeString (5m prior)",
                        color = TextGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = roomIdInput,
                    onValueChange = {
                        roomIdInput = it
                        isSavedLocally = false
                    },
                    label = { Text("CUSTOM ROOM ID", color = TextMuted, fontSize = 10.sp) },
                    placeholder = { Text("e.g. 123456789", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("room_id_input_${tournament.id}"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF333544),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = EsportsSurfaceVariant,
                        unfocusedContainerColor = EsportsSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        isSavedLocally = false
                    },
                    label = { Text("PASSWORD", color = TextMuted, fontSize = 10.sp) },
                    placeholder = { Text("e.g. ABC123", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("room_pass_input_${tournament.id}"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF333544),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = EsportsSurfaceVariant,
                        unfocusedContainerColor = EsportsSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            GoldButton(
                text = if (isSavedLocally || (tournament.roomId.isNotBlank() && tournament.roomId == roomIdInput && tournament.roomPassword == passwordInput)) "SAVED & SECURED" else "SAVE ROOM DETAILS",
                onClick = {
                    onSave(roomIdInput, passwordInput)
                    isSavedLocally = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_room_btn_${tournament.id}"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = EsportsBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}
