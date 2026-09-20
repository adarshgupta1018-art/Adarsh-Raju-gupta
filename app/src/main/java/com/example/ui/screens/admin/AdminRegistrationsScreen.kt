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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
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
import com.example.ui.theme.GrayCancelled
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
    onReject: (String, String) -> Unit = { id, _ -> },
    onUpdateNotes: (regId: String, status: RegistrationStatus, paymentStatus: String, notes: String) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<RegistrationStatus?>(null) }
    var selectedRegistrationForDetail by remember { mutableStateOf<RegistrationItem?>(null) }
    var rejectDialogRegistration by remember { mutableStateOf<RegistrationItem?>(null) }
    var viewingScreenshotUrl by remember { mutableStateOf<String?>(null) }

    val filteredList = registrations.filter { reg ->
        val matchesFilter = selectedStatusFilter == null || reg.status == selectedStatusFilter
        val q = searchQuery.trim().lowercase()
        val matchesSearch = q.isEmpty() ||
                reg.playerName.lowercase().contains(q) ||
                reg.ffUid.lowercase().contains(q) ||
                reg.ffIgn.lowercase().contains(q) ||
                reg.contactNumber.lowercase().contains(q) ||
                reg.paymentRef.lowercase().contains(q) ||
                reg.selectedSlot.toString() == q ||
                "slot ${reg.selectedSlot}".contains(q) ||
                reg.id.lowercase().contains(q)

        matchesFilter && matchesSearch
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
                    text = "PAYMENT & REGISTRATION VERIFICATION",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Verify player UTR/Transaction IDs and payment screenshot receipts before confirming slots.",
                    color = TextGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Player Name, UID, UTR/Txn ID, Slot #...", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_search_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF333544),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = EsportsSurfaceVariant,
                        unfocusedContainerColor = EsportsSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AdminFilterChip(
                        label = "ALL (${registrations.size})",
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null }
                    )
                    RegistrationStatus.entries.forEach { st ->
                        val count = registrations.count { it.status == st }
                        AdminFilterChip(
                            label = "${st.label} ($count)",
                            selected = selectedStatusFilter == st,
                            accentColor = when (st) {
                                RegistrationStatus.CONFIRMED -> EmeraldConfirmed
                                RegistrationStatus.PENDING -> AmberPending
                                RegistrationStatus.REJECTED -> RedRejected
                                RegistrationStatus.CANCELLED -> GrayCancelled
                            },
                            onClick = { selectedStatusFilter = st }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
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
                        text = if (searchQuery.isNotEmpty()) "No registrations found matching '$searchQuery'." else "No registrations match the selected filter.",
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
                    onRejectPrompt = { rejectDialogRegistration = reg },
                    onViewDetails = { selectedRegistrationForDetail = reg },
                    onViewScreenshot = { url -> viewingScreenshotUrl = url },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }

    // Screenshot Viewer Dialog
    viewingScreenshotUrl?.let { url ->
        Dialog(onDismissRequest = { viewingScreenshotUrl = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EsportsSurface),
                border = BorderStroke(1.2.dp, GoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PAYMENT SCREENSHOT",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewingScreenshotUrl = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Payment Receipt Screenshot",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GoldButton(
                        text = "CLOSE PREVIEW",
                        onClick = { viewingScreenshotUrl = null },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Reject Reason Dialog
    rejectDialogRegistration?.let { reg ->
        RejectReasonDialog(
            registration = reg,
            onDismiss = { rejectDialogRegistration = null },
            onReject = { reason ->
                onReject(reg.id, reason)
                rejectDialogRegistration = null
            }
        )
    }

    // Details Modal with Admin edit notes & verification
    selectedRegistrationForDetail?.let { reg ->
        RegistrationDetailDialog(
            registration = reg,
            onDismiss = { selectedRegistrationForDetail = null },
            onConfirm = {
                onConfirm(reg.id)
                selectedRegistrationForDetail = null
            },
            onRejectPrompt = {
                selectedRegistrationForDetail = null
                rejectDialogRegistration = reg
            },
            onViewScreenshot = { url ->
                viewingScreenshotUrl = url
            },
            onSaveNotes = { status, paymentStatus, notes ->
                onUpdateNotes(reg.id, status, paymentStatus, notes)
                selectedRegistrationForDetail = null
            }
        )
    }
}

@Composable
fun AdminRegistrationItemCard(
    registration: RegistrationItem,
    onConfirm: () -> Unit,
    onRejectPrompt: () -> Unit,
    onViewDetails: () -> Unit,
    onViewScreenshot: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(registration.registeredAt))
    val clipboardManager = LocalClipboardManager.current
    var copiedUtr by remember { mutableStateOf(false) }

    EsportsCard(
        modifier = modifier,
        hasGlow = registration.status == RegistrationStatus.PENDING
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: ID, Slot, and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = registration.id,
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GoldPrimary,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "SLOT #${registration.selectedSlot}",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
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
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.5.sp
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
                            text = "TEAM: ${registration.teamName.ifBlank { "Solo" }}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Phone: ${registration.contactNumber}",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }

                    // Winning Prize Row
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = EmeraldConfirmed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Prize: ${registration.displayWinningPrize}",
                            color = EmeraldConfirmed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Payment Reference / UTR Section
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x33000000),
                        border = BorderStroke(0.8.dp, Color(0x44FFD700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "UTR / TXN ID:",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = registration.paymentRef.ifBlank { "MISSING PAYMENT REF" },
                                    color = if (registration.paymentRef.isNotBlank()) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.5.sp
                                )
                            }

                            if (registration.paymentRef.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(registration.paymentRef))
                                        copiedUtr = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy UTR",
                                        tint = if (copiedUtr) EmeraldConfirmed else GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Payment Screenshot Indicator / Button
                    if (registration.paymentScreenshotUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x2610B981),
                            border = BorderStroke(0.8.dp, EmeraldConfirmed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewScreenshot(registration.paymentScreenshotUrl) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = EmeraldConfirmed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "📸 Payment Screenshot Attached — Tap to view",
                                    color = EmeraldConfirmed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (registration.adminNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notes: ${registration.adminNotes}",
                            color = Color(0xFFFDE68A),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: [CONFIRM] [REJECT] [DETAILS]
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
                        Text("VERIFY & CONFIRM", fontWeight = FontWeight.Black, fontSize = 10.sp)
                    }

                    Button(
                        onClick = onRejectPrompt,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x33EF4444),
                            contentColor = RedRejected
                        ),
                        border = BorderStroke(1.dp, RedRejected),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(36.dp)
                            .testTag("admin_reject_${registration.id}")
                    ) {
                        Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }

                GoldOutlinedButton(
                    text = "DETAILS",
                    onClick = onViewDetails,
                    modifier = Modifier
                        .weight(0.8f)
                        .height(36.dp)
                )
            }
        }
    }
}

