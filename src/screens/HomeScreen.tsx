import React from 'react';
import { Clock, ShieldCheck } from 'lucide-react';
import { useI18n } from '../i18n/I18nContext';
import { useSafe } from '../state/SafeContext';

export const HomeScreen: React.FC = () => {
  const {
    openWizard,
    openCheckInModal,
    activeSession,
    setActiveTab,
    activeCheckIn,
    confirmCheckInOk,
    user,
  } = useSafe();
  const { t } = useI18n();

  return (
    <div>
      {/* Greeting */}
      <div style={{ marginTop: 4, marginBottom: 20 }}>
        <div style={{ fontSize: 16, color: 'var(--text-secondary)', fontWeight: 600 }}>
          {t('home.greeting')}{user.signedIn && user.displayName ? ` ${user.displayName}` : ''}
        </div>
        <h1 style={{ fontSize: 28, fontWeight: 800, margin: '4px 0 0', letterSpacing: '-0.02em' }}>
          {t('home.question')}
        </h1>
      </div>

      {/* Active session status banner */}
      {activeSession && (
        <div
          className="card"
          style={{
            marginBottom: 16,
            borderColor: 'var(--green-border)',
            background: 'var(--green-soft)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 12,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <span className="pulse-dot" />
            <div>
              <div style={{ fontWeight: 700, fontSize: 14 }}>{t('home.active.banner')}</div>
              <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                {activeSession.title} → {activeSession.destination.label}
              </div>
            </div>
          </div>
          <button
            type="button"
            className="btn btn-primary-green"
            style={{ minHeight: 40, padding: '8px 14px', fontSize: 13 }}
            onClick={() => setActiveTab('safe')}
          >
            {t('home.active.resume')}
          </button>
        </div>
      )}

      {/* Primary Hero CTA */}
      <div
        className="card"
        style={{
          padding: 20,
          textAlign: 'center',
          marginBottom: 18,
          borderRadius: 24,
        }}
      >
        <button
          type="button"
          className="btn btn-primary-green btn-hero"
          onClick={() => (activeSession ? setActiveTab('safe') : openWizard())}
        >
          <span aria-hidden="true">🟢</span>
          <span>{activeSession ? t('home.active.resume') : t('home.cta.main')}</span>
        </button>
        <p className="muted-text" style={{ margin: '12px 0 0', fontSize: 13 }}>
          {t('home.cta.hint')}
        </p>
      </div>

      {/* Four quick shortcuts */}
      <div className="section-title">{t('home.shortcuts.title')}</div>
      <div className="shortcut-grid" style={{ marginBottom: 16 }}>
        <button
          type="button"
          className="shortcut-card"
          onClick={() => openWizard('walk_home')}
        >
          <span style={{ fontSize: 24 }} aria-hidden="true">🚶</span>
          <div>
            <div style={{ fontWeight: 700, fontSize: 15 }}>{t('home.shortcut.walk_home')}</div>
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
              {t('home.shortcut.walk_home.sub')}
            </div>
          </div>
        </button>

        <button
          type="button"
          className="shortcut-card"
          onClick={() => openWizard('car')}
        >
          <span style={{ fontSize: 24 }} aria-hidden="true">🚗</span>
          <div>
            <div style={{ fontWeight: 700, fontSize: 15 }}>{t('home.shortcut.car')}</div>
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
              {t('home.shortcut.car.sub')}
            </div>
          </div>
        </button>

        <button
          type="button"
          className="shortcut-card"
          onClick={() => openWizard('hike')}
        >
          <span style={{ fontSize: 24 }} aria-hidden="true">🥾</span>
          <div>
            <div style={{ fontWeight: 700, fontSize: 15 }}>{t('home.shortcut.hike')}</div>
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
              {t('home.shortcut.hike.sub')}
            </div>
          </div>
        </button>

        <button
          type="button"
          className="shortcut-card"
          onClick={() => openWizard('activity')}
        >
          <span style={{ fontSize: 24 }} aria-hidden="true">🏊</span>
          <div>
            <div style={{ fontWeight: 700, fontSize: 15 }}>{t('home.shortcut.activity')}</div>
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
              {t('home.shortcut.activity.sub')}
            </div>
          </div>
        </button>
      </div>

      {/* Independent Check-in Card */}
      <div className="card" style={{ marginBottom: 14 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div
              style={{
                width: 44,
                height: 44,
                borderRadius: 14,
                background: 'var(--bg-surface-muted)',
                display: 'grid',
                placeItems: 'center',
              }}
            >
              <Clock size={22} color="#10B981" />
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: 15 }}>{t('home.checkin.title')}</div>
              <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{t('home.checkin.sub')}</div>
            </div>
          </div>
          <button
            type="button"
            className="btn btn-secondary"
            style={{ minHeight: 42, padding: '8px 14px', fontSize: 13 }}
            onClick={openCheckInModal}
          >
            Programmer
          </button>
        </div>

        {activeCheckIn && (
          <div
            style={{
              marginTop: 12,
              paddingTop: 12,
              borderTop: '1px solid var(--border-subtle)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: 8,
            }}
          >
            <div style={{ fontSize: 13 }}>
              <strong>Check-in actif :</strong>{' '}
              {new Date(activeCheckIn.dueAt).toLocaleTimeString('fr-FR', {
                hour: '2-digit',
                minute: '2-digit',
              })}
            </div>
            <button
              type="button"
              className="btn btn-primary-green"
              style={{ minHeight: 36, padding: '6px 12px', fontSize: 12 }}
              onClick={() => confirmCheckInOk(activeCheckIn.id)}
            >
              Tout va bien
            </button>
          </div>
        )}
      </div>

      {/* Calm reassurance footer */}
      <div className="disclaimer-box" style={{ display: 'flex', gap: 10, alignItems: 'flex-start' }}>
        <ShieldCheck size={18} color="#10B981" style={{ flexShrink: 0, marginTop: 1 }} />
        <span>{t('app.disclaimer.complementary')}</span>
      </div>
    </div>
  );
};
