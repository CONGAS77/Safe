package com.example.data

import com.example.domain.model.CheckIn
import com.example.domain.model.CheckInStatus
import java.util.UUID

data class CreateCheckInInput(
    val dueAt: Long,
    val contactId: String?,
    val message: String,
)

class CheckInService {
    fun buildDefaultMessage(dueTimeFormatted: String): String =
        "SAFE : cette personne devait confirmer que tout allait bien avant $dueTimeFormatted et n'a pas encore confirmé."

    fun createCheckIn(input: CreateCheckInInput, now: Long = System.currentTimeMillis()): CheckIn =
        CheckIn(
            id = "chk-$now-${UUID.randomUUID().toString().take(4)}",
            dueAt = input.dueAt,
            contactId = input.contactId,
            message = input.message.trim(),
            status = CheckInStatus.SCHEDULED,
            createdAt = now,
        )

    fun markDue(checkIn: CheckIn, graceMinutes: Int, now: Long = System.currentTimeMillis()): CheckIn {
        if (checkIn.status != CheckInStatus.SCHEDULED) return checkIn
        return checkIn.copy(
            status = CheckInStatus.DUE,
            dueTriggeredAt = now,
            graceExpiresAt = now + graceMinutes * 60_000L,
        )
    }

    fun confirmOk(checkIn: CheckIn, now: Long = System.currentTimeMillis()): CheckIn =
        checkIn.copy(
            status = CheckInStatus.CONFIRMED,
            confirmedAt = now,
        )

    fun escalate(checkIn: CheckIn): CheckIn =
        checkIn.copy(status = CheckInStatus.ESCALATED)

    fun cancel(checkIn: CheckIn): CheckIn =
        checkIn.copy(status = CheckInStatus.CANCELLED)
}
