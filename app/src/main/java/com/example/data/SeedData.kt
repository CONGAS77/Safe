package com.example.data

import com.example.domain.model.ActivityType
import com.example.domain.model.AuthProvider
import com.example.domain.model.SessionHistoryItem
import com.example.domain.model.SessionState
import com.example.domain.model.TrustedContact
import com.example.domain.model.UserProfile

object SeedData {
    val DEMO_USER_PROFILE = UserProfile(
        id = "usr-demo",
        displayName = "Camille",
        email = "camille@safe-demo.app",
        provider = AuthProvider.DEMO,
        signedIn = true,
    )

    fun createDemoContacts(now: Long = System.currentTimeMillis()): List<TrustedContact> = listOf(
        TrustedContact(
            id = "cnt-thomas",
            firstName = "Thomas",
            lastName = "Martin",
            phone = "+33 6 12 34 56 78",
            email = "thomas.martin@example.com",
            isPrimary = true,
            acceptedByContact = true,
            createdAt = now - 10L * 24 * 60 * 60_000L,
        ),
        TrustedContact(
            id = "cnt-sarah",
            firstName = "Sarah",
            lastName = "Benali",
            phone = "+33 6 98 76 54 32",
            email = "sarah.benali@example.com",
            isPrimary = false,
            acceptedByContact = true,
            createdAt = now - 5L * 24 * 60 * 60_000L,
        ),
    )

    fun createDemoHistory(now: Long = System.currentTimeMillis()): List<SessionHistoryItem> {
        val hour = 60 * 60_000L
        val day = 24 * hour
        return listOf(
            SessionHistoryItem(
                id = "hist-1",
                activityType = ActivityType.WALK_HOME,
                title = "Retour à pied",
                destinationLabel = "Maison",
                startedAt = now - 3 * hour,
                endedAt = now - 3 * hour + 23 * 60_000L,
                durationMinutes = 23,
                distanceKm = 2.4,
                endState = SessionState.COMPLETED,
                anomaliesCount = 0,
                anomalies = emptyList(),
                contactName = "Thomas Martin",
            ),
            SessionHistoryItem(
                id = "hist-2",
                activityType = ActivityType.CAR,
                title = "Trajet",
                destinationLabel = "Lyon Part-Dieu",
                startedAt = now - 1 * day - 2 * hour,
                endedAt = now - 1 * day - 48 * 60_000L,
                durationMinutes = 72,
                distanceKm = 64.5,
                endState = SessionState.COMPLETED,
                anomaliesCount = 0,
                anomalies = emptyList(),
                contactName = "Sarah Benali",
            ),
            SessionHistoryItem(
                id = "hist-3",
                activityType = ActivityType.HIKE,
                title = "Randonnée",
                destinationLabel = "Sentier des Crêtes",
                startedAt = now - 6 * day,
                endedAt = now - 6 * day + 221 * 60_000L,
                durationMinutes = 221,
                distanceKm = 12.8,
                endState = SessionState.COMPLETED,
                anomaliesCount = 0,
                anomalies = emptyList(),
                contactName = "Thomas Martin",
            ),
        )
    }
}
