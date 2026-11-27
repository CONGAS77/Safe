import React, { useState } from 'react';
import { Clock, X } from 'lucide-react';
import { useI18n } from '../i18n/I18nContext';
import { useSafe } from '../state/SafeContext';

function defaultTimePlusMinutes(mins: number): string {
  const d = new Date(Date.now() + mins * 60_000);
  const hh = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  return `${hh}:${mm}`;
}

export const CheckInModal: React.FC = () => {
  const {
    checkInModalOpen,
    closeCheckInModal,
    contacts,
    scheduleCheckIn,
    buildDefaultCheckInMessage,
  } = useSafe();
  const { t } = useI18n();

  const initialTime = defaultTimePlusMinutes(30);
  const [timeStr, setTimeStr] = useState(initialTime);
  const [contactId, setContactId] = useState<string>(
    contacts.find((c) => c.isPrimary)?.id ?? contacts[0]?.id ?? ''
  );
  const [message, setMessage] = useState(() => buildDefaultCheckInMessage(initialTime));

  if (!checkInModalOpen) return null;

  const handleTimeChange = (nextTime: string) => {
    setTimeStr(nextTime);
    setMessage(buildDefaultCheckInMessage(nextTime));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const [h, m] = timeStr.split(':').map(Number);
    const due = new Date();
    due.setHours(h || 23, m || 30, 0, 0);
    if (due.getTime() <= Date.now()) {
      due.setDate(due.getDate() + 1);
    }
    scheduleCheckIn(due.getTime(), contactId || null, message);
  };

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-label="Programmer un Check-in">
      <div className="modal-sheet">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <span className="pill pill-green">
            <Clock size={14} /> {t('checkin.title')}
          </span>
          <button type="button" className="icon-btn" onClick={closeCheckInModal} aria-label="Fermer">
            <X size={18} />
          </button>
        </div>

        <p className="muted-text" style={{ margin: '0 0 14px' }}>
          {t('checkin.subtitle')}
        </p>

        <form onSubmit={handleSubmit}>
          <label className="field-label" htmlFor="checkin-time">
            {t('checkin.dueTime')}
          </label>
          <input
            id="checkin-time"
            type="time"
            className="input"
            value={timeStr}
            onChange={(e) => handleTimeChange(e.target.value)}
            style={{ marginBottom: 12 }}
            required
          />

          <label className="field-label" htmlFor="checkin-contact">
            {t('checkin.contact')}
          </label>
          <select
            id="checkin-contact"
            className="input"
            value={contactId}
            onChange={(e) => setContactId(e.target.value)}
            style={{ marginBottom: 12 }}
          >
            <option value="">Aucun contact (rappel personnel)</option>
            {contacts.map((c) => (
              <option key={c.id} value={c.id}>
                {c.firstName} {c.lastName}
              </option>
            ))}
          </select>

          <label className="field-label" htmlFor="checkin-msg">
            {t('checkin.message')}
          </label>
          <textarea
            id="checkin-msg"
            className="input"
            rows={3}
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            style={{ minHeight: 88, resize: 'vertical', marginBottom: 16 }}
            required
          />

          <button type="submit" className="btn btn-primary-green btn-full">
            {t('checkin.scheduleBtn')}
          </button>
        </form>
      </div>
    </div>
  );
};
