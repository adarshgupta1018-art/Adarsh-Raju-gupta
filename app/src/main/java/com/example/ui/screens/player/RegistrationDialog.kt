package com.example.ui.screens.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
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
import com.example.data.model.TournamentItem
import com.example.data.model.UserProfile
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.theme.EsportsBlack
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceVariant
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun RegistrationDialog(
    tournament: TournamentItem,
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onSubmit: (fullName: String, ffIgn: String, ffUid: String, phone: String, paymentRef: String) -> Unit
) {
    var fullName by remember { mutableStateOf(currentUser.name) }
    var ffIgn by remember { mutableStateOf(currentUser.ffIgn) }
    var ffUid by remember { mutableStateOf(currentUser.ffUid) }
    var contactNumber by remember { mutableStateOf(currentUser.contactNumber) }
    var paymentRef by remember { mutableStateOf("UPI-REF-${(100000..999999).random()}") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("registration_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EsportsSurface),
            border = BorderStroke(1.2.dp, GoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
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
                            text = "REGISTER FOR MATCH",
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
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Pill Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EsportsSurfaceVariant,
                    border = BorderStroke(1.dp, Color(0x33FFD700)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
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
                                text = "₹${tournament.entryFee}",
                                color = GoldLight,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fields
                RegistrationTextField(
                    label = "Full Name",
                    value = fullName,
                    onValueChange = { fullName = it },
                    leadingIcon = Icons.Default.Person,
                    testTag = "reg_fullname_input"
                )

                Spacer(modifier = Modifier.height(10.dp))

                RegistrationTextField(
                    label = "Free Fire In-Game Name (IGN)",
                    value = ffIgn,
                    onValueChange = { ffIgn = it },
                    leadingIcon = Icons.Default.SportsEsports,
                    testTag = "reg_ffign_input"
                )

                Spacer(modifier = Modifier.height(10.dp))

                RegistrationTextField(
                    label = "Free Fire UID",
                    value = ffUid,
                    onValueChange = { ffUid = it },
                    leadingIcon = Icons.Default.Info,
                    testTag = "reg_ffuid_input"
                )

                Spacer(modifier = Modifier.height(10.dp))

                RegistrationTextField(
                    label = "WhatsApp / Contact Number",
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    leadingIcon = Icons.Default.Phone,
                    testTag = "reg_phone_input"
                )

                Spacer(modifier = Modifier.height(10.dp))

                RegistrationTextField(
                    label = "Payment / Transaction Reference (Optional)",
                    value = paymentRef,
                    onValueChange = { paymentRef = it },
                    leadingIcon = Icons.Default.Payment,
                    placeholder = "e.g. UPI Ref / GPay / Paytm ID",
                    testTag = "reg_payment_input"
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verification Notice
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x1AFBBF24),
                    border = BorderStroke(1.dp, Color(0x33FBBF24)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Notice: Registration will be submitted with status PENDING CONFIRMATION. The Admin will verify your UID and fee before marking CONFIRMED.",
                        color = Color(0xFFFDE68A),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GoldOutlinedButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    GoldButton(
                        text = "SUBMIT REGISTRATION",
                        onClick = {
                            if (fullName.isBlank() || ffIgn.isBlank() || ffUid.isBlank() || contactNumber.isBlank()) {
                                errorMessage = "Please fill in all mandatory fields"
                            } else {
                                errorMessage = null
                                onSubmit(fullName, ffIgn, ffUid, contactNumber, paymentRef)
                            }
                        },
                        modifier = Modifier
                            .weight(1.6f)
                            .testTag("submit_registration_btn")
                    )
                }
            }
        }
    }
}

@Composable
fun RegistrationTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = "",
    testTag: String = ""
) {
    Column {
        Text(
            text = label,
            color = TextGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(10.dp),
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
}
