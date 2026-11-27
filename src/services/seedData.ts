import { SessionHistoryItem, TrustedContact, UserProfile } from '../types/safe';

export const DEMO_USER_PROFILE: UserProfile = {
  id: 'usr-demo',
  displayName: 'Camille',
  email: 'camille@safe-demo.app',
  provider: 'demo',
  signedIn: true,
};

export const DEMO_CONTACTS: TrustedContact[] = [
  {
    id: 'cnt-thomas',
    firstName: 'Thomas',
    lastName: 'Martin',
    phone: '+33 6 12 34 56 78',
    email: 'thomas.martin@example.com',
    isPrimary: true,
    acceptedByContact: true,
    createdAt: Date.now() - 10 * 24 * 60 * 60_000,
  },
  {
    id: 'cnt-sarah',
    firstName: 'Sarah',
    lastName: 'Benali',
    phone: '+33 6 98 76 54 32',
    email: 'sarah.benali@example.com',
    isPrimary: false,
    acceptedByContact: true,
    createdAt: Date.now() - 5 * 24 * 60 * 60_000,
  },
];

export function createDemoHistory(now = Date.now()): SessionHistoryItem[] {
  const hour = 60 * 60_000;
  const day = 24 * hour;
  return [
    {
      id: 'hist-1',
      activityType: 'walk_home',
      title: 'Retour à pied',
      destinationLabel: 'Maison',
      startedAt: now - 3 * hour,
      endedAt: now - 3 * hour + 23 * 60_000,
      durationMinutes: 23,
      distanceKm: 2.4,
      endState: 'COMPLETED',
      anomaliesCount: 0,
      anomalies: [],
      contactName: 'Thomas Martin',
    },
    {
      id: 'hist-2',
      activityType: 'car',
      title: 'Trajet',
      destinationLabel: 'Lyon Part-Dieu',
      startedAt: now - 1 * day - 2 * hour,
      endedAt: now - 1 * day - 48 * 60_000,
      durationMinutes: 72,
      distanceKm: 64.5,
      endState: 'COMPLETED',
      anomaliesCount: 0,
      anomalies: [],
      contactName: 'Sarah Benali',
    },
    {
      id: 'hist-3',
      activityType: 'hike',
      title: 'Randonnée',
      destinationLabel: 'Sentier des Crêtes',
      startedAt: now - 6 * day,
      endedAt: now - 6 * day + 221 * 60_000,
      durationMinutes: 221,
      distanceKm: 12.8,
      endState: 'COMPLETED',
      anomaliesCount: 0,
      anomalies: [],
      contactName: 'Thomas Martin',
    },
  ];
}
