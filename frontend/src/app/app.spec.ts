import { TestBed } from '@angular/core/testing';
import { DOCUMENT } from '@angular/common';
import { provideRouter, Router } from '@angular/router';
import { App } from './app';
import { provideHttpClient } from '@angular/common/http';
import { RUNTIME_CONFIG } from './core/runtime-config';
import { routes } from './app.routes';
import { UiPreferences, PREFERENCE_KEY, PREFERENCE_STORAGE } from './core/ui-preferences';
import { I18n, messages } from './core/i18n/i18n';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { MatchEntryStore } from './features/matches/match-entry.store';

describe('Application preferences and localization', () => {
  let saved: Map<string, string>;
  beforeEach(async () => {
    saved = new Map();
    await TestBed.configureTestingModule({
      imports: [App, DashboardComponent],
      providers: [
        provideHttpClient(),
        {
          provide: RUNTIME_CONFIG,
          useValue: {
            apiBaseUrl: 'http://localhost:8080/api',
            googleClientId: '',
            requestTimeoutMs: 15000,
          },
        },
        provideRouter(routes),
        {
          provide: PREFERENCE_STORAGE,
          useValue: {
            getItem: (key: string) => saved.get(key) ?? null,
            setItem: (key: string, value: string) => saved.set(key, value),
          },
        },
      ],
    }).compileComponents();
  });

  it('defaults to light and Portuguese and safely ignores corrupt saved preferences', () => {
    saved.set(PREFERENCE_KEY, '{bad json');
    const prefs = TestBed.inject(UiPreferences);
    expect(prefs.theme()).toBe('light');
    expect(prefs.locale()).toBe('pt-BR');
  });

  it('restores and persists validated preferences', () => {
    saved.set(PREFERENCE_KEY, JSON.stringify({ theme: 'dark', locale: 'fr' }));
    const prefs = TestBed.inject(UiPreferences);
    expect(prefs.theme()).toBe('dark');
    expect(prefs.locale()).toBe('fr');
    prefs.toggleTheme();
    prefs.setLocale('en');
    prefs.setLocale('unsupported');
    expect(JSON.parse(saved.get(PREFERENCE_KEY)!)).toEqual({ theme: 'light', locale: 'en' });
  });

  it('keeps working when storage is blocked', () => {
    TestBed.overrideProvider(PREFERENCE_STORAGE, {
      useValue: {
        getItem: () => {
          throw new Error('blocked');
        },
        setItem: () => {
          throw new Error('quota');
        },
      },
    });
    const prefs = TestBed.inject(UiPreferences);
    expect(() => {
      prefs.toggleTheme();
      prefs.setLocale('fr');
    }).not.toThrow();
    expect(prefs.theme()).toBe('dark');
    expect(prefs.locale()).toBe('fr');
  });

  it('localizes numbers, percentages, civil dates and singular/plural counts', () => {
    const prefs = TestBed.inject(UiPreferences);
    const i18n = TestBed.inject(I18n);
    expect(i18n.percent(0.656, 1)).toBe('65,6%');
    prefs.setLocale('en');
    expect(i18n.percent(0.656, 1)).toBe('65.6%');
    expect(i18n.number(1642)).toBe('1,642');
    expect(i18n.matches(1)).toBe('1 match');
    expect(i18n.matches(2)).toBe('2 matches');
    expect(i18n.date('2026-09-07')).toContain('07');
    prefs.setLocale('fr');
    expect(i18n.number(1642).replace(/\s/g, ' ')).toBe('1 642');
    expect(i18n.percent(0.656, 1).replace(/\s/g, ' ')).toBe('65,6 %');
    expect(i18n.date('2026-09-07')).toContain('sept.');
  });

  it('requires matching keys and interpolation variables in all catalogs', () => {
    const tokens = (value: string) =>
      [...value.matchAll(/\{(\w+)\}/g)].map((match) => match[1]).sort();
    for (const catalog of Object.values(messages)) {
      expect(Object.keys(catalog).sort()).toEqual(Object.keys(messages.en).sort());
      for (const key of Object.keys(messages.en) as (keyof typeof messages.en)[]) {
        expect(tokens(catalog[key]), key).toEqual(tokens(messages.en[key]));
      }
    }
  });

  it('preserves selected data while changing language and theme', async () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    const prefs = TestBed.inject(UiPreferences);
    fixture.componentInstance.filter.set('losses');
    fixture.componentInstance.selectedCommander.set('Ajani');
    prefs.setLocale('en');
    prefs.toggleTheme();
    await fixture.whenStable();
    expect(fixture.componentInstance.visibleMatches().map((match) => match.id)).toEqual([3]);
    expect(fixture.componentInstance.commander().name).toBe('Ajani');
    const dom: HTMLElement = fixture.nativeElement;
    expect(dom.textContent).toContain('Ajani · 60% win rate');
    expect(dom.textContent).toContain('Loss');
  });

  it('switches language through the segmented pill selector', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const prefs = TestBed.inject(UiPreferences);
    expect(prefs.locale()).toBe('pt-BR');

    const pills = fixture.nativeElement.querySelectorAll(
      '.language-pill',
    ) as NodeListOf<HTMLButtonElement>;
    expect(pills.length).toBe(3);
    expect(pills[0].textContent?.trim()).toBe('PT');
    expect(pills[1].textContent?.trim()).toBe('EN');
    expect(pills[2].textContent?.trim()).toBe('FR');
    expect(pills[0].classList.contains('active')).toBe(true);

    pills[1].click();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(prefs.locale()).toBe('en');
    expect(pills[1].classList.contains('active')).toBe(true);
    expect(pills[0].classList.contains('active')).toBe(false);
  });

  it('migrates old proposal links and keeps route, draft, title and language coherent', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/fast-match?design=arena#main');
    await fixture.whenStable();
    expect(router.url).toBe('/fast-match#main');
    const prefs = TestBed.inject(UiPreferences);
    expect(prefs.theme()).toBe('dark');
    const store = TestBed.inject(MatchEntryStore);
    store.updateDraft({ opponentName: 'Opponent sample' });
    store.quickPreset(2, 1);
    prefs.setLocale('fr');
    prefs.toggleTheme();
    await fixture.whenStable();
    const document = TestBed.inject(DOCUMENT);
    expect(document.documentElement.lang).toBe('fr');
    expect(document.title).toBe('Ajouter un match · DuelRecord');
    expect(router.url).toBe('/fast-match#main');
    expect(store.currentDraft().opponentName).toBe('Opponent sample');
    expect(store.matchResultSummary().wins).toBe(2);
    const dom: HTMLElement = fixture.nativeElement;
    expect(dom.textContent).toContain('Votre commandant');
    expect(dom.querySelector('.app-shell')?.classList.contains('theme-dark')).toBe(false);
  });
});
