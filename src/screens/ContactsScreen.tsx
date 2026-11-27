import React, { useState } from 'react';
import { Edit2, Plus, Star, Trash2, UserCheck } from 'lucide-react';
import { useI18n } from '../i18n/I18nContext';
import { useSafe } from '../state/SafeContext';
import { TrustedContact } from '../types/safe';

export const ContactsScreen: React.FC = () => {
  const { contacts, addContact, updateContact, deleteContact, setPrimaryContact } = useSafe();
  const { t } = useI18n();

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<TrustedContact | null>(null);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [accepted, setAccepted] = useState(true);

  const openAdd = () => {
    setEditing(null);
    setFirstName('');
    setLastName('');
    setPhone('');
    setEmail('');
    setAccepted(true);
    setFormOpen(true);
  };

  const openEdit = (c: TrustedContact) => {
    setEditing(c);
    setFirstName(c.firstName);
    setLastName(c.lastName);
    setPhone(c.phone);
    setEmail(c.email ?? '');
    setAccepted(c.acceptedByContact);
    setFormOpen(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!firstName.trim() || !phone.trim() || !accepted) return;
    if (editing) {
      updateContact(editing.id, {
        firstName,
        lastName,
        phone,
        email,
        acceptedByContact: accepted,
      });
    } else {
      addContact({
        firstName,
        lastName,
        phone,
        email,
        acceptedByContact: accepted,
      });
    }
    setFormOpen(false);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
        <h1 style={{ margin: 0, fontSize: 24, fontWeight: 800 }}>{t('contacts.title')}</h1>
        <button
          type="button"
          className="btn btn-primary-green"
          style={{ minHeight: 42, padding: '8px 14px', fontSize: 14 }}
          onClick={openAdd}
        >
          <Plus size={16} /> {t('contacts.add')}
        </button>
      </div>

      {contacts.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: 24 }}>
          <p className="muted-text" style={{ margin: '0 0 12px' }}>
            Aucun contact de confiance enregistré.
          </p>
          <button type="button" className="btn btn-primary-green" onClick={openAdd}>
            <Plus size={16} /> {t('contacts.add')}
          </button>
        </div>
      ) : (
        contacts.map((c) => (
          <div key={c.id} className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 10 }}>
              <div>
                <div style={{ fontWeight: 700, fontSize: 17 }}>
                  {c.firstName} {c.lastName}
                </div>
                <div style={{ fontSize: 13, color: 'var(--text-secondary)', marginTop: 2 }}>
                  {c.phone} {c.email ? `· ${c.email}` : ''}
                </div>
              </div>
              <span className={c.isPrimary ? 'pill pill-green' : 'pill pill-neutral'}>
                {c.isPrimary ? t('contacts.primary') : t('contacts.secondary')}
              </span>
            </div>

            <div style={{ fontSize: 12, color: 'var(--green-600)', marginTop: 8, display: 'flex', alignItems: 'center', gap: 5 }}>
              <UserCheck size={14} /> {t('contacts.accepted')}
            </div>

            <div style={{ display: 'flex', gap: 8, marginTop: 12, flexWrap: 'wrap' }}>
              {!c.isPrimary && (
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ minHeight: 38, padding: '6px 12px', fontSize: 12 }}
                  onClick={() => setPrimaryContact(c.id)}
                >
                  <Star size={14} /> {t('contacts.setPrimary')}
                </button>
              )}
              <button
                type="button"
                className="btn btn-secondary"
                style={{ minHeight: 38, padding: '6px 12px', fontSize: 12 }}
                onClick={() => openEdit(c)}
              >
                <Edit2 size={14} /> {t('contacts.edit')}
              </button>
              <button
                type="button"
                className="btn btn-outline-danger"
                style={{ minHeight: 38, padding: '6px 12px', fontSize: 12 }}
                onClick={() => deleteContact(c.id)}
              >
                <Trash2 size={14} /> {t('contacts.delete')}
              </button>
            </div>
          </div>
        ))
      )}

      {formOpen && (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <form className="modal-sheet" onSubmit={handleSubmit}>
            <h2 style={{ margin: '0 0 14px', fontSize: 20 }}>
              {editing ? t('contacts.edit') : t('contacts.add')}
            </h2>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 10 }}>
              <div>
                <label className="field-label">Prénom</label>
                <input
                  className="input"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                />
              </div>
              <div>
                <label className="field-label">Nom</label>
                <input
                  className="input"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                />
              </div>
            </div>

            <label className="field-label">Téléphone</label>
            <input
              className="input"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="+33 6 12 34 56 78"
              required
              style={{ marginBottom: 10 }}
            />

            <label className="field-label">Email (optionnel)</label>
            <input
              className="input"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              style={{ marginBottom: 12 }}
            />

            <label style={{ display: 'flex', gap: 8, alignItems: 'center', fontSize: 13, marginBottom: 16 }}>
              <input
                type="checkbox"
                checked={accepted}
                onChange={(e) => setAccepted(e.target.checked)}
                required
              />
              <span>Le contact a explicitement accepté d’être contact de confiance.</span>
            </label>

            <div style={{ display: 'flex', gap: 10 }}>
              <button type="submit" className="btn btn-primary-green" style={{ flex: 1 }}>
                Enregistrer
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => setFormOpen(false)}>
                Annuler
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
