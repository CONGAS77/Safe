import { SafeNotification } from '../types/safe';

export interface NotificationService {
  createNotification(
    category: SafeNotification['category'],
    title: string,
    body: string
  ): SafeNotification;
  trySendNativeNotification(title: string, body: string): void;
}

export class DefaultNotificationService implements NotificationService {
  createNotification(
    category: SafeNotification['category'],
    title: string,
    body: string
  ): SafeNotification {
    this.trySendNativeNotification(title, body);
    return {
      id: `notif-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
      category,
      title,
      body,
      createdAt: Date.now(),
      read: false,
    };
  }

  trySendNativeNotification(title: string, body: string): void {
    if (typeof window === 'undefined' || !('Notification' in window)) return;
    if (Notification.permission === 'granted') {
      try {
        new Notification(title, { body });
      } catch {
        // Ignore browser notification errors in restricted contexts
      }
    }
  }
}
