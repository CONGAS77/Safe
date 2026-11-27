import { CountryCode, DetectionConfig } from '../types/safe';

export interface AppEnvironmentConfig {
  env: 'demo' | 'production';
  googleMapsApiKey: string;
  backendUrl: string;
  defaultCountry: CountryCode;
}

const metaEnv = (typeof import.meta !== 'undefined' && (import.meta as unknown as { env?: Record<string, string> }).env) || {};

export const APP_ENV: AppEnvironmentConfig = {
  env: metaEnv.VITE_SAFE_ENV === 'production' ? 'production' : 'demo',
  googleMapsApiKey: metaEnv.VITE_GOOGLE_MAPS_API_KEY ?? '',
  backendUrl: metaEnv.VITE_SAFE_BACKEND_URL ?? '',
  defaultCountry: (metaEnv.VITE_DEFAULT_COUNTRY as CountryCode) || 'FR',
};

export const DEFAULT_DETECTION_CONFIG: DetectionConfig = {
  gracePeriodMinutes: 5,
  stationaryThresholdMinutes: 10,
  routeDeviationMeters: 250,
  gpsAccuracyMaxMeters: 120,
  checkInResponseGraceMinutes: 3,
};
