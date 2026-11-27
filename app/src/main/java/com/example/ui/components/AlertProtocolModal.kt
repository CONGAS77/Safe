package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AlertService
import com.example.domain.model.AlertDraftPayload
import com.example.domain.model.CountryCode
import com.example.domain.model.SafeSession
import com.example.ui.theme.LocalSafePalette

@Composable
fun AlertProtocolModal(
    alertModalOpen: Boolean,
    alertDraft: AlertDraftPayload?,
    alertService: AlertService,
    country: CountryCode,
    activeSession: SafeSession?,
    t: (String) -> String,
    onClose: () -> Unit,
) {
    if (!alertModalOpen || alertDraft == null) return

    val context = LocalContext.current
    val palette = LocalSafePalette.current
    var confirmEmergencyOpen by remember { mutableStateOf(false) }
    var contactSentPreview by remember { mutableStateOf(false) }
    var sharePreviewOpen by remember { mutableStateOf(false) }
    var sharedConfirmed by remember { mutableStateOf(false) }
    var callTriggeredMsg by remember { mutableStateOf<String?>(null) }

    val emergency = alertService.getRegionalEmergencyInfo(country)
    val shareText = alertService.prepareShareSummary(alertDraft.lastPosition, alertDraft.sessionTitle)

    fun handleClose() {
        confirmEmergencyOpen = false
        contactSentPreview = false
        sharePreviewOpen = false
        sharedConfirmed = false
        callTriggeredMsg = null
        onClose()
    }

    Dialog(onDismissRequest = ::handleClose) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = palette.bgSurface,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SafePill(
                        text = "SOS / PROTOCOLE D’ALERTE",
                        tone = PillTone.RED,
                        leadingIcon = {
                            Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(14.dp), tint = palette.red600)
                        },
                    )
                    IconButton(onClick = ::handleClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = palette.textPrimary)
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(t("sos.title"), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
                Spacer(Modifier.height(6.dp))
                Text(t("sos.disclaimer"), fontSize = 14.sp, color = palette.textSecondary)
                Spacer(Modifier.height(16.dp))

                if (callTriggeredMsg != null) {
                    SafeCard(
                        containerColor = palette.redSoft,
                        borderColor = palette.redBorder,
                    ) {
                        Text(callTriggeredMsg.orEmpty(), fontWeight = FontWeight.Bold, color = palette.textPrimary)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (confirmEmergencyOpen) {
                    SafeCard(
                        containerColor = palette.redSoft,
                        borderColor = palette.redBorder,
                    ) {
                        Text(
                            text = "Confirmer l’appel vers les secours (${emergency.countryName})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Numéro d’urgence principal : ${emergency.primaryNumber} (Police ${emergency.policeNumber} · Médical ${emergency.medicalNumber} · Pompiers ${emergency.fireNumber}).",
                            fontSize = 13.sp,
                            color = palette.textSecondary,
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SafeGradientButton(
                                onClick = {
                                    val telUri = alertService.buildEmergencyTelUri(emergency.primaryNumber)
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse(telUri))
                                    runCatching { context.startActivity(dialIntent) }
                                    callTriggeredMsg = "Appel préparé vers ${emergency.primaryNumber} ($telUri)."
                                    confirmEmergencyOpen = false
                                },
                                modifier = Modifier.weight(1f),
                                brush = Brush.linearGradient(listOf(palette.red500, palette.red600)),
                                testTag = "sos_confirm_dial_button",
                            ) {
                                Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Appeler ${emergency.primaryNumber}", fontWeight = FontWeight.Bold)
                            }
                            SafeSecondaryButton(onClick = { confirmEmergencyOpen = false }) {
                                Text("Retour")
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                } else {
                    SafeGradientButton(
                        onClick = { confirmEmergencyOpen = true },
                        brush = Brush.linearGradient(listOf(palette.red500, palette.red600)),
                        testTag = "sos_call_emergency_button",
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("📞 ${t("sos.callEmergency")} (${emergency.primaryNumber})", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                }

                SafeGradientButton(
                    onClick = {
                        contactSentPreview = !contactSentPreview
                        sharePreviewOpen = false
                    },
                    brush = Brush.linearGradient(listOf(palette.orange500, palette.orange600)),
                    testTag = "sos_contact_close_button",
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    val contactSuffix = alertDraft.contact?.let { " (${it.firstName})" }.orEmpty()
                    Text("👤 ${t("sos.contactClose")}$contactSuffix", fontWeight = FontWeight.Bold)
                }

                if (contactSentPreview) {
                    Spacer(Modifier.height(10.dp))
                    SafeCard(borderColor = palette.orangeBorder) {
                        Text("Aperçu du message envoyé au contact", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.bgSurfaceMuted,
                        ) {
                            Text(
                                text = alertDraft.messageBody,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = palette.textPrimary,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                        if (activeSession != null) {
                            Spacer(Modifier.height(10.dp))
                            SafeMapView(
                                destination = activeSession.destination,
                                currentPosition = alertDraft.lastPosition,
                                progressPercent = activeSession.progressPercent,
                                compact = true,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        SafeGradientButton(
                            onClick = {
                                sharedConfirmed = true
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, alertDraft.messageBody)
                                }
                                runCatching {
                                    context.startActivity(Intent.createChooser(sendIntent, "Prévenir mon proche"))
                                }
                            },
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (sharedConfirmed) "Message préparé / envoyé" else "Confirmer l’envoi au proche",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                SafeSecondaryButton(
                    onClick = {
                        sharePreviewOpen = !sharePreviewOpen
                        contactSentPreview = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "sos_share_location_button",
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("📍 ${t("sos.shareLocation")}")
                }

                if (sharePreviewOpen) {
                    Spacer(Modifier.height(10.dp))
                    SafeCard {
                        Text(
                            text = "Informations partagées uniquement avec votre accord :",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.bgSurfaceMuted,
                        ) {
                            Text(
                                text = shareText,
                                fontSize = 12.sp,
                                color = palette.textPrimary,
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        SafeGradientButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("SAFE Location", shareText))
                                sharedConfirmed = true
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                runCatching {
                                    context.startActivity(Intent.createChooser(shareIntent, "Partager ma position"))
                                }
                            },
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (sharedConfirmed) "Position prête à être partagée" else "Confirmer le partage de position",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                SafeSecondaryButton(
                    onClick = ::handleClose,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "sos_cancel_button",
                ) {
                    Text("❌ ${t("sos.cancel")}")
                }
            }
        }
    }
}
