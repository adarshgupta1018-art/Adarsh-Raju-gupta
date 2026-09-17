package com.example.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegistrationItem
import com.example.data.model.TournamentItem
import com.example.data.model.TournamentStatus
import com.example.ui.theme.EsportsBlack
import com.example.ui.theme.EsportsSurfaceVariant
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

enum class TournamentFilter {
    ALL,
    OPEN,
    MY_MATCHES,
    CANCELLED
}

@Composable
fun PlayerTournamentsScreen(
    tournaments: List<TournamentItem>,
    myRegistrations: List<RegistrationItem>,
    onRegisterClick: (TournamentItem) -> Unit,
    onViewMyRegistration: (RegistrationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(TournamentFilter.ALL) }

    val filteredList = tournaments.filter { t ->
        when (selectedFilter) {
            TournamentFilter.ALL -> true
            TournamentFilter.OPEN -> t.status == TournamentStatus.OPEN
            TournamentFilter.MY_MATCHES -> myRegistrations.any { it.tournamentId == t.id }
            TournamentFilter.CANCELLED -> t.status == TournamentStatus.CANCELLED
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EsportsBlack),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp)
    ) {
        item {
            Column {
                Text(
                    text = "TOURNAMENT SCHEDULE",
                    color = GoldPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "SOLO Battle Royale Sessions • 20 Slots per lobby • ₹1 to ₹100 Entry",
                    color = TextGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        item {
            // Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTab("ALL", selected = selectedFilter == TournamentFilter.ALL) {
                    selectedFilter = TournamentFilter.ALL
                }
                FilterTab("OPEN", selected = selectedFilter == TournamentFilter.OPEN) {
                    selectedFilter = TournamentFilter.OPEN
                }
                FilterTab("MY MATCHES", selected = selectedFilter == TournamentFilter.MY_MATCHES) {
                    selectedFilter = TournamentFilter.MY_MATCHES
                }
                FilterTab("CANCELLED", selected = selectedFilter == TournamentFilter.CANCELLED) {
                    selectedFilter = TournamentFilter.CANCELLED
                }
            }
        }

        items(filteredList, key = { it.id }) { tournament ->
            val registeredForThis = myRegistrations.firstOrNull { it.tournamentId == tournament.id }
            TournamentSessionCard(
                tournament = tournament,
                userRegistration = registeredForThis,
                onRegisterClick = { onRegisterClick(tournament) },
                onViewRegistration = { registeredForThis?.let { onViewMyRegistration(it) } },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun FilterTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) GoldPrimary else EsportsSurfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) EsportsBlack else TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
