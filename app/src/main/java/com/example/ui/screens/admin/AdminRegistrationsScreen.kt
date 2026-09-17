package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.RegistrationItem
import com.example.data.model.RegistrationStatus
import com.example.ui.components.EsportsCard
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.components.StatusBadge
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminRegistrationsScreen(
    registrations: List<RegistrationItem>,
    onConfirm: (String) -> Unit,
    onReject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatusFilter by remember { mutableStateOf<RegistrationStatus?>(null) }
    var selectedRegistrationForDetail by remember { mutableStateOf<RegistrationItem?>(null) }

    val filteredList = registrations.filter { reg ->
        selectedStatusFilter == null || reg.status == selectedStatusFilter
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
                    text = "REGISTRATION MANAGEMENT",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Verify player UIDs and confirm or reject tournament registrations.",
                    color = TextGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        item {
            // Status Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AdminFilterChip("ALL (${registrations.size})", selected = selectedStatusFilter == null) {
                    selectedStatusFilter = null
                }
                AdminFilterChip(
                    "PENDING (${registrations.count { it.status == RegistrationStatus.PENDING }})",
                    selected = selectedStatusFilter == RegistrationStatus.PENDING,
                    accentColor = AmberPending
                ) {
                    selectedStatusFilter = RegistrationStatus.PENDING
                }
                AdminFilterChip(
                    "CONFIRMED (${registrations.count { it.status == RegistrationStatus.CONFIRMED }})",
                    selected = selectedStatusFilter == RegistrationStatus.CONFIRMED,
                    accentColor = EmeraldConfirmed
                ) {
                    selectedStatusFilter = RegistrationStatus.CONFIRMED
                }
                AdminFilterChip(
                    "REJECTED (${registrations.count { it.status == RegistrationStatus.REJECTED }})",
                    selected = selectedStatusFilter == RegistrationStatus.REJECTED,
                    accentColor = RedRejected
                ) {
                    selectedStatusFilter = RegistrationStatus.REJECTED
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = EsportsSurface)
                ) {
                    Text(
                        text = "No registrations match the selected filter.",
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { reg ->
                AdminRegistrationItemCard(
                    registration = reg,
                    onConfirm = { onConfirm(reg.id) },
                    onReject = { onReject(reg.id) },
                    onViewDetails = { selectedRegistrationForDetail = reg },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }

    // Details Modal
    selectedRegistrationForDetail?.let { reg ->
        RegistrationDetailDialog(
            registration = reg,
            onDismiss = { selectedRegistrationForDetail = null },
            onConfirm = {
                onConfirm(reg.id)
                selectedRegistrationForDetail = null
            },
            onReject = {
                onReject(reg.id)
                selectedRegistrationForDetail = null
            }
        )
    }
}

@Composable
fun AdminRegistrationItemCard(
    registration: RegistrationItem,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(registration.registeredAt))

    EsportsCard(
        modifier = modifier,
        hasGlow = registration.status == RegistrationStatus.PENDING
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: ID and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = registration.id,
                        color = GoldLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Submitted: $dateString",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                StatusBadge(status = registration.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player Info Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EsportsSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PLAYER: ${registration.playerName}",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "FEE: ₹${registration.entryFee}",
                            color = EmeraldConfirmed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "IGN: ${registration.ffIgn}",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "UID: ${registration.ffUid}",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SLOT: ${registration.tournamentTime}",
                            color = TextWhite,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Phone: ${registration.contactNumber}",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: [CONFIRM] [REJECT] [VIEW DETAILS]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (registration.status == RegistrationStatus.PENDING) {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldConfirmed,
                            contentColor = EsportsBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("admin_confirm_${registration.id}")
                    ) {
                        Text("CONFIRM", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x33EF4444),
                            contentColor = RedRejected
                        ),
                        border = BorderStroke(1.dp, RedRejected),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("admin_reject_${registration.id}")
                    ) {
                        Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                GoldOutlinedButton(
                    text = "VIEW DETAILS",
                    onClick = onViewDetails,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                )
            }
        }
    }
}

@Composable
fun RegistrationDetailDialog(
    registration: RegistrationItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EsportsSurface),
            border = BorderStroke(1.2.dp, GoldPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REGISTRATION DETAILS",
                        color = GoldPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                DetailItemRow("Registration ID", registration.id)
                DetailItemRow("Status", registration.status.label)
                DetailItemRow("Player Name", registration.playerName)
                DetailItemRow("Free Fire IGN", registration.ffIgn)
                DetailItemRow("Free Fire UID", registration.ffUid)
                DetailItemRow("WhatsApp Number", registration.contactNumber)
                DetailItemRow("Tournament Session", registration.tournamentTime)
                DetailItemRow("Entry Fee", "₹${registration.entryFee}")
                DetailItemRow("Payment Reference", registration.paymentRef.ifBlank { "Not provided" })
                DetailItemRow(
                    "Submitted At",
                    SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US).format(Date(registration.registeredAt))
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (registration.status == RegistrationStatus.PENDING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onReject,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x33EF4444),
                                contentColor = RedRejected
                            ),
                            border = BorderStroke(1.dp, RedRejected),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldConfirmed,
                                contentColor = EsportsBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CONFIRM PLAYER", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                } else {
                    GoldOutlinedButton(
                        text = "CLOSE",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(text = value, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun AdminFilterChip(
    label: String,
    selected: Boolean,
    accentColor: Color = GoldPrimary,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) accentColor else EsportsSurfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) EsportsBlack else TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
