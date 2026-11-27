import React from 'react';
import { ACTIVITY_META } from '../components/ActivityIcon';
import { useI18n } from '../i18n/I18nContext';
import { useSafe } from '../state/SafeContext';

function formatDayGroup(ts: number): string {
  const date = new Date(ts);
  const now = new Date();
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
  const startOfItem = new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
  const diffDays = Math.round((startOfToday - startOfItem) / (24 * 60 * 60_000));
  if (diffDays === 0) return 'Aujourd’hui';
  if (diffDays === 1) return 'Hier';
  return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long' });
}

function formatDuration(mins: number): string {
  if (mins < 60) return `${mins} min`;
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return `${h} h ${String(m).padStart(2, '0')}`;
}

export const ActivityScreen: React.FC = () => {
  const { history, historyLoading, privacy } = useSafe();
  const { t } = useI18n();

  if (historyLoading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: 28 }}>
        Chargement de l’activité…
      </div>
    );
  }

  return (
    <div>
      <h1 style={{ margin: '0 0 8px', fontSize: 24, fontWeight: 800 }}>{t('history.title')}</h1>
      <div className="disclaimer-box" style={{ marginBottom: 14 }}>
        Politique de conservation active : <strong>{privacy.retentionPolicy}</strong>. Les coordonnées détaillées ne sont conservées que selon vos paramètres de confidentialité.
      </div>

      {history.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: 28 }}>
          <div style={{ fontWeight: 700, marginBottom: 6 }}>{t('history.empty')}</div>
          <div className="muted-text">
            Vos sessions terminées apparaîtront ici si la conservation est activée.
          </div>
        </div>
      ) : (
        history.map((item) => {
          const emoji = ACTIVITY_META[item.activityType]?.emoji ?? '🛡️';
          return (
            <div key={item.id} className="card">
              <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-muted)', marginBottom: 6 }}>
                {formatDayGroup(item.endedAt)}
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 10 }}>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 16 }}>
                    {emoji} {item.title}
                  </div>
                  <div style={{ fontSize: 13, color: 'var(--text-secondary)', marginTop: 2 }}>
                    {item.destinationLabel} · {formatDuration(item.durationMinutes)}
                  </div>
                </div>
                <span
                  className={
                    item.endState === 'COMPLETED'
                      ? 'pill pill-green'
                      : item.endState === 'ESCALATED'
                      ? 'pill pill-red'
                      : 'pill pill-neutral'
                  }
                >
                  {item.endState === 'COMPLETED' ? t('history.normalEnd') : item.endState}
                </span>
              </div>
            </div>
          );
        })
      )}
    </div>
  );
};
