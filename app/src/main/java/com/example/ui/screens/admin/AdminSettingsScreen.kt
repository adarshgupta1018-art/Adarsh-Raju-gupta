package com.example.ui.screens.admin

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TournamentItem
import com.example.data.model.TournamentPrizeRules
import com.example.data.payment.PaymentConfig
import com.example.ui.components.EsportsCard
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.components.PaymentQrCodeView
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
fun AdminSettingsScreen(
    tournaments: List<TournamentItem>,
    currentTimeMillis: Long,
    isSimulated: Boolean,
    paymentConfig: PaymentConfig,
    onResetRealTime: () -> Unit,
    onSimulateTime: (tournament: TournamentItem, minutesBefore: Int) -> Unit,
    onBroadcast: (title: String, message: String) -> Unit,
    onUpdatePaymentConfig: (upiId: String, payeeName: String, upiNote: String, qrImageUrl: String) -> Unit,
    onUploadPaymentQrCode: (context: Context, uri: android.net.Uri, onResult: (Boolean) -> Unit) -> Unit,
    onResetPaymentQrCode: () -> Unit,
    onUpdateTournamentFeeAndPrize: (tournamentId: String, fee: Int, prize: String) -> Unit,
    onResetData: () -> Unit,
    onLogoutAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMessage by remember { mutableStateOf("") }

    // UPI / QR settings state
    var upiIdInput by remember(paymentConfig.upiId) { mutableStateOf(paymentConfig.upiId) }
    var payeeNameInput by remember(paymentConfig.payeeName) { mutableStateOf(paymentConfig.payeeName) }
    var upiNoteInput by remember(paymentConfig.upiNote) { mutableStateOf(paymentConfig.upiNote) }
    var paymentConfigSavedMessage by remember { mutableStateOf<String?>(null) }

    // Tournament Prize Edit dialog state
    var editingTournament by remember { mutableStateOf<TournamentItem?>(null) }

    val context = LocalContext.current
    val qrImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUploadPaymentQrCode(context, uri) { success ->
                if (success) {
                    paymentConfigSavedMessage = "Custom QR code uploaded and activated successfully!"
                }
            }
        }
    }

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
                    text = "ADMIN CONTROLS & PAYMENT CONFIG",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Configure UPI ID & Payment QR, manage Tournament Fees/Prizes, simulate time, and send broadcasts.",
                    color = TextGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // ==========================================
        // 1. PAYMENT CONFIGURATION (UPI ID & QR)
        // ==========================================
        item {
            EsportsCard(hasGlow = true) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TOURNAMENT PAYMENT & UPI CONFIG",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Set your official UPI ID and QR code. Compulsory for all players before slot reservation.",
                        color = TextGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = upiIdInput,
                        onValueChange = {
                            upiIdInput = it
                            paymentConfigSavedMessage = null
                        },
                        label = { Text("UPI ID (e.g. yourname@okhdfcbank / yourname@upi)", color = TextMuted, fontSize = 11.sp) },
                        placeholder = { Text("aceesports@upi", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF333544),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = EsportsSurfaceVariant,
                            unfocusedContainerColor = EsportsSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = payeeNameInput,
                        onValueChange = {
                            payeeNameInput = it
                            paymentConfigSavedMessage = null
                        },
                        label = { Text("Account / Payee Name (displayed on UPI apps)", color = TextMuted, fontSize = 11.sp) },
                        placeholder = { Text("ACE ESPORTS TOURNAMENTS", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF333544),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = EsportsSurfaceVariant,
                            unfocusedContainerColor = EsportsSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = upiNoteInput,
                        onValueChange = {
                            upiNoteInput = it
                            paymentConfigSavedMessage = null
                        },
                        label = { Text("Payment Note / Purpose", color = TextMuted, fontSize = 11.sp) },
                        placeholder = { Text("Free Fire Tournament Entry Fee", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF333544),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = EsportsSurfaceVariant,
                            unfocusedContainerColor = EsportsSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // QR Code Management Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoldOutlinedButton(
                            text = "📸 UPLOAD QR IMAGE",
                            onClick = {
                                qrImagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                onResetPaymentQrCode()
                                paymentConfigSavedMessage = "Payment QR code restored to default live generator."
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E303D),
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text("DEFAULT QR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Save Config Button
                    GoldButton(
                        text = "SAVE UPI PAYMENT SETTINGS",
                        onClick = {
                            val trimmedUpi = upiIdInput.trim()
                            val trimmedName = payeeNameInput.trim().ifBlank { "ACE ESPORTS TOURNAMENTS" }
                            val trimmedNote = upiNoteInput.trim().ifBlank { "Tournament Entry Fee" }
                            if (trimmedUpi.isNotBlank()) {
                                onUpdatePaymentConfig(trimmedUpi, trimmedName, trimmedNote, paymentConfig.qrCodeUri)
                                paymentConfigSavedMessage = "UPI settings updated successfully! All player forms now reflect these changes."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_payment_config_btn")
                    )

                    if (paymentConfigSavedMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✅ $paymentConfigSavedMessage",
                            color = EmeraldConfirmed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live QR Preview Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EsportsSurfaceVariant,
                        border = BorderStroke(1.dp, Color(0x33FFD700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "LIVE QR PREVIEW (Sample Entry ₹10)",
                                color = GoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentQrCodeView(
                                upiId = paymentConfig.upiId,
                                payeeName = paymentConfig.payeeName,
                                amount = 10,
                                qrCodeUri = paymentConfig.qrCodeUri,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // ==========================================
        // 2. TOURNAMENT FEE & PRIZE MANAGEMENT
        // ==========================================
        item {
            EsportsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TOURNAMENT ENTRY FEE & WINNING PRIZES",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customize the entry fee and winning prizes for scheduled tournament sessions. Tap any tournament below to edit.",
                        color = TextGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    tournaments.forEach { tournament ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EsportsSurfaceVariant,
                            border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { editingTournament = tournament }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${tournament.startTime} (${tournament.matchType})",
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Prize: ${if (tournament.winningPrize.isNotBlank()) tournament.winningPrize else TournamentPrizeRules.getDefaultPrizeForFee(tournament.entryFee)}",
                                        color = EmeraldConfirmed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Fee", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "₹${tournament.entryFee}",
                                            color = GoldLight,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Fee/Prize",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // ==========================================
        // 3. LIVE CLOCK & TIME SIMULATOR
        // ==========================================
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

        // ==========================================
        // 4. BROADCAST ANNOUNCEMENT
        // ==========================================
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

        // ==========================================
        // 5. SYSTEM MAINTENANCE
        // ==========================================
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

    // Edit Tournament Fee & Prize Dialog
    editingTournament?.let { tourney ->
        EditTournamentFeeAndPrizeDialog(
            tournament = tourney,
            onDismiss = { editingTournament = null },
            onSave = { fee, prize ->
                onUpdateTournamentFeeAndPrize(tourney.id, fee, prize)
                editingTournament = null
            }
        )
    }
}

@Composable
fun EditTournamentFeeAndPrizeDialog(
    tournament: TournamentItem,
    onDismiss: () -> Unit,
    onSave: (fee: Int, prize: String) -> Unit
) {
    var fee by remember { mutableIntStateOf(tournament.entryFee) }
    var prize by remember {
        mutableStateOf(
            if (tournament.winningPrize.isNotBlank()) tournament.winningPrize
            else TournamentPrizeRules.getDefaultPrizeForFee(tournament.entryFee)
        )
    }

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
            ) {
                Text(
                    text = "EDIT SESSION FEE & PRIZES",
                    color = GoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${tournament.startTime} (${tournament.matchType})",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Official Tier Presets:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Standard Presets
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TournamentPrizeRules.OFFICIAL_TIERS.forEach { tier ->
                        val isSelected = fee == tier.entryFee
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) GoldPrimary else EsportsSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) GoldLight else Color(0x22FFFFFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    fee = tier.entryFee
                                    prize = tier.winningPrize
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Entry Fee: ₹${tier.entryFee}",
                                    color = if (isSelected) Color.Black else GoldLight,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = tier.winningPrize,
                                    color = if (isSelected) Color.Black else EmeraldConfirmed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Entry Fee & Prize input
                OutlinedTextField(
                    value = fee.toString(),
                    onValueChange = {
                        val parsed = it.toIntOrNull()
                        if (parsed != null && parsed >= 0) {
                            fee = parsed
                            if (TournamentPrizeRules.OFFICIAL_TIERS.any { t -> t.entryFee == parsed }) {
                                prize = TournamentPrizeRules.getDefaultPrizeForFee(parsed)
                            }
                        }
                    },
                    label = { Text("Entry Fee (₹)", color = TextMuted, fontSize = 11.sp) },
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
                    value = prize,
                    onValueChange = { prize = it },
                    label = { Text("Winning Prize Structure", color = TextMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
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
                    GoldButton(
                        text = "SAVE FEE & PRIZE",
                        onClick = {
                            onSave(fee, prize.trim())
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}
