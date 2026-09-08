import { Injectable, inject } from '@angular/core';
import { UiPreferences } from '../ui-preferences';
import { en, MessageKey, Messages } from './en';
import { ptBR } from './pt-BR';
import { fr } from './fr';

export const messages = { 'pt-BR': ptBR, en, fr };

@Injectable({ providedIn: 'root' })
export class I18n {
  readonly locale = inject(UiPreferences).locale;

  t(key: MessageKey, params: Record<string, string | number> = {}): string {
    const catalog: Messages = messages[this.locale()];
    return catalog[key].replace(/\{(\w+)\}/g, (token, name: string) =>
      String(params[name] ?? token),
    );
  }
  number(value: number, digits = 0): string {
    return new Intl.NumberFormat(this.locale(), { maximumFractionDigits: digits }).format(value);
  }
  percent(value: number, digits = 0): string {
    return new Intl.NumberFormat(this.locale(), {
      style: 'percent',
      maximumFractionDigits: digits,
    }).format(value);
  }
  // Fixtures represent calendar dates, not instants. UTC prevents a date shift by timezone.
  date(value: string): string {
    return new Intl.DateTimeFormat(this.locale(), {
      day: '2-digit',
      month: 'short',
      timeZone: 'UTC',
    }).format(new Date(`${value}T12:00:00Z`));
  }
  matches(count: number): string {
    const plural = new Intl.PluralRules(this.locale()).select(count);
    return this.t(plural === 'one' ? 'matchOne' : 'matchOther', { count: this.number(count) });
  }
  record(wins: number, losses: number): string {
    return this.t('recordShort', { wins: this.number(wins), losses: this.number(losses) });
  }
}
