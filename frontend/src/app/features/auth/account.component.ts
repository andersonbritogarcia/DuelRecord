import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  OnDestroy,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Auth, Session } from '../../core/auth';
import { I18n } from '../../core/i18n/i18n';
import { RUNTIME_CONFIG } from '../../core/runtime-config';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom, Subscription } from 'rxjs';
import { AutocompleteComponent } from '../../shared/components/autocomplete.component';

interface Country {
  code: string;
  name: string;
}

interface City {
  id: string;
  name: string;
  country: Country;
}

function normalizeText(val: string): string {
  return (val || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [RouterLink, FormsModule, AutocompleteComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="account-page">
      <span class="eyebrow">{{ i18n.t('authConnected') }}</span>
      <div class="account-heading">
        <div class="account-avatar">{{ initial() }}</div>
        <div>
          <h1>{{ session.player()?.displayName || session.player()?.name || i18n.t('authAccount') }}</h1>
          <p>{{ i18n.t('authProfileIntro') }}</p>
        </div>
      </div>

      <div class="profile-panel">
        <div class="profile-intro">
          <span class="eyebrow">{{ i18n.t('authEditProfile') }}</span>
          <h2>{{ i18n.t('authProfileDetails') }}</h2>
          <p>{{ i18n.t('authProfileHint') }}</p>
        </div>

        <form (ngSubmit)="save()" #profileForm="ngForm" novalidate class="profile-form">
          <div class="form-field">
            <label for="display-name">{{ i18n.t('authDisplayName') }}</label>
            <input
              id="display-name"
              name="displayName"
              type="text"
              [(ngModel)]="form.displayName"
              required
              minlength="1"
              maxlength="255"
            />
          </div>

          <div class="form-row">
            <div class="form-field">
              <label for="mtgo-username">Magic Online</label>
              <input
                id="mtgo-username"
                name="mtgoUsername"
                type="text"
                [(ngModel)]="form.mtgoUsername"
                maxlength="100"
              />
            </div>

            <div class="form-field">
              <label for="arena-username">MTG Arena</label>
              <input
                id="arena-username"
                name="arenaUsername"
                type="text"
                [(ngModel)]="form.arenaUsername"
                maxlength="100"
              />
            </div>
          </div>

          <div class="form-row">
            <app-autocomplete
              id="country-select"
              [label]="i18n.t('authCountry')"
              [placeholder]="i18n.t('authSelectCountry')"
              [query]="countryQuery"
              [options]="filteredCountries()"
              [displayFn]="getCountryDisplay"
              [badgeFn]="getCountryBadge"
              [emptyText]="i18n.t('authNoResults')"
              [clearAriaLabel]="i18n.t('authClear')"
              [isSelectedFn]="isCountrySelected"
              (queryChange)="onCountryQueryChange($event)"
              (itemSelected)="onCountrySelect($event)"
              (cleared)="onCountryClear()"
            />

            <app-autocomplete
              id="city-select"
              [label]="i18n.t('authCity')"
              [placeholder]="form.countryCode ? i18n.t('authSelectCity') : i18n.t('authSelectCountryFirst')"
              [query]="cityQuery"
              [options]="cities()"
              [disabled]="!form.countryCode"
              [loading]="loadingCities()"
              [allowCustom]="true"
              [customLabel]="i18n.t('authAddCity', { name: cityQuery.trim() })"
              [displayFn]="getCityDisplay"
              [emptyText]="i18n.t('authNoResults')"
              [clearAriaLabel]="i18n.t('authClear')"
              [isSelectedFn]="isCitySelected"
              (opened)="onCityOpened()"
              (queryChange)="onCityQueryChange($event)"
              (itemSelected)="onCitySelect($event)"
              (customSelected)="onCityCustom($event)"
              (cleared)="onCityClear()"
            />
          </div>

          @if (error()) {
            <p class="auth-notice" role="alert">{{ i18n.t('authSaveError') }}</p>
          }
          @if (saved()) {
            <p class="save-status" role="status">{{ i18n.t('authSaved') }}</p>
          }

          <div class="form-actions">
            <button
              class="primary-button"
              type="submit"
              [disabled]="profileForm.invalid || saving()"
            >
              {{ saving() ? i18n.t('authSaving') : i18n.t('authSave') }}
            </button>
          </div>
        </form>
      </div>

      <dl class="account-details">
        <div>
          <dt>{{ i18n.t('authEmail') }}</dt>
          <dd>{{ session.account()?.email }}</dd>
        </div>
        <div>
          <dt>{{ i18n.t('authCity') }}</dt>
          <dd>{{ session.player()?.city?.name || i18n.t('authNotInformed') }}</dd>
        </div>
      </dl>

      <div class="auth-notice">
        <h2>{{ i18n.t('authNextTitle') }}</h2>
        <p>{{ i18n.t('authNextBody') }}</p>
        <a routerLink="/preview">{{ i18n.t('authPreview') }} →</a>
      </div>
    </section>
  `,
})
export class AccountComponent implements OnDestroy {
  readonly session = inject(Session);
  readonly i18n = inject(I18n);
  private readonly auth = inject(Auth);
  private readonly http = inject(HttpClient);
  private readonly config = inject(RUNTIME_CONFIG);

  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly error = signal(false);
  readonly loadingCities = signal(false);

  readonly countries = signal<Country[]>([]);
  readonly filteredCountries = signal<Country[]>([]);
  readonly cities = signal<City[]>([]);

  countryQuery = '';
  cityQuery = '';

  private cityTimer?: ReturnType<typeof setTimeout>;
  private cityRequest?: Subscription;

  readonly form = {
    displayName: '',
    mtgoUsername: '',
    arenaUsername: '',
    countryCode: '',
    cityId: '',
  };

  readonly getCountryDisplay = (country: Country): string => country.name;
  readonly getCountryBadge = (country: Country): string => country.code;
  readonly getCityDisplay = (city: City): string => city.name;
  readonly isCountrySelected = (country: Country): boolean => country.code === this.form.countryCode;
  readonly isCitySelected = (city: City): boolean => city.id === this.form.cityId;

  constructor() {
    effect(() => {
      const player = this.session.player();
      if (player && !this.saving()) {
        this.form.displayName = player.displayName || player.name || '';
        this.form.mtgoUsername = player.mtgoUsername || '';
        this.form.arenaUsername = player.arenaUsername || '';
        this.form.countryCode = player.city?.country?.code || '';
        this.form.cityId = player.city?.id || '';
        this.countryQuery = player.city?.country?.name || '';
        this.cityQuery = player.city?.name || '';
      }
    });

    void this.loadCountries();
  }

  ngOnDestroy(): void {
    clearTimeout(this.cityTimer);
    this.cityRequest?.unsubscribe();
  }

  initial(): string {
    return (
      this.session.player()?.displayName ||
      this.session.account()?.email ||
      '?'
    )
      .charAt(0)
      .toUpperCase();
  }

  async loadCountries(): Promise<void> {
    try {
      const list = await firstValueFrom(
        this.http.get<Country[]>(`${this.config.apiBaseUrl}/geo/countries`),
      );
      const sorted = [...list].sort((a, b) => a.name.localeCompare(b.name));
      this.countries.set(sorted);
      this.filteredCountries.set(sorted);
    } catch {
      this.error.set(true);
    }
  }

  onCountryQueryChange(query: string): void {
    this.countryQuery = query;
    const norm = normalizeText(query);

    if (!norm) {
      this.filteredCountries.set(this.countries());
      return;
    }

    const matched = this.countries().filter((c) => {
      const normName = normalizeText(c.name);
      const normCode = normalizeText(c.code);
      return normName.includes(norm) || normCode.includes(norm);
    });

    this.filteredCountries.set(matched);
  }

  onCountrySelect(country: Country): void {
    const previousCode = this.form.countryCode;
    this.form.countryCode = country.code;
    this.countryQuery = country.name;

    if (previousCode !== country.code) {
      this.form.cityId = '';
      this.cityQuery = '';
      this.cities.set([]);
      this.fetchInitialCities(country.code);
    }
  }

  onCountryClear(): void {
    this.form.countryCode = '';
    this.countryQuery = '';
    this.form.cityId = '';
    this.cityQuery = '';
    this.filteredCountries.set(this.countries());
    this.cities.set([]);
  }

  onCityOpened(): void {
    if (this.form.countryCode && this.cities().length === 0 && !this.loadingCities()) {
      this.fetchInitialCities(this.form.countryCode);
    }
  }

  fetchInitialCities(countryCode: string): void {
    clearTimeout(this.cityTimer);
    this.cityRequest?.unsubscribe();
    this.loadingCities.set(true);

    this.cityRequest = this.http
      .get<{ content: City[] }>(`${this.config.apiBaseUrl}/geo/cities`, {
        params: { country: countryCode, size: 20, sort: 'name,asc' },
      })
      .subscribe({
        next: (page) => this.cities.set(page.content || []),
        error: () => this.error.set(true),
        complete: () => this.loadingCities.set(false),
      });
  }

  onCityQueryChange(query: string): void {
    this.cityQuery = query;
    this.form.cityId = '';
    clearTimeout(this.cityTimer);
    this.cityRequest?.unsubscribe();

    if (!this.form.countryCode) {
      this.cities.set([]);
      return;
    }

    this.loadingCities.set(true);
    this.cityTimer = setTimeout(() => {
      const trimmed = query.trim();
      const params: Record<string, string | number> = {
        country: this.form.countryCode,
        size: 20,
        sort: 'name,asc',
      };
      if (trimmed) {
        params['q'] = trimmed;
      }

      this.cityRequest = this.http
        .get<{ content: City[] }>(`${this.config.apiBaseUrl}/geo/cities`, { params })
        .subscribe({
          next: (page) => this.cities.set(page.content || []),
          error: () => this.error.set(true),
          complete: () => this.loadingCities.set(false),
        });
    }, 200);
  }

  onCitySelect(city: City): void {
    this.form.cityId = city.id;
    this.cityQuery = city.name;
  }

  onCityClear(): void {
    this.form.cityId = '';
    this.cityQuery = '';
    if (this.form.countryCode) {
      this.fetchInitialCities(this.form.countryCode);
    }
  }

  async onCityCustom(cityName: string): Promise<void> {
    const trimmed = cityName.trim();
    if (!this.form.countryCode || !trimmed) return;

    this.loadingCities.set(true);
    try {
      const created = await firstValueFrom(
        this.http.post<City>(`${this.config.apiBaseUrl}/geo/cities`, {
          countryCode: this.form.countryCode,
          name: trimmed,
        }),
      );
      this.cities.update((list) => [created, ...list.filter((c) => c.id !== created.id)]);
      this.form.cityId = created.id;
      this.cityQuery = created.name;
    } catch {
      this.error.set(true);
    } finally {
      this.loadingCities.set(false);
    }
  }

  async save(): Promise<void> {
    this.saving.set(true);
    this.saved.set(false);
    this.error.set(false);

    try {
      await this.auth.updateProfile({
        displayName: this.form.displayName.trim(),
        mtgoUsername: this.form.mtgoUsername.trim() || null,
        arenaUsername: this.form.arenaUsername.trim() || null,
        cityId: this.form.cityId || null,
      });
      this.saved.set(true);
    } catch {
      this.error.set(true);
    } finally {
      this.saving.set(false);
    }
  }
}
