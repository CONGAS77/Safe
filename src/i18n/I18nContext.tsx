import React, { createContext, useContext, useState } from 'react';
import { SupportedLanguage } from '../types/safe';
import { TRANSLATIONS, TranslationKey } from './translations';

interface I18nContextValue {
  language: SupportedLanguage;
  setLanguage: (lang: SupportedLanguage) => void;
  t: (key: TranslationKey) => string;
}

const I18nContext = createContext<I18nContextValue | null>(null);

export const I18nProvider: React.FC<{
  initialLanguage?: SupportedLanguage;
  onLanguageChange?: (lang: SupportedLanguage) => void;
  children: React.ReactNode;
}> = ({ initialLanguage = 'fr', onLanguageChange, children }) => {
  const [language, setLangState] = useState<SupportedLanguage>(initialLanguage);

  const setLanguage = (lang: SupportedLanguage) => {
    setLangState(lang);
    onLanguageChange?.(lang);
  };

  const t = (key: TranslationKey): string => {
    return TRANSLATIONS[language]?.[key] ?? TRANSLATIONS.fr[key] ?? key;
  };

  return (
    <I18nContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </I18nContext.Provider>
  );
};

export function useI18n(): I18nContextValue {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error('useI18n must be used within I18nProvider');
  return ctx;
}
