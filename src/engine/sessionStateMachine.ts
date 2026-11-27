import { SessionState } from '../types/safe';

const ALLOWED_TRANSITIONS: Record<SessionState, SessionState[]> = {
  DRAFT: ['READY', 'CANCELLED'],
  READY: ['ACTIVE', 'DRAFT', 'CANCELLED'],
  ACTIVE: ['PAUSED', 'WARNING', 'ESCALATED', 'COMPLETED', 'CANCELLED', 'EXPIRED'],
  PAUSED: ['ACTIVE', 'WARNING', 'ESCALATED', 'COMPLETED', 'CANCELLED', 'EXPIRED'],
  WARNING: ['USER_CONFIRMED', 'ACTIVE', 'ESCALATED', 'COMPLETED', 'CANCELLED', 'EXPIRED'],
  USER_CONFIRMED: ['ACTIVE', 'COMPLETED'],
  ESCALATED: ['ACTIVE', 'USER_CONFIRMED', 'COMPLETED', 'CANCELLED', 'EXPIRED'],
  COMPLETED: [],
  CANCELLED: [],
  EXPIRED: [],
};

export function canTransitionSessionState(from: SessionState, to: SessionState): boolean {
  if (from === to) return true;
  return ALLOWED_TRANSITIONS[from]?.includes(to) ?? false;
}

export function transitionSessionState(from: SessionState, to: SessionState): SessionState {
  if (!canTransitionSessionState(from, to)) {
    throw new Error(`Transition d'état de session non autorisée : ${from} -> ${to}`);
  }
  return to;
}

export function isSessionRunning(state: SessionState): boolean {
  return (
    state === 'ACTIVE' ||
    state === 'PAUSED' ||
    state === 'WARNING' ||
    state === 'USER_CONFIRMED' ||
    state === 'ESCALATED'
  );
}

export function isSessionTerminal(state: SessionState): boolean {
  return state === 'COMPLETED' || state === 'CANCELLED' || state === 'EXPIRED';
}
