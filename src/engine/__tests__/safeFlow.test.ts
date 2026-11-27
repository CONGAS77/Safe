import { describe, expect, it } from 'vitest';
import { DEFAULT_DETECTION_CONFIG } from '../../config/env';
import { DefaultAlertService } from '../../services/alertService';
import { DefaultCheckInService } from '../../services/checkInService';
import { DefaultContactService } from '../../services/contactService';
import { MockLocationService, MockMapRouteProvider } from '../../services/locationService';
import { DEMO_CONTACTS } from '../../services/seedData';
import { DefaultSessionService } from '../../services/sessionService';
import { LocalEncryptedStorageService } from '../../services/storageService';
import { evaluateSessionDetection } from '../detectionEngine';
import { canTransitionSessionState, transitionSessionState } from '../sessionStateMachine';

describe('SAFE State Machine & Detection Engine', () => {
  it('enforces valid session state transitions', () => {
    expect(canTransitionSessionState('DRAFT', 'READY')).toBe(true);
    expect(canTransitionSessionState('READY', 'ACTIVE')).toBe(true);
    expect(canTransitionSessionState('ACTIVE', 'WARNING')).toBe(true);
    expect(canTransitionSessionState('WARNING', 'USER_CONFIRMED')).toBe(true);
    expect(canTransitionSessionState('COMPLETED', 'ACTIVE')).toBe(false);
    expect(() => transitionSessionState('COMPLETED', 'ACTIVE')).toThrow();
  });

  it('runs the full required user scenario cleanly', async () => {
    const mapProvider = new MockMapRouteProvider();
    const sessionService = new DefaultSessionService();
    const storage = new LocalEncryptedStorageService();

    // 1. Choose "Retour à pied", "Maison", 20 minutes, Thomas
    const destination = await mapProvider.resolveDestination('Maison');
    const thomas = DEMO_CONTACTS.find((c) => c.firstName === 'Thomas')!;
    expect(thomas).toBeDefined();

    const draft = sessionService.createSession({
      activityType: 'walk_home',
      title: 'Retour à pied',
      originLabel: 'Position actuelle',
      destination,
      expectedDurationMinutes: 20,
      contactId: thomas.id,
    });
    expect(draft.state).toBe('READY');

    // 2. Start session
    const active = sessionService.startSession(draft, 1_000_000);
    expect(active.state).toBe('ACTIVE');

    // 3. Simulate 35 minutes elapsed -> trigger delay anomaly
    const delayed = { ...active, elapsedMinutes: 35 };
    const evalResult = evaluateSessionDetection(delayed, DEFAULT_DETECTION_CONFIG);
    expect(evalResult.anomalyType).toBe('DELAY_WARNING');

    const warned = sessionService.triggerAnomaly(delayed, evalResult.anomalyType!, evalResult.reason ?? '', 1_000_000 + 35 * 60_000);
    expect(warned.state).toBe('WARNING');
    expect(warned.activeAnomaly?.type).toBe('DELAY_WARNING');

    // 4. User answers "Oui, tout va bien" -> continue
    const resumed = sessionService.confirmUserOk(warned, 1_000_000 + 36 * 60_000);
    expect(resumed.state).toBe('ACTIVE');
    expect(resumed.activeAnomaly).toBeNull();

    // 5. Simulate arrival & complete session -> save to history
    const arrived = { ...resumed, progressPercent: 100 };
    const completed = sessionService.completeSession(arrived, 1_000_000 + 38 * 60_000);
    expect(completed.state).toBe('COMPLETED');
    expect(completed.progressPercent).toBe(100);

    const summary = sessionService.toHistoryItem(completed, thomas, true);
    expect(summary.title).toBe('Retour à pied');
    expect(summary.destinationLabel).toBe('Maison');
    expect(summary.endState).toBe('COMPLETED');
    expect(summary.anomaliesCount).toBe(1);

    const retained = storage.applyHistoryRetention([summary], '30d', 1_000_000 + 38 * 60_000);
    expect(retained).toHaveLength(1);
    const wiped = storage.applyHistoryRetention([summary], 'none');
    expect(wiped).toHaveLength(0);
  });

  it('ignores route deviation when GPS signal is inaccurate', async () => {
    const mapProvider = new MockMapRouteProvider();
    const locationService = new MockLocationService();
    const sessionService = new DefaultSessionService();

    const dest = await mapProvider.resolveDestination('Maison');
    const started = sessionService.startSession(
      sessionService.createSession({
        activityType: 'walk_home',
        title: 'Retour à pied',
        originLabel: 'Position actuelle',
        destination: dest,
        expectedDurationMinutes: 20,
        contactId: null,
      })
    );

    const inaccuratePoint = locationService.interpolatePositionAlongRoute(dest, 50, 400, true);
    const sessionWithBadGps = {
      ...started,
      deviationMeters: 400,
      currentPosition: inaccuratePoint,
    };

    const result = evaluateSessionDetection(sessionWithBadGps, DEFAULT_DETECTION_CONFIG);
    expect(result.anomalyType).toBeNull();
    expect(result.gpsImpreciseIgnored).toBe(true);
  });

  it('handles check-ins, contacts, and non-alarmist alert drafts', () => {
    const checkInSvc = new DefaultCheckInService();
    const contactSvc = new DefaultContactService();
    const alertSvc = new DefaultAlertService();

    const msg = checkInSvc.buildDefaultMessage('23:30');
    expect(msg).toContain('23:30');

    const chk = checkInSvc.createCheckIn({
      dueAt: 2_000_000,
      contactId: DEMO_CONTACTS[0].id,
      message: msg,
    });
    const due = checkInSvc.markDue(chk, 3, 2_000_000);
    expect(due.status).toBe('DUE');

    const contacts = contactSvc.createContact(DEMO_CONTACTS, {
      firstName: 'Luc',
      lastName: 'Morel',
      phone: '+33 6 00 00 00 00',
      acceptedByContact: true,
    });
    expect(contacts).toHaveLength(3);

    const draft = alertSvc.buildContactMessageDraft({
      contact: DEMO_CONTACTS[0],
      session: null,
      reason: 'CHECKIN_EXPIRED',
    });
    expect(draft.messageBody).not.toContain('en danger');
    expect(draft.messageBody).toContain("SAFE n'a pas reçu la confirmation attendue.");
    expect(alertSvc.getRegionalEmergencyInfo('FR').primaryNumber).toBe('112');
  });
});
