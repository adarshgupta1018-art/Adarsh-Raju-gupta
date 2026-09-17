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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TournamentItem
import com.example.data.model.TournamentStatus
import com.example.ui.components.EsportsCard
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.components.SlotProgressBar
import com.example.ui.components.TournamentStatusBadge
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

@Composable
fun AdminSlotManagementScreen(
    tournaments: List<TournamentItem>,
    onCancelTournament: (tournamentId: String) -> Unit,
    onReopenTournament: (tournamentId: String) -> Unit,
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
                    text = "SLOT MANAGEMENT",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Lobby cap (20 slots max), minimum player thresholds (12 players), and cancellation controls.",
                    color = TextGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        items(tournaments, key = { it.id }) { tournament ->
            AdminSlotCard(
                tournament = tournament,
                onCancel = { onCancelTournament(tournament.id) },
                onReopen = { onReopenTournament(tournament.id) },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun AdminSlotCard(
    tournament: TournamentItem,
    onCancel: () -> Unit,
    onReopen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCancelled = tournament.status == TournamentStatus.CANCELLED
    val hasEnoughPlayers = tournament.registeredCount >= tournament.minSlots

    EsportsCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tournament.startTime,
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(Entry ₹${tournament.entryFee})",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TournamentStatusBadge(status = tournament.status, isFull = tournament.isFull)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slot Progress Bar
            SlotProgressBar(
                filled = tournament.registeredCount,
                max = tournament.maxSlots,
                minRequired = tournament.minSlots
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Slot breakdown info
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EsportsSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "AVAILABLE SLOTS", color = TextMuted, fontSize = 9.sp)
                        Text(text = "${tournament.availableSlots} Left", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Column {
                        Text(text = "CONFIRMED", color = TextMuted, fontSize = 9.sp)
                        Text(text = "${tournament.confirmedCount} / 20", color = EmeraldConfirmed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "MIN THRESHOLD", color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = if (hasEnoughPlayers) "READY (≥12)" else "NEED ${tournament.minSlots - tournament.registeredCount} MORE",
                            color = if (hasEnoughPlayers) EmeraldConfirmed else AmberPending,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Admin Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isCancelled) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x33EF4444),
                            contentColor = RedRejected
                        ),
                        border = BorderStroke(1.dp, RedRejected),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_tournament_${tournament.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CANCEL (LOW PLAYERS)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onReopen,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldConfirmed,
                            contentColor = EsportsBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reopen_tournament_${tournament.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("REOPEN TOURNAMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
