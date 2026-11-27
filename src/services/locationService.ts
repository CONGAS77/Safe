import { GeoPoint, RouteDestination } from '../types/safe';

export interface LocationService {
  readonly providerName: string;
  requestPermission(): Promise<boolean>;
  getCurrentPosition(): Promise<GeoPoint>;
  interpolatePositionAlongRoute(route: RouteDestination, progressPercent: number, deviationMeters?: number, inaccurateGps?: boolean): GeoPoint;
  calculateProgressPercent(route: RouteDestination, current: GeoPoint): number;
  calculateDistanceKm(from: GeoPoint, to: GeoPoint): number;
}

export interface MapRouteProvider {
  readonly id: 'mock' | 'google_maps';
  isConfigured(): boolean;
  resolveDestination(query: string, origin?: GeoPoint): Promise<RouteDestination>;
  getPresetDestinations(origin?: GeoPoint): RouteDestination[];
}

const DEFAULT_ORIGIN: GeoPoint = {
  lat: 48.8566,
  lng: 2.3522,
  accuracyMeters: 12,
  timestamp: Date.now(),
};

const PRESET_TEMPLATES: Array<{
  id: string;
  label: string;
  address: string;
  distanceKm: number;
  estimatedMinutes: number;
  dLat: number;
  dLng: number;
}> = [
  {
    id: 'dest-home',
    label: 'Maison',
    address: '12 rue Victor Hugo, Paris',
    distanceKm: 2.4,
    estimatedMinutes: 20,
    dLat: 0.015,
    dLng: 0.019,
  },
  {
    id: 'dest-work',
    label: 'Travail',
    address: '45 avenue de l’Opéra, Paris',
    distanceKm: 4.1,
    estimatedMinutes: 30,
    dLat: 0.022,
    dLng: -0.016,
  },
  {
    id: 'dest-station',
    label: 'Gare de Lyon',
    address: 'Place Louis-Armand, Paris',
    distanceKm: 3.3,
    estimatedMinutes: 25,
    dLat: -0.014,
    dLng: 0.024,
  },
  {
    id: 'dest-trail',
    label: 'Sentier des Crêtes',
    address: 'Forêt domaniale — Boucle Nord',
    distanceKm: 6.8,
    estimatedMinutes: 90,
    dLat: 0.032,
    dLng: 0.029,
  },
];

function buildRoutePoints(origin: GeoPoint, dest: GeoPoint): GeoPoint[] {
  const steps = 6;
  const points: GeoPoint[] = [];
  for (let i = 0; i <= steps; i++) {
    const t = i / steps;
    const curveOffset = Math.sin(t * Math.PI) * 0.0022;
    points.push({
      lat: origin.lat + (dest.lat - origin.lat) * t + curveOffset,
      lng: origin.lng + (dest.lng - origin.lng) * t - curveOffset * 0.6,
      accuracyMeters: 12,
      timestamp: origin.timestamp,
    });
  }
  return points;
}

export class MockLocationService implements LocationService {
  readonly providerName: string = 'MockLocationService (Demo)';

  async requestPermission(): Promise<boolean> {
    return true;
  }

  async getCurrentPosition(): Promise<GeoPoint> {
    return { ...DEFAULT_ORIGIN, timestamp: Date.now() };
  }

  interpolatePositionAlongRoute(
    route: RouteDestination,
    progressPercent: number,
    deviationMeters = 0,
    inaccurateGps = false
  ): GeoPoint {
    const clamped = Math.max(0, Math.min(100, progressPercent)) / 100;
    const pts = route.routePoints;
    if (pts.length < 2) {
      return { ...route.origin, timestamp: Date.now() };
    }
    const scaled = clamped * (pts.length - 1);
    const idx = Math.min(pts.length - 2, Math.floor(scaled));
    const frac = scaled - idx;
    const p1 = pts[idx];
    const p2 = pts[idx + 1];

    // Approximate meters to lat/lng offset (~111,000m per degree)
    const devOffsetDeg = deviationMeters / 111000;
    return {
      lat: p1.lat + (p2.lat - p1.lat) * frac + devOffsetDeg * 0.7,
      lng: p1.lng + (p2.lng - p1.lng) * frac - devOffsetDeg * 0.7,
      accuracyMeters: inaccurateGps ? 260 : 12,
      timestamp: Date.now(),
    };
  }

  calculateProgressPercent(route: RouteDestination, current: GeoPoint): number {
    const total = this.calculateDistanceKm(route.origin, route.destination);
    if (total <= 0.01) return 100;
    const remaining = this.calculateDistanceKm(current, route.destination);
    return Math.max(0, Math.min(100, Math.round(((total - remaining) / total) * 100)));
  }