@Composable
fun RejectReasonDialog(
    registration: RegistrationItem,
    onDismiss: () -> Unit,
    onReject: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    val commonReasons = listOf(
        "Payment UTR / Txn ID not found in UPI account",
        "Payment amount mismatch (expected ₹${registration.entryFee})",
        "Invalid Free Fire UID or IGN",
        "Duplicate registration transaction ID",
        "Slot limit reached or tournament cancelled"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EsportsSurface),
            border = BorderStroke(1.2.dp, RedRejected),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "REJECT REGISTRATION",
                    color = RedRejected,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Reject slot #${registration.selectedSlot} for ${registration.ffIgn}. The slot will be freed back to available.",
                    color = TextGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Quick Reasons:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                commonReasons.forEach { cr ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EsportsSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { reason = cr }
                    ) {
                        Text(
                            text = "• $cr",
                            color = TextWhite,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Custom Rejection Reason", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedRejected,
                        unfocusedBorderColor = Color(0xFF333544),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoldOutlinedButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { onReject(reason.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = RedRejected, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("CONFIRM REJECT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RegistrationDetailDialog(
    registration: RegistrationItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onRejectPrompt: () -> Unit,
    onViewScreenshot: (String) -> Unit,
    onSaveNotes: (status: RegistrationStatus, paymentStatus: String, notes: String) -> Unit
) {
    var adminNotes by remember { mutableStateOf(registration.adminNotes) }
    var selectedStatus by remember { mutableStateOf(registration.status) }
    var selectedPaymentStatus by remember { mutableStateOf(registration.paymentStatus) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EsportsSurface),
            border = BorderStroke(1.2.dp, GoldPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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

                Spacer(modifier = Modifier.height(8.dp))

                DetailItemRow("Registration ID", registration.id)
                DetailItemRow("Allocated Slot", "Slot #${registration.selectedSlot}")
                DetailItemRow("Player Name", registration.playerName)
                DetailItemRow("Free Fire IGN", registration.ffIgn)
                DetailItemRow("Free Fire UID", registration.ffUid)
                DetailItemRow("WhatsApp Number", registration.contactNumber)
                DetailItemRow("Team Name", registration.teamName.ifBlank { "Solo Player" })
                DetailItemRow("Tournament Session", registration.tournamentTime)
                DetailItemRow("Entry Fee", "₹${registration.entryFee}")
                DetailItemRow("Winning Prize", registration.displayWinningPrize)
                DetailItemRow("Payment UTR / Ref", registration.paymentRef.ifBlank { "Compulsory (Missing)" })
                DetailItemRow(
                    "Submitted At",
                    SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US).format(Date(registration.registeredAt))
                )

                // Screenshot Preview
                if (registration.paymentScreenshotUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Payment Screenshot:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .clickable { onViewScreenshot(registration.paymentScreenshotUrl) },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = registration.paymentScreenshotUrl,
                            contentDescription = "Screenshot",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Change Registration Status
                Text("Change Registration Status:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RegistrationStatus.entries.forEach { st ->
                        AdminFilterChip(
                            label = st.label,
                            selected = selectedStatus == st,
                            accentColor = when (st) {
                                RegistrationStatus.CONFIRMED -> EmeraldConfirmed
                                RegistrationStatus.PENDING -> AmberPending
                                RegistrationStatus.REJECTED -> RedRejected
                                RegistrationStatus.CANCELLED -> GrayCancelled
                            },
                            onClick = { selectedStatus = st }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Change Payment Status
                Text("Change Payment Status:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("VERIFIED", "UNVERIFIED", "REJECTED").forEach { ps ->
                        AdminFilterChip(
                            label = ps,
                            selected = selectedPaymentStatus == ps,
                            accentColor = if (ps == "VERIFIED") EmeraldConfirmed else if (ps == "REJECTED") RedRejected else AmberPending,
                            onClick = { selectedPaymentStatus = ps }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Editable Admin Notes
                OutlinedTextField(
                    value = adminNotes,
                    onValueChange = { adminNotes = it },
                    label = { Text("Admin Notes / Verification Note", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF333544),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = EsportsSurfaceVariant,
                        unfocusedContainerColor = EsportsSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoldOutlinedButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    GoldButton(
                        text = "SAVE CHANGES",
                        onClick = {
                            onSaveNotes(selectedStatus, selectedPaymentStatus, adminNotes.trim())
                        },
                        modifier = Modifier.weight(1.3f)
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
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(text = value, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
