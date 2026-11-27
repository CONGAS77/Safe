package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TrustedContact
import com.example.ui.components.PillTone
import com.example.ui.components.SafeGradientButton
import com.example.ui.components.SafePill
import com.example.ui.components.SafeSecondaryButton
import com.example.ui.theme.LocalSafePalette
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInModal(
    contacts: List<TrustedContact>,
    t: (String) -> String,
    buildDefaultMessage: (String) -> String,
    onSchedule: (dueAt: Long, contactId: String?, message: String) -> Unit,
    onClose: () -> Unit,
) {
    val palette = LocalSafePalette.current
    var offsetMinutes by remember { mutableIntStateOf(30) }
    val dueCalendar = remember(offsetMinutes) {
        Calendar.getInstance().apply { add(Calendar.MINUTE, offsetMinutes) }
    }
    val formattedTime = remember(offsetMinutes) {
        SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(dueCalendar.timeInMillis))
    }

    var contactId by remember {
        mutableStateOf(contacts.firstOrNull { it.isPrimary }?.id ?: contacts.firstOrNull()?.id ?: "")
    }
    var message by remember { mutableStateOf(buildDefaultMessage(formattedTime)) }
    var contactDropdownOpen by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = palette.bgSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SafePill(
                    text = t("checkin.title"),
                    tone = PillTone.GREEN,
                    leadingIcon = {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = palette.green600)
                    },
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = palette.textPrimary)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(t("checkin.subtitle"), fontSize = 14.sp, color = palette.textSecondary)
            Spacer(Modifier.height(14.dp))

            Text(
                text = "${t("checkin.dueTime")} : $formattedTime",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textSecondary,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(15, 30, 60, 120).forEach { mins ->
                    SafeSecondaryButton(
                        onClick = {
                            offsetMinutes = mins
                            val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, mins) }
                            val hhmm = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(cal.timeInMillis))
                            message = buildDefaultMessage(hhmm)
                        },
                        modifier = Modifier.weight(1f),
                        minHeight = 40.dp,
                    ) {
                        Text(if (mins < 60) "+${mins}m" else "+${mins / 60}h", fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(t("checkin.contact"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textSecondary)
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                val selectedContact = contacts.firstOrNull { it.id == contactId }
                OutlinedButton(
                    onClick = { contactDropdownOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = selectedContact?.let { "${it.firstName} ${it.lastName}" }
                                ?: "Aucun contact (rappel personnel)",
                        )
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = contactDropdownOpen,
                    onDismissRequest = { contactDropdownOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Aucun contact (rappel personnel)") },
                        onClick = {
                            contactId = ""
                            contactDropdownOpen = false
                        },
                    )
                    contacts.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.firstName} ${c.lastName}") },
                            onClick = {
                                contactId = c.id
                                contactDropdownOpen = false
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(t("checkin.message")) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            SafeGradientButton(
                onClick = {
                    val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, offsetMinutes) }
                    onSchedule(cal.timeInMillis, contactId.takeIf { it.isNotBlank() }, message)
                },
                enabled = message.isNotBlank(),
                testTag = "checkin_schedule_submit_button",
            ) {
                Text(t("checkin.scheduleBtn"), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}
