import { CountryCode, EmergencyConfig } from '../types/safe';

export const EMERGENCY_NUMBERS_BY_COUNTRY: Record<CountryCode, EmergencyConfig> = {
  FR: {
    country: 'FR',
    countryName: 'France',
    primaryNumber: '112',
    policeNumber: '17',
    medicalNumber: '15',
    fireNumber: '18',
    disclaimerKey: 'emergency.disclaimer.fr',
  },
  BE: {
    country: 'BE',
    countryName: 'Belgique',
    primaryNumber: '112',
    policeNumber: '101',
    medicalNumber: '112',
    fireNumber: '112',
    disclaimerKey: 'emergency.disclaimer.eu',
  },
  CH: {
    country: 'CH',
    countryName: 'Suisse',
    primaryNumber: '112',
    policeNumber: '117',
    medicalNumber: '144',
    fireNumber: '118',
    disclaimerKey: 'emergency.disclaimer.ch',
  },
  CA: {
    country: 'CA',
    countryName: 'Canada',
    primaryNumber: '911',
    policeNumber: '911',
    medicalNumber: '911',
    fireNumber: '911',
    disclaimerKey: 'emergency.disclaimer.na',
  },
  GB: {
    country: 'GB',
    countryName: 'United Kingdom',
    primaryNumber: '999',
    policeNumber: '999',
    medicalNumber: '999',
    fireNumber: '999',
    disclaimerKey: 'emergency.disclaimer.gb',
  },
  US: {
    country: 'US',
    countryName: 'United States',
    primaryNumber: '911',
    policeNumber: '911',
    medicalNumber: '911',
    fireNumber: '911',
    disclaimerKey: 'emergency.disclaimer.na',
  },
  EU: {
    country: 'EU',
    countryName: 'Union Européenne',
    primaryNumber: '112',
    policeNumber: '112',
    medicalNumber: '112',
    fireNumber: '112',
    disclaimerKey: 'emergency.disclaimer.eu',
  },
};

export function getEmergencyConfig(country: CountryCode): EmergencyConfig {
  return EMERGENCY_NUMBERS_BY_COUNTRY[country] ?? EMERGENCY_NUMBERS_BY_COUNTRY.FR;
}
