import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  DestroyRef,
  OnInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatchEntryStore } from './match-entry.store';
import {
  MatchService,
  CommanderCard,
  PlayerOption,
  RecordMatchPayload,
  MatchRecordResult,
} from './match.service';
import { Session } from '../../core/auth';
import { I18n } from '../../core/i18n/i18n';
import { AutocompleteComponent } from '../../shared/components/autocomplete.component';

export type OutcomeCategory = 'WIN' | 'LOSS' | 'DRAW';

export interface ScoreOption {
  label: string;
  preset: string;
  wins: number;
  losses: number;
  isDraw?: boolean;
}

const BO3_WIN_SCORES: ScoreOption[] = [
  { label: '2–0', preset: '2-0', wins: 2, losses: 0 },
  { label: '2–1', preset: '2-1', wins: 2, losses: 1 },
  { label: '1–0', preset: '1-0', wins: 1, losses: 0 },
];

const BO3_LOSS_SCORES: ScoreOption[] = [
  { label: '0–2', preset: '0-2', wins: 0, losses: 2 },
  { label: '1–2', preset: '1-2', wins: 1, losses: 2 },
  { label: '0–1', preset: '0-1', wins: 0, losses: 1 },
];

const BO3_DRAW_SCORES: ScoreOption[] = [
  { label: '1–1', preset: '1-1', wins: 1, losses: 1, isDraw: true },
  { label: 'ID', preset: 'ID', wins: 0, losses: 0, isDraw: true },
];

const BO1_WIN_SCORES: ScoreOption[] = [
  { label: '1–0', preset: '1-0', wins: 1, losses: 0 },
];

const BO1_LOSS_SCORES: ScoreOption[] = [
  { label: '0–1', preset: '0-1', wins: 0, losses: 1 },
];

const BO1_DRAW_SCORES: ScoreOption[] = [
  { label: 'ID', preset: 'ID', wins: 0, losses: 0, isDraw: true },
];

