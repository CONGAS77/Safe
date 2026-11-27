package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ContactInput
import com.example.data.CreateSessionInput
import com.example.domain.model.ActivityType
import com.example.domain.model.RouteDestination
import com.example.domain.model.TrustedContact
import com.example.ui.components.PillTone
import com.example.ui.components.SafeCard
import com.example.ui.components.SafeDisclaimerBox
import com.example.ui.components.SafeGradientButton
import com.example.ui.components.SafeMapView
import com.example.ui.components.SafePill
import com.example.ui.components.SafeSecondaryButton
import com.example.ui.theme.LocalSafePalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val QUICK_DURATIONS = listOf(15, 20, 30, 60, 120, 240)

private fun formatDuration(mins: Int): String {
    if (mins < 60) return "$mins min"
    val h = mins / 60
    val m = mins % 60
    return if (m == 0) "$h h" else "$h h $m"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateSessionWizard(
    presetActivity: ActivityType?,
    presetDestinations: List<RouteDestination>,
    contacts: List<TrustedContact>,
    t: (String) -> String,
    resolveDestination: (String) -> RouteDestination,
    onAddContact: (ContactInput) -> TrustedContact,
    onStartSession: (CreateSessionInput) -> Unit,
    onClose: () -> Unit,
) {
    val palette = LocalSafePalette.current
    var step by remember { mutableIntStateOf(1) }
    var activity by remember { mutableStateOf(presetActivity ?: ActivityType.WALK_HOME) }
    var destQuery by remember { mutableStateOf("Maison") }
    var resolvedDest by remember { mutableStateOf(presetDestinations.firstOrNull() ?: resolveDestination("Maison")) }
    var durationMinutes by remember { mutableIntStateOf(20) }
    var customDurationMode by remember { mutableStateOf(false) }
    var selectedContactId by remember {
        mutableStateOf(contacts.firstOrNull { it.isPrimary }?.id ?: contacts.firstOrNull()?.id)
    }

    var showAddContact by remember { mutableStateOf(false) }
    var newFirst by remember { mutableStateOf("") }
    var newLast by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newAccepted by remember { mutableStateOf(true) }

    LaunchedEffect(presetActivity) {
        val initialAct = presetActivity ?: ActivityType.WALK_HOME
        activity = initialAct
        when (initialAct) {
            ActivityType.WALK_HOME -> {
                destQuery = "Maison"
                resolvedDest = presetDestinations.getOrNull(0) ?: resolveDestination("Maison")
                durationMinutes = 20
            }
            ActivityType.HIKE -> {
                destQuery = "Sentier des Crêtes"
                resolvedDest = presetDestinations.getOrNull(3) ?: resolveDestination("Sentier des Crêtes")
                durationMinutes = 120
            }
            ActivityType.CAR -> {
                destQuery = "Travail"
                resolvedDest = presetDestinations.getOrNull(1) ?: resolveDestination("Travail")
                durationMinutes = 30
            }
            else -> Unit
        }
    }

    BackHandler {
        if (step > 1) step -= 1 else onClose()
    }

    val arrivalPreview = remember(durationMinutes) {
        SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(System.currentTimeMillis() + durationMinutes * 60_000L))
    }

    val selectedContact = contacts.firstOrNull { it.id == selectedContactId }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = palette.bgApp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.bgSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { if (step > 1) step -= 1 else onClose() },
                    modifier = Modifier.testTag("wizard_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = t("wizard.back"),
                        tint = palette.textPrimary,
                    )
                }
                Text(
                    text = "${t("wizard.step")} $step / 5",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = palette.textPrimary,
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("wizard_close_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Fermer",
                        tint = palette.textPrimary,
                    )
                }
            }

            LinearProgressIndicator(
                progress = { step / 5f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = palette.green500,
                trackColor = palette.borderSubtle,
            )

            // Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                when (step) {
                    1 -> {
                        Text(
                            text = t("wizard.step1.title"),
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(16.dp))
                        val choices = ActivityType.entries
                        choices.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowItems.forEach { act ->
                                    val selected = activity == act
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 76.dp)
                                            .testTag("wizard_activity_${act.name}")
                                            .clickable {
                                                activity = act
                                                step = 2
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (selected) palette.greenSoft else palette.bgSurface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) palette.green500 else palette.borderSubtle,
                                        ),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(act.emoji, fontSize = 22.sp)
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = t(act.translationKey),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = palette.textPrimary,
                                            )
                                        }
                                    }
                                }
                                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    2 -> {
                        Text(
                            text = t("wizard.step2.title"),
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = destQuery,
                            onValueChange = { value ->
                                destQuery = value
                                resolvedDest = resolveDestination(value)
                            },
                            label = { Text("Destination") },
                            placeholder = { Text(t("wizard.step2.placeholder")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("wizard_dest_input"),
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            presetDestinations.forEach { p ->
                                SafeSecondaryButton(
                                    onClick = {
                                        destQuery = p.label
                                        resolvedDest = resolveDestination(p.label)
                                    },
                                    minHeight = 38.dp,
                                    testTag = "wizard_preset_${p.id}",
                                ) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text(p.label, fontSize = 13.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        SafeCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(t("wizard.step2.depart"), fontSize = 14.sp, color = palette.textMuted)
                                Text(t("wizard.step2.currentPos"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.textPrimary)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(t("wizard.step2.arrivee"), fontSize = 14.sp, color = palette.textMuted)
                                Text(resolvedDest.label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.textPrimary)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        SafeMapView(
                            destination = resolvedDest,
                            currentPosition = resolvedDest.origin,
                            progressPercent = 0,
                            compact = true,
                        )
                        Spacer(Modifier.height(12.dp))
                        SafeDisclaimerBox(text = t("wizard.step2.mapNotice"))
                    }

                    3 -> {
                        Text(
                            text = t("wizard.step3.title"),
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(14.dp))
                        QUICK_DURATIONS.chunked(3).forEach { rowMins ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowMins.forEach { mins ->
                                    val selected = !customDurationMode && durationMinutes == mins
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 48.dp)
                                            .testTag("wizard_duration_$mins")
                                            .clickable {
                                                customDurationMode = false
                                                durationMinutes = mins
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (selected) palette.greenSoft else palette.bgSurface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) palette.green500 else palette.borderSubtle,
                                        ),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = formatDuration(mins),
                                                fontWeight = FontWeight.SemiBold,
                                                color = palette.textPrimary,
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        SafeSecondaryButton(
                            onClick = { customDurationMode = !customDurationMode },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("${t("wizard.step3.custom")} (${formatDuration(durationMinutes)})")
                        }

                        if (customDurationMode) {
                            Spacer(Modifier.height(12.dp))
                            SafeCard {
                                OutlinedTextField(
                                    value = durationMinutes.toString(),
                                    onValueChange = { raw ->
                                        val v = raw.filter { it.isDigit() }.toIntOrNull()
                                        if (v != null) durationMinutes = v.coerceIn(5, 720)
                                    },
                                    label = { Text("Durée en minutes") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        SafeCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(t("wizard.step3.arrivalAt"), fontSize = 14.sp, color = palette.textSecondary)
                                Text(
                                    text = arrivalPreview,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = palette.green600,
                                )
                            }
                        }
                    }

                    4 -> {
                        Text(
                            text = t("wizard.step4.title"),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(14.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedContactId = null },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedContactId == null) palette.greenSoft else palette.bgSurface,
                            border = BorderStroke(
                                1.dp,
                                if (selectedContactId == null) palette.green500 else palette.borderSubtle,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(t("wizard.step4.none"), fontWeight = FontWeight.Bold, color = palette.textPrimary)
                                Text(
                                    text = "Utiliser uniquement les rappels personnels sur cet appareil.",
                                    fontSize = 12.sp,
                                    color = palette.textMuted,
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        contacts.forEach { c ->
                            val selected = selectedContactId == c.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wizard_contact_${c.id}")
                                    .clickable { selectedContactId = c.id },
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) palette.greenSoft else palette.bgSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) palette.green500 else palette.borderSubtle,
                                ),
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${c.firstName} ${c.lastName}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = palette.textPrimary)
                                        Text("Contact de confiance · ${c.phone}", fontSize = 13.sp, color = palette.textSecondary)
                                    }
                                    if (selected) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = palette.green600)
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        if (!showAddContact) {
                            SafeSecondaryButton(
                                onClick = { showAddContact = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(t("wizard.step4.add"))
                            }
                        } else {
                            SafeCard {
                                Text("Nouveau contact de confiance", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = newFirst,
                                        onValueChange = { newFirst = it },
                                        label = { Text("Prénom") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                    )
                                    OutlinedTextField(
                                        value = newLast,
                                        onValueChange = { newLast = it },
                                        label = { Text("Nom") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newPhone,
                                    onValueChange = { newPhone = it },
                                    label = { Text("Téléphone (+33 6 ...)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newEmail,
                                    onValueChange = { newEmail = it },
                                    label = { Text("Email (optionnel)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = newAccepted, onCheckedChange = { newAccepted = it })
                                    Text(
                                        text = "Ce proche a explicitement accepté d’être contact de confiance.",
                                        fontSize = 12.sp,
                                        color = palette.textSecondary,
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SafeGradientButton(
                                        onClick = {
                                            if (newFirst.isNotBlank() && newPhone.isNotBlank() && newAccepted) {
                                                val created = onAddContact(
                                                    ContactInput(
                                                        firstName = newFirst,
                                                        lastName = newLast,
                                                        phone = newPhone,
                                                        email = newEmail,
                                                        acceptedByContact = newAccepted,
                                                    ),
                                                )
                                                selectedContactId = created.id
                                                showAddContact = false
                                                newFirst = ""
                                                newLast = ""
                                                newPhone = ""
                                                newEmail = ""
                                            }
                                        },
                                        enabled = newFirst.isNotBlank() && newPhone.isNotBlank() && newAccepted,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text("Enregistrer")
                                    }
                                    SafeSecondaryButton(onClick = { showAddContact = false }) {
                                        Text("Annuler")
                                    }
                                }
                            }
                        }
                    }

                    5 -> {
                        Text(
                            text = t("wizard.step5.title"),
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(14.dp))
                        SafeCard {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                WizardSummaryRow(t("wizard.step5.activity"), "${activity.emoji} ${t(activity.translationKey)}")
                                WizardSummaryRow(t("wizard.step5.start"), t("wizard.step2.currentPos"))
                                WizardSummaryRow(t("wizard.step5.destination"), resolvedDest.label)
                                WizardSummaryRow(t("wizard.step5.duration"), formatDuration(durationMinutes))
                                WizardSummaryRow(t("wizard.step5.contact"), selectedContact?.firstName ?: t("wizard.step4.none"))
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        SafeCard {
                            Text(t("wizard.step5.detection"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.textPrimary)
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                listOf("Position", "Progression", "Temps", "Arrêt prolongé", "Déviation importante").forEach { sig ->
                                    SafePill(text = "✓ $sig", tone = PillTone.GREEN)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        SafeGradientButton(
                            onClick = {
                                onStartSession(
                                    CreateSessionInput(
                                        activityType = activity,
                                        title = t(activity.translationKey),
                                        originLabel = t("wizard.step2.currentPos"),
                                        destination = resolvedDest,
                                        expectedDurationMinutes = durationMinutes,
                                        contactId = selectedContactId,
                                    ),
                                )
                            },
                            minHeight = 58.dp,
                            testTag = "wizard_start_safe_button",
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("🟢 ${t("wizard.step5.startBtn")}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = t("wizard.step5.consent"),
                            fontSize = 12.sp,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            if (step < 5) {
                HorizontalDivider(color = palette.borderSubtle)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.bgSurface)
                        .padding(16.dp),
                ) {
                    SafeGradientButton(
                        onClick = { step += 1 },
                        testTag = "wizard_next_button",
                    ) {
                        Text(t("wizard.next"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardSummaryRow(label: String, value: String) {
    val palette = LocalSafePalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = palette.textMuted)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
    }
}
