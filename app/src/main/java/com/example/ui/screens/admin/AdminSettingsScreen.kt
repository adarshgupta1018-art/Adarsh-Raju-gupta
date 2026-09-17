package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.components.GoldOutlinedButton
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminSettingsScreen(
    tournaments: List<TournamentItem>,
    currentTimeMillis: Long,
    isSimulated: Boolean,
    onResetRealTime: () -> Unit,
    onSimulateTime: (tournament: TournamentItem, minutesBefore: Int) -> Unit,
    onBroadcast: (title: String, message: String) -> Unit,
    onResetData: () -> Unit,
    onLogoutAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMessage by remember { mutableStateOf("") }

    val formattedTime = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date(currentTimeMillis))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EsportsBlack),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp)
    ) {
        item {
            Column {
                Text(
                    text = "ADMIN CONTROLS & TEST TOOLS",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Test 5-minute custom room reveal rule, send push announcements, and inspect backend architecture.",
                    color = TextGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Live Clock & Time Simulation
        item {
            EsportsCard(hasGlow = isSimulated) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "TIME SIMULATOR (5-MIN REVEAL TEST)", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSimulated) Color(0x337C3AED) else Color(0x3310B981)
                        ) {
                            Text(
                                text = if (isSimulated) "SIMULATED CLOCK" else "REAL TIME",
                                color = if (isSimulated) Color(0xFFDDD6FE) else EmeraldConfirmed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Current Clock: $formattedTime",
                        color = GoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Jump clock forward or backward relative to today's tournament sessions to immediately observe locked vs unlocked Custom Room credentials in real-time.",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val tournament7pm = tournaments.firstOrNull { it.startTime.contains("7:00") } ?: tournaments.firstOrNull()

                    if (tournament7pm != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            GoldOutlinedButton(
                                text = "🔒 Jump to 6:54 PM (1 min BEFORE unlock — LOCKED)",
                                onClick = { onSimulateTime(tournament7pm, 6) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            GoldButton(
                                text = "🎮 Jump to 6:55 PM (EXACT 5 MIN UNLOCK — REVEALED)",
                                onClick = { onSimulateTime(tournament7pm, 5) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            GoldOutlinedButton(
                                text = "⚔️ Jump to 6:58 PM (Match in 2 mins — LIVE REVEALED)",
                                onClick = { onSimulateTime(tournament7pm, 2) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (isSimulated) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onResetRealTime,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E303D),
                                contentColor = GoldLight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RESTORE REAL SYSTEM CLOCK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Broadcast announcement card
        item {
            EsportsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "BROADCAST PLAYER ANNOUNCEMENT", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text("Announcement Title", color = TextMuted, fontSize = 11.sp) },
                        placeholder = { Text("e.g. 7:00 PM Room ID will drop in 10 mins!", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF333544),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = broadcastMessage,
                        onValueChange = { broadcastMessage = it },
                        label = { Text("Announcement Body", color = TextMuted, fontSize = 11.sp) },
                        placeholder = { Text("All confirmed players please open Free Fire...", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF333544),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GoldButton(
                        text = "DISPATCH BROADCAST NOTIFICATION",
                        onClick = {
                            if (broadcastTitle.isNotBlank() && broadcastMessage.isNotBlank()) {
                                onBroadcast(broadcastTitle, broadcastMessage)
                                broadcastTitle = ""
                                broadcastMessage = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dispatch_broadcast_btn")
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Database Reset & Logout
        item {
            EsportsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "SYSTEM MAINTENANCE", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Reset all Room Database registrations, test sessions, and seed data to default.", color = TextGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onResetData,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x33EF4444),
                                contentColor = RedRejected
                            ),
                            border = BorderStroke(1.dp, RedRejected),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("RESET TEST DATA", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onLogoutAdmin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF272733),
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("EXIT ADMIN", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
