import { RetentionPolicy, SessionHistoryItem } from '../types/safe';

export interface StorageService {
  save<T>(key: string, value: T): void;
  load<T>(key: string, fallback: T): T;
  clearAll(): void;
  applyHistoryRetention(items: SessionHistoryItem[], policy: RetentionPolicy, now?: number): SessionHistoryItem[];
}

const STORAGE_PREFIX = 'safe_mvp_v1:';

function obfuscate(raw: string): string {
  // Lightweight client-side encoding to avoid plain text snooping in localStorage;
  // Production uses WebCrypto / OS Keystore / encrypted backend storage.
  try {
    return btoa(unescape(encodeURIComponent(raw)));
  } catch {
    return raw;
  }
}

function deobfuscate(encoded: string): string {
  try {
    return decodeURIComponent(escape(atob(encoded)));
  } catch {
    return encoded;
  }
}

export class LocalEncryptedStorageService implements StorageService {
  save<T>(key: string, value: T): void {
    if (typeof localStorage === 'undefined') return;
    try {
      const payload = JSON.stringify(value);
      localStorage.setItem(STORAGE_PREFIX + key, obfuscate(payload));
    } catch {
      // Ignore quota errors in restricted environments
    }
  }

  load<T>(key: string, fallback: T): T {
    if (typeof localStorage === 'undefined') return fallback;
    try {
      const raw = localStorage.getItem(STORAGE_PREFIX + key);
      if (!raw) return fallback;
      return JSON.parse(deobfuscate(raw)) as T;
    } catch {
      return fallback;
    }
  }

  clearAll(): void {
    if (typeof localStorage === 'undefined') return;
    const keysToRemove: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const k = localStorage.key(i);
      if (k && k.startsWith(STORAGE_PREFIX)) {
        keysToRemove.push(k);
      }
    }
    keysToRemove.forEach((k) => localStorage.removeItem(k));
  }

  applyHistoryRetention(
    items: SessionHistoryItem[],
    policy: RetentionPolicy,
    now = Date.now()
  ): SessionHistoryItem[] {
    if (policy === 'none') return [];
    const maxAgeMs =
      policy === '24h'
        ? 24 * 60 * 60_000
        : policy === '7d'
        ? 7 * 24 * 60 * 60_000
        : 30 * 24 * 60 * 60_000;

    return items.filter((item) => now - item.endedAt <= maxAgeMs);
  }
}
