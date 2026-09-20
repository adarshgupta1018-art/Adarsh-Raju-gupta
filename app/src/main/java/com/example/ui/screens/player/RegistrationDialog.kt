package com.example.ui.screens.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsEsports
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RegistrationItem
import com.example.data.model.TournamentItem
import com.example.data.model.TournamentPrizeRules
import com.example.data.payment.PaymentConfig
import com.example.ui.components.CompulsoryPaymentSection
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.theme.AmberPending
import com.example.ui.theme.EmeraldConfirmed
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceVariant
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RedRejected
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrationDialog(
    tournament: TournamentItem,
    paymentConfig: PaymentConfig = PaymentConfig(),
    onDismiss: () -> Unit,
    onSubmit: (
        fullName: String,
        ffIgn: String,
        ffUid: String,
        phone: String,
        teamName: String,
        selectedSlot: Int,
        paymentRef: String,
        paymentScreenshotUrl: String,
        chosenFee: Int,
        winningPrize: String,
        onComplete: (Result<RegistrationItem>) -> Unit
    ) -> Unit,
    onNavigateToMyDashboard: () -> Unit
) {
    // All player input fields start strictly EMPTY with placeholders
    var fullName by remember { mutableStateOf("") }
    var ffIgn by remember { mutableStateOf("") }
    var ffUid by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("") }
    var paymentRef by remember { mutableStateOf("") }
    var paymentScreenshotUri by remember { mutableStateOf("") }

    // Selected Fee & Prize
    val selectedFee by remember { mutableIntStateOf(tournament.entryFee) }
    val winningPrize = remember(selectedFee, tournament.winningPrize) {
        if (tournament.winningPrize.isNotBlank()) tournament.winningPrize
        else TournamentPrizeRules.getDefaultPrizeForFee(selectedFee)
    }

    // Slot selection: Find first unbooked slot (1..20)
    val bookedSlotSet = remember(tournament.bookedSlotNumbers) { tournament.bookedSlotNumbers.toSet() }
    val confirmedSlotSet = remember(tournament.confirmedSlotNumbers) { tournament.confirmedSlotNumbers.toSet() }
    val initialSlot = remember(bookedSlotSet) {
        (1..tournament.maxSlots).firstOrNull { it !in bookedSlotSet } ?: 1
    }
    var selectedSlot by remember { mutableStateOf(initialSlot) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submittedRegistration by remember { mutableStateOf<RegistrationItem?>(null) }

    Dialog(onDismissRequest = {
        if (!isSubmitting) onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .testTag("registration_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EsportsSurface),
            border = BorderStroke(1.2.dp, GoldPrimary)
        ) {
            if (submittedRegistration != null) {
                // Success Confirmation Screen
                RegistrationSuccessView(
                    registration = submittedRegistration!!,
                    tournament = tournament,
                    onViewDashboard = onNavigateToMyDashboard,
                    onClose = onDismiss
                )
            } else {
                // Player Registration Form Screen
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PLAYER REGISTRATION",
                                color = GoldPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "ACE ESPORTS FREE FIRE TOURNAMENT",
                                color = TextGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isSubmitting
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selected Tournament Summary Card with Official Fee & Prize
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EsportsSurfaceVariant,
                        border = BorderStroke(1.dp, Color(0x33FFD700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Selected Session",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${tournament.startTime} (${tournament.matchType})",
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Entry Fee",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "₹$selectedFee",
                                        color = GoldLight,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Winning Prize Highlight
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x2210B981), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = EmeraldConfirmed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Prize: $winningPrize",
                                    color = EmeraldConfirmed,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Live Slot Counter Summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Available: ${tournament.availableSlots}",
                                        color = EmeraldConfirmed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Pending: ${tournament.pendingSlots}",
                                        color = AmberPending,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Confirmed: ${tournament.confirmedSlots}",
                                        color = RedRejected,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "Total: ${tournament.maxSlots}",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SLOT SELECTION SECTION
                    Text(
                        text = "SELECT TOURNAMENT SLOT (1 - ${tournament.maxSlots})",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Currently Selected: Slot #$selectedSlot",
                        color = TextWhite,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 20-Slot Grid
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        maxItemsInEachRow = 5
                    ) {
                        for (slotNum in 1..tournament.maxSlots) {
                            val isConfirmed = slotNum in confirmedSlotSet
                            val isPending = !isConfirmed && (slotNum in bookedSlotSet)
                            val isAvailable = !isConfirmed && !isPending
                            val isSelected = selectedSlot == slotNum

                            val bgColor = when {
                                isSelected -> GoldPrimary
                                isConfirmed -> Color(0x33EF4444)
                                isPending -> Color(0x33FBBF24)
                                else -> EsportsSurfaceVariant
                            }

                            val borderColor = when {
                                isSelected -> GoldLight
                                isConfirmed -> RedRejected
                                isPending -> AmberPending
                                else -> Color(0x3310B981)
                            }

                            val textColor = when {
                                isSelected -> Color.Black
                                isConfirmed -> Color(0xFFFCA5A5)
                                isPending -> Color(0xFFFDE68A)
                                else -> TextWhite
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                    .clickable(enabled = isAvailable && !isSubmitting) {
                                        selectedSlot = slotNum
                                        if (errorMessage != null) errorMessage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$slotNum",
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Slot Legend
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LegendItem(color = EmeraldConfirmed, text = "Available")
                        LegendItem(color = AmberPending, text = "Pending")
                        LegendItem(color = RedRejected, text = "Booked")
                        LegendItem(color = GoldPrimary, text = "Selected")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PLAYER DETAILS INPUT FIELDS
                    RegistrationTextField(
                        label = "Full Name",
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (errorMessage != null) errorMessage = null
                        },
                        leadingIcon = Icons.Default.Person,
                        placeholder = "Enter your full name",
                        testTag = "reg_fullname_input"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RegistrationTextField(
                        label = "Free Fire In-Game Name (IGN)",
                        value = ffIgn,
                        onValueChange = {
                            ffIgn = it
                            if (errorMessage != null) errorMessage = null
                        },
                        leadingIcon = Icons.Default.SportsEsports,
                        placeholder = "Enter your Free Fire IGN",
                        testTag = "reg_ffign_input"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RegistrationTextField(
                        label = "Free Fire UID",
                        value = ffUid,
                        onValueChange = {
                            ffUid = it
                            if (errorMessage != null) errorMessage = null
                        },
                        leadingIcon = Icons.Default.Info,
                        placeholder = "Enter your Free Fire UID",
                        keyboardType = KeyboardType.Number,
                        testTag = "reg_ffuid_input"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RegistrationTextField(
                        label = "WhatsApp / Contact Number",
                        value = contactNumber,
                        onValueChange = {
                            contactNumber = it
                            if (errorMessage != null) errorMessage = null
                        },
                        leadingIcon = Icons.Default.Phone,
                        placeholder = "Enter your WhatsApp number",
                        keyboardType = KeyboardType.Phone,
                        testTag = "reg_phone_input"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RegistrationTextField(
                        label = "Team Name (Optional)",
                        value = teamName,
                        onValueChange = { teamName = it },
                        leadingIcon = Icons.Default.Group,
                        placeholder = "Enter team name if applicable",
                        testTag = "reg_teamname_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // COMPULSORY PAYMENT SECTION
                    CompulsoryPaymentSection(
                        entryFee = selectedFee,
                        winningPrize = winningPrize,
                        selectedSlot = selectedSlot,
                        paymentRef = paymentRef,
                        onPaymentRefChange = {
                            paymentRef = it
                            if (errorMessage != null) errorMessage = null
                        },
                        paymentScreenshotUri = paymentScreenshotUri,
                        onScreenshotPicked = { uri ->
                            paymentScreenshotUri = uri
                        },
                        onRemoveScreenshot = {
                            paymentScreenshotUri = ""
                        },
                        config = paymentConfig
                    )

                    // Error Message Banner
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x26EF4444),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ $errorMessage",
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notice
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x1AFBBF24),
                        border = BorderStroke(1.dp, Color(0x33FBBF24)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ℹ️ Note: Registration is strictly PENDING until Admin verifies your payment of ₹$selectedFee. Slot #$selectedSlot is tentatively held for verification.",
                            color = Color(0xFFFDE68A),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoldOutlinedButton(
                            text = "CANCEL",
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isSubmitting
                        )
                        GoldButton(
                            text = if (isSubmitting) "VERIFYING..." else "PAY ₹$selectedFee & BOOK #$selectedSlot",
                            onClick = {
                                val trimmedName = fullName.trim()
                                val trimmedIgn = ffIgn.trim()
                                val trimmedUid = ffUid.trim()
                                val trimmedPhone = contactNumber.trim()
                                val trimmedTeam = teamName.trim()
                                val trimmedPayment = paymentRef.trim()

                                when {
                                    trimmedName.isEmpty() -> {
                                        errorMessage = "Please enter your full name."
                                    }
                                    trimmedIgn.isEmpty() -> {
                                        errorMessage = "Please enter your Free Fire IGN."
                                    }
                                    trimmedUid.isEmpty() -> {
                                        errorMessage = "Please enter your Free Fire UID."
                                    }
                                    trimmedPhone.isEmpty() -> {
                                        errorMessage = "Please enter your WhatsApp number."
                                    }
                                    selectedSlot in bookedSlotSet -> {
                                        errorMessage = "Slot #$selectedSlot is already booked. Please choose an available slot."
                                    }
                                    trimmedPayment.isEmpty() -> {
                                        errorMessage = "Payment is compulsory! Please pay ₹$selectedFee via QR and enter your Transaction ID/UTR."
                                    }
                                    trimmedPayment.length < 4 -> {
                                        errorMessage = "Please enter a valid Transaction ID / UTR number from your payment app."
                                    }
                                    else -> {
                                        errorMessage = null
                                        isSubmitting = true
                                        onSubmit(
                                            trimmedName,
                                            trimmedIgn,
                                            trimmedUid,
                                            trimmedPhone,
                                            trimmedTeam,
                                            selectedSlot,
                                            trimmedPayment,
                                            paymentScreenshotUri,
                                            selectedFee,
                                            winningPrize
                                        ) { result ->
                                            isSubmitting = false
                                            result.onSuccess { reg ->
                                                submittedRegistration = reg
                                            }.onFailure { ex ->
                                                errorMessage = ex.message ?: "Failed to register. Please try again."
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1.8f)
                                .testTag("submit_registration_btn"),
                            enabled = !isSubmitting
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = TextGray, fontSize = 9.sp)
    }
}

@Composable
fun RegistrationSuccessView(
    registration: RegistrationItem,
    tournament: TournamentItem,
    onViewDashboard: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = EmeraldConfirmed,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "PAYMENT SUBMITTED",
            color = GoldLight,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Slot #${registration.selectedSlot} tentatively reserved • Pending Admin Verification",
            color = TextGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Receipt Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = EsportsSurfaceVariant,
            border = BorderStroke(1.2.dp, GoldPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Registration ID:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = registration.id,
                    color = GoldLight,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Player IGN", color = TextMuted, fontSize = 11.sp)
                        Text(text = registration.ffIgn, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Assigned Slot", color = TextMuted, fontSize = 11.sp)
                        Text(text = "Slot #${registration.selectedSlot}", color = GoldPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Free Fire UID", color = TextMuted, fontSize = 11.sp)
                        Text(text = registration.ffUid, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Status", color = TextMuted, fontSize = 11.sp)
                        Text(text = "🟡 PENDING VERIFICATION", color = AmberPending, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Entry Fee Paid", color = TextMuted, fontSize = 11.sp)
                        Text(text = "₹${registration.entryFee}", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Winning Prize", color = TextMuted, fontSize = 11.sp)
                        Text(text = registration.displayWinningPrize, color = EmeraldConfirmed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column {
                    Text(text = "Transaction ID / UTR", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = registration.paymentRef.ifBlank { "Not provided" },
                        color = TextWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Custom room announcement info
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0x1A10B981),
            border = BorderStroke(1.dp, Color(0x3310B981)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "🔒 Once Admin confirms your payment, your registration status will change to CONFIRMED. Custom Room ID & Password will automatically unlock 5 minutes before ${tournament.startTime} in My Registrations tab.",
                    color = Color(0xFF6EE7B7),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GoldOutlinedButton(
                text = "CLOSE",
                onClick = onClose,
                modifier = Modifier.weight(1f)
            )
            GoldButton(
                text = "VIEW STATUS",
                onClick = onViewDashboard,
                modifier = Modifier
                    .weight(1.4f)
                    .testTag("view_my_reg_btn")
            )
        }
    }
}

@Composable
fun RegistrationTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String = ""
) {
    Column {
        Text(
            text = label,
            color = TextGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
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
    }
}
