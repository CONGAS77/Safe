import { AnomalyType, DetectionConfig, SafeSession } from '../types/safe';

export interface DetectionEvaluation {
  anomalyType: AnomalyType | null;
  reason: string | null;
  gpsImpreciseIgnored: boolean;
}

/**
 * Evaluates configurable safety detection rules on an active session.
 * Never treats inaccurate GPS as an immediate route deviation emergency.
 */
export function evaluateSessionDetection(
  session: SafeSession,
  config: DetectionConfig
): DetectionEvaluation {
  if (session.state !== 'ACTIVE') {
    return { anomalyType: null, reason: null, gpsImpreciseIgnored: false };
  }

  // Rule 1: Delay beyond expected_duration + grace_period
  if (
    session.detectionEnabled.time &&
    session.elapsedMinutes > session.expectedDurationMinutes + config.gracePeriodMinutes
  ) {
    return {
      anomalyType: 'DELAY_WARNING',
      reason: `elapsed (${Math.round(session.elapsedMinutes)}m) > expected (${session.expectedDurationMinutes}m) + grace (${config.gracePeriodMinutes}m)`,
      gpsImpreciseIgnored: false,
    };
  }

  // Rule 2: Prolonged stationary duration
  if (
    session.detectionEnabled.stationary &&
    session.stationaryMinutes >= config.stationaryThresholdMinutes
  ) {
    return {
      anomalyType: 'STATIONARY_WARNING',
      reason: `stationary (${Math.round(session.stationaryMinutes)}m) >= threshold (${config.stationaryThresholdMinutes}m)`,
      gpsImpreciseIgnored: false,
    };
  }

  // Rule 3: Route deviation, guarded by GPS accuracy threshold
  const gpsAccuracy = session.currentPosition?.accuracyMeters ?? 15;
  if (session.detectionEnabled.routeDeviation && session.deviationMeters > config.routeDeviationMeters) {
    if (gpsAccuracy > config.gpsAccuracyMaxMeters) {
      return {
        anomalyType: null,
        reason: null,
        gpsImpreciseIgnored: true,
      };
    }
    return {
      anomalyType: 'ROUTE_DEVIATION_WARNING',
      reason: `deviation (${Math.round(session.deviationMeters)}m) > threshold (${config.routeDeviationMeters}m)`,
      gpsImpreciseIgnored: false,
    };
  }

  return { anomalyType: null, reason: null, gpsImpreciseIgnored: false };
}
