# Architecture Technique — SAFE (Personal Safety Network)

## 1. Vue d'ensemble
SAFE est conçu comme un **outil de sécurité complémentaire** respectant le principe de *Privacy by Design* :
- Aucun suivi permanent : la localisation n'est collectée que lorsqu'une session SAFE est active et avec consentement explicite.
- Minimisation des données : les coordonnées détaillées ne sont conservées que selon la durée choisie par l'utilisateur.
- Aucune fausse promesse de sécurité : aucune détection n'est garantie à 100 %, et les services d'urgence ne sont jamais appelés automatiquement sans action explicite de l'utilisateur.

## 2. Séparation modulaire (`src/`)
- `src/types/` : Modèles de domaine (`Session`, `SessionState`, `CheckIn`, `TrustedContact`, `AnomalyEvent`, `PrivacySettings`, `DetectionConfig`).
- `src/engine/` :
  - `sessionStateMachine.ts` : Machine à états stricte (`DRAFT → READY → ACTIVE ↔ PAUSED / WARNING → USER_CONFIRMED / ESCALATED → COMPLETED / CANCELLED / EXPIRED`).
  - `detectionEngine.ts` : Moteur de règles configurable (retard, arrêt prolongé, déviation d'itinéraire avec tolérance de précision GPS et périodes de grâce).
- `src/services/` : Interfaces abstraites et implémentations modulaires :
  - `LocationService` (`MockLocationService`, `BrowserGeolocationService`, + point d'extension Google Maps / Capacitor).
  - `MapService` (`MockMapProvider`, + interface `GoogleMapsProvider` prête pour Google Maps Platform).
  - `SessionService` (création, démarrage, pause, prolongation, arrivée, expiration).
  - `CheckInService` (programmation, échéance, confirmation, expiration).
  - `AlertService` (préparation de messages, partage contrôlé, confirmation d'appel d'urgence régional).
  - `ContactService` (CRUD contacts de confiance avec acceptation explicite).
  - `NotificationService` (notifications in-app + Web Notification API + hook FCM futur).
  - `StorageService` (stockage local chiffré/obfusqué MVP avec politique de rétention + suppression complète).
  - `AuthService` (MVP email / Google Sign-In simulé + slot Apple Sign-In futur).
- `src/i18n/` : Internationalisation complète (`fr`, `en`, `es`, `de`, `it`).
- `src/config/` : Numéros d'urgence régionaux (`emergencyNumbers.ts`) et configuration environnement.
- `src/state/` : Store réactif central (`SafeContext.tsx`) orchestrant horloge de session, moteur de détection, check-ins, mode démo et notifications.
- `src/components/` & `src/screens/` : Interface mobile-first accessible (zones tactiles ≥ 48px, mode clair/sombre, utilisation à une main).

## 3. Intégration Backend Google Cloud / Firebase (Phase 2)
Pour passer en production sans réécrire l'UI ni la logique métier :
1. **Firebase Authentication** : brancher `FirebaseAuthService` sur `AuthService` (Email, Google Sign-In, Apple Sign-In).
2. **Cloud Firestore** : persister `users/{uid}/contacts`, `users/{uid}/sessions`, `users/{uid}/checkIns` avec règles de sécurité strictes (`request.auth.uid == uid`) et TTL automatique conforme aux paramètres de rétention.
3. **Cloud Functions / Cloud Tasks** : exécuter la vérification d'échéance de check-in et de dépassement de session côté serveur (afin que l'alerte au contact de confiance fonctionne même si le téléphone n'a plus de batterie ou de réseau).
4. **Google Maps Platform** : activer Maps JavaScript API, Routes API et Places API via `VITE_GOOGLE_MAPS_API_KEY` dans `GoogleMapsProvider`.
5. **Firebase Cloud Messaging (FCM)** & passerelle SMS (backend uniquement) : notifier l'utilisateur avant escalade et transmettre le message d'alerte au contact de confiance uniquement après consentement et expiration du délai de grâce.
