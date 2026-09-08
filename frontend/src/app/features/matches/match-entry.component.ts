import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatchEntryStore } from './match-entry.store';
import { I18n } from '../../core/i18n/i18n';

@Component({
  selector: 'app-match-entry',
  imports: [FormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="entry-page">
      <a class="back-link" routerLink="/">← {{ i18n.t('back') }}</a>
      <div class="entry-panel">
        <div class="section-heading">
          <h1>{{ i18n.t('record') }}</h1>
          <span class="section-aside">{{ i18n.t('quickMode') }}</span>
        </div>
        <p class="entry-note">{{ i18n.t('recordDemo') }}</p>
        <div class="entry-fields">
          <div>
            <label for="my-commander">{{ i18n.t('myCommander') }}</label
            ><input
              id="my-commander"
              type="text"
              [placeholder]="i18n.t('commanderExample')"
              [ngModel]="store.currentDraft().myCommander"
              (ngModelChange)="store.updateDraft({ myCommander: $event }); reviewed.set(false)"
            />
          </div>
          <div>
            <label for="their-commander">{{ i18n.t('theirCommander') }}</label
            ><input
              id="their-commander"
              type="text"
              [placeholder]="i18n.t('unknownCommander')"
              [ngModel]="store.currentDraft().opponentCommander"
              (ngModelChange)="
                store.updateDraft({ opponentCommander: $event }); reviewed.set(false)
              "
            />
          </div>
          <div class="entry-opponent">
            <label for="opponent-name">{{ i18n.t('opponentName') }}</label
            ><input
              id="opponent-name"
              type="text"
              [placeholder]="i18n.t('opponentExample')"
              [ngModel]="store.currentDraft().opponentName"
              (ngModelChange)="store.updateDraft({ opponentName: $event }); reviewed.set(false)"
            />
          </div>
        </div>
        <fieldset class="entry-score">
          <legend>{{ i18n.t('quickResult') }}</legend>
          <div class="score-options">
            @for (score of scores; track score.label) {
              <button
                type="button"
                [attr.aria-pressed]="
                  store.matchResultSummary().wins === score.wins &&
                  store.matchResultSummary().losses === score.losses
                "
                (click)="store.quickPreset(score.wins, score.losses); reviewed.set(false)"
              >
                {{ score.label }}
              </button>
            }
          </div>
        </fieldset>
        <p id="review-hint" class="entry-note">{{ i18n.t('reviewHint') }}</p>
        <button
          class="primary-button"
          type="button"
          aria-describedby="review-hint"
          [disabled]="
            !store.currentDraft().opponentName.trim() || !store.currentDraft().games.length
          "
          (click)="reviewed.set(true)"
        >
          {{ i18n.t('review') }} <span aria-hidden="true">↗</span>
        </button>
        <p class="entry-status" role="status">
          @if (reviewed()) {
            {{
              i18n.t('reviewed', {
                score: store.matchResultSummary().wins + '–' + store.matchResultSummary().losses,
              })
            }}
          }
        </p>
      </div>
    </section>
  `,
})
export class MatchEntryComponent {
  readonly store = inject(MatchEntryStore);
  readonly i18n = inject(I18n);
  readonly reviewed = signal(false);
  readonly scores = [
    { label: '2–0', wins: 2, losses: 0 },
    { label: '2–1', wins: 2, losses: 1 },
    { label: '1–2', wins: 1, losses: 2 },
    { label: '0–2', wins: 0, losses: 2 },
  ];
}
