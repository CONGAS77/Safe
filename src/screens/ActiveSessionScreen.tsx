import React, { useState } from 'react';
import {
  BatteryWarning,
  CheckCircle2,
  Edit3,
  Pause,
  Play,
  ShieldAlert,
  WifiOff,
} from 'lucide-react';
import { SafeMapView } from '../components/SafeMapView';
import { useI18n } from '../i18n/I18nContext';
import { useSafe } from '../state/SafeContext';

export const ActiveSessionScreen: React.FC = () => {
  const {
    activeSession,
    lastCompletedSummary,
    clearCompletedSummary,
    setActiveTab,
    openWizard,
    pauseOrResumeSession,
    extendActiveSession,
    updateActiveDestination,
    completeActiveSession,
    openSosProtocol,
    demo,
  } = useSafe();
  const { t } = useI18n();

  const [arrivalSheetOpen, setArrivalSheetOpen] = useState(false);
  const [editingDest, setEditingDest] = useState(false);
  const [newDestInput, setNewDestInput] = useState('');
  const [customExtendMins, setCustomExtendMins] = useState(20);

  // End-of-session summary screen
  if (!activeSession && lastCompletedSummary) {
    return (
      <div style={{ paddingTop: 12 }}>
        <div className="card" style={{ textAlign: 'center', padding: 24, marginBottom: 16 }}>
          <div
            style={{
              width: 72,
              height: 72,
              borderRadius: 24,
              background: 'var(--green-soft)',
              border: '1px solid var(--green-border)',
              display: 'grid',
              placeItems: 'center',
              margin: '0 auto 14px',
            }}
          >
            <CheckCircle2 size={38} color="#10B981" />
          </div>
          <h1 style={{ margin: '0 0 6px', fontSize: 24 }}>✅ {t('summary.title')}</h1>
          <p className="muted-text" style={{ margin: 0 }}>{t('summary.subtitle')}</p>
        </div>

        <div className="card" style={{ marginBottom: 18 }}>
          <div style={{ display: 'grid', rowGap: 14, fontSize: 15 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>{t('summary.duration')}</span>
              <strong>{lastCompletedSummary.durationMinutes} min</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>{t('summary.distance')}</span>
              <strong>{lastCompletedSummary.distanceKm} km</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>{t('summary.anomalies')}</span>
              <strong>
                {lastCompletedSummary.anomaliesCount === 0
                  ? t('summary.none')
                  : `${lastCompletedSummary.anomaliesCount} vérifiée(s)`}
              </strong>
            </div>
          </div>
        </div>

        <button
          type="button"
          className="btn btn-primary-green btn-full"
          onClick={() => {
            clearCompletedSummary();
            setActiveTab('home');
          }}
        >
          {t('summary.backHome')}
        </button>
      </div>
    );
  }

  // Empty state if no session is active
  if (!activeSession) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: 28, marginTop: 20 }}>
        <h2 style={{ margin: '0 0 8px' }}>Aucune session SAFE active</h2>
        <p className="muted-text" style={{ margin: '0 0 18px' }}>
          Démarrez une session lorsque vous partez seul pour activer le suivi temporaire.
        </p>
        <button type="button" className="btn btn-primary-green btn-full" onClick={() => openWizard()}>
          🟢 {t('home.cta.main')}
        </button>
      </div>
    );
  }

  const elapsed = Math.round(activeSession.elapsedMinutes);
  const remaining = Math.max(0, Math.round(activeSession.expectedDurationMinutes - activeSession.elapsedMinutes));
  const etaStr = activeSession.estimatedArrivalAt
    ? new Date(activeSession.estimatedArrivalAt).toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit',
      })
    : '--:--';

  const badgeClass =
    activeSession.state === 'ESCALATED'
      ? 'pill pill-red'
      : activeSession.state === 'WARNING' || activeSession.state === 'PAUSED'
      ? 'pill pill-orange'
      : 'pill pill-green';

  const badgeLabel =
    activeSession.state === 'ESCALATED'
      ? `🔴 ${t('session.escalatedBadge')}`
      : activeSession.state === 'WARNING'
      ? `🟠 ${t('session.warningBadge')}`
      : activeSession.state === 'PAUSED'
      ? `⏸ ${t('session.pausedBadge')}`
      : `🟢 ${t('session.activeBadge')}`;

  return (
    <div>
      {/* Top status badge + Title */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <span className={badgeClass}>{badgeLabel}</span>
        <button
          type="button"
          className="btn btn-secondary"
          style={{ minHeight: 34, padding: '4px 10px', fontSize: 12 }}
          onClick={() => {
            setNewDestInput(activeSession.destination.label);
            setEditingDest((v) => !v);
          }}
        >
          <Edit3 size={13} /> {t('session.changeDest')}
        </button>
      </div>

      <h1 style={{ margin: '0 0 12px', fontSize: 22, fontWeight: 800 }}>
        {activeSession.title} → {activeSession.destination.label}
      </h1>

      {/* Special conditions alerts (Battery / Offline / GPS) */}
      {demo.lowBattery && (
        <div className="card" style={{ borderColor: 'var(--orange-border)', background: 'var(--orange-soft)', marginBottom: 10, padding: 12, display: 'flex', gap: 8, alignItems: 'center', fontSize: 13 }}>
          <BatteryWarning size={18} color="#D97706" />
          <span>{t('session.batteryLow')}</span>
        </div>
      )}
      {demo.offline && (
        <div className="card" style={{ borderColor: 'var(--orange-border)', background: 'var(--orange-soft)', marginBottom: 10, padding: 12, display: 'flex', gap: 8, alignItems: 'center', fontSize: 13 }}>
          <WifiOff size={18} color="#D97706" />
          <span>{t('session.offline')}</span>
        </div>
      )}
      {demo.inaccurateGps && (
        <div className="disclaimer-box" style={{ marginBottom: 10 }}>
          {t('session.gpsImprecise')}
        </div>
      )}

      {editingDest && (
        <form
          className="card"
          style={{ marginBottom: 12, display: 'flex', gap: 8 }}
          onSubmit={async (e) => {
            e.preventDefault();
            if (!newDestInput.trim()) return;
            await updateActiveDestination(newDestInput);
            setEditingDest(false);
          }}
        >
          <input
            className="input"
            value={newDestInput}
            onChange={(e) => setNewDestInput(e.target.value)}
            placeholder="Nouvelle destination"
          />
          <button type="submit" className="btn btn-primary-green">
            OK
          </button>
        </form>
      )}

      {/* Time metrics */}
      <div className="card" style={{ marginBottom: 12 }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8, textAlign: 'center' }}>
          <div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{t('session.elapsed')}</div>
            <div style={{ fontSize: 19, fontWeight: 800, marginTop: 2 }}>{elapsed} min</div>
          </div>
          <div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{t('session.remaining')}</div>
            <div style={{ fontSize: 19, fontWeight: 800, marginTop: 2 }}>{remaining} min</div>
          </div>
          <div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{t('session.eta')}</div>
            <div style={{ fontSize: 19, fontWeight: 800, marginTop: 2 }}>{etaStr}</div>
          </div>
        </div>
      </div>

      {/* Live Map */}
      <div style={{ marginBottom: 12 }}>
        <SafeMapView
          destination={activeSession.destination}
          currentPosition={activeSession.currentPosition}
          progressPercent={activeSession.progressPercent}
          deviationMeters={activeSession.deviationMeters}
          inaccurateGps={demo.inaccurateGps}
        />
      </div>

      {/* Reassuring status message (never "Vous êtes en sécurité") */}
      <div
        className="card"
        style={{
          marginBottom: 14,
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          borderColor: activeSession.state === 'PAUSED' ? 'var(--orange-border)' : 'var(--green-border)',
        }}
      >
        <span className="pulse-dot" />
        <div>
          <div style={{ fontWeight: 700, fontSize: 14 }}>
            {activeSession.state === 'PAUSED' ? t('session.status.paused') : t('session.status.ok')}
          </div>
          <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
            {t('session.status.active')} · {t('session.status.normal')}
          </div>
        </div>
      </div>

      {/* Three primary actions */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <button
          type="button"
          className="btn btn-primary-green btn-full"
          style={{ minHeight: 54, fontSize: 16 }}
          onClick={() => setArrivalSheetOpen(true)}
        >
          <CheckCircle2 size={20} /> {t('session.btn.arrived')}
        </button>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={pauseOrResumeSession}
          >
            {activeSession.state === 'PAUSED' ? <Play size={17} /> : <Pause size={17} />}
            {activeSession.state === 'PAUSED' ? t('session.btn.resume') : t('session.btn.pause')}
          </button>

          <button
            type="button"
            className="btn btn-danger"
            onClick={() => openSosProtocol('SOS')}
          >
            <ShieldAlert size={17} /> 🔴 SOS
          </button>
        </div>
      </div>

      {/* Arrival confirmation / extension sheet */}
      {arrivalSheetOpen && (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-sheet">
            <h2 style={{ margin: '0 0 8px', fontSize: 22 }}>🏠 {t('arrival.title')}</h2>
            <p className="muted-text" style={{ margin: '0 0 16px' }}>
              Destination : <strong>{activeSession.destination.label}</strong>
            </p>

            <button
              type="button"
              className="btn btn-primary-green btn-full"
              style={{ marginBottom: 14, minHeight: 56 }}
              onClick={() => {
                setArrivalSheetOpen(false);
                completeActiveSession();
              }}
            >
              🟢 {t('arrival.yes')} — {t('arrival.yesSub')}
            </button>

            <div className="section-title">⏱ {t('arrival.extend')}</div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8, marginBottom: 10 }}>
              {[15, 30, 60].map((m) => (
                <button
                  key={m}
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => {
                    extendActiveSession(m);
                    setArrivalSheetOpen(false);
                  }}
                >
                  +{m < 60 ? `${m} min` : '1 h'}
                </button>
              ))}
            </div>

            <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
              <input
                type="number"
                min={5}
                max={240}
                className="input"
                value={customExtendMins}
                onChange={(e) => setCustomExtendMins(Math.max(5, Number(e.target.value) || 15))}
              />
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  extendActiveSession(customExtendMins);
                  setArrivalSheetOpen(false);
                }}
              >
                +{customExtendMins} min
              </button>
            </div>

            <button
              type="button"
              className="btn btn-secondary btn-full"
              onClick={() => setArrivalSheetOpen(false)}
            >
              Fermer
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
