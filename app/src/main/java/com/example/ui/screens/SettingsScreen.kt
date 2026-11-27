package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.EmergencyNumbers
import com.example.domain.model.AnomalyType
import com.example.domain.model.CountryCode
import com.example.domain.model.DemoSimulationState
import com.example.domain.model.DetectionConfig
import com.example.domain.model.PrivacySettings
import com.example.domain.model.RetentionPolicy
import com.example.domain.model.SafeSession
import com.example.domain.model.SupportedLanguage
import com.example.domain.model.UserProfile
import com.example.ui.components.SafeCard
import com.example.ui.components.SafeDisclaimerBox
import com.example.ui.components.SafeGradientButton
import com.example.ui.components.SafeOutlineDangerButton
import com.example.ui.components.SafeSecondaryButton
import com.example.ui.components.SectionTitle
import com.example.ui.theme.LocalSafePalette

@Composable
fun SettingsScreen(
    privacy: PrivacySettings,
    demo: DemoSimulationState,
    detectionConfig: DetectionConfig,
    activeSession: SafeSession?,
    language: SupportedLanguage,
    darkMode: Boolean,
    country: CountryCode,
    user: UserProfile,
    t: (String) -> String,
    onSetLocationPermission: (Boolean) -> Unit,
    onSetRetentionPolicy: (RetentionPolicy) -> Unit,
    onDeleteAllData: () -> Unit,
    onSetDemoEnabled: (Boolean) -> Unit,
    onUpdateDetectionConfig: ((DetectionConfig) -> DetectionConfig) -> Unit,
    onOpenDemoDrawer: () -> Unit,
    onTriggerDemoAnomaly: (AnomalyType) -> Unit,
    onSetCountry: (CountryCode) -> Unit,
    onSetLanguage: (SupportedLanguage) -> Unit,
    onToggleDarkMode: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onSignInEmail: (String) -> Unit,
    onSignInGoogle: () -> Unit,
    onAppleSignInNotice: () -> String,
    onSignOut: () -> Unit,
) {
    val palette = LocalSafePalette.current
    var confirmDeleteOpen by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var authNotice by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onSetLocationPermission(granted)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = t("settings.title"),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = palette.textPrimary,
        )

        // Privacy
        SectionTitle("🔒 ${t("settings.privacy")}")
        SafeCard {
            Text("Localisation", fontWeight = FontWeight.Bold, color = palette.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "SAFE utilise votre position uniquement lorsque cela est nécessaire pour une session active et selon les autorisations de votre appareil.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = palette.textSecondary,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Autoriser la localisation pendant les sessions",
                    fontSize = 14.sp,
                    color = palette.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = privacy.locationPermissionGranted,
                    onCheckedChange = { enable ->
                        if (enable) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                ),
                            )
                        } else {
                            onSetLocationPermission(false)
                        }
                    },
                )
            }

            Spacer(Modifier.height(12.dp))
            Text("Historique — Conservation", fontWeight = FontWeight.Bold, color = palette.textPrimary)
            Spacer(Modifier.height(6.dp))
            RetentionDropdown(
                selected = privacy.retentionPolicy,
                onSelect = onSetRetentionPolicy,
            )

            Spacer(Modifier.height(12.dp))
            Text("Partage", fontWeight = FontWeight.Bold, color = palette.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Vous contrôlez les personnes pouvant recevoir vos informations.",
                fontSize = 13.sp,
                color = palette.textSecondary,
            )
            Spacer(Modifier.height(14.dp))

            if (!confirmDeleteOpen) {
                SafeOutlineDangerButton(
                    onClick = { confirmDeleteOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_delete_all_button",
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(t("settings.deleteAll"))
                }
            } else {
                SafeCard(
                    containerColor = palette.redSoft,
                    borderColor = palette.redBorder,
                ) {
                    Text(
                        text = "Confirmer la suppression définitive de toutes vos données locales ?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = palette.textPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SafeGradientButton(
                            onClick = {
                                onDeleteAllData()
                                confirmDeleteOpen = false
                            },
                            modifier = Modifier.weight(1f),
                            brush = Brush.linearGradient(listOf(palette.red500, palette.red600)),
                            testTag = "settings_confirm_delete_button",
                        ) {
                            Text("Oui, supprimer", fontWeight = FontWeight.Bold)
                        }
                        SafeSecondaryButton(onClick = { confirmDeleteOpen = false }) {
                            Text("Annuler")
                        }
                    }
                }
            }
        }

        // Developer / Demo simulation
        SectionTitle("${t("settings.demo")} (Paramètres développeur)")
        SafeCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Activer le Mode simulation (DEMO MODE)",
                    fontSize = 14.sp,
                    color = palette.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = demo.enabled,
                    onCheckedChange = onSetDemoEnabled,
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = detectionConfig.gracePeriodMinutes.toString(),
                    onValueChange = { raw ->
                        val v = raw.filter { it.isDigit() }.toIntOrNull() ?: 0
                        onUpdateDetectionConfig { it.copy(gracePeriodMinutes = v.coerceIn(0, 60)) }
                    },
                    label = { Text("Tolérance retard (min)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = detectionConfig.stationaryThresholdMinutes.toString(),
                    onValueChange = { raw ->
                        val v = raw.filter { it.isDigit() }.toIntOrNull() ?: 1
                        onUpdateDetectionConfig { it.copy(stationaryThresholdMinutes = v.coerceIn(1, 120)) }
                    },
                    label = { Text("Seuil arrêt (min)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SafeSecondaryButton(
                    onClick = onOpenDemoDrawer,
                    modifier = Modifier.weight(1f),
                    testTag = "settings_open_simulator_button",
                ) {
                    Text("Ouvrir simulateur", fontSize = 13.sp)
                }
                SafeGradientButton(
                    onClick = { onTriggerDemoAnomaly(AnomalyType.DELAY_WARNING) },
                    enabled = activeSession != null,
                    modifier = Modifier.weight(1f),
                    brush = Brush.linearGradient(listOf(palette.orange500, palette.orange600)),
                    testTag = "settings_trigger_anomaly_button",
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(15.dp))
                    Text("Déclencher anomalie", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Regional Emergency & Preferences
        SectionTitle("Préférences & Sécurité régionale")
        SafeCard {
            Text("Pays (numéros d’urgence)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textSecondary)
            Spacer(Modifier.height(6.dp))
            CountryDropdown(selected = country, onSelect = onSetCountry)

            Spacer(Modifier.height(12.dp))
            Text(t("settings.language"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textSecondary)
            Spacer(Modifier.height(6.dp))
            LanguageDropdown(selected = language, onSelect = onSetLanguage)

            Spacer(Modifier.height(12.dp))
            SafeSecondaryButton(
                onClick = onToggleDarkMode,
                modifier = Modifier.fillMaxWidth(),
                testTag = "settings_dark_mode_button",
            ) {
                Icon(
                    imageVector = if (darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                )
                Text(
                    text = if (darkMode) "Passer en mode clair" else "Passer en mode sombre (luminosité faible)",
                    fontSize = 13.sp,
                )
            }

            Spacer(Modifier.height(8.dp))
            SafeSecondaryButton(
                onClick = onReplayOnboarding,
                modifier = Modifier.fillMaxWidth(),
                testTag = "settings_replay_onboarding_button",
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(17.dp))
                Text("Revoir la présentation SAFE", fontSize = 13.sp)
            }
        }

        // Account / Auth MVP
        SectionTitle("Compte & Authentification (MVP)")
        SafeCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = palette.textPrimary)
                Column {
                    Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.textPrimary)
                    Text(
                        text = "${user.email.ifBlank { "Session locale hors compte" }} (${user.provider.name.lowercase()})",
                        fontSize = 12.sp,
                        color = palette.textMuted,
                    )
                }
            }

            if (authNotice != null) {
                Spacer(Modifier.height(10.dp))
                SafeDisclaimerBox(text = authNotice.orEmpty())
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    placeholder = { Text("email@exemple.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.weight(1f),
                )
                SafeSecondaryButton(
                    onClick = {
                        if (emailInput.isNotBlank()) {
                            onSignInEmail(emailInput)
                            emailInput = ""
                            authNotice = "Connecté par email (mode MVP)."
                        }
                    },
                ) {
                    Text("Email")
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SafeSecondaryButton(
                    onClick = {
                        onSignInGoogle()
                        authNotice = "Compte Google connecté (mode démo)."
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Google Sign-In", fontSize = 12.sp)
                }
                SafeSecondaryButton(
                    onClick = { authNotice = onAppleSignInNotice() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Apple Sign-In", fontSize = 12.sp)
                }
            }

            if (user.signedIn) {
                Spacer(Modifier.height(8.dp))
                SafeOutlineDangerButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Déconnexion", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun RetentionDropdown(
    selected: RetentionPolicy,
    onSelect: (RetentionPolicy) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selected) {
        RetentionPolicy.NONE -> "Ne rien conserver"
        RetentionPolicy.HOURS_24 -> "Conserver 24 h"
        RetentionPolicy.DAYS_7 -> "Conserver 7 jours"
        RetentionPolicy.DAYS_30 -> "Conserver 30 jours"
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(
                RetentionPolicy.NONE to "Ne rien conserver",
                RetentionPolicy.HOURS_24 to "Conserver 24 h",
                RetentionPolicy.DAYS_7 to "Conserver 7 jours",
                RetentionPolicy.DAYS_30 to "Conserver 30 jours",
            ).forEach { (policy, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(policy)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun CountryDropdown(
    selected: CountryCode,
    onSelect: (CountryCode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val cfg = EmergencyNumbers.getEmergencyConfig(selected)
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${cfg.countryName} — Urgence ${cfg.primaryNumber}")
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            EmergencyNumbers.byCountry.values.forEach { item ->
                DropdownMenuItem(
                    text = { Text("${item.countryName} — Urgence ${item.primaryNumber}") },
                    onClick = {
                        onSelect(item.country)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun LanguageDropdown(
    selected: SupportedLanguage,
    onSelect: (SupportedLanguage) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().testTag("settings_language_dropdown"),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(selected.label)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SupportedLanguage.entries.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.label) },
                    onClick = {
                        onSelect(lang)
                        expanded = false
                    },
                )
            }
        }
    }
}
