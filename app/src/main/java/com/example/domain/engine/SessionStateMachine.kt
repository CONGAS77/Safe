package com.example.domain.engine

import com.example.domain.model.SessionState

object SessionStateMachine {
    private val allowedTransitions: Map<SessionState, Set<SessionState>> = mapOf(
        SessionState.DRAFT to setOf(SessionState.READY, SessionState.CANCELLED),
        SessionState.READY to setOf(SessionState.ACTIVE, SessionState.DRAFT, SessionState.CANCELLED),
        SessionState.ACTIVE to setOf(
            SessionState.PAUSED,
            SessionState.WARNING,
            SessionState.ESCALATED,
            SessionState.COMPLETED,
            SessionState.CANCELLED,
            SessionState.EXPIRED,
        ),
        SessionState.PAUSED to setOf(
            SessionState.ACTIVE,
            SessionState.WARNING,
            SessionState.ESCALATED,
            SessionState.COMPLETED,
            SessionState.CANCELLED,
            SessionState.EXPIRED,
        ),
        SessionState.WARNING to setOf(
            SessionState.USER_CONFIRMED,
            SessionState.ACTIVE,
            SessionState.ESCALATED,
            SessionState.COMPLETED,
            SessionState.CANCELLED,
            SessionState.EXPIRED,
        ),
        SessionState.USER_CONFIRMED to setOf(SessionState.ACTIVE, SessionState.COMPLETED),
        SessionState.ESCALATED to setOf(
            SessionState.ACTIVE,
            SessionState.USER_CONFIRMED,
            SessionState.COMPLETED,
            SessionState.CANCELLED,
            SessionState.EXPIRED,
        ),
        SessionState.COMPLETED to emptySet(),
        SessionState.CANCELLED to emptySet(),
        SessionState.EXPIRED to emptySet(),
    )

    fun canTransitionSessionState(from: SessionState, to: SessionState): Boolean {
        if (from == to) return true
        return allowedTransitions[from]?.contains(to) == true
    }

    fun transitionSessionState(from: SessionState, to: SessionState): SessionState {
        require(canTransitionSessionState(from, to)) {
            "Transition d'état de session non autorisée : $from -> $to"
        }
        return to
    }

    fun isSessionRunning(state: SessionState): Boolean =
        state == SessionState.ACTIVE ||
            state == SessionState.PAUSED ||
            state == SessionState.WARNING ||
            state == SessionState.USER_CONFIRMED ||
            state == SessionState.ESCALATED

    fun isSessionTerminal(state: SessionState): Boolean =
        state == SessionState.COMPLETED ||
            state == SessionState.CANCELLED ||
            state == SessionState.EXPIRED
}
