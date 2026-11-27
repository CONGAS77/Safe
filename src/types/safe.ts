export type SessionState =
  | 'DRAFT'
  | 'READY'
  | 'ACTIVE'
  | 'PAUSED'
  | 'WARNING'
  | 'USER_CONFIRMED'
  | 'ESCALATED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'EXPIRED';

export type ActivityType =
  | 'walk_home'
  | 'walk'
  | 'car'
  | 'bike'
  | 'hike'
  | 'activity'
  | 'travel'
  | 'other';

export type AnomalyType =
  | 'DELAY_WARNING'
  | 'STATIONARY_WARNING'
  | 'ROUTE_DEVIATION_WARNING'
  | 'CHECKIN_OVERDUE'
  | 'MANUAL_SOS';

export type SupportedLanguage = 'fr' | 'en' | 'es' | 'de' | 'it';

export type RetentionPolicy = 'none' | '24h' | '7d' | '30d';

export type CountryCode = 'FR' | 'BE' | 'CH' | 'CA' | 'GB' | 'US' | 'EU';

export type TabId = 'home' | 'safe' | 'contacts' | 'activity' | 'settings';

export interface GeoPoint {
  lat: number;
  lng: number;
  accuracyMeters?: number;
  timestamp: number;
}

export interface RouteDestination {
  id: string;
  label: string;
  address: string;
  distanceKm: number;
  estimatedMinutes: number;
  origin: GeoPoint;
  destination: GeoPoint;
  routePoints: GeoPoint[];
  provider: 'mock' | 'google_maps';
}

export interface TrustedContact {
  id: string;
  firstName: string;
  lastName: string;
  phone: string;
  email?: string;
  isPrimary: boolean;
  acceptedByContact: boolean;
  createdAt: number;
}

export interface DetectionConfig {
  gracePeriodMinutes: number;
  stationaryThresholdMinutes: number;
  routeDeviationMeters: number;
  gpsAccuracyMaxMeters: number;
  checkInResponseGraceMinutes: number;
}

export interface AnomalyRecord {
  id: string;
  type: AnomalyType;
  triggeredAt: number;
  resolvedAt?: number;
  resolution?: 'confirmed_ok' | 'extended' | 'escalated' | 'cancelled' | 'expired';
  details: string;
}

export interface SafeSession {
  id: string;
  state: SessionState;
  activityType: ActivityType;
  title: string;
  originLabel: string;
  destination: RouteDestination;
  expectedDurationMinutes: number;
  startedAt: number | null;
  pausedAt: number | null;
  totalPausedMs: number;
  elapsedMinutes: number;
  estimatedArrivalAt: number | null;
  contactId: string | null;
  progressPercent: number;
  currentPosition: GeoPoint | null;
  lastSignalAt: number | null;
  stationaryMinutes: number;
  deviationMeters: number;
  activeAnomaly: AnomalyRecord | null;
  anomalies: AnomalyRecord[];
  endedAt: number | null;
  detectionEnabled: {
    position: boolean;
    progress: boolean;
    time: boolean;
    stationary: boolean;
    routeDeviation: boolean;
  };
}

export interface SessionHistoryItem {
  id: string;
  activityType: ActivityType;
  title: string;
  destinationLabel: string;
  startedAt: number;
  endedAt: number;
  durationMinutes: number;
  distanceKm: number;
  endState: 'COMPLETED' | 'CANCELLED' | 'ESCALATED' | 'EXPIRED';
  anomaliesCount: number;
  anomalies: AnomalyRecord[];
  contactName?: string;
  /** Stored only when user retention policy permits */
  lastKnownPoint?: GeoPoint;
}

export interface CheckIn {
  id: string;
  dueAt: number;
  contactId: string | null;
  message: string;
  status: 'SCHEDULED' | 'DUE' | 'CONFIRMED' | 'ESCALATED' | 'CANCELLED';
  createdAt: number;
  dueTriggeredAt?: number;
  graceExpiresAt?: number;
  confirmedAt?: number;
}

export interface SafeNotification {
  id: string;
  category: 'session_started' | 'arriving_soon' | 'delay' | 'checkin_due' | 'checkin_unconfirmed' | 'system';
  title: string;
  body: string;
  createdAt: number;
  read: boolean;
}

export interface PrivacySettings {
  locationPermissionGranted: boolean;
  retentionPolicy: RetentionPolicy;
  shareOnlyOnConsent: boolean;
}

export interface DemoSimulationState {
  enabled: boolean;
  simulatedElapsedMinutes: number | null;
  simulatedProgressPercent: number | null;
  simulatedStationaryMinutes: number;
  simulatedDeviationMeters: number;
  lowBattery: boolean;
  offline: boolean;
  inaccurateGps: boolean;
}

export interface UserProfile {
  id: string;
  displayName: string;
  email: string;
  provider: 'demo' | 'email' | 'google' | 'apple_stub';
  signedIn: boolean;
}

export interface EmergencyConfig {
  country: CountryCode;
  countryName: string;
  primaryNumber: string;
  policeNumber: string;
  medicalNumber: string;
  fireNumber: string;
  disclaimerKey: string;
}

export interface AlertDraftPayload {
  contact: TrustedContact | null;
  sessionTitle: string;
  destinationLabel: string;
  lastSignalTimeLabel: string;
  lastPosition: GeoPoint | null;
  messageBody: string;
  reason: 'SESSION_ANOMALY' | 'CHECKIN_EXPIRED' | 'SOS';
}
