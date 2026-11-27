package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AlertReason
import com.example.domain.model.AnomalyType
import com.example.domain.model.CheckIn
import com.example.domain.model.DemoSimulationState
import com.example.domain.model.SafeSession
import com.example.ui.theme.LocalSafePalette
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoControlDrawer(
    demo: DemoSimulationState,
    activeSession: SafeSession?,
    activeCheckIn: CheckIn?,
    demoDrawerOpen: Boolean,
    onCloseDrawer: () -> Unit,
    onUpdateDemo: ((DemoSimulationState) -> DemoSimulationState) -> Unit,
    onTriggerDemoAnomaly: (AnomalyType) -> Unit,
    onSimulateArrival: () -> Unit,
    onSimulateCheckInDueNow: () -> Unit,
    onSimulateCheckInExpiredNow: () -> Unit,
    onOpenSosProtocol: (AlertReason) -> Unit,
) {
    if (!demo.enabled || !demoDrawerOpen) return

    val palette = LocalSafePalette.current

    ModalBottomSheet(
        onDismissRequest = onCloseDrawer,
        containerColor = palette.bgSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Mode simulation (DEMO MODE)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = palette.textPrimary,
                    )
                    Text(
                        text = "Testez la logique de sécurité sans GPS réel",
                        fontSize = 12.sp,
                        color = palette.textSecondary,
                    )
                }
                IconButton(onClick = onCloseDrawer) {
                    Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = palette.textPrimary)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (activeSession != null) {
                SafeCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Position simulée", fontSize = 13.sp, color = palette.textSecondary)
                        Text("${activeSession.progressPercent} %", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                    }
                    Slider(
                        value = activeSession.progressPercent.toFloat(),
                        onValueChange = { value ->
                            onUpdateDemo { it.copy(simulatedProgressPercent = value.roundToInt()) }
                        },
                        valueRange = 0f..100f,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Temps écoulé simulé", fontSize = 13.sp, color = palette.textSecondary)
                        Text("${activeSession.elapsedMinutes.roundToInt()} min", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(12, 20, 35, 60).forEach { m ->
                            SafeSecondaryButton(
                                onClick = {
                                    onUpdateDemo { it.copy(simulatedElapsedMinutes = m.toDouble()) }
                                },
                                modifier = Modifier.weight(1f),
                                minHeight = 38.dp,
                            ) {
                                Text("${m}m", fontSize = 12.sp)
                            }
                        }
                    }
                }

                SectionTitle("Déclencher un scénario")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SafeGradientButton(
                        onClick = {
                            onCloseDrawer()
                            onTriggerDemoAnomaly(AnomalyType.DELAY_WARNING)
                        },
                        modifier = Modifier.weight(1f),
                        brush = Brush.linearGradient(listOf(palette.orange500, palette.orange600)),
                        testTag = "demo_trigger_delay",
                    ) {
                        Text("Retard (35 min)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    SafeGradientButton(
                        onClick = {
                            onCloseDrawer()
                            onTriggerDemoAnomaly(AnomalyType.STATIONARY_WARNING)
                        },
                        modifier = Modifier.weight(1f),
                        brush = Brush.linearGradient(listOf(palette.orange500, palette.orange600)),
                        testTag = "demo_trigger_stationary",
                    ) {
                        Text("Arrêt prolongé", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SafeGradientButton(
                        onClick = {
                            onCloseDrawer()
                            onTriggerDemoAnomaly(AnomalyType.ROUTE_DEVIATION_WARNING)
                        },
                        modifier = Modifier.weight(1f),
                        brush = Brush.linearGradient(listOf(palette.orange500, palette.orange600)),
                        testTag = "demo_trigger_deviation",
                    ) {
                        Text("Déviation GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    SafeGradientButton(
                        onClick = {
                            onCloseDrawer()
                            onSimulateArrival()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "demo_simulate_arrival",
                    ) {
                        Text("Simuler l’arrivée", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                SafeOutlineDangerButton(
                    onClick = {
                        onCloseDrawer()
                        onOpenSosProtocol(AlertReason.SOS)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "demo_simulate_sos",
                ) {
                    Text("Simuler SOS")
                }
            } else {
                SafeCard {
                    Text(
                        text = "Démarrez une session SAFE pour simuler la progression, le retard, l’arrêt prolongé, la déviation ou l’arrivée.",
                        fontSize = 14.sp,
                        color = palette.textSecondary,
                    )
                }
            }

            if (activeCheckIn != null) {
                Spacer(Modifier.height(12.dp))
                SafeCard {
                    Text("Check-in actif", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = palette.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SafeGradientButton(
                            onClick = {
                                onCloseDrawer()
                                onSimulateCheckInDueNow()
                            },
                            modifier = Modifier.weight(1f),
                            brush = Brush.linearGradient(listOf(palette.orange500, palette.orange600)),
                        ) {
                            Text("Échéance maintenant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        SafeGradientButton(
                            onClick = {
                                onCloseDrawer()
                                onSimulateCheckInExpiredNow()
                            },
                            modifier = Modifier.weight(1f),
                            brush = Brush.linearGradient(listOf(palette.red500, palette.red600)),
                        ) {
                            Text("Non confirmé", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            SectionTitle("Conditions appareil")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DeviceToggleRow("Batterie faible", demo.lowBattery) { checked ->
                    onUpdateDemo { it.copy(lowBattery = checked) }
                }
                DeviceToggleRow("Téléphone hors ligne", demo.offline) { checked ->
                    onUpdateDemo { it.copy(offline = checked) }
                }
                DeviceToggleRow("GPS imprécis (> 200 m)", demo.inaccurateGps) { checked ->
                    onUpdateDemo { it.copy(inaccurateGps = checked) }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DeviceToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val palette = LocalSafePalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 14.sp, color = palette.textPrimary)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
