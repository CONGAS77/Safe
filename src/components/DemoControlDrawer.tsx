import React from 'react';
import { Sliders, X } from 'lucide-react';
import { useSafe } from '../state/SafeContext';

export const DemoControlDrawer: React.FC = () => {
  const {
    demo,
    setDemoState,
    activeSession,
    triggerDemoAnomaly,
    simulateArrival,
    demoDrawerOpen,
    setDemoDrawerOpen,
    activeCheckIn,
    simulateCheckInDueNow,
    simulateCheckInExpiredNow,
    openSosProtocol,
  } = useSafe();

  if (!demo.enabled) return null;

  if (!demoDrawerOpen) {
    return (
      <button
        type="button"
        onClick={() => setDemoDrawerOpen(true)}
        aria-label="Ouvrir le simulateur Mode Démo"
        style={{
          position: 'absolute',
          right: 14,
          bottom: 84,
          zIndex: 35,
          borderRadius: 999,
          border: '1px solid var(--border-subtle)',
          background: 'var(--bg-surface)',
          color: 'var(--text-primary)',
          padding: '8px 12px',
          fontSize: 12,
          fontWeight: 700,
          boxShadow: 'var(--shadow-md)',
          display: 'flex',
          alignItems: 'center',
          gap: 6,
          cursor: 'pointer',
        }}
      >
        <Sliders size={14} /> Démo
      </button>
    );
  }

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-label="Mode simulation">
      <div className="modal-sheet">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <div>
            <div style={{ fontWeight: 800, fontSize: 16 }}>Mode simulation (DEMO MODE)</div>
            <div className="muted-text" style={{ fontSize: 12 }}>
              Testez la logique de sécurité sans GPS réel
            </div>
          </div>
          <button type="button" className="icon-btn" onClick={() => setDemoDrawerOpen(false)} aria-label="Fermer">
            <X size={18} />
          </button>
        </div>

        {activeSession ? (
          <>
            <div className="card" style={{ marginBottom: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 6 }}>
                <span>Position simulée</span>
                <strong>{Math.round(activeSession.progressPercent)} %</strong>
              </div>
              <input
                type="range"
                min={0}
                max={100}
                value={Math.round(activeSession.progressPercent)}
                onChange={(e) => setDemoState({ simulatedProgressPercent: Number(e.target.value) })}
                style={{ width: '100%', marginBottom: 12 }}
              />

              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 6 }}>
                <span>Temps écoulé simulé</span>
                <strong>{Math.round(activeSession.elapsedMinutes)} min</strong>
              </div>
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                {[12, 20, 35, 60].map((m) => (
                  <button
                    key={m}
                    type="button"
                    className="btn btn-secondary"
                    style={{ minHeight: 38, padding: '6px 12px', fontSize: 13 }}
                    onClick={() => setDemoState({ simulatedElapsedMinutes: m })}
                  >
                    {m} min
                  </button>
                ))}
              </div>
            </div>

            <div className="section-title">Déclencher un scénario</div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 12 }}>
              <button
                type="button"
                className="btn btn-warning"
                style={{ fontSize: 13 }}
                onClick={() => {
                  setDemoDrawerOpen(false);
                  triggerDemoAnomaly('DELAY_WARNING');
                }}
              >
                Retard (35 min)
              </button>
              <button
                type="button"
                className="btn btn-warning"
                style={{ fontSize: 13 }}
                onClick={() => {
                  setDemoDrawerOpen(false);
                  triggerDemoAnomaly('STATIONARY_WARNING');
                }}
              >
                Arrêt prolongé
              </button>
              <button
                type="button"
                className="btn btn-warning"
                style={{ fontSize: 13 }}
                onClick={() => {
                  setDemoDrawerOpen(false);
                  triggerDemoAnomaly('ROUTE_DEVIATION_WARNING');
                }}
              >
                Déviation GPS
              </button>
              <button
                type="button"
                className="btn btn-primary-green"
                style={{ fontSize: 13 }}
                onClick={() => {
                  setDemoDrawerOpen(false);
                  simulateArrival();
                }}
              >
                Simuler l’arrivée
              </button>
            </div>
            <button
              type="button"
              className="btn btn-outline-danger btn-full"
              style={{ marginBottom: 12 }}
              onClick={() => {
                setDemoDrawerOpen(false);
                openSosProtocol('SOS');
              }}
            >
              Simuler SOS
            </button>
          </>
        ) : (
          <div className="card" style={{ marginBottom: 12 }}>
            <div className="muted-text">
              Démarrez une session SAFE pour simuler la progression, le retard, l’arrêt prolongé, la déviation ou l’arrivée.
            </div>
          </div>
        )}

        {activeCheckIn && (
          <div className="card" style={{ marginBottom: 12 }}>
            <div style={{ fontWeight: 700, fontSize: 13, marginBottom: 8 }}>Check-in actif</div>
            <div style={{ display: 'flex', gap: 8 }}>
              <button
                type="button"
                className="btn btn-warning"
                style={{ flex: 1, fontSize: 13 }}
                onClick={() => {
                  setDemoDrawerOpen(false);
                  simulateCheckInDueNow();
                }}
              >
                Échéance maintenant
              </button>
              <button
                type="button"
                className="btn btn-danger"
                style={{ flex: 1, fontSize: 13 }}
                onClick={() => {
                  setDemoDrawerOpen(false);
                  simulateCheckInExpiredNow();
                }}
              >
                Non confirmé
              </button>
            </div>
          </div>
        )}

        <div className="section-title">Conditions appareil</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <label style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 14 }}>
            <span>Batterie faible</span>
            <input
              type="checkbox"
              checked={demo.lowBattery}
              onChange={(e) => setDemoState({ lowBattery: e.target.checked })}
            />
          </label>
          <label style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 14 }}>
            <span>Téléphone hors ligne</span>
            <input
              type="checkbox"
              checked={demo.offline}
              onChange={(e) => setDemoState({ offline: e.target.checked })}
            />
          </label>
          <label style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 14 }}>
            <span>GPS imprécis (&gt; 200 m)</span>
            <input
              type="checkbox"
              checked={demo.inaccurateGps}
              onChange={(e) => setDemoState({ inaccurateGps: e.target.checked })}
            />
          </label>
        </div>
      </div>
    </div>
  );
};
