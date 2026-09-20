package com.example.ui.screens.player

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.EsportsCard
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.theme.EsportsBlack
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceVariant
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun ProfileScreen(
    user: UserProfile,
    onSaveProfile: (String, String, String, String) -> Unit,
    onOpenAdminPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(user.name) }
    var ffIgn by remember { mutableStateOf(user.ffIgn) }
    var ffUid by remember { mutableStateOf(user.ffUid) }
    var phone by remember { mutableStateOf(user.contactNumber) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EsportsBlack),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp)
    ) {
        item {
            Text(
                text = "PLAYER PROFILE",
                color = GoldPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Manage your Free Fire gamer tag, contact UID, and access Admin Control.",
                color = TextGray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Profile Card
            EsportsCard(hasGlow = true) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1B12))
                            .border(2.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "♠",
                            color = GoldLight,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (user.ffIgn.isNotBlank()) user.ffIgn else "ACE ESPORTS PLAYER",
                        color = GoldLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = if (user.ffUid.isNotBlank()) "UID: ${user.ffUid}" else "UID: Not Set (Register for a Match)",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EsportsSurfaceVariant
                    ) {
                        Text(
                            text = "VERIFIED ESPORTS PLAYER",
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Edit details
            EsportsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GAMER CREDENTIALS",
                            color = GoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isEditing) {
                            GoldOutlinedButton(
                                text = "EDIT",
                                onClick = { isEditing = true },
                                modifier = Modifier.height(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditing) {
                        RegistrationTextField(label = "Full Name", value = name, onValueChange = { name = it }, leadingIcon = Icons.Default.Person)
                        Spacer(modifier = Modifier.height(8.dp))
                        RegistrationTextField(label = "Free Fire IGN", value = ffIgn, onValueChange = { ffIgn = it }, leadingIcon = Icons.Default.SportsEsports)
                        Spacer(modifier = Modifier.height(8.dp))
                        RegistrationTextField(label = "Free Fire UID", value = ffUid, onValueChange = { ffUid = it }, leadingIcon = Icons.Default.Security)
                        Spacer(modifier = Modifier.height(8.dp))
                        RegistrationTextField(label = "WhatsApp Number", value = phone, onValueChange = { phone = it }, leadingIcon = Icons.Default.Phone)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GoldOutlinedButton(text = "CANCEL", onClick = { isEditing = false }, modifier = Modifier.weight(1f))
                            GoldButton(
                                text = "SAVE",
                                onClick = {
                                    onSaveProfile(name, ffIgn, ffUid, phone)
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        ProfileInfoRow("Full Name", user.name)
                        ProfileInfoRow("Free Fire IGN", user.ffIgn)
                        ProfileInfoRow("Free Fire UID", user.ffUid)
                        ProfileInfoRow("WhatsApp Contact", user.contactNumber)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // ADMIN PANEL ACCESS CARD
            EsportsCard(
                borderColor = Color(0x66FFD700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ADMIN PANEL ACCESS",
                                color = TextWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Manage registrations, enter Room IDs, and trigger 5-min reveals.",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    GoldButton(
                        text = "SWITCH TO ADMIN PANEL",
                        onClick = onOpenAdminPanel,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
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

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(
            text = if (value.isNotBlank()) value else "Not Set",
            color = if (value.isNotBlank()) TextWhite else TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
