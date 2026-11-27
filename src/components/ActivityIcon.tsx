import React from 'react';
import { ActivityType } from '../types/safe';

export const ACTIVITY_META: Record<ActivityType, { emoji: string; key: string }> = {
  walk_home: { emoji: '🚶', key: 'activity.walk_home' },
  walk: { emoji: '🚶', key: 'activity.walk' },
  car: { emoji: '🚗', key: 'activity.car' },
  bike: { emoji: '🚲', key: 'activity.bike' },
  hike: { emoji: '🥾', key: 'activity.hike' },
  activity: { emoji: '🏊', key: 'activity.activity' },
  travel: { emoji: '✈️', key: 'activity.travel' },
  other: { emoji: '✏️', key: 'activity.other' },
};

export const ActivityBadge: React.FC<{ type: ActivityType; label: string }> = ({ type, label }) => {
  const meta = ACTIVITY_META[type] ?? ACTIVITY_META.other;
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6, fontWeight: 600 }}>
      <span aria-hidden="true">{meta.emoji}</span>
      <span>{label}</span>
    </span>
  );
};
