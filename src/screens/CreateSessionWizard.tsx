import React, { useEffect, useState } from 'react';
import { ArrowLeft, Check, MapPin, Plus, Shield, UserCheck, X } from 'lucide-react';
import { ACTIVITY_META } from '../components/ActivityIcon';
import { SafeMapView } from '../components/SafeMapView';
import { useI18n } from '../i18n/I18nContext';
import { TranslationKey } from '../i18n/translations';
import { useSafe } from '../state/SafeContext';
import { ActivityType, RouteDestination } from '../types/safe';

const ACTIVITY_CHOICES: ActivityType[] = [
  'walk_home',
  'walk',
  'car',
  'bike',
  'hike',
  'activity',
  'travel',
  'other',
];

const QUICK_DURATIONS = [15, 20, 30, 60, 120, 240];

function formatDuration(mins: number): string {
  if (mins < 60) return `${mins} min`;
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return m === 0 ? `${h} h` : `${h} h ${m}`;
}

export const CreateSessionWizard: React.FC = () => {
  const {
    wizardOpen,
    wizardPresetActivity,
    closeWizard,
    presetDestinations,
    resolveDestination,
    contacts,
    addContact,
    startNewSession,
  } = useSafe();
  const { t } = useI18n();

  const [step, setStep] = useState(1);
  const [activity, setActivity] = useState<ActivityType>('walk_home');
  const [destQuery, setDestQuery] = useState('Maison');
  const [resolvedDest, setResolvedDest] = useState<RouteDestination>(presetDestinations[0]);
  const [durationMinutes, setDurationMinutes] = useState<number>(20);
  const [customDurationMode, setCustomDurationMode] = useState(false);
  const [selectedContactId, setSelectedContactId] = useState<string | null>(
    contacts.find((c) => c.isPrimary)?.id ?? contacts[0]?.id ?? null
  );

  // Inline add contact form
  const [showAddContact, setShowAddContact] = useState(false);
  const [newFirst, setNewFirst] = useState('');
  const [newLast, setNewLast] = useState('');
  const [newPhone, setNewPhone] = useState('');
  const [newEmail, setNewEmail] = useState('');
  const [newAccepted, setNewAccepted] = useState(true);

  useEffect(() => {
    if (wizardOpen) {
      setStep(1);
      const initialAct = wizardPresetActivity ?? 'walk_home';
      setActivity(initialAct);
      if (initialAct === 'walk_home') {
        setDestQuery('Maison');
        setResolvedDest(presetDestinations[0]);
        setDurationMinutes(20);
      } else if (initialAct === 'hike') {
        setDestQuery('Sentier des Crêtes');
        setResolvedDest(presetDestinations[3] ?? presetDestinations[0]);
        setDurationMinutes(120);
      } else if (initialAct === 'car') {
        setDestQuery('Travail');
        setResolvedDest(presetDestinations[1] ?? presetDestinations[0]);
        setDurationMinutes(30);
      }
      setSelectedContactId(contacts.find((c) => c.isPrimary)?.id ?? contacts[0]?.id ?? null);
    }
  }, [wizardOpen, wizardPresetActivity]);

  if (!wizardOpen) return null;

  const handleSelectDestination = async (value: string) => {
    setDestQuery(value);
    const route = await resolveDestination(value);
    setResolvedDest(route);
  };

  const handleArrivalTimeInput = (hhmm: string) => {
    const [hStr, mStr] = hhmm.split(':');
    const h = Number(hStr);
    const m = Number(mStr);
    if (Number.isNaN(h) || Number.isNaN(m)) return;
    const now = new Date();
    const target = new Date(now);
    target.setHours(h, m, 0, 0);
    if (target.getTime() <= now.getTime()) {
      target.setDate(target.getDate() + 1);
    }
    const diffMins = Math.max(5, Math.round((target.getTime() - now.getTime()) / 60_000));
    setDurationMinutes(diffMins);
  };

  const arrivalTimePreview = new Date(Date.now() + durationMinutes * 60_000).toLocaleTimeString('fr-FR', {
    hour: '2-digit',
    minute: '2-digit',
  });

  const selectedContact = contacts.find((c) => c.id === selectedContactId) ?? null;

  const handleAddInlineContact = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newFirst.trim() || !newPhone.trim() || !newAccepted) return;
    const created = addContact({
      firstName: newFirst,
      lastName: newLast,
      phone: newPhone,
      email: newEmail,
      acceptedByContact: newAccepted,
    });
    setSelectedContactId(created.id);
    setShowAddContact(false);
    setNewFirst('');
    setNewLast('');
    setNewPhone('');
    setNewEmail('');
  };

  const handleStart = () => {
    const title = t(ACTIVITY_META[activity].key as TranslationKey);
    startNewSession({
      activityType: activity,
      title,
      originLabel: t('wizard.step2.currentPos'),
      destination: resolvedDest,
      expectedDurationMinutes: durationMinutes,
      contactId: selectedContactId,
    });
  };

  return (
    <div className="full-overlay" role="dialog" aria-modal="true" aria-label="Créer une session SAFE">
      {/* Header */}
      <div className="safe-header" style={{ borderBottom: '1px solid var(--border-subtle)' }}>
        <button
          type="button"
          className="icon-btn"
          onClick={() => (step > 1 ? setStep((s) => s - 1) : closeWizard())}
          aria-label={t('wizard.back')}
        >
          <ArrowLeft size={18} />
        </button>
        <div style={{ fontWeight: 700, fontSize: 15 }}>
          {t('wizard.step')} {step} / 5
        </div>
        <button type="button" className="icon-btn" onClick={closeWizard} aria-label="Fermer">
          <X size={18} />
        </button>
      </div>

      {/* Progress bar */}
      <div style={{ height: 4, background: 'var(--border-subtle)' }}>
        <div
          style={{
            height: '100%',
            width: `${(step / 5) * 100}%`,
            background: 'var(--green-500)',
            transition: 'width 0.25s ease',
          }}
        />
      </div>

      <div style={{ flex: 1, overflowY: 'auto', padding: 20 }}>
        {/* STEP 1: Activity */}
        {step === 1 && (
          <div>
            <h2 style={{ margin: '0 0 16px', fontSize: 23 }}>{t('wizard.step1.title')}</h2>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
              {ACTIVITY_CHOICES.map((act) => {
                const meta = ACTIVITY_META[act];
                const active = activity === act;
                return (
                  <button
                    key={act}
                    type="button"
                    className="shortcut-card"
                    style={{
                      minHeight: 74,
                      borderColor: active ? 'var(--green-500)' : 'var(--border-subtle)',
                      background: active ? 'var(--green-soft)' : 'var(--bg-surface)',
                    }}
                    onClick={() => {
                      setActivity(act);
                      setStep(2);
                    }}
                  >
                    <span style={{ fontSize: 22 }} aria-hidden="true">{meta.emoji}</span>
                    <span style={{ fontWeight: 700, fontSize: 14 }}>
                      {t(meta.key as TranslationKey)}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* STEP 2: Destination */}
        {step === 2 && (
          <div>
            <h2 style={{ margin: '0 0 12px', fontSize: 23 }}>{t('wizard.step2.title')}</h2>
            <label className="field-label" htmlFor="dest-input">
              Destination
            </label>
            <input
              id="dest-input"
              className="input"
              value={destQuery}
              placeholder={t('wizard.step2.placeholder')}
              onChange={(e) => handleSelectDestination(e.target.value)}
              style={{ marginBottom: 10 }}
            />

            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 14 }}>
              {presetDestinations.map((p) => (
                <button
                  key={p.id}
                  type="button"
                  className="btn btn-secondary"
                  style={{
                    minHeight: 38,
                    padding: '6px 12px',
                    fontSize: 13,
                    borderColor: destQuery === p.label ? 'var(--green-500)' : 'var(--border-subtle)',
                  }}
                  onClick={() => handleSelectDestination(p.label)}
                >
                  <MapPin size={14} /> {p.label}
                </button>
              ))}
            </div>

            <div className="card" style={{ marginBottom: 14 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: 14 }}>
                <span style={{ color: 'var(--text-muted)' }}>{t('wizard.step2.depart')}</span>
                <strong>{t('wizard.step2.currentPos')}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14 }}>
                <span style={{ color: 'var(--text-muted)' }}>{t('wizard.step2.arrivee')}</span>
                <strong>{resolvedDest.label}</strong>
              </div>
            </div>

            <SafeMapView
              destination={resolvedDest}
              currentPosition={resolvedDest.origin}
              progressPercent={0}
              compact
            />

            <p className="disclaimer-box" style={{ marginTop: 12 }}>
              {t('wizard.step2.mapNotice')}
            </p>
          </div>
        )}

        {/* STEP 3: Duration */}
        {step === 3 && (
          <div>
            <h2 style={{ margin: '0 0 14px', fontSize: 23 }}>{t('wizard.step3.title')}</h2>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10, marginBottom: 14 }}>
              {QUICK_DURATIONS.map((mins) => {
                const selected = !customDurationMode && durationMinutes === mins;
                return (
                  <button
                    key={mins}
                    type="button"
                    className="btn btn-secondary"
                    style={{
                      borderColor: selected ? 'var(--green-500)' : 'var(--border-subtle)',
                      background: selected ? 'var(--green-soft)' : 'var(--bg-surface)',
                    }}
                    onClick={() => {
                      setCustomDurationMode(false);
                      setDurationMinutes(mins);
                    }}
                  >
                    {formatDuration(mins)}
                  </button>
                );
              })}
            </div>

            <button
              type="button"
              className="btn btn-secondary btn-full"
              style={{
                marginBottom: 14,
                borderColor: customDurationMode ? 'var(--green-500)' : 'var(--border-subtle)',
              }}
              onClick={() => setCustomDurationMode((v) => !v)}
            >
              {t('wizard.step3.custom')} ({formatDuration(durationMinutes)})
            </button>

            {customDurationMode && (
              <div className="card" style={{ marginBottom: 14 }}>
                <label className="field-label" htmlFor="custom-mins">
                  Durée en minutes
                </label>
                <input
                  id="custom-mins"
                  type="number"
                  min={5}
                  max={720}
                  className="input"
                  value={durationMinutes}
                  onChange={(e) => setDurationMinutes(Math.max(5, Number(e.target.value) || 15))}
                />
              </div>
            )}

            <div className="card">
              <label className="field-label" htmlFor="eta-input">
                {t('wizard.step3.arrivalAt')} : <strong>{arrivalTimePreview}</strong>
              </label>
              <input
                id="eta-input"
                type="time"
                className="input"
                value={arrivalTimePreview}
                onChange={(e) => handleArrivalTimeInput(e.target.value)}
              />
            </div>
          </div>
        )}

        {/* STEP 4: Trusted Contact */}
        {step === 4 && (
          <div>
            <h2 style={{ margin: '0 0 14px', fontSize: 22 }}>{t('wizard.step4.title')}</h2>

            <button
              type="button"
              className="card"
              style={{
                width: '100%',
                textAlign: 'left',
                marginBottom: 10,
                cursor: 'pointer',
                borderColor: selectedContactId === null ? 'var(--green-500)' : 'var(--border-subtle)',
                background: selectedContactId === null ? 'var(--green-soft)' : 'var(--bg-surface)',
              }}
              onClick={() => setSelectedContactId(null)}
            >
              <div style={{ fontWeight: 700 }}>{t('wizard.step4.none')}</div>
              <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                Utiliser uniquement les rappels personnels sur cet appareil.
              </div>
            </button>

            {contacts.map((c) => {
              const selected = selectedContactId === c.id;
              return (
                <button
                  key={c.id}
                  type="button"
                  className="card"
                  style={{
                    width: '100%',
                    textAlign: 'left',
                    marginBottom: 10,
                    cursor: 'pointer',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    borderColor: selected ? 'var(--green-500)' : 'var(--border-subtle)',
                    background: selected ? 'var(--green-soft)' : 'var(--bg-surface)',
                  }}
                  onClick={() => setSelectedContactId(c.id)}
                >
                  <div>
                    <div style={{ fontWeight: 700, fontSize: 16 }}>
                      {c.firstName} {c.lastName}
                    </div>
                    <div style={{ fontSize: 13, color: 'var(--text-secondary)' }}>
                      Contact de confiance · {c.phone}
                    </div>
                  </div>
                  {selected && <Check size={20} color="#059669" />}
                </button>
              );
            })}

            {!showAddContact ? (
              <button
                type="button"
                className="btn btn-secondary btn-full"
                onClick={() => setShowAddContact(true)}
              >
                <Plus size={18} /> {t('wizard.step4.add')}
              </button>
            ) : (
              <form className="card" onSubmit={handleAddInlineContact}>
                <div style={{ fontWeight: 700, marginBottom: 10 }}>Nouveau contact de confiance</div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 8 }}>
                  <input
                    className="input"
                    placeholder="Prénom"
                    value={newFirst}
                    onChange={(e) => setNewFirst(e.target.value)}
                    required
                  />
                  <input
                    className="input"
                    placeholder="Nom"
                    value={newLast}
                    onChange={(e) => setNewLast(e.target.value)}
                  />
                </div>
                <input
                  className="input"
                  placeholder="Téléphone (ex. +33 6 ...)"
                  value={newPhone}
                  onChange={(e) => setNewPhone(e.target.value)}
                  required
                  style={{ marginBottom: 8 }}
                />
                <input
                  className="input"
                  placeholder="Email (optionnel)"
                  type="email"
                  value={newEmail}
                  onChange={(e) => setNewEmail(e.target.value)}
                  style={{ marginBottom: 10 }}
                />
                <label style={{ display: 'flex', gap: 8, alignItems: 'center', fontSize: 13, marginBottom: 12 }}>
                  <input
                    type="checkbox"
                    checked={newAccepted}
                    onChange={(e) => setNewAccepted(e.target.checked)}
                    required
                  />
                  <span>
                    <UserCheck size={14} style={{ verticalAlign: 'middle' }} /> Ce proche a explicitement accepté d’être contact de confiance.
                  </span>
                </label>
                <div style={{ display: 'flex', gap: 8 }}>
                  <button type="submit" className="btn btn-primary-green" style={{ flex: 1 }}>
                    Enregistrer
                  </button>
                  <button type="button" className="btn btn-secondary" onClick={() => setShowAddContact(false)}>
                    Annuler
                  </button>
                </div>
              </form>
            )}
          </div>
        )}

        {/* STEP 5: Summary */}
        {step === 5 && (
          <div>
            <h2 style={{ margin: '0 0 14px', fontSize: 23 }}>{t('wizard.step5.title')}</h2>

            <div className="card" style={{ marginBottom: 14 }}>
              <div style={{ display: 'grid', rowGap: 12, fontSize: 14 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>{t('wizard.step5.activity')}</span>
                  <strong>
                    {ACTIVITY_META[activity].emoji} {t(ACTIVITY_META[activity].key as TranslationKey)}
                  </strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>{t('wizard.step5.start')}</span>
                  <strong>{t('wizard.step2.currentPos')}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>{t('wizard.step5.destination')}</span>
                  <strong>{resolvedDest.label}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>{t('wizard.step5.duration')}</span>
                  <strong>{formatDuration(durationMinutes)}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>{t('wizard.step5.contact')}</span>
                  <strong>
                    {selectedContact ? selectedContact.firstName : t('wizard.step4.none')}
                  </strong>
                </div>
              </div>
            </div>

            <div className="card" style={{ marginBottom: 16 }}>
              <div style={{ fontWeight: 700, fontSize: 14, marginBottom: 8 }}>{t('wizard.step5.detection')}</div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                {['Position', 'Progression', 'Temps', 'Arrêt prolongé', 'Déviation importante'].map((sig) => (
                  <span key={sig} className="pill pill-green">
                    ✓ {sig}
                  </span>
                ))}
              </div>
            </div>

            <button
              type="button"
              className="btn btn-primary-green btn-full"
              style={{ minHeight: 58, fontSize: 17, fontWeight: 800 }}
              onClick={handleStart}
            >
              <Shield size={20} /> 🟢 {t('wizard.step5.startBtn')}
            </button>

            <p className="muted-text" style={{ fontSize: 12, textAlign: 'center', marginTop: 10 }}>
              {t('wizard.step5.consent')}
            </p>
          </div>
        )}
      </div>

      {step < 5 && (
        <div style={{ padding: 16, borderTop: '1px solid var(--border-subtle)', background: 'var(--bg-surface)' }}>
          <button
            type="button"
            className="btn btn-primary-green btn-full"
            onClick={() => setStep((s) => s + 1)}
          >
            {t('wizard.next')}
          </button>
        </div>
      )}
    </div>
  );
};
