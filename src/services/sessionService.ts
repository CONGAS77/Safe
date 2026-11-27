import { transitionSessionState } from '../engine/sessionStateMachine';
import {
  ActivityType,
  AnomalyRecord,
  AnomalyType,
  RouteDestination,
  SafeSession,
  SessionHistoryItem,
  TrustedContact,
} from '../types/safe';

export interface CreateSessionInput {
  activityType: ActivityType;
  title: string;
  originLabel: string;
  destination: RouteDestination;
  expectedDurationMinutes: number;
  contactId: string | null;
}

export interface SessionService {
  createSession(input: CreateSessionInput): SafeSession;
  startSession(session: SafeSession, now?: number): SafeSession;
  pauseSession(session: SafeSession, now?: number): SafeSession;
  resumeSession(session: SafeSession, now?: number): SafeSession;
  extendSession(session: SafeSession, extraMinutes: number): SafeSession;
  updateDestination(session: SafeSession, destination: RouteDestination): SafeSession;
  triggerAnomaly(session: SafeSession, type: AnomalyType, details: string, now?: number): SafeSession;
  confirmUserOk(session: SafeSession, now?: number): SafeSession;
  escalateSession(session: SafeSession, details: string, now?: number): SafeSession;
  completeSession(session: SafeSession, now?: number): SafeSession;
  cancelSession(session: SafeSession, now?: number): SafeSession;
  toHistoryItem(
    session: SafeSession,
    contact: TrustedContact | null,
    keepLocation: boolean
  ): SessionHistoryItem;
}

