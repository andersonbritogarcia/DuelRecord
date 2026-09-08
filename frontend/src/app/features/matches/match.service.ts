import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { RUNTIME_CONFIG } from '../../core/runtime-config';

export interface CommanderCard {
  id: string;
  name: string;
  typeLine?: string;
  manaCost?: string;
  colorIdentity?: string;
  imageUriSmall?: string;
  imageUriNormal?: string;
  imageUriArtCrop?: string;
}

export interface PlayerOption {
  id: string;
  name: string;
  displayName: string | null;
  mtgoUsername?: string | null;
  arenaUsername?: string | null;
  city?: { name: string } | null;
}

export interface DeckIdentity {
  id: string;
  name: string;
  colorIdentity: string;
  signature?: string;
}

export interface GameRecordInput {
  gameNumber: number;
  winnerPlayerId?: string | null;
  startingPlayerId?: string | null;
  isDraw?: boolean;
  durationSeconds?: number | null;
  notes?: string | null;
}

export interface RecordMatchPayload {
  format: 'DUEL_COMMANDER' | 'DUEL_COMMANDER_500' | 'BRAWL';
  platform: 'PAPER' | 'MTGO' | 'ARENA';
  seat1: { playerId: string; deckIdentityId: string };
  seat2: { playerId: string; deckIdentityId: string };
  scorePreset?: string;
  isIntentionalDraw?: boolean;
  source: 'MANUAL';
  playedAt?: string;
  notes?: string;
  games?: GameRecordInput[];
}

export interface MatchRecordResult {
  id: string;
  format: string;
  platform: string;
  winnerPlayerId: string | null;
  isDraw: boolean;
  isIntentionalDraw: boolean;
  createdAt: string;
}

interface PageResponse<T> {
  content: T[];
  totalElements: number;
}

@Injectable({ providedIn: 'root' })
export class MatchService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(RUNTIME_CONFIG);

  private getEndpoint(path: string): string {
    const cleanBase = this.config.apiBaseUrl.replace(/\/$/, '');
    const cleanPath = path.startsWith('/') ? path : `/${path}`;
    if (cleanBase.endsWith('/api') && cleanPath.startsWith('/api/')) {
      return `${cleanBase}${cleanPath.slice(4)}`;
    }
    if (!cleanBase.endsWith('/api') && !cleanPath.startsWith('/api/')) {
      return `${cleanBase}/api${cleanPath}`;
    }
    return `${cleanBase}${cleanPath}`;
  }

  searchCommanders(search: string, limit = 20): Observable<CommanderCard[]> {
    const url = `${this.getEndpoint('/cards/commanders')}?search=${encodeURIComponent(search)}&limit=${limit}`;
    return this.http.get<CommanderCard[]>(url);
  }

  searchPlayers(query: string, size = 15): Observable<PlayerOption[]> {
    const url = `${this.getEndpoint('/players')}?q=${encodeURIComponent(query)}&size=${size}`;
    return this.http.get<PageResponse<PlayerOption>>(url).pipe(
      map((res) => res.content || []),
    );
  }

  createGhostPlayer(name: string): Observable<PlayerOption> {
    const url = this.getEndpoint('/players/ghost');
    return this.http.post<PlayerOption>(url, { name });
  }

  getOrCreateDeckIdentity(cardId: string): Observable<DeckIdentity> {
    const url = this.getEndpoint('/decks/identities');
    return this.http.post<DeckIdentity>(url, {
      items: [{ cardId, role: 'COMMANDER' }],
    });
  }

  recordMatch(payload: RecordMatchPayload): Observable<MatchRecordResult> {
    const url = this.getEndpoint('/matches');
    return this.http.post<MatchRecordResult>(url, payload);
  }
}
