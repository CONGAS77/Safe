package com.example.data

import com.example.domain.model.TrustedContact
import java.util.UUID

data class ContactInput(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String? = null,
    val acceptedByContact: Boolean,
    val isPrimary: Boolean? = null,
)

class ContactService {
    fun createContact(existing: List<TrustedContact>, input: ContactInput): List<TrustedContact> {
        val shouldBePrimary = input.isPrimary == true || existing.isEmpty()
        val base = if (shouldBePrimary) existing.map { it.copy(isPrimary = false) } else existing
        val next = TrustedContact(
            id = "cnt-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(3)}",
            firstName = input.firstName.trim(),
            lastName = input.lastName.trim(),
            phone = input.phone.trim(),
            email = input.email?.trim()?.takeIf { it.isNotEmpty() },
            isPrimary = shouldBePrimary,
            acceptedByContact = input.acceptedByContact,
            createdAt = System.currentTimeMillis(),
        )
        return base + next
    }

    fun updateContact(existing: List<TrustedContact>, id: String, input: ContactInput): List<TrustedContact> {
        val makePrimary = input.isPrimary == true
        return existing.map { c ->
            if (c.id != id) {
                if (makePrimary) c.copy(isPrimary = false) else c
            } else {
                c.copy(
                    firstName = input.firstName.trim(),
                    lastName = input.lastName.trim(),
                    phone = input.phone.trim(),
                    email = input.email?.trim()?.takeIf { it.isNotEmpty() },
                    acceptedByContact = input.acceptedByContact,
                    isPrimary = if (makePrimary) true else c.isPrimary,
                )
            }
        }
    }

    fun deleteContact(existing: List<TrustedContact>, id: String): List<TrustedContact> {
        val remaining = existing.filter { it.id != id }
        return if (remaining.isNotEmpty() && remaining.none { it.isPrimary }) {
            remaining.mapIndexed { idx, c -> if (idx == 0) c.copy(isPrimary = true) else c }
        } else {
            remaining
        }
    }

    fun setPrimaryContact(existing: List<TrustedContact>, id: String): List<TrustedContact> =
        existing.map { it.copy(isPrimary = it.id == id) }
}
