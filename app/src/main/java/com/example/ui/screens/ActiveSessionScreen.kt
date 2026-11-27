package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AlertReason
import com.example.domain.model.DemoSimulationState
import com.example.domain.model.SafeSession
import com.example.domain.model.SessionHistoryItem
import com.example.domain.model.SessionState
import com.example.ui.components.PillTone
import com.example.ui.components.PulseDot
import com.example.ui.components.SafeCard
import com.example.ui.components.SafeDisclaimerBox
import com.example.ui.components.SafeGradientButton
import com.example.ui.components.SafeMapView
import com.example.ui.components.SafePill
import com.example.ui.components.SafeSecondaryButton
import com.example.ui.components.SectionTitle
import com.example.ui.theme.LocalSafePalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    activeSession: SafeSession?,
    lastCompletedSummary: SessionHistoryItem?,
    demo: DemoSimulationState,
    t: (String) -> String,
    onOpenWizard: () -> Unit,
    onPauseOrResume: () -> Unit,
    onExtendSession: (Int) -> Unit,
    onUpdateDestination: (String) -> Unit,
    onCompleteSession: () -> Unit,
    onClearSummaryAndGoHome: () -> Unit,
    onOpenSosProtocol: (AlertReason) -> Unit,
) {
    val palette = LocalSafePalette.current
    var arrivalSheetOpen by remember { mutableStateOf(false) }
    var editingDest by remember { mutableStateOf(false) }
    var newDestInput by remember { mutableStateOf("") }
    var customExtendMins by remember { mutableIntStateOf(20) }

    // End-of-session summary screen
    if (activeSession == null && lastCompletedSummary != null) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            SafeCard(contentPadding = PaddingValues(24.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = palette.greenSoft,
                        border = BorderStroke(1.dp, palette.greenBorder),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = t("summary.title"),
                                tint = palette.green500,
                                modifier = Modifier.size(38.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "✅ ${t("summary.title")}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.textPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = t("summary.subtitle"),
                        fontSize = 14.sp,
                        color = palette.textSecondary,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            SafeCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SummaryRow(t("summary.duration"), "${lastCompletedSummary.durationMinutes} min")
                    SummaryRow(t("summary.distance"), "${lastCompletedSummary.distanceKm} km")
                    SummaryRow(
                        t("summary.anomalies"),
                        if (lastCompletedSummary.anomaliesCount == 0) {
                            t("summary.none")
                        } else {
                            "${lastCompletedSummary.anomaliesCount} vérifiée(s)"
                        },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            SafeGradientButton(
                onClick = onClearSummaryAndGoHome,
                testTag = "summary_back_home_button",
            ) {
                Text(t("summary.backHome"), fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // Empty state
    if (activeSession == null) {
        SafeCard(
            modifier = Modifier.padding(top = 20.dp),
            contentPadding = PaddingValues(28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Aucune session SAFE active",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = palette.textPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Démarrez une session lorsque vous partez seul pour activer le suivi temporaire.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    color = palette.textSecondary,
                )
                Spacer(Modifier.height(18.dp))
                SafeGradientButton(
                    onClick = onOpenWizard,
                    testTag = "session_empty_start_button",
                ) {
                    Text("🟢 ${t("home.cta.main")}", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val elapsed = activeSession.elapsedMinutes.roundToInt()
    val remaining = max(0, (activeSession.expectedDurationMinutes - activeSession.elapsedMinutes).roundToInt())
    val etaStr = activeSession.estimatedArrivalAt?.let {
        SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(it))
    } ?: "--:--"

    val (badgeTone, badgeLabel) = when (activeSession.state) {
        SessionState.ESCALATED -> PillTone.RED to "🔴 ${t("session.escalatedBadge")}"
        SessionState.WARNING -> PillTone.ORANGE to "🟠 ${t("session.warningBadge")}"
        SessionState.PAUSED -> PillTone.ORANGE to "⏸ ${t("session.pausedBadge")}"
        else -> PillTone.GREEN to "🟢 ${t("session.activeBadge")}"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SafePill(text = badgeLabel, tone = badgeTone)
            SafeSecondaryButton(
                onClick = {
                    newDestInput = activeSession.destination.label
                    editingDest = !editingDest
                },
                minHeight = 38.dp,
                testTag = "session_change_dest_button",
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Text(t("session.changeDest"), fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${activeSession.title} → ${activeSession.destination.label}",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = palette.textPrimary,
        )

        Spacer(Modifier.height(12.dp))

        if (demo.lowBattery) {
            SafeCard(
                containerColor = palette.orangeSoft,
                borderColor = palette.orangeBorder,
                contentPadding = PaddingValues(12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.BatteryAlert, contentDescription = null, tint = palette.orange600)
                    Text(t("session.batteryLow"), fontSize = 13.sp, color = palette.textPrimary)
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (demo.offline) {
            SafeCard(
                containerColor = palette.orangeSoft,
                borderColor = palette.orangeBorder,
                contentPadding = PaddingValues(12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.WifiOff, contentDescription = null, tint = palette.orange600)
                    Text(t("session.offline"), fontSize = 13.sp, color = palette.textPrimary)
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (demo.inaccurateGps) {
            SafeDisclaimerBox(text = t("session.gpsImprecise"))
            Spacer(Modifier.height(10.dp))
        }

        if (editingDest) {
            SafeCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newDestInput,
                        onValueChange = { newDestInput = it },
                        placeholder = { Text("Nouvelle destination") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("edit_dest_input"),
                    )
                    SafeGradientButton(
                        onClick = {
                            if (newDestInput.isNotBlank()) {
                                onUpdateDestination(newDestInput)
                                editingDest = false
                            }
                        },
                        modifier = Modifier.weight(0.35f),
                        testTag = "edit_dest_confirm",
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Time metrics
        SafeCard {
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricItem(t("session.elapsed"), "$elapsed min", Modifier.weight(1f))
                MetricItem(t("session.remaining"), "$remaining min", Modifier.weight(1f))
                MetricItem(t("session.eta"), etaStr, Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(12.dp))

        SafeMapView(
            destination = activeSession.destination,
            currentPosition = activeSession.currentPosition,
            progressPercent = activeSession.progressPercent,
            deviationMeters = activeSession.deviationMeters,
            inaccurateGps = demo.inaccurateGps,
        )

        Spacer(Modifier.height(12.dp))

        SafeCard(
            borderColor = if (activeSession.state == SessionState.PAUSED) {
                palette.orangeBorder
            } else {
                palette.greenBorder
            },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PulseDot(
                    color = if (activeSession.state == SessionState.PAUSED) palette.orange500 else palette.green500,
                )
                Column {
                    Text(
                        text = if (activeSession.state == SessionState.PAUSED) {
                            t("session.status.paused")
                        } else {
                            t("session.status.ok")
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = palette.textPrimary,
                    )
                    Text(
                        text = "${t("session.status.active")} · ${t("session.status.normal")}",
                        fontSize = 12.sp,
                        color = palette.textMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        SafeGradientButton(
            onClick = { arrivalSheetOpen = true },
            minHeight = 54.dp,
            testTag = "session_arrived_button",
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(t("session.btn.arrived"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SafeSecondaryButton(
                onClick = onPauseOrResume,
                modifier = Modifier.weight(1f),
                testTag = "session_pause_button",
            ) {
                Icon(
                    imageVector = if (activeSession.state == SessionState.PAUSED) {
                        Icons.Filled.PlayArrow
                    } else {
                        Icons.Filled.Pause
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = if (activeSession.state == SessionState.PAUSED) {
                        t("session.btn.resume")
                    } else {
                        t("session.btn.pause")
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            SafeGradientButton(
                onClick = { onOpenSosProtocol(AlertReason.SOS) },
                modifier = Modifier.weight(1f),
                brush = Brush.linearGradient(listOf(palette.red500, palette.red600)),
                testTag = "session_sos_button",
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("🔴 SOS", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (arrivalSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { arrivalSheetOpen = false },
            containerColor = palette.bgSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "🏠 ${t("arrival.title")}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Destination : ${activeSession.destination.label}",
                    fontSize = 14.sp,
                    color = palette.textSecondary,
                )
                Spacer(Modifier.height(16.dp))

                SafeGradientButton(
                    onClick = {
                        arrivalSheetOpen = false
                        onCompleteSession()
                    },
                    minHeight = 56.dp,
                    testTag = "arrival_confirm_button",
                ) {
                    Text(
                        text = "🟢 ${t("arrival.yes")} — ${t("arrival.yesSub")}",
                        fontWeight = FontWeight.Bold,
                    )
                }

                SectionTitle("⏱ ${t("arrival.extend")}")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(15, 30, 60).forEach { mins ->
                        SafeSecondaryButton(
                            onClick = {
                                onExtendSession(mins)
                                arrivalSheetOpen = false
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "arrival_extend_$mins",
                        ) {
                            Text(if (mins < 60) "+$mins min" else "+1 h")
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = customExtendMins.toString(),
                        onValueChange = { raw ->
                            val parsed = raw.filter { it.isDigit() }.toIntOrNull()
                            if (parsed != null) customExtendMins = parsed.coerceIn(5, 240)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    SafeSecondaryButton(
                        onClick = {
                            onExtendSession(customExtendMins)
                            arrivalSheetOpen = false
                        },
                    ) {
                        Text("+$customExtendMins min")
                    }
                }

                Spacer(Modifier.height(12.dp))
                SafeSecondaryButton(
                    onClick = { arrivalSheetOpen = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Fermer")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, modifier: Modifier = Modifier) {
    val palette = LocalSafePalette.current
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = palette.textMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(text = value, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val palette = LocalSafePalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, fontSize = 15.sp, color = palette.textMuted)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
    }
}
