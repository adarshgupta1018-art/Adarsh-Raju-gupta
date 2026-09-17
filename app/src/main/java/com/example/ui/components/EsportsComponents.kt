package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegistrationStatus
import com.example.data.model.TournamentStatus
import com.example.ui.theme.AmberPending
import com.example.ui.theme.EmeraldConfirmed
import com.example.ui.theme.EsportsBlack
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceElevated
import com.example.ui.theme.EsportsSurfaceVariant
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GrayCancelled
import com.example.ui.theme.RedRejected
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun EsportsCard(
    modifier: Modifier = Modifier,
    hasGlow: Boolean = false,
    borderColor: Color = Color(0x33FFD700),
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "borderGlow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val effectiveBorder = if (hasGlow) {
        BorderStroke(1.2.dp, GoldPrimary.copy(alpha = alpha))
    } else {
        BorderStroke(1.dp, borderColor)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (hasGlow) Modifier.shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = GoldGlow,
                    spotColor = GoldPrimary
                ) else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = EsportsSurface
        ),
        border = effectiveBorder
    ) {
        content()
    }
}

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    testTag: String = "gold_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .testTag(testTag)
            .height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = EsportsBlack,
            disabledContainerColor = Color(0xFF2A2A33),
            disabledContentColor = Color(0xFF6B7280)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun GoldOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    testTag: String = "gold_outlined_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .testTag(testTag)
            .height(48.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.2.dp, if (enabled) GoldPrimary else Color(0xFF374151)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (enabled) GoldPrimary else Color(0xFF6B7280),
            containerColor = Color.Transparent
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun StatusBadge(status: RegistrationStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        RegistrationStatus.PENDING -> Triple(Color(0x2BFBBF24), AmberPending, "🟡 PENDING")
        RegistrationStatus.CONFIRMED -> Triple(Color(0x2B10B981), EmeraldConfirmed, "🟢 CONFIRMED")
        RegistrationStatus.REJECTED -> Triple(Color(0x2BEF4444), RedRejected, "🔴 REJECTED")
        RegistrationStatus.CANCELLED -> Triple(Color(0x2B94A3B8), GrayCancelled, "⚪ CANCELLED")
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TournamentStatusBadge(status: TournamentStatus, isFull: Boolean, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text) = when {
        status == TournamentStatus.CANCELLED -> Triple(Color(0x2BEF4444), RedRejected, "CANCELLED")
        status == TournamentStatus.COMPLETED -> Triple(Color(0x2B94A3B8), GrayCancelled, "COMPLETED")
        isFull || status == TournamentStatus.SLOTS_FULL -> Triple(Color(0x2BF97316), Color(0xFFFB923C), "SLOTS FULL")
        else -> Triple(Color(0x2B10B981), EmeraldConfirmed, "OPEN")
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(6.dp)),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun SlotProgressBar(
    filled: Int,
    max: Int = 20,
    minRequired: Int = 12,
    modifier: Modifier = Modifier
) {
    val progress = (filled.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    val barColor = when {
        filled >= max -> RedRejected
        filled >= 16 -> Color(0xFFF59E0B)
        filled >= minRequired -> EmeraldConfirmed
        else -> GoldPrimary
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$filled / $max SLOTS FILLED",
                color = if (filled >= max) RedRejected else TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (filled >= minRequired) "Ready to Start" else "Min $minRequired required",
                color = if (filled >= minRequired) EmeraldConfirmed else TextMuted,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Color(0xFF232530),
        )
    }
}

@Composable
fun HeroHeader(
    modifier: Modifier = Modifier,
    onAdminClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF221E12),
                        Color(0xFF14141A),
                        EsportsBlack
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color(0x4DFFD700), Color(0x00000000))
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(GoldLight, GoldDark)))
                            .border(1.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♠", color = EsportsBlack, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ACE ESPORTS",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "FREE FIRE TOURNAMENTS",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Live status chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x2210B981),
                    border = BorderStroke(1.dp, EmeraldConfirmed.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EmeraldConfirmed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE TODAY",
                            color = EmeraldConfirmed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Punchline
            Text(
                text = "“YOUR SKILLS. YOUR STRATEGY. YOUR BOOYAH.”",
                color = GoldLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tournament spec pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpecPill("SOLO BR")
                SpecPill("MAP: BR")
                SpecPill("1 MATCH")
                SpecPill("20 SLOTS")
            }
        }
    }
}

@Composable
fun SpecPill(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = EsportsSurfaceVariant,
        border = BorderStroke(1.dp, Color(0x26FFD700))
    ) {
        Text(
            text = label,
            color = TextGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SimulatedClockBanner(
    simulatedTimeOverride: Long?,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (simulatedTimeOverride != null) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0x3D7C3AED),
            border = BorderStroke(1.dp, Color(0xFFA78BFA))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Simulated Time Active",
                        tint = Color(0xFFDDD6FE),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TEST SIMULATION CLOCK ACTIVE",
                        color = Color(0xFFDDD6FE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "[ RESET TO REAL TIME ]",
                    color = GoldLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onResetClick() }
                )
            }
        }
    }
}
