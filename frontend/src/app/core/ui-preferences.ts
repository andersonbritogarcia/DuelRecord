import { DOCUMENT } from '@angular/common';
import { Injectable, InjectionToken, effect, inject, signal } from '@angular/core';

export type Theme = 'light' | 'dark';
export type Locale = 'pt-BR' | 'en' | 'fr';
type Preferences = { theme: Theme; locale: Locale };
type PreferenceStorage = Pick<Storage, 'getItem' | 'setItem'>;
export const PREFERENCE_KEY = 'duelrecord.preferences.v1';
export const PREFERENCE_STORAGE = new InjectionToken<PreferenceStorage | null>(
  'UI preference storage',
  {
    providedIn: 'root',
    factory: () => {
      try {
        return inject(DOCUMENT).defaultView?.localStorage ?? null;
      } catch {
        return null;
      }
    },
  },
);

@Injectable({ providedIn: 'root' })
export class UiPreferences {
  private readonly storage = inject(PREFERENCE_STORAGE);
  private readonly document = inject(DOCUMENT);
  private readonly initial = this.read();
  private readonly themeState = signal<Theme>(this.initial.theme);
  private readonly localeState = signal<Locale>(this.initial.locale);
  readonly theme = this.themeState.asReadonly();
  readonly locale = this.localeState.asReadonly();

  constructor() {
    effect(() => {
      this.document.documentElement.dataset['theme'] = this.theme();
      this.document.documentElement.style.colorScheme = this.theme();
      this.document.documentElement.lang = this.locale();
    });
  }

  setTheme(theme: Theme): void {
    this.themeState.set(theme);
    this.persist();
  }
  toggleTheme(): void {
    this.setTheme(this.theme() === 'light' ? 'dark' : 'light');
  }
  setLocale(locale: string): void {
    if (locale === 'pt-BR' || locale === 'en' || locale === 'fr') {
      this.localeState.set(locale);
      this.persist();
    }
  }

  private read(): Preferences {
    try {
      const saved = JSON.parse(this.storage?.getItem(PREFERENCE_KEY) ?? '{}');
      return {
        theme: saved?.theme === 'dark' ? 'dark' : 'light',
        locale: saved?.locale === 'en' || saved?.locale === 'fr' ? saved.locale : 'pt-BR',
      };
    } catch {
      return { theme: 'light', locale: 'pt-BR' };
    }
  }

  private persist(): void {
    try {
      this.storage?.setItem(
        PREFERENCE_KEY,
        JSON.stringify({ theme: this.theme(), locale: this.locale() }),
      );
    } catch {
      /* Preferences remain usable in memory when storage is unavailable. */
    }
  }
}
