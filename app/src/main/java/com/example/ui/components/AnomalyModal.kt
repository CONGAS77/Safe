package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.AlertReason
import com.example.domain.model.AnomalyType
import com.example.domain.model.CheckIn
import com.example.domain.model.SafeSession
import com.example.domain.model.SessionState
import com.example.ui.theme.LocalSafePalette
import kotlin.math.roundToInt

@Composable
fun AnomalyModal(
    activeSession: SafeSession?,
    dueCheckIn: CheckIn?,
    t: (String) -> String,
    onConfirmAnomalyOk: () -> Unit,
    onExtendSession: (Int) -> Unit,
    onOpenSosProtocol: (AlertReason) -> Unit,
    onConfirmCheckInOk: (String) -> Unit,
    onEscalateCheckIn: (String) -> Unit,
) {
    val palette = LocalSafePalette.current
    var deviationVerifyOpen by remember { mutableStateOf(false) }

    val anomaly = if (activeSession?.state == SessionState.WARNING) activeSession.activeAnomaly else null
    if (anomaly == null && dueCheckIn == null) return

    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = palette.bgSurface,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (anomaly != null && activeSession != null) {
                    when (anomaly.type) {
                        AnomalyType.DELAY_WARNING -> {
                            SafePill(
                                text = "RETARD DÉTECTÉ",
                                tone = PillTone.ORANGE,
                                leadingIcon = {
                                    Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(14.dp), tint = palette.orange600)
                                },
                            )
                            Spacer(Modifier.height(10.dp))
                            Text("⚠️ ${t("anomaly.delay.title")}", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Durée prévue : ${activeSession.expectedDurationMinutes} min · Temps écoulé : ${activeSession.elapsedMinutes.roundToInt()} min",
                                fontSize = 14.sp,
                                color = palette.textSecondary,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(t("anomaly.delay.question"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                            Spacer(Modifier.height(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                SafeGradientButton(
                                    onClick = onConfirmAnomalyOk,
                                    testTag = "anomaly_delay_ok_button",
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("🟢 ${t("anomaly.delay.ok")}", fontWeight = FontWeight.Bold)
                                }
                                SafeSecondaryButton(
                                    onClick = { onExtendSession(15) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("⏱ Prolonger de 15 min")
                                }
                                SafeGradientButton(
                                    onClick = { onOpenSosProtocol(AlertReason.SESSION_ANOMALY) },
                                    brush = Brush.linearGradient(listOf(palette.orange500, palette.orange600)),
                                ) {
                                    Text("🟠 ${t("anomaly.delay.help")}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        AnomalyType.STATIONARY_WARNING -> {
                            SafePill(
                                text = "ARRÊT PROLONGÉ",
                                tone = PillTone.ORANGE,
                                leadingIcon = {
                                    Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(14.dp), tint = palette.orange600)
                                },
                            )
                            Spacer(Modifier.height(10.dp))
                            Text("⚠️ ${t("anomaly.stationary.title")}", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Immobile depuis environ ${activeSession.stationaryMinutes.roundToInt()} min.",
                                fontSize = 14.sp,
                                color = palette.textSecondary,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(t("anomaly.stationary.question"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                            Spacer(Modifier.height(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                SafeGradientButton(
                                    onClick = onConfirmAnomalyOk,
                                    testTag = "anomaly_stationary_ok_button",
                                ) {
                                    Text("🟢 ${t("anomaly.stationary.ok")}", fontWeight = FontWeight.Bold)
                                }
                                SafeGradientButton(
                                    onClick = { onOpenSosProtocol(AlertReason.SESSION_ANOMALY) },
                                    brush = Brush.linearGradient(listOf(palette.orange500, palette.orange600)),
                                ) {
                                    Text("🟠 ${t("anomaly.stationary.help")}", fontWeight = FontWeight.Bold)
                                }
                                SafeGradientButton(
                                    onClick = { onOpenSosProtocol(AlertReason.SOS) },
                                    brush = Brush.linearGradient(listOf(palette.red500, palette.red600)),
                                ) {
                                    Text("🔴 ${t("anomaly.stationary.sos")}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        AnomalyType.ROUTE_DEVIATION_WARNING -> {
                            SafePill(
                                text = "CHANGEMENT D’ITINÉRAIRE",
                                tone = PillTone.ORANGE,
                                leadingIcon = {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = palette.orange600)
                                },
                            )
                            Spacer(Modifier.height(10.dp))
                            Text("⚠️ ${t("anomaly.deviation.title")}", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
                            Spacer(Modifier.height(6.dp))
                            Text(t("anomaly.deviation.question"), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                            Spacer(Modifier.height(10.dp))
                            SafeDisclaimerBox(text = t("anomaly.deviation.gpsNote"))
                            Spacer(Modifier.height(12.dp))

                            if (deviationVerifyOpen) {
                                SafeMapView(
                                    destination = activeSession.destination,
                                    currentPosition = activeSession.currentPosition,
                                    progressPercent = activeSession.progressPercent,
                                    deviationMeters = activeSession.deviationMeters,
                                    compact = true,
                                )
                                Spacer(Modifier.height(12.dp))
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                SafeGradientButton(
                                    onClick = {
                                        deviationVerifyOpen = false
                                        onConfirmAnomalyOk()
                                    },
                                    testTag = "anomaly_deviation_ok_button",
                                ) {
                                    Text("🟢 ${t("anomaly.deviation.ok")} — ${t("anomaly.deviation.okSub")}", fontWeight = FontWeight.Bold)
                                }
                                if (!deviationVerifyOpen) {
                                    SafeSecondaryButton(
                                        onClick = { deviationVerifyOpen = true },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("🟠 ${t("anomaly.deviation.unsure")} — ${t("anomaly.deviation.unsureSub")}", fontSize = 13.sp)
                                    }
                                }
                                SafeGradientButton(
                                    onClick = {
                                        deviationVerifyOpen = false
                                        onOpenSosProtocol(AlertReason.SESSION_ANOMALY)
                                    },
                                    brush = Brush.linearGradient(listOf(palette.red500, palette.red600)),
                                ) {
                                    Text("🔴 ${t("anomaly.deviation.help")}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        else -> Unit
                    }
                } else if (dueCheckIn != null) {
                    SafePill(
                        text = "CHECK-IN ARRIVÉ À ÉCHÉANCE",
                        tone = PillTone.ORANGE,
                        leadingIcon = {
                            Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(14.dp), tint = palette.orange600)
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(t("checkin.due.title"), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Confirmez que tout va bien. Sans réponse, votre proche configuré pourra être prévenu avec le message prévu.",
                        fontSize = 14.sp,
                        color = palette.textSecondary,
                    )
                    Spacer(Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SafeGradientButton(
                            onClick = { onConfirmCheckInOk(dueCheckIn.id) },
                            testTag = "checkin_due_ok_button",
                        ) {
                            Text("🟢 ${t("checkin.due.ok")}", fontWeight = FontWeight.Bold)
                        }
                        SafeGradientButton(
                            onClick = { onEscalateCheckIn(dueCheckIn.id) },
                            brush = Brush.linearGradient(listOf(palette.orange500, palette.orange600)),
                        ) {
                            Text("🟠 ${t("checkin.due.help")}", fontWeight = FontWeight.Bold)
                        }
                        SafeGradientButton(
                            onClick = { onEscalateCheckIn(dueCheckIn.id) },
                            brush = Brush.linearGradient(listOf(palette.red500, palette.red600)),
                        ) {
                            Text("🔴 ${t("checkin.due.sos")}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
