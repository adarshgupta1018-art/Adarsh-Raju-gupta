package com.example.data.payment

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PaymentConfig(
    val upiId: String = "aceesports@upi",
    val payeeName: String = "ACE ESPORTS",
    val upiNote: String = "Free Fire Tournament Entry Fee",
    val qrCodeUri: String = ""
)

class PaymentConfigManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ace_payment_config", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<PaymentConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): PaymentConfig {
        return PaymentConfig(
            upiId = prefs.getString("upi_id", "aceesports@upi") ?: "aceesports@upi",
            payeeName = prefs.getString("payee_name", "ACE ESPORTS") ?: "ACE ESPORTS",
            upiNote = prefs.getString("upi_note", "Free Fire Tournament Entry Fee") ?: "Free Fire Tournament Entry Fee",
            qrCodeUri = prefs.getString("qr_code_uri", "") ?: ""
        )
    }

    fun updateConfig(upiId: String, payeeName: String, upiNote: String = "Free Fire Tournament Entry Fee", qrCodeUri: String? = null) {
        val cleanUpi = upiId.trim().ifBlank { "aceesports@upi" }
        val cleanName = payeeName.trim().ifBlank { "ACE ESPORTS" }
        val cleanNote = upiNote.trim().ifBlank { "Free Fire Tournament Entry Fee" }
        val editor = prefs.edit()
            .putString("upi_id", cleanUpi)
            .putString("payee_name", cleanName)
            .putString("upi_note", cleanNote)

        if (qrCodeUri != null) {
            editor.putString("qr_code_uri", qrCodeUri.trim())
        }
        editor.apply()
        _configFlow.value = loadConfig()
    }

    fun setQrCodeUri(uri: String) {
        prefs.edit().putString("qr_code_uri", uri.trim()).apply()
        _configFlow.value = loadConfig()
    }

    fun resetToDefault() {
        prefs.edit().clear().apply()
        _configFlow.value = loadConfig()
    }
}
