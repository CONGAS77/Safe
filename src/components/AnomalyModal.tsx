import React, { useState } from 'react';
import { AlertTriangle, CheckCircle2, MapPin, ShieldAlert } from 'lucide-react';
import { useI18n } from '../i18n/I18nContext';
import { useSafe } from '../state/SafeContext';
import { SafeMapView } from './SafeMapView';

export const AnomalyModal: React.FC = () => {
  const {
    activeSession,
    confirmAnomalyOk,
    extendActiveSession,
    openSosProtocol,
    checkIns,
    confirmCheckInOk,
    escalateCheckIn,
  } = useSafe();
  const { t } = useI18n();
  const [deviationVerifyOpen, setDeviationVerifyOpen] = useState(false);

  const dueCheckIn = checkIns.find((c) => c.status === 'DUE');
  const anomaly = activeSession?.state === 'WARNING' ? activeSession.activeAnomaly : null;

  if (!anomaly && !dueCheckIn) return null;

  // Priority 1: Active session anomaly
  if (anomaly && activeSession) {
    if (anomaly.type === 'DELAY_WARNING') {
      return (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-sheet">
            <span className="pill pill-orange" style={{ marginBottom: 10 }}>
              <AlertTriangle size={14} /> RETARD DÉTECTÉ
            </span>
            <h2 style={{ margin: '0 0 6px', fontSize: 21 }}>⚠️ {t('anomaly.delay.title')}</h2>
            <p className="muted-text" style={{ margin: '0 0 12px' }}>
              Durée prévue : <strong>{activeSession.expectedDurationMinutes} min</strong> · Temps écoulé :{' '}
              <strong>{Math.round(activeSession.elapsedMinutes)} min</strong>
            </p>
            <div style={{ fontSize: 18, fontWeight: 700, marginBottom: 16 }}>
              {t('anomaly.delay.question')}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <button type="button" className="btn btn-primary-green btn-full" onClick={confirmAnomalyOk}>
                <CheckCircle2 size={18} /> 🟢 {t('anomaly.delay.ok')}
              </button>
              <button
                type="button"
                className="btn btn-secondary btn-full"
                onClick={() => extendActiveSession(15)}
              >
                ⏱ Prolonger de 15 min
              </button>
              <button
                type="button"
                className="btn btn-warning btn-full"
                onClick={() => openSosProtocol('SESSION_ANOMALY')}
              >
                🟠 {t('anomaly.delay.help')}
              </button>
            </div>
          </div>
        </div>
      );
    }

    if (anomaly.type === 'STATIONARY_WARNING') {
      return (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-sheet">
            <span className="pill pill-orange" style={{ marginBottom: 10 }}>
              <AlertTriangle size={14} /> ARRÊT PROLONGÉ
            </span>
            <h2 style={{ margin: '0 0 6px', fontSize: 21 }}>⚠️ {t('anomaly.stationary.title')}</h2>
            <p className="muted-text" style={{ margin: '0 0 12px' }}>
              Immobile depuis environ {Math.round(activeSession.stationaryMinutes)} min.
            </p>
            <div style={{ fontSize: 18, fontWeight: 700, marginBottom: 16 }}>
              {t('anomaly.stationary.question')}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <button type="button" className="btn btn-primary-green btn-full" onClick={confirmAnomalyOk}>
                🟢 {t('anomaly.stationary.ok')}
              </button>
              <button
                type="button"
                className="btn btn-warning btn-full"
                onClick={() => openSosProtocol('SESSION_ANOMALY')}
              >
                🟠 {t('anomaly.stationary.help')}
              </button>
              <button
                type="button"
                className="btn btn-danger btn-full"
                onClick={() => openSosProtocol('SOS')}
              >
                🔴 {t('anomaly.stationary.sos')}
              </button>
            </div>
          </div>
        </div>
      );
    }

    if (anomaly.type === 'ROUTE_DEVIATION_WARNING') {
      return (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-sheet">
            <span className="pill pill-orange" style={{ marginBottom: 10 }}>
              <MapPin size={14} /> CHANGEMENT D’ITINÉRAIRE
            </span>
            <h2 style={{ margin: '0 0 6px', fontSize: 21 }}>⚠️ {t('anomaly.deviation.title')}</h2>
            <div style={{ fontSize: 17, fontWeight: 700, marginBottom: 10 }}>
              {t('anomaly.deviation.question')}
            </div>
            <div className="disclaimer-box" style={{ marginBottom: 12 }}>
              {t('anomaly.deviation.gpsNote')}
            </div>

            {deviationVerifyOpen && (
              <div style={{ marginBottom: 12 }}>
                <SafeMapView
                  destination={activeSession.destination}
                  currentPosition={activeSession.currentPosition}
                  progressPercent={activeSession.progressPercent}
                  deviationMeters={activeSession.deviationMeters}
                  compact
                />
              </div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <button
                type="button"
                className="btn btn-primary-green btn-full"
                onClick={() => {
                  setDeviationVerifyOpen(false);
                  confirmAnomalyOk();
                }}
              >
                🟢 {t('anomaly.deviation.ok')} — {t('anomaly.deviation.okSub')}
              </button>
              {!deviationVerifyOpen && (
                <button
                  type="button"
                  className="btn btn-secondary btn-full"
                  onClick={() => setDeviationVerifyOpen(true)}
                >
                  🟠 {t('anomaly.deviation.unsure')} — {t('anomaly.deviation.unsureSub')}
                </button>
              )}
              <button
                type="button"
                className="btn btn-danger btn-full"
                onClick={() => {
                  setDeviationVerifyOpen(false);
                  openSosProtocol('SESSION_ANOMALY');
                }}
              >
                🔴 {t('anomaly.deviation.help')}
              </button>
            </div>
          </div>
        </div>
      );
    }
  }

  // Priority 2: Due Check-In
  if (dueCheckIn) {
    return (
      <div className="modal-backdrop" role="dialog" aria-modal="true">
        <div className="modal-sheet">
          <span className="pill pill-orange" style={{ marginBottom: 10 }}>
            <ShieldAlert size={14} /> CHECK-IN ARRIVÉ À ÉCHÉANCE
          </span>
          <h2 style={{ margin: '0 0 8px', fontSize: 22 }}>{t('checkin.due.title')}</h2>
          <p className="muted-text" style={{ margin: '0 0 16px' }}>
            Confirmez que tout va bien. Sans réponse, votre proche configuré pourra être prévenu avec le message prévu.
          </p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <button
              type="button"
              className="btn btn-primary-green btn-full"
              onClick={() => confirmCheckInOk(dueCheckIn.id)}
            >
              🟢 {t('checkin.due.ok')}
            </button>
            <button
              type="button"
              className="btn btn-warning btn-full"
              onClick={() => escalateCheckIn(dueCheckIn.id)}
            >
              🟠 {t('checkin.due.help')}
            </button>
            <button
              type="button"
              className="btn btn-danger btn-full"
              onClick={() => escalateCheckIn(dueCheckIn.id)}
            >
              🔴 {t('checkin.due.sos')}
            </button>
          </div>
        </div>
      </div>
    );
  }

  return null;
};
