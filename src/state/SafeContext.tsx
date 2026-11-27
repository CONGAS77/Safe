import React, { createContext, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { APP_ENV, DEFAULT_DETECTION_CONFIG } from '../config/env';
import { evaluateSessionDetection } from '../engine/detectionEngine';
import { DefaultAlertService } from '../services/alertService';
import { MockAuthService } from '../services/authService';
import { DefaultCheckInService } from '../services/checkInService';
import { ContactInput, DefaultContactService } from '../services/contactService';
import {
  GoogleMapsRouteProvider,
  MockLocationService,
  MockMapRouteProvider,
} from '../services/locationService';
import { DefaultNotificationService } from '../services/notificationService';
import { createDemoHistory, DEMO_CONTACTS, DEMO_USER_PROFILE } from '../services/seedData';
import { CreateSessionInput, DefaultSessionService } from '../services/sessionService';
import { LocalEncryptedStorageService } from '../services/storageService';
import {
  ActivityType,
  AlertDraftPayload,
  AnomalyType,
  CheckIn,
  CountryCode,
  DemoSimulationState,
  DetectionConfig,
  PrivacySettings,
  RetentionPolicy,
  RouteDestination,
  SafeNotification,
  SafeSession,
  SessionHistoryItem,
  SupportedLanguage,
  TabId,
  TrustedContact,
  UserProfile,
} from '../types/safe';

interface SafeContextValue {
  activeTab: TabId;
  setActiveTab: (tab: TabId) => void;
  onboardingDone: boolean;
  completeOnboarding: () => void;
  replayOnboarding: () => void;
  darkMode: boolean;
  toggleDarkMode: () => void;
  language: SupportedLanguage;
  setLanguage: (lang: SupportedLanguage) => void;
  country: CountryCode;
  setCountry: (country: CountryCode) => void;

  // User & Auth
  user: UserProfile;
  signInEmail: (email: string, name?: string) => Promise<void>;
  signInGoogle: () => Promise<void>;
  signInAppleStub: () => Promise<string>;
  signOut: () => Promise<void>;

  // Contacts
  contacts: TrustedContact[];
  addContact: (input: ContactInput) => TrustedContact;
  updateContact: (id: string, input: ContactInput) => void;
  deleteContact: (id: string) => void;
  setPrimaryContact: (id: string) => void;

  // Wizard & Route
  wizardOpen: boolean;
  wizardPresetActivity: ActivityType | null;
  openWizard: (presetActivity?: ActivityType) => void;
  closeWizard: () => void;
  presetDestinations: RouteDestination[];
  resolveDestination: (query: string) => Promise<RouteDestination>;

  // Active Session
  activeSession: SafeSession | null;
  lastCompletedSummary: SessionHistoryItem | null;
  clearCompletedSummary: () => void;
  startNewSession: (input: CreateSessionInput) => void;
  pauseOrResumeSession: () => void;
  extendActiveSession: (minutes: number) => void;
  updateActiveDestination: (query: string) => Promise<void>;
  confirmAnomalyOk: () => void;
  completeActiveSession: () => void;
  cancelActiveSession: () => void;

  // Check-ins
  checkIns: CheckIn[];
  activeCheckIn: CheckIn | null;
  checkInModalOpen: boolean;
  openCheckInModal: () => void;
  closeCheckInModal: () => void;
  scheduleCheckIn: (dueAt: number, contactId: string | null, message: string) => void;
  buildDefaultCheckInMessage: (hhmm: string) => string;
  confirmCheckInOk: (id: string) => void;
  escalateCheckIn: (id: string) => void;
  cancelCheckIn: (id: string) => void;

  // Alert / SOS Protocol
  alertModalOpen: boolean;
  alertDraft: AlertDraftPayload | null;
  openSosProtocol: (reason?: AlertDraftPayload['reason'], customMsg?: string) => void;
  closeAlertModal: () => void;
  alertService: DefaultAlertService;

  // History & Privacy
  history: SessionHistoryItem[];
  historyLoading: boolean;
  privacy: PrivacySettings;
  setLocationPermission: (granted: boolean) => void;
  setRetentionPolicy: (policy: RetentionPolicy) => void;
  deleteAllUserData: () => void;

  // Notifications
  notifications: SafeNotification[];
  activeBanner: SafeNotification | null;
  dismissBanner: () => void;
  markAllNotificationsRead: () => void;

  // Demo & Detection Engine
  demo: DemoSimulationState;
  setDemoState: (patch: Partial<DemoSimulationState>) => void;
  detectionConfig: DetectionConfig;
  setDetectionConfig: (patch: Partial<DetectionConfig>) => void;
  triggerDemoAnomaly: (type: AnomalyType) => void;
  simulateArrival: () => void;
  simulateCheckInDueNow: () => void;
  simulateCheckInExpiredNow: () => void;
  demoDrawerOpen: boolean;
  setDemoDrawerOpen: (open: boolean) => void;
}

const SafeContext = createContext<SafeContextValue | null>(null);

const storage = new LocalEncryptedStorageService();
const locationService = new MockLocationService();
const mapProvider = APP_ENV.googleMapsApiKey
  ? new GoogleMapsRouteProvider(APP_ENV.googleMapsApiKey)
  : new MockMapRouteProvider();
const sessionService = new DefaultSessionService();
const checkInService = new DefaultCheckInService();
const alertService = new DefaultAlertService();
const contactService = new DefaultContactService();
const notificationService = new DefaultNotificationService();
const authService = new MockAuthService();

export const SafeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [activeTab, setActiveTab] = useState<TabId>('home');
  const [onboardingDone, setOnboardingDone] = useState<boolean>(() =>
    storage.load<boolean>('onboardingDone', false)
  );
  const [darkMode, setDarkMode] = useState<boolean>(() => storage.load<boolean>('darkMode', false));
  const [language, setLanguageState] = useState<SupportedLanguage>(() =>
    storage.load<SupportedLanguage>('language', 'fr')
  );
  const [country, setCountryState] = useState<CountryCode>(() =>
    storage.load<CountryCode>('country', APP_ENV.defaultCountry)
  );
  const [user, setUser] = useState<UserProfile>(() =>
    storage.load<UserProfile>('user', DEMO_USER_PROFILE)
  );
  const [contacts, setContacts] = useState<TrustedContact[]>(() =>
    storage.load<TrustedContact[]>('contacts', DEMO_CONTACTS)
  );
  const [privacy, setPrivacy] = useState<PrivacySettings>(() =>
    storage.load<PrivacySettings>('privacy', {
      locationPermissionGranted: true,
      retentionPolicy: '30d',
      shareOnlyOnConsent: true,
    })
  );
  const [history, setHistory] = useState<SessionHistoryItem[]>(() => {
    const initial = storage.load<SessionHistoryItem[]>('history', createDemoHistory());
    return storage.applyHistoryRetention(initial, storage.load<PrivacySettings>('privacy', {
      locationPermissionGranted: true,
      retentionPolicy: '30d',
      shareOnlyOnConsent: true,
    }).retentionPolicy);
  });
  const [historyLoading] = useState(false);

  const [activeSession, setActiveSession] = useState<SafeSession | null>(() =>
    storage.load<SafeSession | null>('activeSession', null)
  );
  const [lastCompletedSummary, setLastCompletedSummary] = useState<SessionHistoryItem | null>(null);
  const [checkIns, setCheckIns] = useState<CheckIn[]>(() =>
    storage.load<CheckIn[]>('checkIns', [])
  );
  const [notifications, setNotifications] = useState<SafeNotification[]>([]);
  const [activeBanner, setActiveBanner] = useState<SafeNotification | null>(null);

  const [wizardOpen, setWizardOpen] = useState(false);
  const [wizardPresetActivity, setWizardPresetActivity] = useState<ActivityType | null>(null);
  const [checkInModalOpen, setCheckInModalOpen] = useState(false);
  const [alertModalOpen, setAlertModalOpen] = useState(false);
  const [alertDraft, setAlertDraft] = useState<AlertDraftPayload | null>(null);
  const [demoDrawerOpen, setDemoDrawerOpen] = useState(false);

  const [detectionConfig, setDetectionConfigState] = useState<DetectionConfig>(() =>
    storage.load<DetectionConfig>('detectionConfig', DEFAULT_DETECTION_CONFIG)
  );
  const [demo, setDemo] = useState<DemoSimulationState>({
    enabled: true,
    simulatedElapsedMinutes: null,
    simulatedProgressPercent: null,
    simulatedStationaryMinutes: 0,
    simulatedDeviationMeters: 0,
    lowBattery: false,
    offline: false,
    inaccurateGps: false,
  });

  const arrivingSoonNotifiedRef = useRef<string | null>(null);

  const pushNotification = (
    category: SafeNotification['category'],
    title: string,
    body: string
  ) => {
    const notif = notificationService.createNotification(category, title, body);
    setNotifications((prev) => [notif, ...prev].slice(0, 30));
    setActiveBanner(notif);
  };

  // Persist key state locally
  useEffect(() => storage.save('onboardingDone', onboardingDone), [onboardingDone]);
  useEffect(() => storage.save('darkMode', darkMode), [darkMode]);
  useEffect(() => storage.save('language', language), [language]);
  useEffect(() => storage.save('country', country), [country]);
  useEffect(() => storage.save('user', user), [user]);
  useEffect(() => storage.save('contacts', contacts), [contacts]);
  useEffect(() => storage.save('privacy', privacy), [privacy]);
  useEffect(() => storage.save('history', history), [history]);
  useEffect(() => storage.save('activeSession', activeSession), [activeSession]);
  useEffect(() => storage.save('checkIns', checkIns), [checkIns]);
  useEffect(() => storage.save('detectionConfig', detectionConfig), [detectionConfig]);

  // Apply dark mode attribute to root document
  useEffect(() => {
    if (typeof document !== 'undefined') {
      document.documentElement.dataset.theme = darkMode ? 'dark' : 'light';
    }
  }, [darkMode]);

  // Auto-dismiss notification banner after 5.5s
  useEffect(() => {
    if (!activeBanner) return;
    const id = setTimeout(() => setActiveBanner(null), 5500);
    return () => clearTimeout(id);
  }, [activeBanner]);

  // Real-time session + check-in clock & detection engine tick
  useEffect(() => {
    const interval = setInterval(() => {
      const now = Date.now();

      // Evaluate active session updates
      setActiveSession((prev) => {
        if (!prev) return null;
        if (prev.state === 'COMPLETED' || prev.state === 'CANCELLED' || prev.state === 'EXPIRED') {
          return prev;
        }

        const realElapsedMs =
          prev.startedAt && prev.state !== 'PAUSED'
            ? Math.max(0, now - prev.startedAt - prev.totalPausedMs)
            : prev.elapsedMinutes * 60_000;
        const realElapsedMinutes = realElapsedMs / 60_000;

        const elapsedMinutes =
          demo.enabled && demo.simulatedElapsedMinutes !== null
            ? demo.simulatedElapsedMinutes
            : realElapsedMinutes;

        const autoProgress =
          prev.expectedDurationMinutes > 0
            ? Math.min(98, Math.round((elapsedMinutes / prev.expectedDurationMinutes) * 100))
            : 0;
        const progressPercent =
          demo.enabled && demo.simulatedProgressPercent !== null
            ? demo.simulatedProgressPercent
            : Math.max(prev.progressPercent, autoProgress);

        const stationaryMinutes = demo.enabled
          ? demo.simulatedStationaryMinutes
          : prev.stationaryMinutes;
        const deviationMeters = demo.enabled
          ? demo.simulatedDeviationMeters
          : prev.deviationMeters;

        const currentPosition = privacy.locationPermissionGranted
          ? locationService.interpolatePositionAlongRoute(
              prev.destination,
              progressPercent,
              deviationMeters,
              demo.inaccurateGps
            )
          : prev.currentPosition;

        const updated: SafeSession = {
          ...prev,
          elapsedMinutes,
          progressPercent,
          stationaryMinutes,
          deviationMeters,
          currentPosition,
          lastSignalAt: demo.offline ? prev.lastSignalAt : now,
        };

        // Arriving soon notification (~10 min remaining)
        const remaining = updated.expectedDurationMinutes - updated.elapsedMinutes;
        if (
          updated.state === 'ACTIVE' &&
          remaining <= 10 &&
          remaining > 0 &&
          arrivingSoonNotifiedRef.current !== updated.id
        ) {
          arrivingSoonNotifiedRef.current = updated.id;
          pushNotification(
            'arriving_soon',
            'Arrivée prévue bientôt',
            'Votre arrivée est prévue dans environ 10 minutes.'
          );
        }

        const evaluation = evaluateSessionDetection(updated, detectionConfig);
        if (evaluation.anomalyType && updated.state === 'ACTIVE') {
          const withAnomaly = sessionService.triggerAnomaly(
            updated,
            evaluation.anomalyType,
            evaluation.reason ?? '',
            now
          );
          if (evaluation.anomalyType === 'DELAY_WARNING') {
            pushNotification('delay', 'Retard', 'Votre session dure plus longtemps que prévu.');
          }
          return withAnomaly;
        }
        return updated;
      });

      // Evaluate scheduled / due check-ins
      setCheckIns((prevList) => {
        let changed = false;
        const nextList = prevList.map((chk) => {
          if (chk.status === 'SCHEDULED' && now >= chk.dueAt) {
            changed = true;
            pushNotification('checkin_due', 'Check-in', 'Tout va bien ?');
            return checkInService.markDue(chk, detectionConfig.checkInResponseGraceMinutes, now);
          }
          if (chk.status === 'DUE' && chk.graceExpiresAt && now >= chk.graceExpiresAt) {
            changed = true;
            pushNotification(
              'checkin_unconfirmed',
              'Check-in non confirmé',
              'Votre confirmation est attendue.'
            );
            const contact = contacts.find((c) => c.id === chk.contactId) ?? null;
            setAlertDraft(
              alertService.buildContactMessageDraft({
                contact,
                session: null,
                customMessage: chk.message,
                reason: 'CHECKIN_EXPIRED',
              })
            );
            setAlertModalOpen(true);
            return checkInService.escalate(chk);
          }
          return chk;
        });
        return changed ? nextList : prevList;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [demo, detectionConfig, privacy.locationPermissionGranted, contacts]);

  const presetDestinations = useMemo(() => mapProvider.getPresetDestinations(), []);

  const resolveDestination = async (query: string) => {
    return mapProvider.resolveDestination(query);
  };

  const openWizard = (presetActivity?: ActivityType) => {
    setWizardPresetActivity(presetActivity ?? null);
    setWizardOpen(true);
  };

  const closeWizard = () => {
    setWizardOpen(false);
    setWizardPresetActivity(null);
  };

  const startNewSession = (input: CreateSessionInput) => {
    const draft = sessionService.createSession(input);
    const started = sessionService.startSession(draft);
    arrivingSoonNotifiedRef.current = null;
    setDemo((prev) => ({
      ...prev,
      simulatedElapsedMinutes: null,
      simulatedProgressPercent: null,
      simulatedStationaryMinutes: 0,
      simulatedDeviationMeters: 0,
    }));
    setActiveSession(started);
    setLastCompletedSummary(null);
    setWizardOpen(false);
    setActiveTab('safe');
    pushNotification('session_started', 'Session commencée', 'SAFE est maintenant actif.');
  };

  const pauseOrResumeSession = () => {
    setActiveSession((prev) => {
      if (!prev) return null;
      if (prev.state === 'PAUSED') return sessionService.resumeSession(prev);
      if (prev.state === 'ACTIVE' || prev.state === 'WARNING') {
        return sessionService.pauseSession(prev);
      }
      return prev;
    });
  };

  const extendActiveSession = (minutes: number) => {
    setDemo((prev) => ({
      ...prev,
      simulatedStationaryMinutes: 0,
      simulatedDeviationMeters: 0,
    }));
    setActiveSession((prev) => (prev ? sessionService.extendSession(prev, minutes) : null));
  };

  const updateActiveDestination = async (query: string) => {
    const resolved = await resolveDestination(query);
    setActiveSession((prev) => (prev ? sessionService.updateDestination(prev, resolved) : null));
  };

  const confirmAnomalyOk = () => {
    setDemo((prev) => ({
      ...prev,
      simulatedStationaryMinutes: 0,
      simulatedDeviationMeters: 0,
    }));
    setActiveSession((prev) => (prev ? sessionService.confirmUserOk(prev) : null));
  };

  const completeActiveSession = () => {
    setActiveSession((prev) => {
      if (!prev) return null;
      const completed = sessionService.completeSession(prev);
      const contact = contacts.find((c) => c.id === completed.contactId) ?? null;
      const keepLoc = privacy.retentionPolicy !== 'none' && privacy.locationPermissionGranted;
      const item = sessionService.toHistoryItem(completed, contact, keepLoc);
      setHistory((hist) =>
        storage.applyHistoryRetention([item, ...hist], privacy.retentionPolicy)
      );
      setLastCompletedSummary(item);
      return null;
    });
    setDemo((prev) => ({
      ...prev,
      simulatedElapsedMinutes: null,
      simulatedProgressPercent: null,
      simulatedStationaryMinutes: 0,
      simulatedDeviationMeters: 0,
    }));
  };

  const cancelActiveSession = () => {
    setActiveSession((prev) => {
      if (!prev) return null;
      const cancelled = sessionService.cancelSession(prev);
      const contact = contacts.find((c) => c.id === cancelled.contactId) ?? null;
      const item = sessionService.toHistoryItem(cancelled, contact, false);
      setHistory((hist) =>
        storage.applyHistoryRetention([item, ...hist], privacy.retentionPolicy)
      );
      return null;
    });
    setActiveTab('home');
  };

  const scheduleCheckIn = (dueAt: number, contactId: string | null, message: string) => {
    const created = checkInService.createCheckIn({ dueAt, contactId, message });
    setCheckIns((prev) => [created, ...prev]);
    setCheckInModalOpen(false);
    pushNotification(
      'system',
      'Check-in programmé',
      `Confirmation attendue à ${new Date(dueAt).toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit',
      })}.`
    );
  };

  const confirmCheckInOk = (id: string) => {
    setCheckIns((prev) =>
      prev.map((c) => (c.id === id ? checkInService.confirmOk(c) : c))
    );
  };

  const escalateCheckIn = (id: string) => {
    const target = checkIns.find((c) => c.id === id);
    setCheckIns((prev) =>
      prev.map((c) => (c.id === id ? checkInService.escalate(c) : c))
    );
    const contact = contacts.find((c) => c.id === target?.contactId) ?? contacts.find((c) => c.isPrimary) ?? null;
    setAlertDraft(
      alertService.buildContactMessageDraft({
        contact,
        session: null,
        customMessage: target?.message,
        reason: 'CHECKIN_EXPIRED',
      })
    );
    setAlertModalOpen(true);
  };

  const cancelCheckIn = (id: string) => {
    setCheckIns((prev) =>
      prev.map((c) => (c.id === id ? checkInService.cancel(c) : c))
    );
  };

  const openSosProtocol = (
    reason: AlertDraftPayload['reason'] = 'SOS',
    customMsg?: string
  ) => {
    const contact =
      contacts.find((c) => c.id === activeSession?.contactId) ??
      contacts.find((c) => c.isPrimary) ??
      contacts[0] ??
      null;

    if (activeSession && (reason === 'SOS' || reason === 'SESSION_ANOMALY')) {
      setActiveSession((prev) =>
        prev && prev.state !== 'ESCALATED'
          ? sessionService.escalateSession(prev, 'Protocole d’alerte déclenché par l’utilisateur')
          : prev
      );
    }

    setAlertDraft(
      alertService.buildContactMessageDraft({
        contact,
        session: activeSession,
        customMessage: customMsg,
        reason,
      })
    );
    setAlertModalOpen(true);
  };

  const triggerDemoAnomaly = (type: AnomalyType) => {
    if (!activeSession) return;
    if (type === 'DELAY_WARNING') {
      const targetElapsed = activeSession.expectedDurationMinutes + detectionConfig.gracePeriodMinutes + 10;
      setDemo((prev) => ({
        ...prev,
        enabled: true,
        simulatedElapsedMinutes: Math.max(35, targetElapsed),
      }));
      setActiveSession((prev) =>
        prev
          ? sessionService.triggerAnomaly(
              { ...prev, elapsedMinutes: Math.max(35, targetElapsed) },
              'DELAY_WARNING',
              'Simulation manuelle : retard détecté'
            )
          : null
      );
      pushNotification('delay', 'Retard', 'Votre session dure plus longtemps que prévu.');
    } else if (type === 'STATIONARY_WARNING') {
      setDemo((prev) => ({
        ...prev,
        enabled: true,
        simulatedStationaryMinutes: detectionConfig.stationaryThresholdMinutes + 2,
      }));
      setActiveSession((prev) =>
        prev
          ? sessionService.triggerAnomaly(
              { ...prev, stationaryMinutes: detectionConfig.stationaryThresholdMinutes + 2 },
              'STATIONARY_WARNING',
              'Simulation manuelle : arrêt prolongé'
            )
          : null
      );
    } else if (type === 'ROUTE_DEVIATION_WARNING') {
      setDemo((prev) => ({
        ...prev,
        enabled: true,
        simulatedDeviationMeters: detectionConfig.routeDeviationMeters + 150,
      }));
      setActiveSession((prev) =>
        prev
          ? sessionService.triggerAnomaly(
              { ...prev, deviationMeters: detectionConfig.routeDeviationMeters + 150 },
              'ROUTE_DEVIATION_WARNING',
              'Simulation manuelle : changement d’itinéraire'
            )
          : null
      );
    }
  };

  const simulateArrival = () => {
    setDemo((prev) => ({
      ...prev,
      enabled: true,
      simulatedProgressPercent: 100,
    }));
    setActiveSession((prev) =>
      prev
        ? {
            ...prev,
            progressPercent: 100,
            currentPosition: prev.destination.destination,
            activeAnomaly: null,
            state: prev.state === 'WARNING' ? 'ACTIVE' : prev.state,
          }
        : null
    );
  };

  const simulateCheckInDueNow = () => {
    const dueChk = checkIns.find((c) => c.status === 'SCHEDULED');
    if (!dueChk) return;
    setCheckIns((prev) =>
      prev.map((c) =>
        c.id === dueChk.id
          ? checkInService.markDue(c, detectionConfig.checkInResponseGraceMinutes)
          : c
      )
    );
    pushNotification('checkin_due', 'Check-in', 'Tout va bien ?');
  };

  const simulateCheckInExpiredNow = () => {
    const target = checkIns.find((c) => c.status === 'SCHEDULED' || c.status === 'DUE');
    if (!target) return;
    escalateCheckIn(target.id);
    pushNotification(
      'checkin_unconfirmed',
      'Check-in non confirmé',
      'Votre confirmation est attendue.'
    );
  };

  const activeCheckIn = useMemo(
    () =>
      checkIns.find((c) => c.status === 'DUE' || c.status === 'SCHEDULED') ?? null,
    [checkIns]
  );

  const deleteAllUserData = () => {
    storage.clearAll();
    setActiveSession(null);
    setLastCompletedSummary(null);
    setCheckIns([]);
    setHistory([]);
    setContacts([]);
    setNotifications([]);
    setActiveBanner(null);
    setPrivacy({
      locationPermissionGranted: false,
      retentionPolicy: 'none',
      shareOnlyOnConsent: true,
    });
    pushNotification('system', 'Données supprimées', 'Toutes vos données locales ont été effacées.');
  };

  const value: SafeContextValue = {
    activeTab,
    setActiveTab,
    onboardingDone,
    completeOnboarding: () => setOnboardingDone(true),
    replayOnboarding: () => setOnboardingDone(false),
    darkMode,
    toggleDarkMode: () => setDarkMode((d) => !d),
    language,
    setLanguage: setLanguageState,
    country,
    setCountry: setCountryState,
    user,
    signInEmail: async (email, name) => setUser(await authService.signInWithEmail(email, name)),
    signInGoogle: async () => setUser(await authService.signInWithGoogle()),
    signInAppleStub: async () => {
      try {
        await authService.signInWithApple();
        return '';
      } catch (err) {
        return err instanceof Error ? err.message : 'Apple Sign-In non disponible';
      }
    },
    signOut: async () => setUser(await authService.signOut()),
    contacts,
    addContact: (input) => {
      const next = contactService.createContact(contacts, input);
      setContacts(next);
      return next[next.length - 1];
    },
    updateContact: (id, input) =>
      setContacts((prev) => contactService.updateContact(prev, id, input)),
    deleteContact: (id) =>
      setContacts((prev) => contactService.deleteContact(prev, id)),
    setPrimaryContact: (id) =>
      setContacts((prev) => contactService.setPrimaryContact(prev, id)),
    wizardOpen,
    wizardPresetActivity,
    openWizard,
    closeWizard,
    presetDestinations,
    resolveDestination,
    activeSession,
    lastCompletedSummary,
    clearCompletedSummary: () => setLastCompletedSummary(null),
    startNewSession,
    pauseOrResumeSession,
    extendActiveSession,
    updateActiveDestination,
    confirmAnomalyOk,
    completeActiveSession,
    cancelActiveSession,
    checkIns,
    activeCheckIn,
    checkInModalOpen,
    openCheckInModal: () => setCheckInModalOpen(true),
    closeCheckInModal: () => setCheckInModalOpen(false),
    scheduleCheckIn,
    buildDefaultCheckInMessage: (hhmm) => checkInService.buildDefaultMessage(hhmm),
    confirmCheckInOk,
    escalateCheckIn,
    cancelCheckIn,
    alertModalOpen,
    alertDraft,
    openSosProtocol,
    closeAlertModal: () => setAlertModalOpen(false),
    alertService,
    history,
    historyLoading,
    privacy,
    setLocationPermission: (granted) =>
      setPrivacy((p) => ({ ...p, locationPermissionGranted: granted })),
    setRetentionPolicy: (policy) => {
      setPrivacy((p) => ({ ...p, retentionPolicy: policy }));
      setHistory((h) => storage.applyHistoryRetention(h, policy));
    },
    deleteAllUserData,
    notifications,
    activeBanner,
    dismissBanner: () => setActiveBanner(null),
    markAllNotificationsRead: () =>
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true }))),
    demo,
    setDemoState: (patch) => setDemo((prev) => ({ ...prev, ...patch })),
    detectionConfig,
    setDetectionConfig: (patch) => setDetectionConfigState((prev) => ({ ...prev, ...patch })),
    triggerDemoAnomaly,
    simulateArrival,
    simulateCheckInDueNow,
    simulateCheckInExpiredNow,
    demoDrawerOpen,
    setDemoDrawerOpen,
  };

  return <SafeContext.Provider value={value}>{children}</SafeContext.Provider>;
};

export function useSafe(): SafeContextValue {
  const ctx = useContext(SafeContext);
  if (!ctx) throw new Error('useSafe must be used within SafeProvider');
  return ctx;
}
