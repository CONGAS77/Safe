import { CheckIn } from '../types/safe';

export interface CreateCheckInInput {
  dueAt: number;
  contactId: string | null;
  message: string;
}

export interface CheckInService {
  createCheckIn(input: CreateCheckInInput, now?: number): CheckIn;
  markDue(checkIn: CheckIn, graceMinutes: number, now?: number): CheckIn;
  confirmOk(checkIn: CheckIn, now?: number): CheckIn;
  escalate(checkIn: CheckIn): CheckIn;
  cancel(checkIn: CheckIn): CheckIn;
  buildDefaultMessage(dueTimeFormatted: string): string;
}

export class DefaultCheckInService implements CheckInService {
  buildDefaultMessage(dueTimeFormatted: string): string {
    return `SAFE : cette personne devait confirmer que tout allait bien avant ${dueTimeFormatted} et n'a pas encore confirmé.`;
  }

  createCheckIn(input: CreateCheckInInput, now = Date.now()): CheckIn {
    return {
      id: `chk-${now}-${Math.random().toString(36).slice(2, 6)}`,
      dueAt: input.dueAt,
      contactId: input.contactId,
      message: input.message.trim(),
      status: 'SCHEDULED',
      createdAt: now,
    };
  }

  markDue(checkIn: CheckIn, graceMinutes: number, now = Date.now()): CheckIn {
    if (checkIn.status !== 'SCHEDULED') return checkIn;
    return {
      ...checkIn,
      status: 'DUE',
      dueTriggeredAt: now,
      graceExpiresAt: now + graceMinutes * 60_000,
    };
  }

  confirmOk(checkIn: CheckIn, now = Date.now()): CheckIn {
    return {
      ...checkIn,
      status: 'CONFIRMED',
      confirmedAt: now,
    };
  }

  escalate(checkIn: CheckIn): CheckIn {
    return {
      ...checkIn,
      status: 'ESCALATED',
    };
  }

  cancel(checkIn: CheckIn): CheckIn {
    return {
      ...checkIn,
      status: 'CANCELLED',
    };
  }
}