export class DefaultSessionService implements SessionService {
  createSession(input: CreateSessionInput): SafeSession {
    return {
      id: `sess-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
      state: 'READY',
      activityType: input.activityType,
      title: input.title,
      originLabel: input.originLabel,
      destination: input.destination,
      expectedDurationMinutes: input.expectedDurationMinutes,
      startedAt: null,
      pausedAt: null,
      totalPausedMs: 0,
      elapsedMinutes: 0,
      estimatedArrivalAt: null,
      contactId: input.contactId,
      progressPercent: 0,
      currentPosition: input.destination.origin,
      lastSignalAt: null,
      stationaryMinutes: 0,
      deviationMeters: 0,
      activeAnomaly: null,
      anomalies: [],
      endedAt: null,
      detectionEnabled: {
        position: true,
        progress: true,
        time: true,
        stationary: true,
        routeDeviation: true,
      },
    };
  }

  startSession(session: SafeSession, now = Date.now()): SafeSession {
    const nextState = transitionSessionState(session.state, 'ACTIVE');
    return {
      ...session,
      state: nextState,
      startedAt: now,
      lastSignalAt: now,
      estimatedArrivalAt: now + session.expectedDurationMinutes * 60_000,
    };
  }

  pauseSession(session: SafeSession, now = Date.now()): SafeSession {
    const nextState = transitionSessionState(session.state, 'PAUSED');
    return {
      ...session,
      state: nextState,
      pausedAt: now,
    };
  }

  resumeSession(session: SafeSession, now = Date.now()): SafeSession {
    const nextState = transitionSessionState(session.state, 'ACTIVE');
    const pausedDuration = session.pausedAt ? Math.max(0, now - session.pausedAt) : 0;
    return {
      ...session,
      state: nextState,
      pausedAt: null,
      totalPausedMs: session.totalPausedMs + pausedDuration,
      estimatedArrivalAt: session.estimatedArrivalAt
        ? session.estimatedArrivalAt + pausedDuration
        : now + Math.max(1, session.expectedDurationMinutes - session.elapsedMinutes) * 60_000,
      lastSignalAt: now,
    };
  }

  extendSession(session: SafeSession, extraMinutes: number): SafeSession {
    const baseTarget = Math.max(session.expectedDurationMinutes, Math.ceil(session.elapsedMinutes));
    const newExpected = baseTarget + extraMinutes;
    const baseArrival = session.startedAt ?? Date.now();
    const nextState =
      session.state === 'WARNING' || session.state === 'ESCALATED'
        ? transitionSessionState(session.state, 'ACTIVE')
        : session.state;

    const resolvedAnomaly = session.activeAnomaly
      ? {
          ...session.activeAnomaly,
          resolvedAt: Date.now(),
          resolution: 'extended' as const,
        }
      : null;

    return {
      ...session,
      state: nextState,
      expectedDurationMinutes: newExpected,
      estimatedArrivalAt: baseArrival + newExpected * 60_000 + session.totalPausedMs,
      activeAnomaly: null,
      anomalies: resolvedAnomaly
        ? session.anomalies.map((a) => (a.id === resolvedAnomaly.id ? resolvedAnomaly : a))
        : session.anomalies,
    };
  }

  updateDestination(session: SafeSession, destination: RouteDestination): SafeSession {
    return {
      ...session,
      destination,
      deviationMeters: 0,
    };
  }

  triggerAnomaly(session: SafeSession, type: AnomalyType, details: string, now = Date.now()): SafeSession {
    if (session.activeAnomaly && session.activeAnomaly.type === type) {
      return session;
    }
    const nextState = transitionSessionState(session.state, 'WARNING');
    const anomaly: AnomalyRecord = {
      id: `anom-${now}-${Math.random().toString(36).slice(2, 6)}`,
      type,
      triggeredAt: now,
      details,
    };
    return {
      ...session,
      state: nextState,
      activeAnomaly: anomaly,
      anomalies: [...session.anomalies, anomaly],
    };
  }

  confirmUserOk(session: SafeSession, now = Date.now()): SafeSession {
    const confirmedState = transitionSessionState(session.state, 'USER_CONFIRMED');
    const activeState = transitionSessionState(confirmedState, 'ACTIVE');
    const resolvedAnomaly = session.activeAnomaly
      ? {
          ...session.activeAnomaly,
          resolvedAt: now,
          resolution: 'confirmed_ok' as const,
        }
      : null;

    // Reset stationary / deviation / delay pressure cleanly after explicit user confirmation
    const adjustedExpected =
      session.activeAnomaly?.type === 'DELAY_WARNING'
        ? Math.max(session.expectedDurationMinutes, Math.ceil(session.elapsedMinutes) + 10)
        : session.expectedDurationMinutes;

    return {
      ...session,
      state: activeState,
      expectedDurationMinutes: adjustedExpected,
      estimatedArrivalAt: session.startedAt
        ? session.startedAt + adjustedExpected * 60_000 + session.totalPausedMs
        : session.estimatedArrivalAt,
      stationaryMinutes: 0,
      deviationMeters: 0,
      activeAnomaly: null,
      lastSignalAt: now,
      anomalies: resolvedAnomaly
        ? session.anomalies.map((a) => (a.id === resolvedAnomaly.id ? resolvedAnomaly : a))
        : session.anomalies,
    };
  }

  escalateSession(session: SafeSession, details: string, now = Date.now()): SafeSession {
    const nextState = transitionSessionState(session.state, 'ESCALATED');
    const anomaly: AnomalyRecord = session.activeAnomaly
      ? { ...session.activeAnomaly, resolvedAt: now, resolution: 'escalated' }
      : {
          id: `anom-${now}-sos`,
          type: 'MANUAL_SOS',
          triggeredAt: now,
          resolvedAt: now,
          resolution: 'escalated',
          details,
        };

    const hasExisting = session.activeAnomaly !== null;
    return {
      ...session,
      state: nextState,
      activeAnomaly: anomaly,
      anomalies: hasExisting
        ? session.anomalies.map((a) => (a.id === anomaly.id ? anomaly : a))
        : [...session.anomalies, anomaly],
    };
  }

  completeSession(session: SafeSession, now = Date.now()): SafeSession {
    const nextState = transitionSessionState(session.state, 'COMPLETED');
    return {
      ...session,
      state: nextState,
      progressPercent: 100,
      currentPosition: session.destination.destination,
      activeAnomaly: null,
      endedAt: now,
    };
  }

  cancelSession(session: SafeSession, now = Date.now()): SafeSession {
    const nextState = transitionSessionState(session.state, 'CANCELLED');
    return {
      ...session,
      state: nextState,
      activeAnomaly: null,
      endedAt: now,
    };
  }

  toHistoryItem(
    session: SafeSession,
    contact: TrustedContact | null,
    keepLocation: boolean
  ): SessionHistoryItem {
    const endedAt = session.endedAt ?? Date.now();
    const startedAt = session.startedAt ?? endedAt;
    const durationMinutes = Math.max(1, Math.round(session.elapsedMinutes));
    const endState =
      session.state === 'CANCELLED' || session.state === 'ESCALATED' || session.state === 'EXPIRED'
        ? session.state
        : 'COMPLETED';

    return {
      id: session.id,
      activityType: session.activityType,
      title: session.title,
      destinationLabel: session.destination.label,
      startedAt,
      endedAt,
      durationMinutes,
      distanceKm: session.destination.distanceKm,
      endState,
      anomaliesCount: session.anomalies.length,
      anomalies: session.anomalies,
      contactName: contact ? `${contact.firstName} ${contact.lastName}`.trim() : undefined,
      lastKnownPoint: keepLocation && session.currentPosition ? session.currentPosition : undefined,
    };
  }
}
