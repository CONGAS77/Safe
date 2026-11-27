import React from 'react';
import { Bell, Moon, Sun, X } from 'lucide-react';
import { AlertProtocolModal } from './components/AlertProtocolModal';
import { AnomalyModal } from './components/AnomalyModal';
import { BottomNav } from './components/BottomNav';
import { DemoControlDrawer } from './components/DemoControlDrawer';
import { I18nProvider, useI18n } from './i18n/I18nContext';
import { ActiveSessionScreen } from './screens/ActiveSessionScreen';
import { ActivityScreen } from './screens/ActivityScreen';
import { CheckInModal } from './screens/CheckInModal';
import { ContactsScreen } from './screens/ContactsScreen';
import { CreateSessionWizard } from './screens/CreateSessionWizard';
import { HomeScreen } from './screens/HomeScreen';
import { OnboardingScreen } from './screens/OnboardingScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { SafeProvider, useSafe } from './state/SafeContext';

const SafeShell: React.FC = () => {
  const {
    activeTab,
    onboardingDone,
    darkMode,
    toggleDarkMode,
    activeBanner,
    dismissBanner,
    notifications,
    markAllNotificationsRead,
  } = useSafe();
  const { t } = useI18n();

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <div className="safe-viewport">
      <div className="safe-shell">
        {/* Top App Bar */}
        <header className="safe-header">
          <div className="safe-brand">
            <div className="safe-logo-mark">S</div>
            <div>
              <div className="safe-brand-title">{t('app.name')}</div>
              <div className="safe-brand-sub">{t('app.subtitle')}</div>
            </div>
          </div>

          <div className="safe-header-actions">
            <button
              type="button"
              className="icon-btn"
              onClick={toggleDarkMode}
              aria-label="Changer le thème"
            >
              {darkMode ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <button
              type="button"
              className="icon-btn"
              onClick={markAllNotificationsRead}
              aria-label="Notifications"
              style={{ position: 'relative' }}
            >
              <Bell size={18} />
              {unreadCount > 0 && (
                <span
                  style={{
                    position: 'absolute',
                    top: 6,
                    right: 6,
                    width: 8,
                    height: 8,
                    borderRadius: 99,
                    background: 'var(--green-500)',
                  }}
                />
              )}
            </button>
          </div>
        </header>

        {/* Live in-app notification toast */}
        {activeBanner && (
          <div
            className={`toast-banner ${
              activeBanner.category === 'delay' ||
              activeBanner.category === 'checkin_due' ||
              activeBanner.category === 'checkin_unconfirmed'
                ? 'warn'
                : ''
            }`}
            role="status"
          >
            <div>
              <div style={{ fontWeight: 700, fontSize: 13 }}>{activeBanner.title}</div>
              <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{activeBanner.body}</div>
            </div>
            <button
              type="button"
              className="icon-btn"
              style={{ width: 30, height: 30 }}
              onClick={dismissBanner}
              aria-label="Fermer la notification"
            >
              <X size={14} />
            </button>
          </div>
        )}

        {/* Main Screen Content */}
        <main className="safe-content">
          {activeTab === 'home' && <HomeScreen />}
          {activeTab === 'safe' && <ActiveSessionScreen />}
          {activeTab === 'contacts' && <ContactsScreen />}
          {activeTab === 'activity' && <ActivityScreen />}
          {activeTab === 'settings' && <SettingsScreen />}
        </main>

        {/* Bottom Navigation */}
        <BottomNav />

        {/* Flows & Modals */}
        {!onboardingDone && <OnboardingScreen />}
        <CreateSessionWizard />
        <CheckInModal />
        <AnomalyModal />
        <AlertProtocolModal />
        <DemoControlDrawer />
      </div>
    </div>
  );
};

const AppWithI18n: React.FC = () => {
  const { language, setLanguage } = useSafe();
  return (
    <I18nProvider initialLanguage={language} onLanguageChange={setLanguage}>
      <SafeShell />
    </I18nProvider>
  );
};

export function App() {
  return (
    <SafeProvider>
      <AppWithI18n />
    </SafeProvider>
  );
}

export default App;
