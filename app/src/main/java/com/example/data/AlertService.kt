package com.example.data

import com.example.config.EmergencyNumbers
import com.example.domain.model.AlertDraftPayload
import com.example.domain.model.AlertReason
import com.example.domain.model.CountryCode
import com.example.domain.model.EmergencyConfig
import com.example.domain.model.GeoPoint
import com.example.domain.model.SafeSession
import com.example.domain.model.TrustedContact
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class AlertService {
    private fun formatTimeHHMM(ts: Long?): String {
        val date = Date(ts ?: System.currentTimeMillis())
        return SimpleDateFormat("HH:mm", Locale.FRANCE).format(date)
    }

    fun buildContactMessageDraft(
        contact: TrustedContact?,
        session: SafeSession?,
        customMessage: String? = null,
        reason: AlertReason,
    ): AlertDraftPayload {
        val lastSignalTimeLabel = formatTimeHHMM(session?.lastSignalAt ?: System.currentTimeMillis())
        val sessionTitle = session?.title ?: "Check-in SAFE"
        val destinationLabel = session?.destination?.label ?: "Non spécifiée"
        val lastPosition = session?.currentPosition

        val messageBody = customMessage?.trim()?.takeIf { it.isNotEmpty() } ?: listOf(
            "⚠️ SAFE",
            "SAFE n'a pas reçu la confirmation attendue.",
            "Une personne dont vous êtes un contact de confiance n'a pas confirmé que tout allait bien.",
            "Session : $sessionTitle",
            "Destination : $destinationLabel",
            "Heure du dernier signal : $lastSignalTimeLabel",
            "Vérifiez si vous pouvez la contacter.",
        ).joinToString("\n")

        return AlertDraftPayload(
            contact = contact,
            sessionTitle = sessionTitle,
            destinationLabel = destinationLabel,
            lastSignalTimeLabel = lastSignalTimeLabel,
            lastPosition = lastPosition,
            messageBody = messageBody,
            reason = reason,
        )
    }

    fun prepareShareSummary(position: GeoPoint?, sessionTitle: String): String {
        if (position == null) {
            return "SAFE — $sessionTitle : aucune coordonnée partagée sans signal actif."
        }
        val lat = String.format(Locale.US, "%.4f", position.lat)
        val lng = String.format(Locale.US, "%.4f", position.lng)
        val acc = position.accuracyMeters.roundToInt()
        return "SAFE — $sessionTitle\nPosition approximative : $lat, $lng (±$acc m)\nHeure : ${formatTimeHHMM(position.timestamp)}"
    }

    fun getRegionalEmergencyInfo(country: CountryCode): EmergencyConfig =
        EmergencyNumbers.getEmergencyConfig(country)

    fun buildEmergencyTelUri(phoneNumber: String): String =
        "tel:${phoneNumber.replace(Regex("\\s+"), "")}"
}
