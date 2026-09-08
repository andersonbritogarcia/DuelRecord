import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { I18n } from '../../core/i18n/i18n';
import { MessageKey } from '../../core/i18n/en';

type Color = 'W' | 'U' | 'B' | 'R' | 'G';
interface Commander {
  name: string;
  art: string;
  colors: Color[];
  wins: number;
  losses: number;
}
type ResultFilter = 'all' | 'wins' | 'losses';
@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {
  readonly i18n = inject(I18n);
  readonly filter = signal<ResultFilter>('all');
  readonly filters: { id: ResultFilter; label: MessageKey }[] = [
    { id: 'all', label: 'filterAll' },
    { id: 'wins', label: 'filterWins' },
    { id: 'losses', label: 'filterLosses' },
  ];
  readonly selectedCommander = signal('Tamiyo');
  readonly commanders: Commander[] = [
    { name: 'Tamiyo', art: 'tamiyo', colors: ['G', 'U'], wins: 12, losses: 4 },
    { name: 'Ajani', art: 'ajani', colors: ['W', 'R'], wins: 6, losses: 4 },
    { name: 'Tasigur', art: 'tasigur', colors: ['B', 'G', 'U'], wins: 3, losses: 3 },
  ];
  readonly commander = computed(
    () => this.commanders.find((c) => c.name === this.selectedCommander()) ?? this.commanders[0],
  );
  readonly colors = computed(() =>
    this.commander()
      .colors.map((color) => this.i18n.t(color))
      .join(' · '),
  );
  readonly ratingValues = computed(() =>
    [1560, 1578, 1565, 1602, 1590, 1627, 1642].map((n) => this.i18n.number(n)).join(' → '),
  );
  readonly streak = [true, true, false, true, true];
  readonly matches = [
    {
      id: 1,
      opponent: 'Lucas M.',
      commander: 'Ajani, Nacatl Pariah',
      art: 'ajani',
      mine: 'Tamiyo',
      score: '2–1',
      win: true,
      date: '2026-09-07',
      paper: true,
      league: true,
    },
    {
      id: 2,
      opponent: 'Marina S.',
      commander: 'Tasigur, the Golden Fang',
      art: 'tasigur',
      mine: 'Tamiyo',
      score: '2–0',
      win: true,
      date: '2026-09-07',
      paper: true,
      league: true,
    },
    {
      id: 3,
      opponent: 'Rafael C.',
      commander: 'Tamiyo, Inquisitive Student',
      art: 'tamiyo',
      mine: 'Ajani',
      score: '1–2',
      win: false,
      date: '2026-09-06',
      paper: false,
      league: false,
    },
    {
      id: 4,
      opponent: 'Lucas M.',
      commander: 'Ajani, Nacatl Pariah',
      art: 'ajani',
      mine: 'Tamiyo',
      score: '2–1',
      win: true,
      date: '2026-09-05',
      paper: true,
      league: false,
    },
  ];
  readonly visibleMatches = computed(() =>
    this.matches.filter((m) => this.filter() === 'all' || m.win === (this.filter() === 'wins')),
  );
}
