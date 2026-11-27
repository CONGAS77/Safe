package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ContactInput
import com.example.domain.model.TrustedContact
import com.example.ui.components.PillTone
import com.example.ui.components.SafeCard
import com.example.ui.components.SafeGradientButton
import com.example.ui.components.SafeOutlineDangerButton
import com.example.ui.components.SafePill
import com.example.ui.components.SafeSecondaryButton
import com.example.ui.theme.LocalSafePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<TrustedContact>,
    t: (String) -> String,
    onAddContact: (ContactInput) -> Unit,
    onUpdateContact: (String, ContactInput) -> Unit,
    onDeleteContact: (String) -> Unit,
    onSetPrimaryContact: (String) -> Unit,
) {
    val palette = LocalSafePalette.current
    var formOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TrustedContact?>(null) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(true) }

    fun openAdd() {
        editing = null
        firstName = ""
        lastName = ""
        phone = ""
        email = ""
        accepted = true
        formOpen = true
    }

    fun openEdit(c: TrustedContact) {
        editing = c
        firstName = c.firstName
        lastName = c.lastName
        phone = c.phone
        email = c.email.orEmpty()
        accepted = c.acceptedByContact
        formOpen = true
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = t("contacts.title"),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = palette.textPrimary,
            )
            SafeGradientButton(
                onClick = ::openAdd,
                modifier = Modifier.fillMaxWidth(0.38f),
                minHeight = 42.dp,
                testTag = "contacts_add_button",
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(t("contacts.add"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(14.dp))

        if (contacts.isEmpty()) {
            SafeCard(contentPadding = PaddingValues(24.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Aucun contact de confiance enregistré.",
                        fontSize = 14.sp,
                        color = palette.textSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    SafeGradientButton(onClick = ::openAdd) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(t("contacts.add"))
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                contacts.forEach { c ->
                    SafeCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${c.firstName} ${c.lastName}".trim(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = palette.textPrimary,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = buildString {
                                        append(c.phone)
                                        if (!c.email.isNullOrBlank()) {
                                            append(" · ")
                                            append(c.email)
                                        }
                                    },
                                    fontSize = 13.sp,
                                    color = palette.textSecondary,
                                )
                            }
                            SafePill(
                                text = if (c.isPrimary) t("contacts.primary") else t("contacts.secondary"),
                                tone = if (c.isPrimary) PillTone.GREEN else PillTone.NEUTRAL,
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = palette.green600,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = t("contacts.accepted"),
                                fontSize = 12.sp,
                                color = palette.green600,
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (!c.isPrimary) {
                                SafeSecondaryButton(
                                    onClick = { onSetPrimaryContact(c.id) },
                                    minHeight = 40.dp,
                                    testTag = "contact_primary_${c.id}",
                                ) {
                                    Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text(t("contacts.setPrimary"), fontSize = 12.sp)
                                }
                            }
                            SafeSecondaryButton(
                                onClick = { openEdit(c) },
                                minHeight = 40.dp,
                                testTag = "contact_edit_${c.id}",
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(t("contacts.edit"), fontSize = 12.sp)
                            }
                            SafeOutlineDangerButton(
                                onClick = { onDeleteContact(c.id) },
                                minHeight = 40.dp,
                                testTag = "contact_delete_${c.id}",
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(t("contacts.delete"), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (formOpen) {
        ModalBottomSheet(
            onDismissRequest = { formOpen = false },
            containerColor = palette.bgSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    text = if (editing != null) t("contacts.edit") else t("contacts.add"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Prénom") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("contact_first_name_input"),
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Nom") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("contact_last_name_input"),
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone") },
                    placeholder = { Text("+33 6 12 34 56 78") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("contact_phone_input"),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = { accepted = it },
                    )
                    Text(
                        text = "Le contact a explicitement accepté d’être contact de confiance.",
                        fontSize = 13.sp,
                        color = palette.textSecondary,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SafeGradientButton(
                        onClick = {
                            if (firstName.isNotBlank() && phone.isNotBlank() && accepted) {
                                val input = ContactInput(
                                    firstName = firstName,
                                    lastName = lastName,
                                    phone = phone,
                                    email = email,
                                    acceptedByContact = accepted,
                                )
                                val currentEditing = editing
                                if (currentEditing != null) {
                                    onUpdateContact(currentEditing.id, input)
                                } else {
                                    onAddContact(input)
                                }
                                formOpen = false
                            }
                        },
                        enabled = firstName.isNotBlank() && phone.isNotBlank() && accepted,
                        modifier = Modifier.weight(1f),
                        testTag = "contact_save_button",
                    ) {
                        Text("Enregistrer", fontWeight = FontWeight.Bold)
                    }
                    SafeSecondaryButton(onClick = { formOpen = false }) {
                        Text("Annuler")
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
