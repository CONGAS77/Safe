import { UserProfile } from '../types/safe';

export interface AuthService {
  signInWithEmail(email: string, displayName?: string): Promise<UserProfile>;
  signInWithGoogle(): Promise<UserProfile>;
  signInWithApple(): Promise<UserProfile>;
  signOut(): Promise<UserProfile>;
}

export class MockAuthService implements AuthService {
  async signInWithEmail(email: string, displayName?: string): Promise<UserProfile> {
    const cleanEmail = email.trim() || 'utilisateur@safe-network.app';
    const derivedName = displayName?.trim() || cleanEmail.split('@')[0] || 'Utilisateur';
    return {
      id: `usr-${Date.now()}`,
      displayName: derivedName.charAt(0).toUpperCase() + derivedName.slice(1),
      email: cleanEmail,
      provider: 'email',
      signedIn: true,
    };
  }

  async signInWithGoogle(): Promise<UserProfile> {
    return {
      id: 'usr-google-demo',
      displayName: 'Camille Laurent',
      email: 'camille.laurent@gmail.com',
      provider: 'google',
      signedIn: true,
    };
  }

  async signInWithApple(): Promise<UserProfile> {
    throw new Error("Apple Sign-In est préparé dans l'architecture et sera activé en production iOS.");
  }

  async signOut(): Promise<UserProfile> {
    return {
      id: 'usr-guest',
      displayName: 'Mode local',
      email: '',
      provider: 'demo',
      signedIn: false,
    };
  }
}
