import React, { useState } from 'react';
import { AlertTriangle, Lock, Moon, Shield, Sun, Trash2, User } from 'lucide-react';
import { EMERGENCY_NUMBERS_BY_COUNTRY } from '../config/emergencyNumbers';
import { useI18n } from '../i18n/I18nContext';
import { LANGUAGE_LABELS } from '../i18n/translations';
import { useSafe } from '../state/SafeContext';
import { CountryCode, RetentionPolicy, SupportedLanguage } from '../types/safe';

export const SettingsScreen: React.FC = () => {
  const {
    privacy,
    setLocationPermission,
    setRetentionPolicy,
    deleteAllUserData,
    demo,
    setDemoState,
    detectionConfig,
    setDetectionConfig,
    activeSession,
    triggerDemoAnomaly,
    setDemoDrawerOpen,
    language,
    setLanguage,
    darkMode,
    toggleDarkMode,
    country,
    setCountry,
    user,
    signInEmail,
    signInGoogle,
    signInAppleStub,
    signOut,
    replayOnboarding,
  } = useSafe();
  const { t, setLanguage: setI18nLang } = useI18n();

  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [emailInput, setEmailInput] = useState('');
  const [authNotice, setAuthNotice] = useState<string | null>(null);

  const handleLangChange = (next: SupportedLanguage) => {
    setLanguage(next);
    setI18nLang(next);
  };

  return (
    <div>
      <h1 style={{ margin: '0 0 14px', fontSize: 24, fontWeight: 800 }}>{t('settings.title')}</h1>

      {/* Privacy section */}
      <div className="section-title">
        <Lock size={13} style={{ verticalAlign: 'middle', marginRight: 4 }} /> {t('settings.privacy')}
      </div>
      <div className="card">
        <div style={{ fontWeight: 700, marginBottom: 4 }}>Localisation</div>
        <p className="muted-text" style={{ margin: '0 0 10px', fontSize: 13 }}>
          SAFE utilise votre position uniquement lorsque cela est nécessaire pour une session active et selon les autorisations de votre appareil.
        </p>
        <label style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 14, marginBottom: 14 }}>
          <span>Autoriser la localisation pendant les sessions</span>
          <input
            type="checkbox"
            checked={privacy.locationPermissionGranted}
            onChange={(e) => setLocationPermission(e.target.checked)}
          />
        </label>

        <div style={{ fontWeight: 700, marginBottom: 6 }}>Historique — Conservation</div>
        <select
          className="input"
          value={privacy.retentionPolicy}
          onChange={(e) => setRetentionPolicy(e.target.value as RetentionPolicy)}
          style={{ marginBottom: 14 }}
        >
          <option value="none">Ne rien conserver</option>
          <option value="24h">Conserver 24 h</option>
          <option value="7d">Conserver 7 jours</option>
          <option value="30d">Conserver 30 jours</option>
        </select>

        <div style={{ fontWeight: 700, marginBottom: 4 }}>Partage</div>
        <p className="muted-text" style={{ margin: '0 0 14px', fontSize: 13 }}>
          Vous contrôlez les personnes pouvant recevoir vos informations.
        </p>

        {!confirmDeleteOpen ? (
          <button
            type="button"
            className="btn btn-outline-danger btn-full"
            onClick={() => setConfirmDeleteOpen(true)}
          >
            <Trash2 size={16} /> {t('settings.deleteAll')}
          </button>
        ) : (
          <div className="card" style={{ borderColor: 'var(--red-border)', background: 'var(--red-soft)' }}>
            <div style={{ fontWeight: 700, fontSize: 14, marginBottom: 8 }}>
              Confirmer la suppression définitive de toutes vos données locales ?
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <button
                type="button"
                className="btn btn-danger"
                style={{ flex: 1 }}
                onClick={() => {
                  deleteAllUserData();
                  setConfirmDeleteOpen(false);
                }}
              >
                Oui, supprimer
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setConfirmDeleteOpen(false)}
              >
                Annuler
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Developer / Demo Simulation */}
      <div className="section-title">{t('settings.demo')} (Paramètres développeur)</div>
      <div className="card">
        <label style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 14, marginBottom: 12 }}>
          <span>Activer le Mode simulation (DEMO MODE)</span>
          <input
            type="checkbox"
            checked={demo.enabled}
            onChange={(e) => setDemoState({ enabled: e.target.checked })}
          />
        </label>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 12 }}>
          <div>
            <label className="field-label">Tolérance retard (min)</label>
            <input
              type="number"
              min={0}
              max={60}
              className="input"
              value={detectionConfig.gracePeriodMinutes}
              onChange={(e) =>
                setDetectionConfig({ gracePeriodMinutes: Math.max(0, Number(e.target.value) || 0) })
              }
            />
          </div>
          <div>
            <label className="field-label">Seuil arrêt (min)</label>
            <input
              type="number"
              min={1}
              max={120}
              className="input"
              value={detectionConfig.stationaryThresholdMinutes}
              onChange={(e) =>
                setDetectionConfig({
                  stationaryThresholdMinutes: Math.max(1, Number(e.target.value) || 1),
                })
              }
            />
          </div>
        </div>

        <div style={{ display: 'flex', gap: 8 }}>
          <button
            type="button"
            className="btn btn-secondary"
            style={{ flex: 1, fontSize: 13 }}
            onClick={() => setDemoDrawerOpen(true)}
          >
            Ouvrir simulateur
          </button>
          <button
            type="button"
            className="btn btn-warning"
            style={{ flex: 1, fontSize: 13 }}
            disabled={!activeSession}
            onClick={() => triggerDemoAnomaly('DELAY_WARNING')}
          >
            <AlertTriangle size={15} /> Déclencher une anomalie
          </button>
        </div>
      </div>

      {/* Regional Emergency & Preferences */}
      <div className="section-title">Préférences & Sécurité régionale</div>
      <div className="card">
        <label className="field-label">Pays (numéros d’urgence)</label>
        <select
          className="input"
          value={country}
          onChange={(e) => setCountry(e.target.value as CountryCode)}
          style={{ marginBottom: 12 }}
        >
          {Object.values(EMERGENCY_NUMBERS_BY_COUNTRY).map((cfg) => (
            <option key={cfg.country} value={cfg.country}>
              {cfg.countryName} — Urgence {cfg.primaryNumber}
            </option>
          ))}
        </select>

        <label className="field-label">{t('settings.language')}</label>
        <select
          className="input"
          value={language}
          onChange={(e) => handleLangChange(e.target.value as SupportedLanguage)}
          style={{ marginBottom: 12 }}
        >
          {(Object.keys(LANGUAGE_LABELS) as SupportedLanguage[]).map((code) => (
            <option key={code} value={code}>
              {LANGUAGE_LABELS[code]}
            </option>
          ))}
        </select>

        <button
          type="button"
          className="btn btn-secondary btn-full"
          style={{ marginBottom: 8 }}
          onClick={toggleDarkMode}
        >
          {darkMode ? <Sun size={17} /> : <Moon size={17} />}
          {darkMode ? 'Passer en mode clair' : 'Passer en mode sombre (luminosité faible)'}
        </button>

        <button type="button" className="btn btn-secondary btn-full" onClick={replayOnboarding}>
          <Shield size={17} /> Revoir la présentation SAFE
        </button>
      </div>

      {/* Account / Auth MVP */}
      <div className="section-title">Compte & Authentification (MVP)</div>
      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 10 }}>
          <User size={18} />
          <div>
            <div style={{ fontWeight: 700, fontSize: 14 }}>{user.displayName}</div>
            <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
              {user.email || 'Session locale hors compte'} ({user.provider})
            </div>
          </div>
        </div>

        {authNotice && (
          <div className="disclaimer-box" style={{ marginBottom: 10 }}>
            {authNotice}
          </div>
        )}

        <form
          style={{ display: 'flex', gap: 8, marginBottom: 8 }}
          onSubmit={async (e) => {
            e.preventDefault();
            if (!emailInput.trim()) return;
            await signInEmail(emailInput);
            setEmailInput('');
            setAuthNotice('Connecté par email (mode MVP).');
          }}
        >
          <input
            type="email"
            className="input"
            placeholder="email@exemple.com"
            value={emailInput}
            onChange={(e) => setEmailInput(e.target.value)}
          />
          <button type="submit" className="btn btn-secondary">
            Email
          </button>
        </form>

        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <button
            type="button"
            className="btn btn-secondary"
            style={{ flex: 1, fontSize: 13 }}
            onClick={async () => {
              await signInGoogle();
              setAuthNotice('Compte Google connecté (mode démo).');
            }}
          >
            Google Sign-In
          </button>
          <button
            type="button"
            className="btn btn-secondary"
            style={{ flex: 1, fontSize: 13 }}
            onClick={async () => {
              const msg = await signInAppleStub();
              setAuthNotice(msg);
            }}
          >
            Apple Sign-In
          </button>
          {user.signedIn && (
            <button
              type="button"
              className="btn btn-outline-danger"
              style={{ fontSize: 13 }}
              onClick={signOut}
            >
              Déconnexion
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
