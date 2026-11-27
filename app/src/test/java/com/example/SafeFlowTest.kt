package com.example

import com.example.data.AlertService
import com.example.data.CheckInService
import com.example.data.ContactInput
import com.example.data.ContactService
import com.example.data.CreateCheckInInput
import com.example.data.CreateSessionInput
import com.example.data.LocationService
import com.example.data.SeedData
import com.example.data.SessionService
import com.example.data.StorageService
import com.example.domain.engine.DetectionEngine
import com.example.domain.engine.SessionStateMachine
import com.example.domain.model.ActivityType
import com.example.domain.model.AlertReason
import com.example.domain.model.AnomalyType
import com.example.domain.model.CheckInStatus
import com.example.domain.model.CountryCode
import com.example.domain.model.DetectionConfig
import com.example.domain.model.RetentionPolicy
import com.example.domain.model.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeFlowTest {
    @Test
    fun enforcesValidSessionStateTransitions() {
        assertTrue(SessionStateMachine.canTransitionSessionState(SessionState.DRAFT, SessionState.READY))
        assertTrue(SessionStateMachine.canTransitionSessionState(SessionState.READY, SessionState.ACTIVE))
        assertTrue(SessionStateMachine.canTransitionSessionState(SessionState.ACTIVE, SessionState.WARNING))
        assertTrue(SessionStateMachine.canTransitionSessionState(SessionState.WARNING, SessionState.USER_CONFIRMED))
        assertFalse(SessionStateMachine.canTransitionSessionState(SessionState.COMPLETED, SessionState.ACTIVE))
    }

    @Test
    fun runsFullRequiredUserScenarioCleanly() {
        val locationService = LocationService()
        val sessionService = SessionService()
        val storage = StorageService()
        val config = DetectionConfig()

        val destination = locationService.resolveDestination("Maison")
        val contacts = SeedData.createDemoContacts()
        val thomas = contacts.first { it.firstName == "Thomas" }

        val draft = sessionService.createSession(
            CreateSessionInput(
                activityType = ActivityType.WALK_HOME,
                title = "Retour à pied",
                originLabel = "Position actuelle",
                destination = destination,
                expectedDurationMinutes = 20,
                contactId = thomas.id,
            ),
        )
        assertEquals(SessionState.READY, draft.state)

        val active = sessionService.startSession(draft, 1_000_000L)
        assertEquals(SessionState.ACTIVE, active.state)

        val delayed = active.copy(elapsedMinutes = 35.0)
        val evalResult = DetectionEngine.evaluateSessionDetection(delayed, config)
        assertEquals(AnomalyType.DELAY_WARNING, evalResult.anomalyType)

        val warned = sessionService.triggerAnomaly(
            delayed,
            evalResult.anomalyType!!,
            evalResult.reason.orEmpty(),
            1_000_000L + 35L * 60_000L,
        )
        assertEquals(SessionState.WARNING, warned.state)
        assertEquals(AnomalyType.DELAY_WARNING, warned.activeAnomaly?.type)

        val resumed = sessionService.confirmUserOk(warned, 1_000_000L + 36L * 60_000L)
        assertEquals(SessionState.ACTIVE, resumed.state)
        assertNull(resumed.activeAnomaly)

        val arrived = resumed.copy(progressPercent = 100)
        val completed = sessionService.completeSession(arrived, 1_000_000L + 38L * 60_000L)
        assertEquals(SessionState.COMPLETED, completed.state)
        assertEquals(100, completed.progressPercent)

        val summary = sessionService.toHistoryItem(completed, thomas, true)
        assertEquals("Retour à pied", summary.title)
        assertEquals("Maison", summary.destinationLabel)
        assertEquals(SessionState.COMPLETED, summary.endState)
        assertEquals(1, summary.anomaliesCount)

        val retained = storage.applyHistoryRetention(listOf(summary), RetentionPolicy.DAYS_30, 1_000_000L + 38L * 60_000L)
        assertEquals(1, retained.size)
        val wiped = storage.applyHistoryRetention(listOf(summary), RetentionPolicy.NONE)
        assertEquals(0, wiped.size)
    }

    @Test
    fun ignoresRouteDeviationWhenGpsIsInaccurate() {
        val locationService = LocationService()
        val sessionService = SessionService()
        val dest = locationService.resolveDestination("Maison")
        val started = sessionService.startSession(
            sessionService.createSession(
                CreateSessionInput(
                    activityType = ActivityType.WALK_HOME,
                    title = "Retour à pied",
                    originLabel = "Position actuelle",
                    destination = dest,
                    expectedDurationMinutes = 20,
                    contactId = null,
                ),
            ),
        )

        val inaccuratePoint = locationService.interpolatePositionAlongRoute(dest, 50, 400.0, true)
        val sessionWithBadGps = started.copy(
            deviationMeters = 400.0,
            currentPosition = inaccuratePoint,
        )

        val result = DetectionEngine.evaluateSessionDetection(sessionWithBadGps, DetectionConfig())
        assertNull(result.anomalyType)
        assertTrue(result.gpsImpreciseIgnored)
    }

    @Test
    fun handlesCheckInsContactsAndNonAlarmistAlertDrafts() {
        val checkInSvc = CheckInService()
        val contactSvc = ContactService()
        val alertSvc = AlertService()
        val demoContacts = SeedData.createDemoContacts()

        val msg = checkInSvc.buildDefaultMessage("23:30")
        assertTrue(msg.contains("23:30"))

        val chk = checkInSvc.createCheckIn(
            CreateCheckInInput(
                dueAt = 2_000_000L,
                contactId = demoContacts.first().id,
                message = msg,
            ),
        )
        val due = checkInSvc.markDue(chk, 3, 2_000_000L)
        assertEquals(CheckInStatus.DUE, due.status)

        val contacts = contactSvc.createContact(
            demoContacts,
            ContactInput(
                firstName = "Luc",
                lastName = "Morel",
                phone = "+33 6 00 00 00 00",
                acceptedByContact = true,
            ),
        )
        assertEquals(3, contacts.size)

        val draft = alertSvc.buildContactMessageDraft(
            contact = demoContacts.first(),
            session = null,
            reason = AlertReason.CHECKIN_EXPIRED,
        )
        assertFalse(draft.messageBody.contains("en danger"))
        assertTrue(draft.messageBody.contains("SAFE n'a pas reçu la confirmation attendue."))
        assertNotNull(draft.contact)
        assertEquals("112", alertSvc.getRegionalEmergencyInfo(CountryCode.FR).primaryNumber)
    }
}
