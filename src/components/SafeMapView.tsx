import React from 'react';
import { GeoPoint, RouteDestination } from '../types/safe';

interface SafeMapViewProps {
  destination: RouteDestination;
  currentPosition: GeoPoint | null;
  progressPercent: number;
  deviationMeters?: number;
  inaccurateGps?: boolean;
  compact?: boolean;
}

export const SafeMapView: React.FC<SafeMapViewProps> = ({
  destination,
  currentPosition,
  progressPercent,
  deviationMeters = 0,
  inaccurateGps = false,
  compact = false,
}) => {
  const pts = destination.routePoints;
  const allPts = currentPosition ? [...pts, currentPosition] : pts;

  const lats = allPts.map((p) => p.lat);
  const lngs = allPts.map((p) => p.lng);
  const minLat = Math.min(...lats) - 0.004;
  const maxLat = Math.max(...lats) + 0.004;
  const minLng = Math.min(...lngs) - 0.004;
  const maxLng = Math.max(...lngs) + 0.004;

  const project = (p: GeoPoint) => {
    const x = ((p.lng - minLng) / Math.max(0.0001, maxLng - minLng)) * 280 + 20;
    const y = 140 - ((p.lat - minLat) / Math.max(0.0001, maxLat - minLat)) * 110;
    return { x, y };
  };

  const projectedRoute = pts.map(project);
  const polylinePoints = projectedRoute.map((p) => `${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ');
  const startPx = projectedRoute[0] ?? { x: 40, y: 120 };
  const endPx = projectedRoute[projectedRoute.length - 1] ?? { x: 270, y: 40 };
  const curPx = currentPosition ? project(currentPosition) : startPx;

  return (
    <div
      style={{
        borderRadius: 18,
        overflow: 'hidden',
        border: '1px solid var(--border-subtle)',
        background: 'var(--bg-surface)',
      }}
    >
      <div
        style={{
          position: 'relative',
          height: compact ? 130 : 175,
          background:
            'radial-gradient(circle at 20% 20%, rgba(16,185,129,0.09), transparent 55%), linear-gradient(180deg, rgba(148,163,184,0.12) 1px, transparent 1px), linear-gradient(90deg, rgba(148,163,184,0.12) 1px, transparent 1px)',
          backgroundSize: '100% 100%, 28px 28px, 28px 28px',
        }}
      >
        <svg viewBox="0 0 320 165" width="100%" height="100%" aria-label="Carte d'itinéraire SAFE">
          {/* Planned route */}
          <polyline
            fill="none"
            stroke="#94a3b8"
            strokeWidth="4"
            strokeDasharray="6 5"
            strokeLinecap="round"
            points={polylinePoints}
          />
          {/* Progress highlight */}
          <line
            x1={startPx.x}
            y1={startPx.y}
            x2={curPx.x}
            y2={curPx.y}
            stroke={deviationMeters > 250 ? '#f59e0b' : '#10b981'}
            strokeWidth="4.5"
            strokeLinecap="round"
          />
          {/* GPS uncertainty halo */}
          <circle
            cx={curPx.x}
            cy={curPx.y}
            r={inaccurateGps ? 26 : 13}
            fill={deviationMeters > 250 ? 'rgba(245,158,11,0.22)' : 'rgba(16,185,129,0.2)'}
          />
          {/* Origin pin */}
          <circle cx={startPx.x} cy={startPx.y} r="6" fill="#0f172a" stroke="#fff" strokeWidth="2" />
          {/* Destination pin */}
          <circle cx={endPx.x} cy={endPx.y} r="7" fill="#059669" stroke="#fff" strokeWidth="2" />
          {/* Current position */}
          <circle
            cx={curPx.x}
            cy={curPx.y}
            r="7"
            fill={deviationMeters > 250 ? '#d97706' : '#10b981'}
            stroke="#fff"
            strokeWidth="2.5"
          />
        </svg>

        <div
          style={{
            position: 'absolute',
            top: 10,
            left: 10,
            right: 10,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            pointerEvents: 'none',
          }}
        >
          <span className="pill pill-neutral" style={{ fontSize: 11, background: 'var(--bg-surface)' }}>
            {destination.provider === 'google_maps' ? 'Google Maps' : 'Carte simulée (MVP)'}
          </span>
          <span className="pill pill-green" style={{ fontSize: 11 }}>
            {Math.round(progressPercent)}% · {destination.distanceKm} km
          </span>
        </div>
      </div>

      <div
        style={{
          padding: '10px 14px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          fontSize: 12,
          color: 'var(--text-secondary)',
          borderTop: '1px solid var(--border-subtle)',
        }}
      >
        <span>Départ : Position actuelle</span>
        <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>→ {destination.label}</span>
      </div>
    </div>
  );
};
