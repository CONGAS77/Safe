import { getEmergencyConfig } from '../config/emergencyNumbers';
import {
  AlertDraftPayload,
  CountryCode,
  EmergencyConfig,
  GeoPoint,
  SafeSession,
  TrustedContact,
} from '../types/safe';

export interface AlertService {
  buildContactMessageDraft(params: {
    contact: TrustedContact | null;
    session: SafeSession | null;
    customMessage?: string;
    reason: AlertDraftPayload['reason'];
  }): AlertDraftPayload;
  prepareShareSummary(position: GeoPoint | null, sessionTitle: string): string;
  getRegionalEmergencyInfo(country: CountryCode): EmergencyConfig;
  triggerConfirmedEmergencyCall(phoneNumber: string): { initiated: boolean; telUri: string };
}

function formatTimeHHMM(ts: number | null): string {
  const date = ts ? new Date(ts) : new Date();
  return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
}

export class DefaultAlertService implements AlertService {
  buildContactMessageDraft({
    contact,
    session,
    customMessage,
    reason,
  }: {
    contact: TrustedContact | null;
    session: SafeSession | null;
    customMessage?: string;
    reason: AlertDraftPayload['reason'];
  }): AlertDraftPayload {
    const lastSignalTimeLabel = formatTimeHHMM(session?.lastSignalAt ?? Date.now());
    const sessionTitle = session?.title ?? 'Check-in SAFE';
    const destinationLabel = session?.destination.label ?? 'Non spécifiée';
    const lastPosition = session?.currentPosition ?? null;

    const messageBody =
      customMessage?.trim() ||
      [
        '⚠️ SAFE',
        "SAFE n'a pas reçu la confirmation attendue.",
        `Une personne dont vous êtes un contact de confiance n'a pas confirmé que tout allait bien.`,
        `Session : ${sessionTitle}`,
        `Destination : ${destinationLabel}`,
        `Heure du dernier signal : ${lastSignalTimeLabel}`,
        'Vérifiez si vous pouvez la contacter.',
      ].join('\n');

    return {
      contact,
      sessionTitle,
      destinationLabel,
      lastSignalTimeLabel,
      lastPosition,
      messageBody,
      reason,
    };
  }

  prepareShareSummary(position: GeoPoint | null, sessionTitle: string): string {
    if (!position) {
      return `SAFE — ${sessionTitle} : aucune coordonnée partagée sans signal actif.`;
    }
    return `SAFE — ${sessionTitle}\nPosition approximative : ${position.lat.toFixed(4)}, ${position.lng.toFixed(4)} (±${Math.round(position.accuracyMeters ?? 15)} m)\nHeure : ${formatTimeHHMM(position.timestamp)}`;
  }

  getRegionalEmergencyInfo(country: CountryCode): EmergencyConfig {
    return getEmergencyConfig(country);
  }

  triggerConfirmedEmergencyCall(phoneNumber: string): { initiated: boolean; telUri: string } {
    const telUri = `tel:${phoneNumber.replace(/\s+/g, '')}`;
    return { initiated: true, telUri };
  }
}
