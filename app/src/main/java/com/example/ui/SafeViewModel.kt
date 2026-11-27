package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlertService
import com.example.data.AuthService
import com.example.data.CheckInService
import com.example.data.ContactInput
import com.example.data.ContactService
import com.example.data.CreateCheckInInput
import com.example.data.CreateSessionInput
import com.example.data.LocationService
import com.example.data.NotificationService
import com.example.data.SeedData
import com.example.data.SessionService
import com.example.data.StorageService
import com.example.domain.engine.DetectionEngine
import com.example.domain.model.ActivityType
import com.example.domain.model.AlertDraftPayload
import com.example.domain.model.AlertReason
import com.example.domain.model.AnomalyType
import com.example.domain.model.CheckIn
import com.example.domain.model.CheckInStatus
import com.example.domain.model.CountryCode
import com.example.domain.model.DemoSimulationState
import com.example.domain.model.DetectionConfig
import com.example.domain.model.NotificationCategory
import com.example.domain.model.PrivacySettings
import com.example.domain.model.RetentionPolicy
import com.example.domain.model.RouteDestination
import com.example.domain.model.SafeNotification
import com.example.domain.model.SafeSession
import com.example.domain.model.SessionHistoryItem
import com.example.domain.model.SessionState
import com.example.domain.model.SupportedLanguage
import com.example.domain.model.TabId
import com.example.domain.model.TrustedContact
import com.example.domain.model.UserProfile
import com.example.i18n.SafeTranslations
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class SafeUiState(
    val activeTab: TabId = TabId.HOME,
    val onboardingDone: Boolean = false,
    val darkMode: Boolean = false,
    val language: SupportedLanguage = SupportedLanguage.FR,
    val country: CountryCode = CountryCode.FR,
    val user: UserProfile = SeedData.DEMO_USER_PROFILE,
    val contacts: List<TrustedContact> = emptyList(),
    val privacy: PrivacySettings = PrivacySettings(),
    val history: List<SessionHistoryItem> = emptyList(),
    val activeSession: SafeSession? = null,
    val lastCompletedSummary: SessionHistoryItem? = null,
    val checkIns: List<CheckIn> = emptyList(),
    val notifications: List<SafeNotification> = emptyList(),
    val activeBanner: SafeNotification? = null,
    val wizardOpen: Boolean = false,
    val wizardPresetActivity: ActivityType? = null,
    val checkInModalOpen: Boolean = false,
    val alertModalOpen: Boolean = false,
    val alertDraft: AlertDraftPayload? = null,
    val demoDrawerOpen: Boolean = false,
    val notificationsSheetOpen: Boolean = false,
    val detectionConfig: DetectionConfig = DetectionConfig(),
    val demo: DemoSimulationState = DemoSimulationState(),
    val presetDestinations: List<RouteDestination> = emptyList(),
) {
    val activeCheckIn: CheckIn?
        get() = checkIns.firstOrNull {
            it.status == CheckInStatus.DUE || it.status == CheckInStatus.SCHEDULED
        }

    val dueCheckIn: CheckIn?
        get() = checkIns.firstOrNull { it.status == CheckInStatus.DUE }

    val unreadNotificationsCount: Int
        get() = notifications.count { !it.read }
}

class SafeViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = StorageService(application)
    private val locationService = LocationService(application)
    private val sessionService = SessionService()
    private val checkInService = CheckInService()
    val alertService = AlertService()
    private val contactService = ContactService()
    private val notificationService = NotificationService(application)
    private val authService = AuthService()

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<SafeUiState> = _uiState.asStateFlow()

    private var arrivingSoonNotifiedSessionId: String? = null
    private var bannerClearDeadlineMs: Long = 0L

    init {
        startClockLoop()
    }

    private fun loadInitialState(): SafeUiState {
        val privacy = storage.load("privacy", PrivacySettings())
        val initialHistory = storage.load("history", SeedData.createDemoHistory())
        val retainedHistory = storage.applyHistoryRetention(initialHistory, privacy.retentionPolicy)
        return SafeUiState(
            onboardingDone = storage.load("onboardingDone", false),
            darkMode = storage.load("darkMode", false),
            language = storage.load("language", SupportedLanguage.FR),
            country = storage.load("country", CountryCode.FR),
            user = storage.load("user", SeedData.DEMO_USER_PROFILE),
            contacts = storage.load("contacts", SeedData.createDemoContacts()),
            privacy = privacy,
            history = retainedHistory,
            activeSession = storage.load<SafeSession?>("activeSession", null),
            checkIns = storage.load("checkIns", emptyList()),
            detectionConfig = storage.load("detectionConfig", DetectionConfig()),
            presetDestinations = locationService.getPresetDestinations(),
        )
    }

    private fun startClockLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                tick(System.currentTimeMillis())
            }
        }
    }

    private fun tick(now: Long) {
        val current = _uiState.value

        // Clear banner after 5.5s
        if (current.activeBanner != null && now >= bannerClearDeadlineMs) {
            _uiState.update { it.copy(activeBanner = null) }
        }

        // Update active session
        val prevSession = current.activeSession
        if (prevSession != null &&
            prevSession.state != SessionState.COMPLETED &&
            prevSession.state != SessionState.CANCELLED &&
            prevSession.state != SessionState.EXPIRED
        ) {
            val demo = current.demo
            val realElapsedMs = if (prevSession.startedAt != null && prevSession.state != SessionState.PAUSED) {
                max(0L, now - prevSession.startedAt - prevSession.totalPausedMs)
            } else {
                (prevSession.elapsedMinutes * 60_000.0).toLong()
            }
            val realElapsedMinutes = realElapsedMs / 60_000.0

            val elapsedMinutes = if (demo.enabled && demo.simulatedElapsedMinutes != null) {
                demo.simulatedElapsedMinutes
            } else {
                realElapsedMinutes
            }

            val autoProgress = if (prevSession.expectedDurationMinutes > 0) {
                min(98, ((elapsedMinutes / prevSession.expectedDurationMinutes) * 100.0).roundToInt())
            } else {
                0
            }
            val progressPercent = if (demo.enabled && demo.simulatedProgressPercent != null) {
                demo.simulatedProgressPercent
            } else {
                max(prevSession.progressPercent, autoProgress)
            }

            val stationaryMinutes = if (demo.enabled) demo.simulatedStationaryMinutes else prevSession.stationaryMinutes
            val deviationMeters = if (demo.enabled) demo.simulatedDeviationMeters else prevSession.deviationMeters

            val currentPosition = if (current.privacy.locationPermissionGranted) {
                locationService.interpolatePositionAlongRoute(
                    route = prevSession.destination,
                    progressPercent = progressPercent,
                    deviationMeters = deviationMeters,
                    inaccurateGps = demo.inaccurateGps,
                )
            } else {
                prevSession.currentPosition
            }

            val updated = prevSession.copy(
                elapsedMinutes = elapsedMinutes,
                progressPercent = progressPercent,
                stationaryMinutes = stationaryMinutes,
                deviationMeters = deviationMeters,
                currentPosition = currentPosition,
                lastSignalAt = if (demo.offline) prevSession.lastSignalAt else now,
            )

            val remaining = updated.expectedDurationMinutes - updated.elapsedMinutes
            if (updated.state == SessionState.ACTIVE &&
                remaining <= 10.0 &&
                remaining > 0.0 &&
                arrivingSoonNotifiedSessionId != updated.id
            ) {
                arrivingSoonNotifiedSessionId = updated.id
                pushNotification(
                    NotificationCategory.ARRIVING_SOON,
                    "Arrivée prévue bientôt",
                    "Votre arrivée est prévue dans environ 10 minutes.",
                )
            }

            val evaluation = DetectionEngine.evaluateSessionDetection(updated, current.detectionConfig)
            val nextSession = if (evaluation.anomalyType != null && updated.state == SessionState.ACTIVE) {
                val withAnomaly = sessionService.triggerAnomaly(
                    updated,
                    evaluation.anomalyType,
                    evaluation.reason.orEmpty(),
                    now,
                )
                if (evaluation.anomalyType == AnomalyType.DELAY_WARNING) {
                    pushNotification(
                        NotificationCategory.DELAY,
                        "Retard",
                        "Votre session dure plus longtemps que prévu.",
                    )
                }
                withAnomaly
            } else {
                updated
            }

            _uiState.update { it.copy(activeSession = nextSession) }
            storage.save("activeSession", nextSession)
        }

        // Update check-ins
        val prevCheckIns = _uiState.value.checkIns
        if (prevCheckIns.isNotEmpty()) {
            var changed = false
            var escalatedContactCheckIn: CheckIn? = null
            val nextCheckIns = prevCheckIns.map { chk ->
                when {
                    chk.status == CheckInStatus.SCHEDULED && now >= chk.dueAt -> {
                        changed = true
                        pushNotification(NotificationCategory.CHECKIN_DUE, "Check-in", "Tout va bien ?")
                        checkInService.markDue(chk, current.detectionConfig.checkInResponseGraceMinutes, now)
                    }
                    chk.status == CheckInStatus.DUE && chk.graceExpiresAt != null && now >= chk.graceExpiresAt -> {
                        changed = true
                        pushNotification(
                            NotificationCategory.CHECKIN_UNCONFIRMED,
                            "Check-in non confirmé",
                            "Votre confirmation est attendue.",
                        )
                        escalatedContactCheckIn = chk
                        checkInService.escalate(chk)
                    }
                    else -> chk
                }
            }
            if (changed) {
                _uiState.update { state ->
                    val draft = escalatedContactCheckIn?.let { chk ->
                        val contact = state.contacts.firstOrNull { it.id == chk.contactId }
                        alertService.buildContactMessageDraft(
                            contact = contact,
                            session = null,
                            customMessage = chk.message,
                            reason = AlertReason.CHECKIN_EXPIRED,
                        )
                    }
                    state.copy(
                        checkIns = nextCheckIns,
                        alertDraft = draft ?: state.alertDraft,
                        alertModalOpen = if (draft != null) true else state.alertModalOpen,
                    )
                }
                storage.save("checkIns", nextCheckIns)
            }
        }
    }

    fun t(key: String): String = SafeTranslations.t(_uiState.value.language, key)

    private fun pushNotification(category: NotificationCategory, title: String, body: String) {
        val notif = notificationService.createNotification(category, title, body)
        bannerClearDeadlineMs = System.currentTimeMillis() + 5500L
        _uiState.update { state ->
            state.copy(
                notifications = (listOf(notif) + state.notifications).take(30),
                activeBanner = notif,
            )
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(activeBanner = null) }
    }

    fun markAllNotificationsRead() {
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map { it.copy(read = true) })
        }
    }

    fun setNotificationsSheetOpen(open: Boolean) {
        if (open) markAllNotificationsRead()
        _uiState.update { it.copy(notificationsSheetOpen = open) }
    }

    fun setActiveTab(tab: TabId) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun completeOnboarding() {
        _uiState.update { it.copy(onboardingDone = true) }
        storage.save("onboardingDone", true)
    }

    fun replayOnboarding() {
        _uiState.update { it.copy(onboardingDone = false) }
        storage.save("onboardingDone", false)
    }

    fun toggleDarkMode() {
        val next = !_uiState.value.darkMode
        _uiState.update { it.copy(darkMode = next) }
        storage.save("darkMode", next)
    }

    fun setLanguage(lang: SupportedLanguage) {
        _uiState.update { it.copy(language = lang) }
        storage.save("language", lang)
    }

    fun setCountry(country: CountryCode) {
        _uiState.update { it.copy(country = country) }
        storage.save("country", country)
    }

    // Auth
    fun signInEmail(email: String, name: String? = null) {
        val nextUser = authService.signInWithEmail(email, name)
        _uiState.update { it.copy(user = nextUser) }
        storage.save("user", nextUser)
    }

    fun signInGoogle() {
        val nextUser = authService.signInWithGoogle()
        _uiState.update { it.copy(user = nextUser) }
        storage.save("user", nextUser)
    }

    fun appleSignInNotice(): String = authService.appleSignInUnavailableMessage()

    fun signOut() {
        val nextUser = authService.signOut()
        _uiState.update { it.copy(user = nextUser) }
        storage.save("user", nextUser)
    }

    // Contacts
    fun addContact(input: ContactInput): TrustedContact {
        val next = contactService.createContact(_uiState.value.contacts, input)
        _uiState.update { it.copy(contacts = next) }
        storage.save("contacts", next)
        return next.last()
    }

    fun updateContact(id: String, input: ContactInput) {
        val next = contactService.updateContact(_uiState.value.contacts, id, input)
        _uiState.update { it.copy(contacts = next) }
        storage.save("contacts", next)
    }

    fun deleteContact(id: String) {
        val next = contactService.deleteContact(_uiState.value.contacts, id)
        _uiState.update { it.copy(contacts = next) }
        storage.save("contacts", next)
    }

    fun setPrimaryContact(id: String) {
        val next = contactService.setPrimaryContact(_uiState.value.contacts, id)
        _uiState.update { it.copy(contacts = next) }
        storage.save("contacts", next)
    }

    // Wizard & Route
    fun openWizard(presetActivity: ActivityType? = null) {
        _uiState.update {
            it.copy(
                wizardOpen = true,
                wizardPresetActivity = presetActivity,
                presetDestinations = locationService.getPresetDestinations(),
            )
        }
    }

    fun closeWizard() {
        _uiState.update { it.copy(wizardOpen = false, wizardPresetActivity = null) }
    }

    fun resolveDestination(query: String): RouteDestination =
        locationService.resolveDestination(query)

    // Session
    fun startNewSession(input: CreateSessionInput) {
        val draft = sessionService.createSession(input)
        val started = sessionService.startSession(draft)
        arrivingSoonNotifiedSessionId = null
        _uiState.update { state ->
            state.copy(
                activeSession = started,
                lastCompletedSummary = null,
                wizardOpen = false,
                wizardPresetActivity = null,
                activeTab = TabId.SAFE,
                demo = state.demo.copy(
                    simulatedElapsedMinutes = null,
                    simulatedProgressPercent = null,
                    simulatedStationaryMinutes = 0.0,
                    simulatedDeviationMeters = 0.0,
                ),
            )
        }
        storage.save("activeSession", started)
        pushNotification(NotificationCategory.SESSION_STARTED, "Session commencée", "SAFE est maintenant actif.")
    }

    fun pauseOrResumeSession() {
        val prev = _uiState.value.activeSession ?: return
        val next = when (prev.state) {
            SessionState.PAUSED -> sessionService.resumeSession(prev)
            SessionState.ACTIVE, SessionState.WARNING -> sessionService.pauseSession(prev)
            else -> prev
        }
        _uiState.update { it.copy(activeSession = next) }
        storage.save("activeSession", next)
    }

    fun extendActiveSession(minutes: Int) {
        val prev = _uiState.value.activeSession ?: return
        val next = sessionService.extendSession(prev, minutes)
        _uiState.update { state ->
            state.copy(
                activeSession = next,
                demo = state.demo.copy(
                    simulatedStationaryMinutes = 0.0,
                    simulatedDeviationMeters = 0.0,
                ),
            )
        }
        storage.save("activeSession", next)
    }

    fun updateActiveDestination(query: String) {
        val prev = _uiState.value.activeSession ?: return
        val resolved = resolveDestination(query)
        val next = sessionService.updateDestination(prev, resolved)
        _uiState.update { it.copy(activeSession = next) }
        storage.save("activeSession", next)
    }

    fun confirmAnomalyOk() {
        val prev = _uiState.value.activeSession ?: return
        val next = sessionService.confirmUserOk(prev)
        _uiState.update { state ->
            state.copy(
                activeSession = next,
                demo = state.demo.copy(
                    simulatedStationaryMinutes = 0.0,
                    simulatedDeviationMeters = 0.0,
                ),
            )
        }
        storage.save("activeSession", next)
    }

    fun completeActiveSession() {
        val state = _uiState.value
        val prev = state.activeSession ?: return
        val completed = sessionService.completeSession(prev)
        val contact = state.contacts.firstOrNull { it.id == completed.contactId }
        val keepLoc = state.privacy.retentionPolicy != RetentionPolicy.NONE && state.privacy.locationPermissionGranted
        val item = sessionService.toHistoryItem(completed, contact, keepLoc)
        val nextHistory = storage.applyHistoryRetention(listOf(item) + state.history, state.privacy.retentionPolicy)

        _uiState.update {
            it.copy(
                activeSession = null,
                history = nextHistory,
                lastCompletedSummary = item,
                demo = it.demo.copy(
                    simulatedElapsedMinutes = null,
                    simulatedProgressPercent = null,
                    simulatedStationaryMinutes = 0.0,
                    simulatedDeviationMeters = 0.0,
                ),
            )
        }
        storage.save<SafeSession?>("activeSession", null)
        storage.save("history", nextHistory)
    }

    fun clearCompletedSummary() {
        _uiState.update { it.copy(lastCompletedSummary = null) }
    }

    fun cancelActiveSession() {
        val state = _uiState.value
        val prev = state.activeSession ?: return
        val cancelled = sessionService.cancelSession(prev)
        val contact = state.contacts.firstOrNull { it.id == cancelled.contactId }
        val item = sessionService.toHistoryItem(cancelled, contact, false)
        val nextHistory = storage.applyHistoryRetention(listOf(item) + state.history, state.privacy.retentionPolicy)
        _uiState.update {
            it.copy(
                activeSession = null,
                history = nextHistory,
                activeTab = TabId.HOME,
            )
        }
        storage.save<SafeSession?>("activeSession", null)
        storage.save("history", nextHistory)
    }

    // Check-ins
    fun openCheckInModal() {
        _uiState.update { it.copy(checkInModalOpen = true) }
    }

    fun closeCheckInModal() {
        _uiState.update { it.copy(checkInModalOpen = false) }
    }

    fun buildDefaultCheckInMessage(hhmm: String): String = checkInService.buildDefaultMessage(hhmm)

    fun scheduleCheckIn(dueAt: Long, contactId: String?, message: String) {
        val created = checkInService.createCheckIn(CreateCheckInInput(dueAt, contactId, message))
        val nextList = listOf(created) + _uiState.value.checkIns
        _uiState.update { it.copy(checkIns = nextList, checkInModalOpen = false) }
        storage.save("checkIns", nextList)
        val hhmm = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(dueAt))
        pushNotification(NotificationCategory.SYSTEM, "Check-in programmé", "Confirmation attendue à $hhmm.")
    }

    fun confirmCheckInOk(id: String) {
        val nextList = _uiState.value.checkIns.map { if (it.id == id) checkInService.confirmOk(it) else it }
        _uiState.update { it.copy(checkIns = nextList) }
        storage.save("checkIns", nextList)
    }

    fun escalateCheckIn(id: String) {
        val state = _uiState.value
        val target = state.checkIns.firstOrNull { it.id == id }
        val nextList = state.checkIns.map { if (it.id == id) checkInService.escalate(it) else it }
        val contact = state.contacts.firstOrNull { it.id == target?.contactId }
            ?: state.contacts.firstOrNull { it.isPrimary }
        val draft = alertService.buildContactMessageDraft(
            contact = contact,
            session = null,
            customMessage = target?.message,
            reason = AlertReason.CHECKIN_EXPIRED,
        )
        _uiState.update {
            it.copy(
                checkIns = nextList,
                alertDraft = draft,
                alertModalOpen = true,
            )
        }
        storage.save("checkIns", nextList)
    }

    // SOS / Alert Protocol
    fun openSosProtocol(reason: AlertReason = AlertReason.SOS, customMsg: String? = null) {
        val state = _uiState.value
        val contact = state.contacts.firstOrNull { it.id == state.activeSession?.contactId }
            ?: state.contacts.firstOrNull { it.isPrimary }
            ?: state.contacts.firstOrNull()

        val updatedSession = if (state.activeSession != null &&
            (reason == AlertReason.SOS || reason == AlertReason.SESSION_ANOMALY) &&
            state.activeSession.state != SessionState.ESCALATED
        ) {
            sessionService.escalateSession(
                state.activeSession,
                "Protocole d’alerte déclenché par l’utilisateur",
            )
        } else {
            state.activeSession
        }

        val draft = alertService.buildContactMessageDraft(
            contact = contact,
            session = updatedSession,
            customMessage = customMsg,
            reason = reason,
        )

        _uiState.update {
            it.copy(
                activeSession = updatedSession,
                alertDraft = draft,
                alertModalOpen = true,
            )
        }
        if (updatedSession != null) {
            storage.save("activeSession", updatedSession)
        }
    }

    fun closeAlertModal() {
        _uiState.update { it.copy(alertModalOpen = false) }
    }

    // Privacy
    fun setLocationPermission(granted: Boolean) {
        val next = _uiState.value.privacy.copy(locationPermissionGranted = granted)
        _uiState.update { it.copy(privacy = next) }
        storage.save("privacy", next)
    }

    fun setRetentionPolicy(policy: RetentionPolicy) {
        val state = _uiState.value
        val nextPrivacy = state.privacy.copy(retentionPolicy = policy)
        val nextHistory = storage.applyHistoryRetention(state.history, policy)
        _uiState.update { it.copy(privacy = nextPrivacy, history = nextHistory) }
        storage.save("privacy", nextPrivacy)
        storage.save("history", nextHistory)
    }

    fun deleteAllUserData() {
        storage.clearAll()
        val resetPrivacy = PrivacySettings(
            locationPermissionGranted = false,
            retentionPolicy = RetentionPolicy.NONE,
            shareOnlyOnConsent = true,
        )
        _uiState.update {
            it.copy(
                activeSession = null,
                lastCompletedSummary = null,
                checkIns = emptyList(),
                history = emptyList(),
                contacts = emptyList(),
                notifications = emptyList(),
                activeBanner = null,
                privacy = resetPrivacy,
            )
        }
        pushNotification(
            NotificationCategory.SYSTEM,
            "Données supprimées",
            "Toutes vos données locales ont été effacées.",
        )
    }

    // Demo & Detection Engine
    fun setDemoEnabled(enabled: Boolean) {
        _uiState.update { it.copy(demo = it.demo.copy(enabled = enabled)) }
    }

    fun updateDemo(transform: (DemoSimulationState) -> DemoSimulationState) {
        _uiState.update { it.copy(demo = transform(it.demo)) }
    }

    fun setDemoDrawerOpen(open: Boolean) {
        _uiState.update { it.copy(demoDrawerOpen = open) }
    }

    fun updateDetectionConfig(transform: (DetectionConfig) -> DetectionConfig) {
        val next = transform(_uiState.value.detectionConfig)
        _uiState.update { it.copy(detectionConfig = next) }
        storage.save("detectionConfig", next)
    }

    fun triggerDemoAnomaly(type: AnomalyType) {
        val state = _uiState.value
        val active = state.activeSession ?: return
        val cfg = state.detectionConfig
        when (type) {
            AnomalyType.DELAY_WARNING -> {
                val targetElapsed = max(35, active.expectedDurationMinutes + cfg.gracePeriodMinutes + 10).toDouble()
                val nextSession = sessionService.triggerAnomaly(
                    active.copy(elapsedMinutes = targetElapsed),
                    AnomalyType.DELAY_WARNING,
                    "Simulation manuelle : retard détecté",
                )
                _uiState.update {
                    it.copy(
                        demo = it.demo.copy(enabled = true, simulatedElapsedMinutes = targetElapsed),
                        activeSession = nextSession,
                    )
                }
                storage.save("activeSession", nextSession)
                pushNotification(NotificationCategory.DELAY, "Retard", "Votre session dure plus longtemps que prévu.")
            }
            AnomalyType.STATIONARY_WARNING -> {
                val targetStat = (cfg.stationaryThresholdMinutes + 2).toDouble()
                val nextSession = sessionService.triggerAnomaly(
                    active.copy(stationaryMinutes = targetStat),
                    AnomalyType.STATIONARY_WARNING,
                    "Simulation manuelle : arrêt prolongé",
                )
                _uiState.update {
                    it.copy(
                        demo = it.demo.copy(enabled = true, simulatedStationaryMinutes = targetStat),
                        activeSession = nextSession,
                    )
                }
                storage.save("activeSession", nextSession)
            }
            AnomalyType.ROUTE_DEVIATION_WARNING -> {
                val targetDev = (cfg.routeDeviationMeters + 150).toDouble()
                val nextSession = sessionService.triggerAnomaly(
                    active.copy(deviationMeters = targetDev),
                    AnomalyType.ROUTE_DEVIATION_WARNING,
                    "Simulation manuelle : changement d’itinéraire",
                )
                _uiState.update {
                    it.copy(
                        demo = it.demo.copy(enabled = true, simulatedDeviationMeters = targetDev),
                        activeSession = nextSession,
                    )
                }
                storage.save("activeSession", nextSession)
            }
            else -> Unit
        }
    }

    fun simulateArrival() {
        val active = _uiState.value.activeSession ?: return
        val nextSession = active.copy(
            progressPercent = 100,
            currentPosition = active.destination.destination,
            activeAnomaly = null,
            state = if (active.state == SessionState.WARNING) SessionState.ACTIVE else active.state,
        )
        _uiState.update {
            it.copy(
                demo = it.demo.copy(enabled = true, simulatedProgressPercent = 100),
                activeSession = nextSession,
            )
        }
        storage.save("activeSession", nextSession)
    }

    fun simulateCheckInDueNow() {
        val state = _uiState.value
        val dueChk = state.checkIns.firstOrNull { it.status == CheckInStatus.SCHEDULED } ?: return
        val nextList = state.checkIns.map {
            if (it.id == dueChk.id) checkInService.markDue(it, state.detectionConfig.checkInResponseGraceMinutes) else it
        }
        _uiState.update { it.copy(checkIns = nextList) }
        storage.save("checkIns", nextList)
        pushNotification(NotificationCategory.CHECKIN_DUE, "Check-in", "Tout va bien ?")
    }

    fun simulateCheckInExpiredNow() {
        val state = _uiState.value
        val target = state.checkIns.firstOrNull {
            it.status == CheckInStatus.SCHEDULED || it.status == CheckInStatus.DUE
        } ?: return
        escalateCheckIn(target.id)
        pushNotification(
            NotificationCategory.CHECKIN_UNCONFIRMED,
            "Check-in non confirmé",
            "Votre confirmation est attendue.",
        )
    }
}
