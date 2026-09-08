import { Injectable, signal, computed } from '@angular/core';

export interface MatchDraft {
  format: 'DUEL_COMMANDER' | 'DUEL_COMMANDER_500' | 'BRAWL';
  platform: 'PAPER' | 'MTGO' | 'ARENA';
  opponentName: string;
  opponentCommander: string;
  myCommander: string;
  games: GameResult[];
}

export interface GameResult {
  winner: 'ME' | 'OPPONENT' | 'DRAW';
  playDraw: 'PLAY' | 'DRAW' | 'UNKNOWN';
}

@Injectable({ providedIn: 'root' })
export class MatchEntryStore {
  private readonly _draft = signal<MatchDraft>({
    format: 'DUEL_COMMANDER',
    platform: 'PAPER',
    opponentName: '',
    opponentCommander: '',
    myCommander: '',
    games: []
  });

  readonly currentDraft = this._draft.asReadonly();

  readonly matchResultSummary = computed(() => {
    const games = this._draft().games;
    const wins = games.filter(g => g.winner === 'ME').length;
    const losses = games.filter(g => g.winner === 'OPPONENT').length;
    const draws = games.filter(g => g.winner === 'DRAW').length;
    return { wins, losses, draws };
  });

  updateDraft(partial: Partial<MatchDraft>) {
    this._draft.update(state => ({ ...state, ...partial }));
  }

  quickPreset(wins: number, losses: number) {
    const games: GameResult[] = [];
    for(let i = 0; i < wins; i++) games.push({ winner: 'ME', playDraw: 'UNKNOWN' });
    for(let i = 0; i < losses; i++) games.push({ winner: 'OPPONENT', playDraw: 'UNKNOWN' });
    this.updateDraft({ games });
  }

  submitDraft() {
    // We will save to IndexedDB / API here (Task 5.1/5.3 integration)
    console.log('Submitting:', this._draft());
  }
}
