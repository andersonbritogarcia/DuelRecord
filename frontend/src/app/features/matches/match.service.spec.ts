import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatchService } from './match.service';
import { RUNTIME_CONFIG } from '../../core/runtime-config';

describe('MatchService', () => {
  let service: MatchService;
  let httpMock: HttpTestingController;

  describe('with apiBaseUrl ending in /api', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          provideHttpClient(),
          provideHttpClientTesting(),
          {
            provide: RUNTIME_CONFIG,
            useValue: {
              apiBaseUrl: 'http://localhost:8080/api',
              googleClientId: '',
              requestTimeoutMs: 15000,
            },
          },
        ],
      });

      service = TestBed.inject(MatchService);
      httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
      httpMock.verify();
    });

    it('searches commanders without duplicate /api/api in URL', () => {
      service.searchCommanders('Tasigur', 10).subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/cards/commanders?search=Tasigur&limit=10');
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('searches players without duplicate /api/api in URL', () => {
      service.searchPlayers('Yas', 15).subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/players?q=Yas&size=15');
      expect(req.request.method).toBe('GET');
      req.flush({ content: [], totalElements: 0 });
    });

    it('creates ghost player at correct URL', () => {
      service.createGhostPlayer('Ghost Pilot').subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/players/ghost');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ name: 'Ghost Pilot' });
      req.flush({ id: '1', name: 'Ghost Pilot', displayName: null });
    });

    it('resolves deck identity at correct URL', () => {
      service.getOrCreateDeckIdentity('card-uuid').subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/decks/identities');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ items: [{ cardId: 'card-uuid', role: 'COMMANDER' }] });
      req.flush({ id: 'deck-1', name: 'Identity', colorIdentity: 'U' });
    });

    it('records match at correct URL', () => {
      service
        .recordMatch({
          format: 'DUEL_COMMANDER',
          platform: 'PAPER',
          seat1: { playerId: 'p1', deckIdentityId: 'd1' },
          seat2: { playerId: 'p2', deckIdentityId: 'd2' },
          scorePreset: '2-0',
          source: 'MANUAL',
        })
        .subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/matches');
      expect(req.request.method).toBe('POST');
      req.flush({ id: 'match-1', format: 'DUEL_COMMANDER' });
    });
  });

  describe('with apiBaseUrl without /api suffix', () => {
    beforeEach(() => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          provideHttpClient(),
          provideHttpClientTesting(),
          {
            provide: RUNTIME_CONFIG,
            useValue: {
              apiBaseUrl: 'http://localhost:8080',
              googleClientId: '',
              requestTimeoutMs: 15000,
            },
          },
        ],
      });

      service = TestBed.inject(MatchService);
      httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
      httpMock.verify();
    });

    it('adds /api automatically when base URL does not have it', () => {
      service.searchPlayers('Yas', 15).subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/players?q=Yas&size=15');
      expect(req.request.method).toBe('GET');
      req.flush({ content: [], totalElements: 0 });
    });
  });
});
