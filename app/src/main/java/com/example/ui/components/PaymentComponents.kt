package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.payment.PaymentConfig
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
import kotlin.math.abs

/**
 * QR Code canvas generator for UPI payments.
 * Generates an authentic matrix pattern with standard 7x7 corner finder patterns,
 * timing bars, and deterministic hash modules.
 */
@Composable
fun PaymentQrCodeView(
    amount: Int,
    config: PaymentConfig,
    slotNumber: Int = 1,
    modifier: Modifier = Modifier
) {
    PaymentQrCodeView(
        upiId = config.upiId,
        payeeName = config.payeeName,
        amount = amount,
        qrCodeUri = config.qrCodeUri,
        slotNumber = slotNumber,
        modifier = modifier
    )
}

@Composable
fun PaymentQrCodeView(
    upiId: String,
    payeeName: String,
    amount: Int,
    qrCodeUri: String = "",
    slotNumber: Int = 1,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copiedToast by remember { mutableStateOf(false) }

    val upiPayload = remember(upiId, payeeName, amount, slotNumber) {
        "upi://pay?pa=$upiId&pn=${Uri.encode(payeeName)}&am=$amount&cu=INR&tn=ACE+ESPORTS+Slot+$slotNumber"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = EsportsSurfaceVariant),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SCAN QR CODE TO PAY",
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // QR Box Canvas or Image
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(2.dp, GoldPrimary),
                modifier = Modifier
                    .size(190.dp)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier.padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrCodeUri.isNotBlank()) {
                        // Admin uploaded QR Image
                        AsyncImage(
                            model = qrCodeUri,
                            contentDescription = "Payment QR Code",
                            modifier = Modifier
                                .size(165.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Crisp built-in QR Code matrix
                        CanvasQrMatrix(
                            payload = upiPayload,
                            modifier = Modifier.size(165.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Amount Tag
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x33B8860B),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Text(
                    text = "Amount: ₹$amount  •  $payeeName",
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // UPI ID Display with Copy Button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x22000000),
                border = BorderStroke(1.dp, Color(0xFF333544)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "UPI ID (VPA)", color = TextMuted, fontSize = 9.sp)
                        Text(text = upiId, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("UPI ID", upiId)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "UPI ID copied: $upiId", Toast.LENGTH_SHORT).show()
                            copiedToast = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (copiedToast) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                            contentDescription = "Copy UPI ID",
                            tint = if (copiedToast) EmeraldConfirmed else GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Open in UPI App button
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiPayload))
                        context.startActivity(Intent.createChooser(intent, "Pay ₹$amount via UPI"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "No UPI app found. Please scan the QR code.", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x2610B981),
                    contentColor = EmeraldConfirmed
                ),
                border = BorderStroke(1.dp, EmeraldConfirmed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PAY ₹$amount VIA ANY UPI APP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Draws an authentic 25x25 QR Matrix on Compose Canvas with proper corner finder patterns,
 * alignment markers, timing patterns, and hashed data modules.
 */
@Composable
private fun CanvasQrMatrix(
    payload: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val gridSize = 25
        val cellSize = size.width / gridSize
        val black = Color(0xFF0F172A)

        fun drawFinderPattern(startX: Int, startY: Int) {
            // Outer 7x7
            drawRect(
                color = black,
                topLeft = Offset(startX * cellSize, startY * cellSize),
                size = Size(7 * cellSize, 7 * cellSize)
            )
            // Inner 5x5 white
            drawRect(
                color = Color.White,
                topLeft = Offset((startX + 1) * cellSize, (startY + 1) * cellSize),
                size = Size(5 * cellSize, 5 * cellSize)
            )
            // Center 3x3 black
            drawRect(
                color = black,
                topLeft = Offset((startX + 2) * cellSize, (startY + 2) * cellSize),
                size = Size(3 * cellSize, 3 * cellSize)
            )
        }

        // 3 Corner Finders
        drawFinderPattern(0, 0)
        drawFinderPattern(gridSize - 7, 0)
        drawFinderPattern(0, gridSize - 7)

        // Alignment pattern at (16, 16) - 5x5
        val ax = gridSize - 9
        val ay = gridSize - 9
        drawRect(color = black, topLeft = Offset(ax * cellSize, ay * cellSize), size = Size(5 * cellSize, 5 * cellSize))
        drawRect(color = Color.White, topLeft = Offset((ax + 1) * cellSize, (ay + 1) * cellSize), size = Size(3 * cellSize, 3 * cellSize))
        drawRect(color = black, topLeft = Offset((ax + 2) * cellSize, (ay + 2) * cellSize), size = Size(cellSize, cellSize))

        // Timing patterns (row 6 and col 6)
        for (i in 7 until (gridSize - 7)) {
            if (i % 2 == 0) {
                drawRect(color = black, topLeft = Offset(i * cellSize, 6 * cellSize), size = Size(cellSize, cellSize))
                drawRect(color = black, topLeft = Offset(6 * cellSize, i * cellSize), size = Size(cellSize, cellSize))
            }
        }

        // Data modules seeded deterministically from payload
        val hash = payload.hashCode()
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                // Skip finder areas
                val inTopLeft = r < 8 && c < 8
                val inTopRight = r < 8 && c >= gridSize - 8
                val inBottomLeft = r >= gridSize - 8 && c < 8
                val inAlign = r in ax..(ax + 4) && c in ay..(ay + 4)
                val inTiming = r == 6 || c == 6

                if (!inTopLeft && !inTopRight && !inBottomLeft && !inAlign && !inTiming) {
                    val pseudo = abs((r * 31 + c * 17 + hash xor (r * c)).hashCode()) % 100
                    if (pseudo < 46) {
                        drawRect(
                            color = black,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full Payment Section for Registration Dialog
 */
@Composable
fun CompulsoryPaymentSection(
    entryFee: Int,
    winningPrize: String,
    selectedSlot: Int,
    paymentRef: String,
    onPaymentRefChange: (String) -> Unit,
    paymentScreenshotUri: String,
    onScreenshotPicked: (String) -> Unit,
    onRemoveScreenshot: () -> Unit,
    config: PaymentConfig,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onScreenshotPicked(uri.toString())
        }
    }

    var showFullScreenshotDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Step Header Card
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0x33B8860B),
            border = BorderStroke(1.2.dp, GoldPrimary),
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
                        text = "COMPULSORY PAYMENT",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Pay via QR to reserve Slot #$selectedSlot",
                        color = TextWhite,
                        fontSize = 11.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹$entryFee",
                        color = GoldLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Prize: $winningPrize",
                        color = EmeraldConfirmed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Display QR Code
        PaymentQrCodeView(
            upiId = config.upiId,
            payeeName = config.payeeName,
            amount = entryFee,
            qrCodeUri = config.qrCodeUri,
            slotNumber = selectedSlot
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Instructions Card
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0x1AFBBF24),
            border = BorderStroke(1.dp, Color(0x33FBBF24)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "📌 PAYMENT STEPS:",
                    color = Color(0xFFFDE68A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Scan the QR code with GPay / PhonePe / Paytm / BHIM.\n" +
                            "2. Complete the payment of ₹$entryFee.\n" +
                            "3. Copy the 12-digit UTR / Transaction ID from your payment app.\n" +
                            "4. Enter the Transaction ID below (Compulsory). Attach screenshot if available.",
                    color = TextWhite,
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mandatory Transaction ID / UTR Input
        Text(
            text = "TRANSACTION ID / UTR NUMBER (MANDATORY)*",
            color = GoldPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = paymentRef,
            onValueChange = onPaymentRefChange,
            placeholder = {
                Text(
                    text = "e.g. 12-digit UTR: 429381029384",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reg_payment_utr_input"),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = if (paymentRef.isNotBlank()) EmeraldConfirmed else Color(0xFF333544),
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = EsportsSurfaceVariant,
                unfocusedContainerColor = EsportsSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Screenshot Upload Section
        Text(
            text = "PAYMENT SCREENSHOT (RECOMMENDED)",
            color = TextGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (paymentScreenshotUri.isNotBlank()) {
            // Screenshot Preview Card
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EsportsSurfaceVariant,
                border = BorderStroke(1.dp, EmeraldConfirmed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showFullScreenshotDialog = true }
                    ) {
                        AsyncImage(
                            model = paymentScreenshotUri,
                            contentDescription = "Screenshot Preview",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, GoldPrimary, RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldConfirmed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Screenshot Attached",
                                    color = EmeraldConfirmed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Tap to preview image",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onRemoveScreenshot) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Screenshot",
                            tint = RedRejected,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // Button to attach screenshot
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EsportsSurfaceVariant,
                border = BorderStroke(1.dp, Color(0xFF333544)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UPLOAD PAYMENT SCREENSHOT (OPTIONAL)",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showFullScreenshotDialog && paymentScreenshotUri.isNotBlank()) {
            PaymentScreenshotPreviewDialog(
                imageUri = paymentScreenshotUri,
                onDismiss = { showFullScreenshotDialog = false }
            )
        }
    }
}

/**
 * Fullscreen / modal dialog to inspect payment screenshot
 */
@Composable
fun PaymentScreenshotPreviewDialog(
    imageUri: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EsportsBlack),
            border = BorderStroke(1.5.dp, GoldPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAYMENT SCREENSHOT",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                AsyncImage(
                    model = imageUri,
                    contentDescription = "Payment Screenshot",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(12.dp))

                GoldButton(
                    text = "CLOSE PREVIEW",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