@Component({
  selector: 'app-match-entry',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AutocompleteComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="entry-page">
      @if (recordedMatch()) {
        <!-- Celebration / Receipt card -->
        <div class="entry-panel entry-success-card">
          <div class="success-icon-wrap" aria-hidden="true">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 6L9 17l-5-5"></path>
            </svg>
          </div>
          <h1>{{ i18n.t('matchSuccessTitle') }}</h1>
          <p class="entry-note">{{ i18n.t('matchSuccessBody') }}</p>

          <div class="match-recap">
            <div class="recap-combatant">
              <div class="recap-label">{{ i18n.t('you') }}</div>
              <div class="recap-commander">{{ lastMatchRecap()?.myCommander }}</div>
            </div>
            <div class="recap-score" [class.is-win]="lastMatchRecap()?.isWin" [class.is-loss]="lastMatchRecap()?.isLoss">
              {{ lastMatchRecap()?.scoreLabel }}
            </div>
            <div class="recap-combatant">
              <div class="recap-label">{{ lastMatchRecap()?.opponentName }}</div>
              <div class="recap-commander">{{ lastMatchRecap()?.opponentCommander }}</div>
            </div>
          </div>

          <div class="success-actions">
            <button class="primary-button" type="button" (click)="resetForNewMatch()">
              {{ i18n.t('matchRecordAnother') }}
            </button>
            <a routerLink="/" class="secondary-button">
              {{ i18n.t('matchViewOverview') }}
            </a>
          </div>
        </div>
      } @else {
        <!-- Recording form -->
        <div class="entry-panel">
          <div class="entry-header">
            <div>
              <h1>{{ i18n.t('record') }}</h1>
              <p class="entry-note">{{ i18n.t('tagline') }}</p>
            </div>
            <span class="section-aside">{{ i18n.t('quickMode') }}</span>
          </div>

          @if (!session.authenticated()) {
            <div class="entry-auth-warning" role="alert">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              <div class="auth-warning-body">
                <span>{{ i18n.t('matchLoginRequired') }}</span>
                <a routerLink="/login" class="auth-warning-link">{{ i18n.t('authSignIn') }} →</a>
              </div>
            </div>
          }

          <div class="entry-meta-grid">
            <div class="meta-field">
              <label class="meta-label">{{ i18n.t('matchFormatLabel') }}</label>
              <div class="segmented-control" role="radiogroup" [attr.aria-label]="i18n.t('matchFormatLabel')">
                <button
                  type="button"
                  role="radio"
                  class="segment-pill"
                  [attr.aria-checked]="store.currentDraft().format === 'DUEL_COMMANDER'"
                  (click)="onSelectFormat('DUEL_COMMANDER')"
                >
                  {{ i18n.t('formatDC') }}
                </button>
                <button
                  type="button"
                  role="radio"
                  class="segment-pill"
                  [attr.aria-checked]="store.currentDraft().format === 'DUEL_COMMANDER_500'"
                  (click)="onSelectFormat('DUEL_COMMANDER_500')"
                >
                  {{ i18n.t('formatDC500') }}
                </button>
                <button
                  type="button"
                  role="radio"
                  class="segment-pill"
                  [attr.aria-checked]="store.currentDraft().format === 'BRAWL'"
                  (click)="onSelectFormat('BRAWL')"
                >
                  {{ i18n.t('formatBrawl') }}
                </button>
              </div>
            </div>

            <div class="meta-field">
              <label class="meta-label">{{ i18n.t('matchPlatformLabel') }}</label>
              <div class="segmented-control" role="radiogroup" [attr.aria-label]="i18n.t('matchPlatformLabel')">
                @if (store.currentDraft().format === 'BRAWL') {
                  <button
                    type="button"
                    role="radio"
                    class="segment-pill active"
                    aria-checked="true"
                  >
                    {{ i18n.t('platformArena') }}
                  </button>
                } @else {
                  <button
                    type="button"
                    role="radio"
                    class="segment-pill"
                    [attr.aria-checked]="store.currentDraft().platform === 'PAPER'"
                    (click)="onSelectPlatform('PAPER')"
                  >
                    {{ i18n.t('paper') }}
                  </button>
                  <button
                    type="button"
                    role="radio"
                    class="segment-pill"
                    [attr.aria-checked]="store.currentDraft().platform === 'MTGO'"
                    (click)="onSelectPlatform('MTGO')"
                  >
                    {{ i18n.t('platformMtgo') }}
                  </button>
                }
              </div>
            </div>
          </div>

          <div class="entry-fields">
            <!-- My Commander -->
            <div class="commander-field">
              <app-autocomplete
                [id]="'my-commander'"
                [label]="i18n.t('myCommander')"
                [placeholder]="i18n.t('commanderExample')"
                [options]="myCommanderSuggestions()"
                [loading]="myCommanderLoading()"
                [query]="store.currentDraft().myCommander"
                [loadingText]="i18n.t('matchCommanderSearching')"
                [emptyText]="i18n.t('matchCommanderNotFound')"
                [clearAriaLabel]="i18n.t('authClear')"
                [displayFn]="commanderDisplay"
                [subtitleFn]="commanderSubtitle"
                [thumbnailFn]="commanderThumb"
                [badgeFn]="commanderBadge"
                (queryChange)="onMyCommanderQuery($event)"
                (itemSelected)="onMyCommanderSelected($event)"
                (cleared)="onMyCommanderCleared()"
              />
              @if (store.currentDraft().myCommanderCard) {
                <div class="commander-selected-chip">
                  <img
                    *ngIf="store.currentDraft().myCommanderCard?.imageUriArtCrop"
                    [src]="store.currentDraft().myCommanderCard?.imageUriArtCrop"
                    [alt]="store.currentDraft().myCommanderCard?.name"
                    class="chip-crop"
                  />
                  <span class="chip-name">{{ store.currentDraft().myCommanderCard?.name }}</span>
                  <span *ngIf="store.currentDraft().myCommanderCard?.manaCost" class="chip-mana">
                    {{ store.currentDraft().myCommanderCard?.manaCost }}
                  </span>
                </div>
              }
            </div>

            <!-- Opponent Commander -->
            <div class="commander-field">
              <app-autocomplete
                [id]="'their-commander'"
                [label]="i18n.t('theirCommander')"
                [placeholder]="i18n.t('unknownCommander')"
                [options]="theirCommanderSuggestions()"
                [loading]="theirCommanderLoading()"
                [query]="store.currentDraft().opponentCommander"
                [loadingText]="i18n.t('matchCommanderSearching')"
                [emptyText]="i18n.t('matchCommanderNotFound')"
                [clearAriaLabel]="i18n.t('authClear')"
                [allowCustom]="true"
                [customLabel]="i18n.t('matchCommanderNotFound')"
                [displayFn]="commanderDisplay"
                [subtitleFn]="commanderSubtitle"
                [thumbnailFn]="commanderThumb"
                [badgeFn]="commanderBadge"
                (queryChange)="onTheirCommanderQuery($event)"
                (itemSelected)="onTheirCommanderSelected($event)"
                (customSelected)="onTheirCommanderCustomSelected($event)"
                (cleared)="onTheirCommanderCleared()"
              />
              @if (store.currentDraft().opponentCommanderCard) {
                <div class="commander-selected-chip">
                  <img
                    *ngIf="store.currentDraft().opponentCommanderCard?.imageUriArtCrop"
                    [src]="store.currentDraft().opponentCommanderCard?.imageUriArtCrop"
                    [alt]="store.currentDraft().opponentCommanderCard?.name"
                    class="chip-crop"
                  />
                  <span class="chip-name">{{ store.currentDraft().opponentCommanderCard?.name }}</span>
                  <span *ngIf="store.currentDraft().opponentCommanderCard?.manaCost" class="chip-mana">
                    {{ store.currentDraft().opponentCommanderCard?.manaCost }}
                  </span>
                </div>
              }
            </div>

            <!-- Opponent Player -->
            <div class="entry-opponent">
              <app-autocomplete
                [id]="'opponent-name'"
                [label]="i18n.t('opponentName')"
                [placeholder]="i18n.t('opponentExample')"
                [options]="opponentSuggestions()"
                [loading]="opponentLoading()"
                [query]="store.currentDraft().opponentName"
                [loadingText]="i18n.t('matchOpponentSearching')"
                [emptyText]="i18n.t('matchPlayerNotFound')"
                [clearAriaLabel]="i18n.t('authClear')"
                [allowCustom]="true"
                [customLabel]="i18n.t('matchOpponentAdd', { name: store.currentDraft().opponentName.trim() })"
                [displayFn]="playerDisplay"
                [badgeFn]="playerBadge"
                (queryChange)="onOpponentQuery($event)"
                (itemSelected)="onOpponentSelected($event)"
                (customSelected)="onOpponentCustomSelected($event)"
                (cleared)="onOpponentCleared()"
              />
            </div>

            <div class="entry-notes">
              <label for="match-notes">{{ i18n.t('matchNotes') }}</label>
              <input
                id="match-notes"
                type="text"
                [ngModel]="notes()"
                (ngModelChange)="notes.set($event)"
                placeholder="Ex.: Round 2, mulligan a 5…"
              />
            </div>
          </div>

          <!-- Score selection: Option 1 (Outcome Category + Subscores) -->
          <fieldset class="entry-score">
            <legend>{{ i18n.t('quickResult') }}</legend>
            <div class="outcome-selector" role="radiogroup" [attr.aria-label]="i18n.t('quickResult')">
              <button
                type="button"
                role="radio"
                class="outcome-btn outcome-win"
                [class.active]="selectedOutcome() === 'WIN'"
                [attr.aria-checked]="selectedOutcome() === 'WIN'"
                (click)="onSelectOutcome('WIN')"
              >
                <svg class="outcome-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/>
                  <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
                  <path d="M4 22h16"/>
                  <path d="M10 14.66V17c0 .55-.45 1-1 1H8c-.55 0-1 .45-1 1v1c0 .55.45 1 1 1h8c.55 0 1-.45 1-1v-1c0-.55-.45-1-1-1h-1c-.55 0-1-.45-1-1v-2.34"/>
                  <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/>
                </svg>
                <span>{{ i18n.t('win') }}</span>
              </button>

              <button
                type="button"
                role="radio"
                class="outcome-btn outcome-loss"
                [class.active]="selectedOutcome() === 'LOSS'"
                [attr.aria-checked]="selectedOutcome() === 'LOSS'"
                (click)="onSelectOutcome('LOSS')"
              >
                <svg class="outcome-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
                <span>{{ i18n.t('loss') }}</span>
              </button>

              <button
                type="button"
                role="radio"
                class="outcome-btn outcome-draw"
                [class.active]="selectedOutcome() === 'DRAW'"
                [attr.aria-checked]="selectedOutcome() === 'DRAW'"
                (click)="onSelectOutcome('DRAW')"
              >
                <svg class="outcome-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <path d="m11 17 2 2a1 1 0 1 0 3-3"/>
                  <path d="m14 14 2.5 2.5a1 1 0 1 0 3-3l-3.88-3.88a3 3 0 0 0-4.24 0l-.88.88"/>
                  <path d="m7 7-2-2a1 1 0 1 0-3 3"/>
                  <path d="m10 10-2.5-2.5a1 1 0 1 0-3 3l3.88 3.88a3 3 0 0 0 4.24 0l.88-.88"/>
                </svg>
                <span>{{ i18n.t('matchDraw') }}</span>
              </button>
            </div>

            @if (currentSubScores().length > 0) {
              <div class="score-suboptions">
                @for (score of currentSubScores(); track score.preset) {
                  <button
                    type="button"
                    class="subscore-pill"
                    [class.active]="isScoreActive(score)"
                    [attr.aria-pressed]="isScoreActive(score)"
                    (click)="onSelectScore(score)"
                  >
                    {{ score.label }}
                  </button>
                }
              </div>
            }
          </fieldset>

          <!-- Intentional Draw notice vs Play/Draw Breakdown -->
          @if (store.currentDraft().scorePreset === 'ID') {
            <div class="entry-id-notice" role="status">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              <span>{{ i18n.t('intentionalDrawNotice') }}</span>
            </div>
          } @else if (store.currentDraft().scorePreset) {
            <div class="entry-playdraw-wrapper">
              <div class="entry-playdraw-header">
                <span class="meta-label">{{ i18n.t('game1Starting') }}</span>
                <div class="segmented-control playdraw-control" role="radiogroup">
                  <button
                    type="button"
                    role="radio"
                    class="segment-pill"
                    [class.active]="store.currentDraft().firstGamePlayDraw === 'PLAY'"
                    [attr.aria-checked]="store.currentDraft().firstGamePlayDraw === 'PLAY'"
                    (click)="onSelectFirstGamePlayDraw('PLAY')"
                  >
                    <svg class="pill-icon" width="13" height="13" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                      <polygon points="5 3 19 12 5 21 5 3"/>
                    </svg>
                    <span>{{ i18n.t('onPlay') }}</span>
                  </button>
                  <button
                    type="button"
                    role="radio"
                    class="segment-pill"
                    [class.active]="store.currentDraft().firstGamePlayDraw === 'DRAW'"
                    [attr.aria-checked]="store.currentDraft().firstGamePlayDraw === 'DRAW'"
                    (click)="onSelectFirstGamePlayDraw('DRAW')"
                  >
                    <svg class="pill-icon" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                      <polyline points="9 10 4 15 9 20"/>
                      <path d="M20 4v7a4 4 0 0 1-4 4H4"/>
                    </svg>
                    <span>{{ i18n.t('onDraw') }}</span>
                  </button>
                </div>
              </div>

              @if (store.currentDraft().games.length > 0) {
                <div class="games-breakdown">
                  <span class="breakdown-label">{{ i18n.t('gameDetails') }}</span>
                  <div class="game-chips-list">
                    @for (game of store.currentDraft().games; track $index) {
                      <button
                        type="button"
                        class="game-chip"
                        [class.chip-win]="game.winner === 'ME'"
                        [class.chip-loss]="game.winner === 'OPPONENT'"
                        (click)="store.toggleGamePlayDraw($index)"
                        [title]="i18n.t('togglePlayDraw', { game: ($index + 1).toString() })"
                      >
                        <span class="chip-gnum">G{{ $index + 1 }}</span>
                        <span class="chip-outcome">{{ game.winner === 'ME' ? i18n.t('gameWin') : i18n.t('gameLoss') }}</span>
                        <span class="chip-bullet">•</span>
                        <span
                          class="chip-pd"
                          [class.is-play]="game.playDraw === 'PLAY'"
                          [class.is-draw]="game.playDraw === 'DRAW'"
                        >
                          {{ game.playDraw === 'PLAY' ? i18n.t('play') : i18n.t('drawAction') }}
                        </span>
                      </button>
                    }
                  </div>
                </div>
              }
            </div>
          }

          @if (saveError()) {
            <p class="auth-notice" role="alert">{{ saveError() }}</p>
          }

          <button
            class="primary-button submit-button"
            type="button"
            [disabled]="isSubmitDisabled()"
            (click)="onSaveMatch()"
          >
            @if (saving()) {
              <svg class="dr-autocomplete-spinner" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.2"></circle>
                <path d="M12 3a9 9 0 0 1 9 9" stroke="currentColor" stroke-linecap="round"></path>
              </svg>
              <span>{{ i18n.t('matchSaving') }}</span>
            } @else {
              <span>{{ i18n.t('matchSave') }}</span>
            }
          </button>
        </div>
      }
    </section>
  `,
})
export class MatchEntryComponent implements OnInit {
  readonly store = inject(MatchEntryStore);
  readonly matchService = inject(MatchService);
  readonly session = inject(Session);
  readonly i18n = inject(I18n);
  readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly saving = signal(false);
  readonly saveError = signal<string | null>(null);
  readonly recordedMatch = signal<MatchRecordResult | null>(null);
  readonly notes = signal('');
  readonly reviewed = signal(false);

  // Search subjects
  private readonly myCommanderQuery$ = new Subject<string>();
  private readonly theirCommanderQuery$ = new Subject<string>();
  private readonly opponentQuery$ = new Subject<string>();

  // Suggestions
  readonly myCommanderSuggestions = signal<CommanderCard[]>([]);
  readonly myCommanderLoading = signal(false);

  readonly theirCommanderSuggestions = signal<CommanderCard[]>([]);
  readonly theirCommanderLoading = signal(false);

  readonly opponentSuggestions = signal<PlayerOption[]>([]);
  readonly opponentLoading = signal(false);

  // Stored deck identities
  private myDeckIdentityId: string | null = null;
  private theirDeckIdentityId: string | null = null;

  readonly lastMatchRecap = signal<{
    myCommander: string;
    opponentCommander: string;
    opponentName: string;
    scoreLabel: string;
    isWin: boolean;
    isLoss: boolean;
  } | null>(null);

  readonly selectedOutcome = computed<OutcomeCategory | null>(() => {
    const preset = this.store.currentDraft().scorePreset;
    if (!preset) return null;
    if (['2-0', '2-1', '1-0'].includes(preset)) return 'WIN';
    if (['0-2', '1-2', '0-1'].includes(preset)) return 'LOSS';
    if (['1-1', 'ID'].includes(preset)) return 'DRAW';
    return null;
  });

  readonly currentSubScores = computed<ScoreOption[]>(() => {
    const outcome = this.selectedOutcome();
    const format = this.store.currentDraft().format;
    if (!outcome) return [];
    if (outcome === 'WIN') {
      return format === 'BRAWL' ? BO1_WIN_SCORES : BO3_WIN_SCORES;
    }
    if (outcome === 'LOSS') {
      return format === 'BRAWL' ? BO1_LOSS_SCORES : BO3_LOSS_SCORES;
    }
    return format === 'BRAWL' ? BO1_DRAW_SCORES : BO3_DRAW_SCORES;
  });

  readonly isSubmitDisabled = computed(() => {
    if (this.saving()) return true;
    if (!this.session.authenticated()) return true;
    const draft = this.store.currentDraft();
    if (!draft.opponentName.trim()) return true;
    if (!draft.scorePreset) return true;
    return false;
  });

  ngOnInit(): void {
    // Setup reactive searches
    this.myCommanderQuery$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap((q) => {
          if (!q || q.trim().length < 2) {
            return of([]);
          }
          this.myCommanderLoading.set(true);
          return this.matchService.searchCommanders(q.trim()).pipe(
            catchError(() => of([])),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((res) => {
        this.myCommanderSuggestions.set(res);
        this.myCommanderLoading.set(false);
      });

    this.theirCommanderQuery$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap((q) => {
          if (!q || q.trim().length < 2) {
            return of([]);
          }
          this.theirCommanderLoading.set(true);
          return this.matchService.searchCommanders(q.trim()).pipe(
            catchError(() => of([])),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((res) => {
        this.theirCommanderSuggestions.set(res);
        this.theirCommanderLoading.set(false);
      });

    this.opponentQuery$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap((q) => {
          if (!q || q.trim().length < 1) {
            return of([]);
          }
          this.opponentLoading.set(true);
          return this.matchService.searchPlayers(q.trim()).pipe(
            catchError(() => of([])),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((res) => {
        this.opponentSuggestions.set(res);
        this.opponentLoading.set(false);
      });
  }

  // Display functions for Autocomplete
  commanderDisplay = (item: CommanderCard) => item.name;
  commanderSubtitle = (item: CommanderCard) => item.typeLine || '';
  commanderThumb = (item: CommanderCard) => item.imageUriArtCrop || item.imageUriSmall || '';
  commanderBadge = (item: CommanderCard) => item.colorIdentity || '';

  playerDisplay = (item: PlayerOption) => item.displayName || item.name;
  playerBadge = (item: PlayerOption) => (item.city ? item.city.name : '');

  onSelectFormat(format: 'DUEL_COMMANDER' | 'DUEL_COMMANDER_500' | 'BRAWL') {
    const platform = format === 'BRAWL' ? 'ARENA' : 'PAPER';
    this.store.updateDraft({ format, platform });
    const currentOutcome = this.selectedOutcome();
    if (currentOutcome) {
      this.onSelectOutcome(currentOutcome);
    } else {
      this.store.quickPreset(0, 0, false, '');
    }
  }

  onSelectPlatform(platform: 'PAPER' | 'MTGO') {
    this.store.updateDraft({ platform });
  }

  onSelectOutcome(outcome: OutcomeCategory) {
    const format = this.store.currentDraft().format;
    if (outcome === 'WIN') {
      const defaultScore = format === 'BRAWL' ? BO1_WIN_SCORES[0] : BO3_WIN_SCORES[0];
      this.onSelectScore(defaultScore);
    } else if (outcome === 'LOSS') {
      const defaultScore = format === 'BRAWL' ? BO1_LOSS_SCORES[0] : BO3_LOSS_SCORES[0];
      this.onSelectScore(defaultScore);
    } else {
      const defaultScore = format === 'BRAWL' ? BO1_DRAW_SCORES[0] : BO3_DRAW_SCORES[0];
      this.onSelectScore(defaultScore);
    }
  }

  onSelectScore(score: ScoreOption) {
    this.store.quickPreset(score.wins, score.losses, score.isDraw, score.preset);
  }

  isScoreActive(score: ScoreOption): boolean {
    const draft = this.store.currentDraft();
    return draft.scorePreset === score.preset;
  }

  onSelectFirstGamePlayDraw(playDraw: 'PLAY' | 'DRAW') {
    this.store.setFirstGamePlayDraw(playDraw);
  }

  onMyCommanderQuery(query: string) {
    this.store.updateDraft({ myCommander: query });
    this.myCommanderQuery$.next(query);
  }

  onMyCommanderSelected(card: CommanderCard) {
    this.store.updateDraft({
      myCommander: card.name,
      myCommanderCard: card,
    });
    this.matchService.getOrCreateDeckIdentity(card.id).subscribe({
      next: (identity) => {
        this.myDeckIdentityId = identity.id;
      },
    });
  }

  onMyCommanderCleared() {
    this.store.updateDraft({ myCommander: '', myCommanderCard: null });
    this.myDeckIdentityId = null;
    this.myCommanderSuggestions.set([]);
  }

  onTheirCommanderQuery(query: string) {
    this.store.updateDraft({ opponentCommander: query });
    this.theirCommanderQuery$.next(query);
  }

  onTheirCommanderSelected(card: CommanderCard) {
    this.store.updateDraft({
      opponentCommander: card.name,
      opponentCommanderCard: card,
    });
    this.matchService.getOrCreateDeckIdentity(card.id).subscribe({
      next: (identity) => {
        this.theirDeckIdentityId = identity.id;
      },
    });
  }

  onTheirCommanderCustomSelected(name: string) {
    this.store.updateDraft({
      opponentCommander: name,
      opponentCommanderCard: null,
    });
    this.theirDeckIdentityId = null;
  }

  onTheirCommanderCleared() {
    this.store.updateDraft({ opponentCommander: '', opponentCommanderCard: null });
    this.theirDeckIdentityId = null;
    this.theirCommanderSuggestions.set([]);
  }

  onOpponentQuery(query: string) {
    this.store.updateDraft({ opponentName: query });
    this.opponentQuery$.next(query);
  }

  onOpponentSelected(player: PlayerOption) {
    this.store.updateDraft({
      opponentName: player.displayName || player.name,
      opponentPlayer: player,
    });
  }

  onOpponentCustomSelected(name: string) {
    this.store.updateDraft({
      opponentName: name,
      opponentPlayer: null,
    });
  }

  onOpponentCleared() {
    this.store.updateDraft({ opponentName: '', opponentPlayer: null });
    this.opponentSuggestions.set([]);
  }

  async onSaveMatch() {
    if (this.isSubmitDisabled()) return;

    this.saving.set(true);
    this.saveError.set(null);

    try {
      const me = this.session.player();
      if (!me) {
        throw new Error('Not authenticated');
      }

      const draft = this.store.currentDraft();

      // 1. Resolve Opponent Player ID
      let opponentPlayerId = draft.opponentPlayer?.id;
      if (!opponentPlayerId) {
        const ghost = await new Promise<PlayerOption>((resolve, reject) => {
          this.matchService.createGhostPlayer(draft.opponentName.trim()).subscribe({
            next: resolve,
            error: reject,
          });
        });
        opponentPlayerId = ghost.id;
      }

      // 2. Resolve My Deck Identity ID
      let myDeckId = this.myDeckIdentityId;
      if (!myDeckId) {
        const cards = await new Promise<CommanderCard[]>((resolve) => {
          if (!draft.myCommander.trim()) return resolve([]);
          this.matchService.searchCommanders(draft.myCommander.trim(), 1).subscribe({
            next: resolve,
            error: () => resolve([]),
          });
        });
        const card = cards[0];
        if (card) {
          const identity = await new Promise<{ id: string }>((resolve, reject) => {
            this.matchService.getOrCreateDeckIdentity(card.id).subscribe({
              next: resolve,
              error: reject,
            });
          });
          myDeckId = identity.id;
        } else {
          throw new Error('Could not identify commander');
        }
      }

      // 3. Resolve Opponent Deck Identity ID
      let theirDeckId = this.theirDeckIdentityId;
      if (!theirDeckId) {
        const cards = await new Promise<CommanderCard[]>((resolve) => {
          if (!draft.opponentCommander.trim()) return resolve([]);
          this.matchService.searchCommanders(draft.opponentCommander.trim(), 1).subscribe({
            next: resolve,
            error: () => resolve([]),
          });
        });
        const card = cards[0];
        if (card) {
          const identity = await new Promise<{ id: string }>((resolve, reject) => {
            this.matchService.getOrCreateDeckIdentity(card.id).subscribe({
              next: resolve,
              error: reject,
            });
          });
          theirDeckId = identity.id;
        } else {
          theirDeckId = myDeckId;
        }
      }

      // 4. Submit match payload with Play/Draw games
      const isIntentionalDraw = draft.scorePreset === 'ID';

      const gamesPayload = isIntentionalDraw
        ? undefined
        : draft.games.map((g, idx) => ({
            gameNumber: idx + 1,
            winnerPlayerId:
              g.winner === 'ME'
                ? me.id
                : g.winner === 'OPPONENT'
                  ? opponentPlayerId
                  : null,
            startingPlayerId:
              g.playDraw === 'PLAY' ? me.id : opponentPlayerId,
            isDraw: g.winner === 'DRAW',
          }));

      const payload: RecordMatchPayload = {
        format: draft.format,
        platform: draft.platform,
        seat1: { playerId: me.id, deckIdentityId: myDeckId },
        seat2: { playerId: opponentPlayerId, deckIdentityId: theirDeckId },
        source: 'MANUAL',
        scorePreset: isIntentionalDraw ? 'ID' : undefined,
        isIntentionalDraw: isIntentionalDraw,
        notes: this.notes().trim() || undefined,
        games: gamesPayload,
      };

      const result = await new Promise<MatchRecordResult>((resolve, reject) => {
        this.matchService.recordMatch(payload).subscribe({
          next: resolve,
          error: reject,
        });
      });

      const summary = this.store.matchResultSummary();
      const scoreLabel =
        draft.scorePreset === 'ID'
          ? 'ID'
          : draft.scorePreset === '1-1'
            ? '1 – 1'
            : `${summary.wins} – ${summary.losses}`;

      this.lastMatchRecap.set({
        myCommander: draft.myCommander,
        opponentCommander: draft.opponentCommander,
        opponentName: draft.opponentName,
        scoreLabel,
        isWin: summary.wins > summary.losses,
        isLoss: summary.losses > summary.wins,
      });

      this.recordedMatch.set(result);
      this.store.reset();
    } catch (err: unknown) {
      this.saveError.set(this.i18n.t('matchError'));
    } finally {
      this.saving.set(false);
    }
  }

  resetForNewMatch() {
    this.recordedMatch.set(null);
    this.lastMatchRecap.set(null);
    this.saveError.set(null);
    this.notes.set('');
    this.myDeckIdentityId = null;
    this.theirDeckIdentityId = null;
    this.store.reset();
  }
}
