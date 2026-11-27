package com.example.data

import com.example.domain.engine.SessionStateMachine
import com.example.domain.model.ActivityType
import com.example.domain.model.AnomalyRecord
import com.example.domain.model.AnomalyResolution
import com.example.domain.model.AnomalyType
import com.example.domain.model.RouteDestination
import com.example.domain.model.SafeSession
import com.example.domain.model.SessionHistoryItem
import com.example.domain.model.SessionState
import com.example.domain.model.TrustedContact
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

data class CreateSessionInput(
    val activityType: ActivityType,
    val title: String,
    val originLabel: String,
    val destination: RouteDestination,
    val expectedDurationMinutes: Int,
    val contactId: String?,
)

class SessionService {
    fun createSession(input: CreateSessionInput): SafeSession = SafeSession(
        id = "sess-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(4)}",
        state = SessionState.READY,
        activityType = input.activityType,
        title = input.title,
        originLabel = input.originLabel,
        destination = input.destination,
        expectedDurationMinutes = input.expectedDurationMinutes,
        currentPosition = input.destination.origin,
        contactId = input.contactId,
    )

    fun startSession(session: SafeSession, now: Long = System.currentTimeMillis()): SafeSession {
        val nextState = SessionStateMachine.transitionSessionState(session.state, SessionState.ACTIVE)
        return session.copy(
            state = nextState,
            startedAt = now,
            lastSignalAt = now,
            estimatedArrivalAt = now + session.expectedDurationMinutes * 60_000L,
        )
    }

    fun pauseSession(session: SafeSession, now: Long = System.currentTimeMillis()): SafeSession {
        val nextState = SessionStateMachine.transitionSessionState(session.state, SessionState.PAUSED)
        return session.copy(
            state = nextState,
            pausedAt = now,
        )
    }

    fun resumeSession(session: SafeSession, now: Long = System.currentTimeMillis()): SafeSession {
        val nextState = SessionStateMachine.transitionSessionState(session.state, SessionState.ACTIVE)
        val pausedDuration = session.pausedAt?.let { max(0L, now - it) } ?: 0L
        val updatedEta = session.estimatedArrivalAt?.plus(pausedDuration)
            ?: (now + max(1, (session.expectedDurationMinutes - session.elapsedMinutes).roundToInt()) * 60_000L)
        return session.copy(
            state = nextState,
            pausedAt = null,
            totalPausedMs = session.totalPausedMs + pausedDuration,
            estimatedArrivalAt = updatedEta,
            lastSignalAt = now,
        )
    }

    fun extendSession(session: SafeSession, extraMinutes: Int, now: Long = System.currentTimeMillis()): SafeSession {
        val baseTarget = max(session.expectedDurationMinutes, ceil(session.elapsedMinutes).toInt())
        val newExpected = baseTarget + extraMinutes
        val baseArrival = session.startedAt ?: now
        val nextState = if (session.state == SessionState.WARNING || session.state == SessionState.ESCALATED) {
            SessionStateMachine.transitionSessionState(session.state, SessionState.ACTIVE)
        } else {
            session.state
        }
        val resolvedAnomaly = session.activeAnomaly?.copy(
            resolvedAt = now,
            resolution = AnomalyResolution.EXTENDED,
        )
        return session.copy(
            state = nextState,
            expectedDurationMinutes = newExpected,
            estimatedArrivalAt = baseArrival + newExpected * 60_000L + session.totalPausedMs,
            activeAnomaly = null,
            anomalies = if (resolvedAnomaly != null) {
                session.anomalies.map { if (it.id == resolvedAnomaly.id) resolvedAnomaly else it }
            } else {
                session.anomalies
            },
        )
    }

    fun updateDestination(session: SafeSession, destination: RouteDestination): SafeSession =
        session.copy(
            destination = destination,
            deviationMeters = 0.0,
        )

    fun triggerAnomaly(
        session: SafeSession,
        type: AnomalyType,
        details: String,
        now: Long = System.currentTimeMillis(),
    ): SafeSession {
        if (session.activeAnomaly?.type == type) return session
        val nextState = SessionStateMachine.transitionSessionState(session.state, SessionState.WARNING)
        val anomaly = AnomalyRecord(
            id = "anom-$now-${UUID.randomUUID().toString().take(4)}",
            type = type,
            triggeredAt = now,
            details = details,
        )
        return session.copy(
            state = nextState,
            activeAnomaly = anomaly,
            anomalies = session.anomalies + anomaly,
        )
    }

