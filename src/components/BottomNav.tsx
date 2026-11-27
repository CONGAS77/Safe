import React from 'react';
import { ClipboardList, Home, Settings, Shield, Users } from 'lucide-react';
import { useI18n } from '../i18n/I18nContext';
import { useSafe } from '../state/SafeContext';
import { TabId } from '../types/safe';

export const BottomNav: React.FC = () => {
  const { activeTab, setActiveTab, activeSession, openWizard } = useSafe();
  const { t } = useI18n();

  const handleSafeTap = () => {
    if (activeSession) {
      setActiveTab('safe');
    } else {
      openWizard();
    }
  };

  const navClass = (tab: TabId) => `nav-item ${activeTab === tab ? 'active' : ''}`;

  const safeBtnClass =
    activeSession?.state === 'ESCALATED'
      ? 'nav-item nav-safe-center active-sos'
      : activeSession?.state === 'WARNING'
      ? 'nav-item nav-safe-center active-warning'
      : 'nav-item nav-safe-center';

  return (
    <nav className="bottom-nav" aria-label="Navigation principale">
      <button type="button" className={navClass('home')} onClick={() => setActiveTab('home')}>
        <Home size={20} />
        <span>{t('nav.home')}</span>
      </button>

      <button type="button" className={navClass('contacts')} onClick={() => setActiveTab('contacts')}>
        <Users size={20} />
        <span>{t('nav.contacts')}</span>
      </button>

      <button type="button" className={safeBtnClass} onClick={handleSafeTap} aria-label="SAFE">
        <Shield size={22} />
        <span>{t('nav.safe')}</span>
      </button>

      <button type="button" className={navClass('activity')} onClick={() => setActiveTab('activity')}>
        <ClipboardList size={20} />
        <span>{t('nav.activity')}</span>
      </button>

      <button type="button" className={navClass('settings')} onClick={() => setActiveTab('settings')}>
        <Settings size={20} />
        <span>{t('nav.settings')}</span>
      </button>
    </nav>
  );
};
