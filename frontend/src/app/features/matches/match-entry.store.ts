import { Injectable, signal, computed } from '@angular/core';
import { CommanderCard, PlayerOption } from './match.service';

export interface GameResult {
  winner: 'ME' | 'OPPONENT' | 'DRAW';
  playDraw: 'PLAY' | 'DRAW';
}

export interface MatchDraft {
  format: 'DUEL_COMMANDER' | 'DUEL_COMMANDER_500' | 'BRAWL';
  platform: 'PAPER' | 'MTGO' | 'ARENA';
  opponentName: string;
  opponentCommander: string;
  myCommander: string;
  games: GameResult[];
  scorePreset?: string;
  firstGamePlayDraw: 'PLAY' | 'DRAW';
  myCommanderCard?: CommanderCard | null;
  opponentCommanderCard?: CommanderCard | null;
  opponentPlayer?: PlayerOption | null;
}

const DEFAULT_DRAFT: MatchDraft = {
  format: 'DUEL_COMMANDER',
  platform: 'PAPER',
  opponentName: '',
  opponentCommander: '',
  myCommander: '',
  games: [],
  scorePreset: '',
  firstGamePlayDraw: 'PLAY',
  myCommanderCard: null,
  opponentCommanderCard: null,
  opponentPlayer: null,
};

function deduceGames(
  preset: string,
  firstGamePlayDraw: 'PLAY' | 'DRAW',
): GameResult[] {
  const oppFirst = firstGamePlayDraw === 'PLAY' ? 'DRAW' : 'PLAY';

  switch (preset) {
    case '2-0':
      return [
        { winner: 'ME', playDraw: firstGamePlayDraw },
        { winner: 'ME', playDraw: oppFirst },
      ];
    case '2-1':
      return [
        { winner: 'ME', playDraw: firstGamePlayDraw },
        { winner: 'OPPONENT', playDraw: oppFirst },
        { winner: 'ME', playDraw: 'PLAY' },
      ];
    case '1-2':
      return [
        { winner: 'OPPONENT', playDraw: firstGamePlayDraw },
        { winner: 'ME', playDraw: 'PLAY' },
        { winner: 'OPPONENT', playDraw: 'DRAW' },
      ];
    case '0-2':
      return [
        { winner: 'OPPONENT', playDraw: firstGamePlayDraw },
        { winner: 'OPPONENT', playDraw: 'PLAY' },
      ];
    case '1-1':
      return [
        { winner: 'ME', playDraw: firstGamePlayDraw },
        { winner: 'OPPONENT', playDraw: oppFirst },
      ];
    case '1-0':
      return [{ winner: 'ME', playDraw: firstGamePlayDraw }];
    case '0-1':
      return [{ winner: 'OPPONENT', playDraw: firstGamePlayDraw }];
    case 'ID':
      return [];
    default:
      return [];
  }
}

@Injectable({ providedIn: 'root' })
export class MatchEntryStore {
  private readonly _draft = signal<MatchDraft>({ ...DEFAULT_DRAFT });

  readonly currentDraft = this._draft.asReadonly();

  readonly matchResultSummary = computed(() => {
    const draft = this._draft();
    const games = draft.games;
    const wins = games.filter((g) => g.winner === 'ME').length;
    const losses = games.filter((g) => g.winner === 'OPPONENT').length;
    const draws = games.filter((g) => g.winner === 'DRAW').length;
    return { wins, losses, draws };
  });

  updateDraft(partial: Partial<MatchDraft>) {
    this._draft.update((state) => ({ ...state, ...partial }));
  }

  setFirstGamePlayDraw(playDraw: 'PLAY' | 'DRAW') {
    const current = this._draft();
    if (current.scorePreset && current.scorePreset !== 'ID') {
      const games = deduceGames(current.scorePreset, playDraw);
      this._draft.update((state) => ({
        ...state,
        firstGamePlayDraw: playDraw,
        games,
      }));
    } else {
      this._draft.update((state) => ({
        ...state,
        firstGamePlayDraw: playDraw,
      }));
    }
  }

  quickPreset(wins: number, losses: number, isDraw = false, explicitPreset?: string) {
    let scorePreset = explicitPreset;
    if (!scorePreset) {
      if (isDraw || (wins === 0 && losses === 0)) {
        scorePreset = 'ID';
      } else {
        scorePreset = `${wins}-${losses}`;
      }
    }

    const firstPlayDraw = this._draft().firstGamePlayDraw || 'PLAY';
    const games = deduceGames(scorePreset, firstPlayDraw);

    this.updateDraft({ games, scorePreset });
  }

  toggleGamePlayDraw(gameIndex: number) {
    this._draft.update((state) => {
      const games = [...state.games];
      if (games[gameIndex]) {
        const nextPlayDraw = games[gameIndex].playDraw === 'PLAY' ? 'DRAW' : 'PLAY';
        games[gameIndex] = { ...games[gameIndex], playDraw: nextPlayDraw };
        const firstGamePlayDraw = gameIndex === 0 ? nextPlayDraw : state.firstGamePlayDraw;
        return { ...state, games, firstGamePlayDraw };
      }
      return state;
    });
  }

  toggleGameWinner(gameIndex: number) {
    this._draft.update((state) => {
      const games = [...state.games];
      if (games[gameIndex]) {
        const nextWinner = games[gameIndex].winner === 'ME' ? 'OPPONENT' : 'ME';
        games[gameIndex] = { ...games[gameIndex], winner: nextWinner };
        return { ...state, games };
      }
      return state;
    });
  }

  reset() {
    this._draft.set({ ...DEFAULT_DRAFT });
  }
}
