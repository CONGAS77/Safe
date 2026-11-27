import React, { useState } from 'react';
import { Check, MapPin, Phone, Send, ShieldAlert, X } from 'lucide-react';
import { useI18n } from '../i18n/I18nContext';
import { useSafe } from '../state/SafeContext';
import { SafeMapView } from './SafeMapView';

export const AlertProtocolModal: React.FC = () => {
  const { alertModalOpen, alertDraft, closeAlertModal, alertService, country, activeSession } = useSafe();
  const { t } = useI18n();
  const [confirmEmergencyOpen, setConfirmEmergencyOpen] = useState(false);
  const [contactSentPreview, setContactSentPreview] = useState(false);
  const [sharePreviewOpen, setSharePreviewOpen] = useState(false);
  const [sharedConfirmed, setSharedConfirmed] = useState(false);
  const [callTriggeredMsg, setCallTriggeredMsg] = useState<string | null>(null);

  if (!alertModalOpen || !alertDraft) return null;

  const emergency = alertService.getRegionalEmergencyInfo(country);
  const shareText = alertService.prepareShareSummary(alertDraft.lastPosition, alertDraft.sessionTitle);

  const handleClose = () => {
    setConfirmEmergencyOpen(false);
    setContactSentPreview(false);
    setSharePreviewOpen(false);
    setSharedConfirmed(false);
    setCallTriggeredMsg(null);
    closeAlertModal();
  };

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="sos-modal-title">
      <div className="modal-sheet">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <span className="pill pill-red">
            <ShieldAlert size={14} /> SOS / PROTOCOLE D’ALERTE
          </span>
          <button type="button" className="icon-btn" onClick={handleClose} aria-label="Fermer">
            <X size={18} />
          </button>
        </div>

        <h2 id="sos-modal-title" style={{ margin: '0 0 6px', fontSize: 22 }}>
          {t('sos.title')}
        </h2>
        <p className="muted-text" style={{ margin: '0 0 16px' }}>
          {t('sos.disclaimer')}
        </p>

        {callTriggeredMsg && (
          <div className="card" style={{ borderColor: 'var(--red-border)', background: 'var(--red-soft)', marginBottom: 12 }}>
            <strong>{callTriggeredMsg}</strong>
          </div>
        )}

        {confirmEmergencyOpen ? (
          <div className="card" style={{ borderColor: 'var(--red-border)', background: 'var(--red-soft)', marginBottom: 12 }}>
            <div style={{ fontWeight: 700, fontSize: 16, marginBottom: 6 }}>
              Confirmer l’appel vers les secours ({emergency.countryName})
            </div>
            <p className="muted-text" style={{ margin: '0 0 12px' }}>
              Numéro d’urgence principal : <strong>{emergency.primaryNumber}</strong> (Police {emergency.policeNumber} · Médical {emergency.medicalNumber} · Pompiers {emergency.fireNumber}).
            </p>
            <div style={{ display: 'flex', gap: 10 }}>
              <a
                href={`tel:${emergency.primaryNumber}`}
                className="btn btn-danger"
                style={{ flex: 1, textDecoration: 'none' }}
                onClick={() => {
                  const res = alertService.triggerConfirmedEmergencyCall(emergency.primaryNumber);
                  setCallTriggeredMsg(`Appel préparé vers ${emergency.primaryNumber} (${res.telUri}).`);
                  setConfirmEmergencyOpen(false);
                }}
              >
                <Phone size={18} /> Appeler {emergency.primaryNumber}
              </a>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setConfirmEmergencyOpen(false)}
              >
                Retour
              </button>
            </div>
          </div>
        ) : (
          <button
            type="button"
            className="btn btn-danger btn-full"
            style={{ marginBottom: 10 }}
            onClick={() => setConfirmEmergencyOpen(true)}
          >
            <Phone size={18} /> 📞 {t('sos.callEmergency')} ({emergency.primaryNumber})
          </button>
        )}

        <button
          type="button"
          className="btn btn-warning btn-full"
          style={{ marginBottom: 10 }}
          onClick={() => {
            setContactSentPreview((v) => !v);
            setSharePreviewOpen(false);
          }}
        >
          <Send size={18} /> 👤 {t('sos.contactClose')}
          {alertDraft.contact ? ` (${alertDraft.contact.firstName})` : ''}
        </button>

        {contactSentPreview && (
          <div className="card" style={{ marginBottom: 10, borderColor: 'var(--orange-border)' }}>
            <div style={{ fontWeight: 700, marginBottom: 8 }}>
              Aperçu du message envoyé au contact
            </div>
            <div
              style={{
                background: 'var(--bg-surface-muted)',
                padding: 12,
                borderRadius: 12,
                fontSize: 13,
                lineHeight: 1.5,
                whiteSpace: 'pre-line',
                marginBottom: 10,
              }}
            >
              {alertDraft.messageBody}
            </div>
            {activeSession && (
              <div style={{ marginBottom: 10 }}>
                <SafeMapView
                  destination={activeSession.destination}
                  currentPosition={alertDraft.lastPosition}
                  progressPercent={activeSession.progressPercent}
                  compact
                />
              </div>
            )}
            <button
              type="button"
              className="btn btn-primary-green btn-full"
              onClick={() => setSharedConfirmed(true)}
            >
              <Check size={18} /> {sharedConfirmed ? 'Message préparé / envoyé (démo)' : 'Confirmer l’envoi au proche'}
            </button>
          </div>
        )}

        <button
          type="button"
          className="btn btn-secondary btn-full"
          style={{ marginBottom: 10 }}
          onClick={() => {
            setSharePreviewOpen((v) => !v);
            setContactSentPreview(false);
          }}
        >
          <MapPin size={18} /> 📍 {t('sos.shareLocation')}
        </button>

        {sharePreviewOpen && (
          <div className="card" style={{ marginBottom: 10 }}>
            <div style={{ fontWeight: 700, fontSize: 14, marginBottom: 6 }}>
              Informations partagées uniquement avec votre accord :
            </div>
            <pre
              style={{
                margin: '0 0 10px',
                padding: 10,
                background: 'var(--bg-surface-muted)',
                borderRadius: 10,
                fontSize: 12,
                whiteSpace: 'pre-wrap',
              }}
            >
              {shareText}
            </pre>
            <button
              type="button"
              className="btn btn-primary-green btn-full"
              onClick={() => {
                if (typeof navigator !== 'undefined' && navigator.clipboard) {
                  navigator.clipboard.writeText(shareText).catch(() => {});
                }
                setSharedConfirmed(true);
              }}
            >
              <Check size={18} /> {sharedConfirmed ? 'Position prête à être partagée' : 'Confirmer le partage de position'}
            </button>
          </div>
        )}

        <button type="button" className="btn btn-secondary btn-full" onClick={handleClose}>
          ❌ {t('sos.cancel')}
        </button>
      </div>
    </div>
  );
};
