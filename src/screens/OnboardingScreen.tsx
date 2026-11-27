import React, { useState } from 'react';
import { ArrowRight, Lock, ShieldCheck, Sliders, Users } from 'lucide-react';
import { useI18n } from '../i18n/I18nContext';
import { useSafe } from '../state/SafeContext';

export const OnboardingScreen: React.FC = () => {
  const { completeOnboarding } = useSafe();
  const { t } = useI18n();
  const [step, setStep] = useState(0);

  const slides = [
    {
      icon: <ShieldCheck size={42} color="#10B981" />,
      title: t('onboarding.1.title'),
      body: t('onboarding.1.body'),
      note: t('app.disclaimer.complementary'),
    },
    {
      icon: <Sliders size={42} color="#10B981" />,
      title: t('onboarding.2.title'),
      body: t('onboarding.2.body'),
      note: 'Vous décidez quand démarrer, mettre en pause ou arrêter chaque session.',
    },
    {
      icon: <Users size={42} color="#10B981" />,
      title: t('onboarding.3.title'),
      body: t('onboarding.3.body'),
      note: 'Chaque contact doit accepter explicitement son rôle de contact de confiance.',
    },
    {
      icon: <Lock size={42} color="#10B981" />,
      title: t('onboarding.4.title'),
      body: t('onboarding.4.body'),
      note: 'Aucun suivi permanent. Vous pouvez effacer vos données à tout moment.',
    },
  ];

  const current = slides[step];
  const isLast = step === slides.length - 1;

  return (
    <div className="full-overlay" style={{ padding: 24, justifyContent: 'space-between' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div className="safe-brand">
          <div className="safe-logo-mark">S</div>
          <div>
            <div className="safe-brand-title">SAFE</div>
            <div className="safe-brand-sub">{t('app.subtitle')}</div>
          </div>
        </div>
        {!isLast && (
          <button
            type="button"
            className="btn btn-secondary"
            style={{ minHeight: 38, padding: '6px 14px', fontSize: 13 }}
            onClick={completeOnboarding}
          >
            {t('onboarding.skip')}
          </button>
        )}
      </div>

      <div style={{ textAlign: 'center', padding: '20px 8px' }}>
        <div
          style={{
            width: 92,
            height: 92,
            borderRadius: 28,
            background: 'var(--green-soft)',
            border: '1px solid var(--green-border)',
            display: 'grid',
            placeItems: 'center',
            margin: '0 auto 24px',
          }}
        >
          {current.icon}
        </div>
        <h1 style={{ fontSize: 26, fontWeight: 800, margin: '0 0 12px', lineHeight: 1.2 }}>
          {current.title}
        </h1>
        <p style={{ fontSize: 17, color: 'var(--text-secondary)', margin: '0 0 18px', lineHeight: 1.5 }}>
          {current.body}
        </p>
        <div className="disclaimer-box">{current.note}</div>
      </div>

      <div>
        <div style={{ display: 'flex', justifyContent: 'center', gap: 8, marginBottom: 20 }}>
          {slides.map((_, idx) => (
            <span
              key={idx}
              style={{
                width: idx === step ? 24 : 8,
                height: 8,
                borderRadius: 999,
                background: idx === step ? 'var(--green-500)' : 'var(--border-subtle)',
                transition: 'width 0.2s ease',
              }}
            />
          ))}
        </div>

        <button
          type="button"
          className="btn btn-primary-green btn-full"
          style={{ minHeight: 56, fontSize: 17 }}
          onClick={() => (isLast ? completeOnboarding() : setStep((s) => s + 1))}
        >
          {isLast ? t('onboarding.start') : t('onboarding.next')}
          <ArrowRight size={18} />
        </button>
      </div>
    </div>
  );
};
