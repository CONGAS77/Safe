package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SessionState {
    DRAFT,
    READY,
    ACTIVE,
    PAUSED,
    WARNING,
    USER_CONFIRMED,
    ESCALATED,
    COMPLETED,
    CANCELLED,
    EXPIRED,
}

@Serializable
enum class ActivityType(val emoji: String, val translationKey: String) {
    WALK_HOME("🚶", "activity.walk_home"),
    WALK("🚶", "activity.walk"),
    CAR("🚗", "activity.car"),
    BIKE("🚲", "activity.bike"),
    HIKE("🥾", "activity.hike"),
    ACTIVITY("🏊", "activity.activity"),
    TRAVEL("✈️", "activity.travel"),
    OTHER("✏️", "activity.other"),
}

@Serializable
enum class AnomalyType {
    DELAY_WARNING,
    STATIONARY_WARNING,
    ROUTE_DEVIATION_WARNING,
    CHECKIN_OVERDUE,
    MANUAL_SOS,
}

@Serializable
enum class AnomalyResolution {
    CONFIRMED_OK,
    EXTENDED,
    ESCALATED,
    CANCELLED,
    EXPIRED,
}

@Serializable
enum class SupportedLanguage(val code: String, val label: String) {
    FR("fr", "Français"),
    EN("en", "English"),
    ES("es", "Español"),
    DE("de", "Deutsch"),
    IT("it", "Italiano"),
}

@Serializable
enum class RetentionPolicy(val value: String) {
    NONE("none"),
    HOURS_24("24h"),
    DAYS_7("7d"),
    DAYS_30("30d"),
}

@Serializable
enum class CountryCode(val code: String) {
    FR("FR"),
    BE("BE"),
    CH("CH"),
    CA("CA"),
    GB("GB"),
    US("US"),
    EU("EU"),
}

enum class TabId {
    HOME,
    CONTACTS,
    SAFE,
    ACTIVITY,
    SETTINGS,
}

@Serializable
enum class RouteProvider {
    MOCK,
    GOOGLE_MAPS,
}

@Serializable
data class GeoPoint(
    val lat: Double,
    val lng: Double,
    val accuracyMeters: Double = 12.0,
    val timestamp: Long = System.currentTimeMillis(),
)

@Serializable
data class RouteDestination(
    val id: String,
    val label: String,
    val address: String,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val origin: GeoPoint,
    val destination: GeoPoint,
    val routePoints: List<GeoPoint>,
    val provider: RouteProvider = RouteProvider.MOCK,
)

@Serializable
data class TrustedContact(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String? = null,
    val isPrimary: Boolean = false,
    val acceptedByContact: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
data class DetectionConfig(
    val gracePeriodMinutes: Int = 5,
    val stationaryThresholdMinutes: Int = 10,
    val routeDeviationMeters: Int = 250,
    val gpsAccuracyMaxMeters: Int = 120,
    val checkInResponseGraceMinutes: Int = 3,
)

@Serializable
data class AnomalyRecord(
    val id: String,
    val type: AnomalyType,
    val triggeredAt: Long,
    val resolvedAt: Long? = null,
    val resolution: AnomalyResolution? = null,
    val details: String,
)

@Serializable
data class DetectionEnabledFlags(
    val position: Boolean = true,
    val progress: Boolean = true,
    val time: Boolean = true,
    val stationary: Boolean = true,
    val routeDeviation: Boolean = true,
)

@Serializable
data class SafeSession(
    val id: String,
    val state: SessionState,
    val activityType: ActivityType,
    val title: String,
    val originLabel: String,
    val destination: RouteDestination,
    val expectedDurationMinutes: Int,
    val startedAt: Long? = null,
    val pausedAt: Long? = null,
    val totalPausedMs: Long = 0L,
    val elapsedMinutes: Double = 0.0,
    val estimatedArrivalAt: Long? = null,
    val contactId: String? = null,
    val progressPercent: Int = 0,
    val currentPosition: GeoPoint? = null,
    val lastSignalAt: Long? = null,
    val stationaryMinutes: Double = 0.0,
    val deviationMeters: Double = 0.0,
    val activeAnomaly: AnomalyRecord? = null,
    val anomalies: List<AnomalyRecord> = emptyList(),
    val endedAt: Long? = null,
    val detectionEnabled: DetectionEnabledFlags = DetectionEnabledFlags(),
)

@Serializable
data class SessionHistoryItem(
    val id: String,
    val activityType: ActivityType,
    val title: String,
    val destinationLabel: String,
    val startedAt: Long,
    val endedAt: Long,
    val durationMinutes: Int,
    val distanceKm: Double,
    val endState: SessionState,
    val anomaliesCount: Int,
    val anomalies: List<AnomalyRecord> = emptyList(),
    val contactName: String? = null,
    val lastKnownPoint: GeoPoint? = null,
)

@Serializable
enum class CheckInStatus {
    SCHEDULED,
    DUE,
    CONFIRMED,
    ESCALATED,
    CANCELLED,
}

@Serializable
data class CheckIn(
    val id: String,
    val dueAt: Long,
    val contactId: String? = null,
    val message: String,
    val status: CheckInStatus,
    val createdAt: Long,
    val dueTriggeredAt: Long? = null,
    val graceExpiresAt: Long? = null,
    val confirmedAt: Long? = null,
)

enum class NotificationCategory {
    SESSION_STARTED,
    ARRIVING_SOON,
    DELAY,
    CHECKIN_DUE,
    CHECKIN_UNCONFIRMED,
    SYSTEM,
}

data class SafeNotification(
    val id: String,
    val category: NotificationCategory,
    val title: String,
    val body: String,
    val createdAt: Long,
    val read: Boolean = false,
)

@Serializable
data class PrivacySettings(
    val locationPermissionGranted: Boolean = true,
    val retentionPolicy: RetentionPolicy = RetentionPolicy.DAYS_30,
    val shareOnlyOnConsent: Boolean = true,
)

data class DemoSimulationState(
    val enabled: Boolean = true,
    val simulatedElapsedMinutes: Double? = null,
    val simulatedProgressPercent: Int? = null,
    val simulatedStationaryMinutes: Double = 0.0,
    val simulatedDeviationMeters: Double = 0.0,
    val lowBattery: Boolean = false,
    val offline: Boolean = false,
    val inaccurateGps: Boolean = false,
)

@Serializable
enum class AuthProvider {
    DEMO,
    EMAIL,
    GOOGLE,
    APPLE_STUB,
}

@Serializable
data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String,
    val provider: AuthProvider,
    val signedIn: Boolean,
)

data class EmergencyConfig(
    val country: CountryCode,
    val countryName: String,
    val primaryNumber: String,
    val policeNumber: String,
    val medicalNumber: String,
    val fireNumber: String,
    val disclaimerKey: String,
)

enum class AlertReason {
    SESSION_ANOMALY,
    CHECKIN_EXPIRED,
    SOS,
}

data class AlertDraftPayload(
    val contact: TrustedContact?,
    val sessionTitle: String,
    val destinationLabel: String,
    val lastSignalTimeLabel: String,
    val lastPosition: GeoPoint?,
    val messageBody: String,
    val reason: AlertReason,
)
