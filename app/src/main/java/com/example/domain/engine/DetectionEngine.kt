package com.example.domain.engine

import com.example.domain.model.AnomalyType
import com.example.domain.model.DetectionConfig
import com.example.domain.model.SafeSession
import kotlin.math.roundToInt

data class DetectionEvaluation(
    val anomalyType: AnomalyType? = null,
    val reason: String? = null,
    val gpsImpreciseIgnored: Boolean = false,
)

object DetectionEngine {
    /**
     * Evaluates configurable safety detection rules on an active session.
     * Never treats inaccurate GPS as an immediate route deviation emergency.
     */
    fun evaluateSessionDetection(
        session: SafeSession,
        config: DetectionConfig,
    ): DetectionEvaluation {
        if (session.state != com.example.domain.model.SessionState.ACTIVE) {
            return DetectionEvaluation()
        }

        // Rule 1: Delay beyond expected_duration + grace_period
        if (
            session.detectionEnabled.time &&
            session.elapsedMinutes > session.expectedDurationMinutes + config.gracePeriodMinutes
        ) {
            return DetectionEvaluation(
                anomalyType = AnomalyType.DELAY_WARNING,
                reason = "elapsed (${session.elapsedMinutes.roundToInt()}m) > expected (${session.expectedDurationMinutes}m) + grace (${config.gracePeriodMinutes}m)",
            )
        }

        // Rule 2: Prolonged stationary duration
        if (
            session.detectionEnabled.stationary &&
            session.stationaryMinutes >= config.stationaryThresholdMinutes
        ) {
            return DetectionEvaluation(
                anomalyType = AnomalyType.STATIONARY_WARNING,
                reason = "stationary (${session.stationaryMinutes.roundToInt()}m) >= threshold (${config.stationaryThresholdMinutes}m)",
            )
        }

        // Rule 3: Route deviation, guarded by GPS accuracy threshold
        val gpsAccuracy = session.currentPosition?.accuracyMeters ?: 15.0
        if (session.detectionEnabled.routeDeviation && session.deviationMeters > config.routeDeviationMeters) {
            if (gpsAccuracy > config.gpsAccuracyMaxMeters) {
                return DetectionEvaluation(
                    anomalyType = null,
                    reason = null,
                    gpsImpreciseIgnored = true,
                )
            }
            return DetectionEvaluation(
                anomalyType = AnomalyType.ROUTE_DEVIATION_WARNING,
                reason = "deviation (${session.deviationMeters.roundToInt()}m) > threshold (${config.routeDeviationMeters}m)",
            )
        }

        return DetectionEvaluation()
    }
}