  calculateDistanceKm(from: GeoPoint, to: GeoPoint): number {
    const R = 6371;
    const dLat = ((to.lat - from.lat) * Math.PI) / 180;
    const dLng = ((to.lng - from.lng) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((from.lat * Math.PI) / 180) *
        Math.cos((to.lat * Math.PI) / 180) *
        Math.sin(dLng / 2) *
        Math.sin(dLng / 2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }
}

export class BrowserGeolocationService extends MockLocationService {
  override readonly providerName: string = 'BrowserGeolocationService';

  override async requestPermission(): Promise<boolean> {
    if (typeof navigator === 'undefined' || !navigator.geolocation) return false;
    try {
      await this.getCurrentPosition();
      return true;
    } catch {
      return false;
    }
  }

  override getCurrentPosition(): Promise<GeoPoint> {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      return super.getCurrentPosition();
    }
    return new Promise((resolve) => {
      navigator.geolocation.getCurrentPosition(
        (pos) =>
          resolve({
            lat: pos.coords.latitude,
            lng: pos.coords.longitude,
            accuracyMeters: Math.round(pos.coords.accuracy),
            timestamp: pos.timestamp,
          }),
        () => resolve({ ...DEFAULT_ORIGIN, timestamp: Date.now() }),
        { enableHighAccuracy: true, timeout: 4000, maximumAge: 15000 }
      );
    });
  }
}

export class MockMapRouteProvider implements MapRouteProvider {
  readonly id = 'mock' as const;

  isConfigured(): boolean {
    return true;
  }

  getPresetDestinations(origin: GeoPoint = DEFAULT_ORIGIN): RouteDestination[] {
    return PRESET_TEMPLATES.map((tpl) => {
      const destination: GeoPoint = {
        lat: origin.lat + tpl.dLat,
        lng: origin.lng + tpl.dLng,
        accuracyMeters: 10,
        timestamp: Date.now(),
      };
      return {
        id: tpl.id,
        label: tpl.label,
        address: tpl.address,
        distanceKm: tpl.distanceKm,
        estimatedMinutes: tpl.estimatedMinutes,
        origin,
        destination,
        routePoints: buildRoutePoints(origin, destination),
        provider: 'mock',
      };
    });
  }

  async resolveDestination(query: string, origin: GeoPoint = DEFAULT_ORIGIN): Promise<RouteDestination> {
    const trimmed = query.trim() || 'Maison';
    const presets = this.getPresetDestinations(origin);
    const matched = presets.find(
      (p) =>
        p.label.toLowerCase() === trimmed.toLowerCase() ||
        p.address.toLowerCase().includes(trimmed.toLowerCase())
    );
    if (matched) return matched;

    // Deterministic simulated route for any custom address
    const hash = Array.from(trimmed).reduce((acc, ch) => acc + ch.charCodeAt(0), 0);
    const distanceKm = Number((1.4 + (hash % 45) / 10).toFixed(1));
    const estimatedMinutes = Math.max(10, Math.round(distanceKm * 10));
    const destination: GeoPoint = {
      lat: origin.lat + 0.012 + ((hash % 15) - 7) * 0.002,
      lng: origin.lng + 0.014 + (((hash >> 2) % 15) - 7) * 0.002,
      accuracyMeters: 10,
      timestamp: Date.now(),
    };
    return {
      id: `dest-custom-${hash}`,
      label: trimmed,
      address: trimmed,
      distanceKm,
      estimatedMinutes,
      origin,
      destination,
      routePoints: buildRoutePoints(origin, destination),
      provider: 'mock',
    };
  }
}

/**
 * Adapter stub ready for Google Maps Platform (Places + Routes API).
 * Falls back to MockMapRouteProvider when no API key is configured.
 */
export class GoogleMapsRouteProvider implements MapRouteProvider {
  readonly id = 'google_maps' as const;
  private readonly apiKey: string;
  private readonly fallback = new MockMapRouteProvider();

  constructor(apiKey: string) {
    this.apiKey = apiKey;
  }

  isConfigured(): boolean {
    return Boolean(this.apiKey && this.apiKey.trim().length > 0);
  }

  getPresetDestinations(origin?: GeoPoint): RouteDestination[] {
    return this.fallback.getPresetDestinations(origin);
  }

  async resolveDestination(query: string, origin?: GeoPoint): Promise<RouteDestination> {
    if (!this.isConfigured()) {
      return this.fallback.resolveDestination(query, origin);
    }
    // Future production hook: call Google Maps Places & Routes API via backend proxy or Maps JS SDK
    return this.fallback.resolveDestination(query, origin);
  }
}
