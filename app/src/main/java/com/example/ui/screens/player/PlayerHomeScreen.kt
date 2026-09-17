package com.example.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegistrationItem
import com.example.data.model.TournamentItem
import com.example.data.model.TournamentStatus
import com.example.ui.components.EsportsCard
import com.example.ui.components.GoldButton
import com.example.ui.components.HeroHeader
import com.example.ui.components.SlotProgressBar
import com.example.ui.components.TournamentStatusBadge
import com.example.ui.theme.EmeraldConfirmed
import com.example.ui.theme.EsportsBlack
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceVariant
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RedRejected
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun PlayerHomeScreen(
    tournaments: List<TournamentItem>,
    myRegistrations: List<RegistrationItem>,
    onRegisterClick: (TournamentItem) -> Unit,
    onViewMyRegistration: (RegistrationItem) -> Unit,
    onSwitchToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EsportsBlack),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            HeroHeader(onAdminClick = onSwitchToAdmin)
        }

        item {
            // Tournament format & rules notice
            FormatRulesCard(modifier = Modifier.padding(16.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S TOURNAMENT SESSIONS",
                    color = GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${tournaments.size} SESSIONS",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        items(tournaments, key = { it.id }) { tournament ->
            val registeredForThis = myRegistrations.firstOrNull { it.tournamentId == tournament.id }
            TournamentSessionCard(
                tournament = tournament,
                userRegistration = registeredForThis,
                onRegisterClick = { onRegisterClick(tournament) },
                onViewRegistration = { registeredForThis?.let { onViewMyRegistration(it) } },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun FormatRulesCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EsportsSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFD700))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Tournament Rules",
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "OFFICIAL MATCH RULES & SLOTS",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• 20 Maximum Slots per session. Fast registration recommended.\n• Minimum 12 registrations required for match to commence.\n• Custom Room ID & Password automatically revealed exactly 5 minutes before scheduled match time.",
                    color = TextGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun TournamentSessionCard(
    tournament: TournamentItem,
    userRegistration: RegistrationItem?,
    onRegisterClick: () -> Unit,
    onViewRegistration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCancelled = tournament.status == TournamentStatus.CANCELLED
    val isFull = tournament.isFull || tournament.status == TournamentStatus.SLOTS_FULL
    val isRegistered = userRegistration != null

    EsportsCard(
        modifier = modifier,
        hasGlow = isRegistered
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Time & Entry Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isCancelled) RedRejected else GoldPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tournament.startTime,
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Entry Fee Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x33B8860B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Entry ₹${tournament.entryFee}",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub info: Mode, Map, Min Players
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${tournament.matchType} • ${tournament.map}",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                TournamentStatusBadge(
                    status = tournament.status,
                    isFull = tournament.isFull
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Slots Progress Bar
            SlotProgressBar(
                filled = tournament.registeredCount,
                max = tournament.maxSlots,
                minRequired = tournament.minSlots
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row
            when {
                isRegistered -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2610B981),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldConfirmed.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldConfirmed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "YOU ARE REGISTERED (${userRegistration.status.label})",
                                    color = EmeraldConfirmed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        GoldButton(
                            text = "VIEW DETAILS",
                            onClick = onViewRegistration,
                            modifier = Modifier.testTag("view_reg_${tournament.id}")
                        )
                    }
                }

                isCancelled -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x26EF4444),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedRejected.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "TOURNAMENT CANCELLED (Not Enough Registrations)",
                            color = RedRejected,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                isFull -> {
                    GoldButton(
                        text = "SLOTS FULL",
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slots_full_${tournament.id}")
                    )
                }

                else -> {
                    GoldButton(
                        text = "REGISTER NOW",
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_now_${tournament.id}"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = EsportsBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
