import { TrustedContact } from '../types/safe';

export interface ContactInput {
  firstName: string;
  lastName: string;
  phone: string;
  email?: string;
  acceptedByContact: boolean;
  isPrimary?: boolean;
}

export interface ContactService {
  createContact(existing: TrustedContact[], input: ContactInput): TrustedContact[];
  updateContact(existing: TrustedContact[], id: string, input: ContactInput): TrustedContact[];
  deleteContact(existing: TrustedContact[], id: string): TrustedContact[];
  setPrimaryContact(existing: TrustedContact[], id: string): TrustedContact[];
}

export class DefaultContactService implements ContactService {
  createContact(existing: TrustedContact[], input: ContactInput): TrustedContact[] {
    const shouldBePrimary = Boolean(input.isPrimary) || existing.length === 0;
    const base = shouldBePrimary ? existing.map((c) => ({ ...c, isPrimary: false })) : existing;
    const next: TrustedContact = {
      id: `cnt-${Date.now()}-${Math.random().toString(36).slice(2, 5)}`,
      firstName: input.firstName.trim(),
      lastName: input.lastName.trim(),
      phone: input.phone.trim(),
      email: input.email?.trim() || undefined,
      isPrimary: shouldBePrimary,
      acceptedByContact: input.acceptedByContact,
      createdAt: Date.now(),
    };
    return [...base, next];
  }

  updateContact(existing: TrustedContact[], id: string, input: ContactInput): TrustedContact[] {
    const makePrimary = Boolean(input.isPrimary);
    return existing.map((c) => {
      if (c.id !== id) {
        return makePrimary ? { ...c, isPrimary: false } : c;
      }
      return {
        ...c,
        firstName: input.firstName.trim(),
        lastName: input.lastName.trim(),
        phone: input.phone.trim(),
        email: input.email?.trim() || undefined,
        acceptedByContact: input.acceptedByContact,
        isPrimary: makePrimary ? true : c.isPrimary,
      };
    });
  }

  deleteContact(existing: TrustedContact[], id: string): TrustedContact[] {
    const remaining = existing.filter((c) => c.id !== id);
    if (remaining.length > 0 && !remaining.some((c) => c.isPrimary)) {
      remaining[0] = { ...remaining[0], isPrimary: true };
    }
    return remaining;
  }

  setPrimaryContact(existing: TrustedContact[], id: string): TrustedContact[] {
    return existing.map((c) => ({
      ...c,
      isPrimary: c.id === id,
    }));
  }
}