    fun confirmUserOk(session: SafeSession, now: Long = System.currentTimeMillis()): SafeSession {
        val confirmedState = SessionStateMachine.transitionSessionState(session.state, SessionState.USER_CONFIRMED)
        val activeState = SessionStateMachine.transitionSessionState(confirmedState, SessionState.ACTIVE)
        val resolvedAnomaly = session.activeAnomaly?.copy(
            resolvedAt = now,
            resolution = AnomalyResolution.CONFIRMED_OK,
        )
        val adjustedExpected = if (session.activeAnomaly?.type == AnomalyType.DELAY_WARNING) {
            max(session.expectedDurationMinutes, ceil(session.elapsedMinutes).toInt() + 10)
        } else {
            session.expectedDurationMinutes
        }
        return session.copy(
            state = activeState,
            expectedDurationMinutes = adjustedExpected,
            estimatedArrivalAt = session.startedAt?.let {
                it + adjustedExpected * 60_000L + session.totalPausedMs
            } ?: session.estimatedArrivalAt,
            stationaryMinutes = 0.0,
            deviationMeters = 0.0,
            activeAnomaly = null,
            lastSignalAt = now,
            anomalies = if (resolvedAnomaly != null) {
                session.anomalies.map { if (it.id == resolvedAnomaly.id) resolvedAnomaly else it }
            } else {
                session.anomalies
            },
        )
    }

    fun escalateSession(
        session: SafeSession,
        details: String,
        now: Long = System.currentTimeMillis(),
    ): SafeSession {
        val nextState = SessionStateMachine.transitionSessionState(session.state, SessionState.ESCALATED)
        val existing = session.activeAnomaly
        val anomaly = existing?.copy(
            resolvedAt = now,
            resolution = AnomalyResolution.ESCALATED,
        ) ?: AnomalyRecord(
            id = "anom-$now-sos",
            type = AnomalyType.MANUAL_SOS,
            triggeredAt = now,
            resolvedAt = now,
            resolution = AnomalyResolution.ESCALATED,
            details = details,
        )
        return session.copy(
            state = nextState,
            activeAnomaly = anomaly,
            anomalies = if (existing != null) {
                session.anomalies.map { if (it.id == anomaly.id) anomaly else it }
            } else {
                session.anomalies + anomaly
            },
        )
    }

    fun completeSession(session: SafeSession, now: Long = System.currentTimeMillis()): SafeSession {
        val nextState = SessionStateMachine.transitionSessionState(session.state, SessionState.COMPLETED)
        return session.copy(
            state = nextState,
            progressPercent = 100,
            currentPosition = session.destination.destination,
            activeAnomaly = null,
            endedAt = now,
        )
    }

    fun cancelSession(session: SafeSession, now: Long = System.currentTimeMillis()): SafeSession {
        val nextState = SessionStateMachine.transitionSessionState(session.state, SessionState.CANCELLED)
        return session.copy(
            state = nextState,
            activeAnomaly = null,
            endedAt = now,
        )
    }

    fun toHistoryItem(
        session: SafeSession,
        contact: TrustedContact?,
        keepLocation: Boolean,
    ): SessionHistoryItem {
        val endedAt = session.endedAt ?: System.currentTimeMillis()
        val startedAt = session.startedAt ?: endedAt
        val durationMinutes = max(1, session.elapsedMinutes.roundToInt())
        val endState = when (session.state) {
            SessionState.CANCELLED, SessionState.ESCALATED, SessionState.EXPIRED -> session.state
            else -> SessionState.COMPLETED
        }
        return SessionHistoryItem(
            id = session.id,
            activityType = session.activityType,
            title = session.title,
            destinationLabel = session.destination.label,
            startedAt = startedAt,
            endedAt = endedAt,
            durationMinutes = durationMinutes,
            distanceKm = session.destination.distanceKm,
            endState = endState,
            anomaliesCount = session.anomalies.size,
            anomalies = session.anomalies,
            contactName = contact?.let { "${it.firstName} ${it.lastName}".trim() },
            lastKnownPoint = if (keepLocation) session.currentPosition else null,
        )
    }
}
