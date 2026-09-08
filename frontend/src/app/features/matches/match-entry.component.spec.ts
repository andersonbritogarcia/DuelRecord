import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MatchEntryComponent } from './match-entry.component';
import { MatchEntryStore } from './match-entry.store';
import { MatchService, CommanderCard, PlayerOption, MatchRecordResult } from './match.service';
import { Session, Account, Player } from '../../core/auth';
import { RUNTIME_CONFIG } from '../../core/runtime-config';

describe('MatchEntryComponent', () => {
  let mockMatchService: {
    searchCommanders: any;
    searchPlayers: any;
    createGhostPlayer: any;
    getOrCreateDeckIdentity: any;
    recordMatch: any;
  };

  const dummyCommander: CommanderCard = {
    id: 'card-123',
    name: 'Yoshimaru, Ever Faithful',
    typeLine: 'Legendary Creature — Dog',
    manaCost: '{W}',
    colorIdentity: 'W',
    imageUriSmall: 'https://img.scryfall.com/small/yoshimaru.jpg',
    imageUriArtCrop: 'https://img.scryfall.com/art/yoshimaru.jpg',
  };

  const dummyOpponentCommander: CommanderCard = {
    id: 'card-456',
    name: 'Rograkh, Son of Rohgahh',
    typeLine: 'Legendary Creature — Kobold Warrior',
    manaCost: '{0}',
    colorIdentity: 'R',
  };

  const dummyPlayer: PlayerOption = {
    id: 'player-789',
    name: 'Lucas Rival',
    displayName: 'Lucas Rival',
    city: { name: 'São Paulo' },
  };

  const mockAccount: Account = {
    id: 'acc-1',
    email: 'anderson@example.com',
    status: 'ACTIVE',
  };

  const mockSessionPlayer: Player = {
    id: 'pilot-1',
    userId: 'acc-1',
    name: 'Anderson Brito',
    displayName: 'Anderson',
    mtgoUsername: null,
    arenaUsername: null,
    city: null,
  };

  beforeEach(() => {
    mockMatchService = {
      searchCommanders: vi.fn().mockReturnValue(of([dummyCommander])),
      searchPlayers: vi.fn().mockReturnValue(of([dummyPlayer])),
      createGhostPlayer: vi.fn().mockReturnValue(of({ id: 'ghost-1', name: 'Ghost Player', displayName: null })),
      getOrCreateDeckIdentity: vi.fn().mockReturnValue(of({ id: 'deck-id-1', name: 'Identity 1', colorIdentity: 'W' })),
      recordMatch: vi.fn().mockReturnValue(
        of({
          id: 'match-999',
          format: 'DUEL_COMMANDER',
          platform: 'PAPER',
          winnerPlayerId: 'pilot-1',
          isDraw: false,
          isIntentionalDraw: false,
          createdAt: new Date().toISOString(),
        } as MatchRecordResult),
      ),
    };

    TestBed.configureTestingModule({
      imports: [MatchEntryComponent],
      providers: [
        provideRouter([]),
        {
          provide: RUNTIME_CONFIG,
          useValue: {
            apiBaseUrl: 'http://localhost:8080/api',
            authDomain: 'http://localhost:8080',
            googleClientId: 'test-client',
            requestTimeoutMs: 5000,
          },
        },
        { provide: MatchService, useValue: mockMatchService },
      ],
    });

    const session = TestBed.inject(Session);
    session.clear();
    const store = TestBed.inject(MatchEntryStore);
    store.reset();
  });

  it('renders default format, platform, and outcome selector (Win/Loss/Draw)', () => {
    const fixture = TestBed.createComponent(MatchEntryComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const dom = fixture.nativeElement as HTMLElement;
    expect(dom.querySelector('.entry-panel')).toBeTruthy();
    expect(dom.textContent).toContain('Duel Commander');
    expect(dom.textContent).toContain('Presencial');

    const outcomeBtns = dom.querySelectorAll('.outcome-btn');
    expect(outcomeBtns.length).toBe(3);

    component.onSelectOutcome('WIN');
    fixture.detectChanges();

    const subScores = component.currentSubScores();
    expect(subScores.map((s) => s.label)).toEqual(['2–0', '2–1', '1–0']);
  });

  it('adapts platforms and score presets when switching to Brawl format', () => {
    const fixture = TestBed.createComponent(MatchEntryComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.onSelectFormat('BRAWL');
    fixture.detectChanges();

    expect(component.store.currentDraft().format).toBe('BRAWL');
    expect(component.store.currentDraft().platform).toBe('ARENA');

    component.onSelectOutcome('WIN');
    fixture.detectChanges();

    const subScores = component.currentSubScores();
    expect(subScores.map((s) => s.label)).toEqual(['1–0']);
  });

  it('shows auth alert banner and disables save when unauthenticated', () => {
    const fixture = TestBed.createComponent(MatchEntryComponent);
    fixture.detectChanges();

    const dom = fixture.nativeElement as HTMLElement;
    expect(dom.querySelector('.entry-auth-warning')).toBeTruthy();

    const submitBtn = dom.querySelector('.submit-button') as HTMLButtonElement;
    expect(submitBtn.disabled).toBe(true);
  });

  it('searches and selects commanders, and updates draft', async () => {
    const fixture = TestBed.createComponent(MatchEntryComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.onMyCommanderSelected(dummyCommander);
    fixture.detectChanges();

    expect(component.store.currentDraft().myCommander).toBe(dummyCommander.name);
    expect(mockMatchService.getOrCreateDeckIdentity).toHaveBeenCalledWith(dummyCommander.id);

    const dom = fixture.nativeElement as HTMLElement;
    expect(dom.textContent).toContain(dummyCommander.name);
  });

  it('submits match successfully with detailed Play/Draw games and displays receipt card', async () => {
    const session = TestBed.inject(Session);
    session.accept('valid-jwt', Date.now() + 60000, mockAccount, mockSessionPlayer);

    const fixture = TestBed.createComponent(MatchEntryComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.onMyCommanderSelected(dummyCommander);
    component.onTheirCommanderSelected(dummyOpponentCommander);
    component.onOpponentSelected(dummyPlayer);
    component.onSelectOutcome('WIN');
    component.onSelectScore({ label: '2–1', preset: '2-1', wins: 2, losses: 1 });
    fixture.detectChanges();

    expect(component.isSubmitDisabled()).toBe(false);

    await component.onSaveMatch();
    fixture.detectChanges();

    expect(mockMatchService.recordMatch).toHaveBeenCalledWith(
      expect.objectContaining({
        format: 'DUEL_COMMANDER',
        platform: 'PAPER',
        seat1: { playerId: 'pilot-1', deckIdentityId: 'deck-id-1' },
        seat2: { playerId: 'player-789', deckIdentityId: 'deck-id-1' },
        source: 'MANUAL',
        games: [
          { gameNumber: 1, winnerPlayerId: 'pilot-1', startingPlayerId: 'pilot-1', isDraw: false },
          { gameNumber: 2, winnerPlayerId: 'player-789', startingPlayerId: 'player-789', isDraw: false },
          { gameNumber: 3, winnerPlayerId: 'pilot-1', startingPlayerId: 'pilot-1', isDraw: false },
        ],
      }),
    );

    const dom = fixture.nativeElement as HTMLElement;
    expect(dom.querySelector('.entry-success-card')).toBeTruthy();
    expect(dom.textContent).toContain('Partida registrada!');

    component.resetForNewMatch();
    fixture.detectChanges();
    expect(dom.querySelector('.entry-success-card')).toBeNull();
  });

  it('records played draw 1-1 with 2 games and Play/Draw breakdown', async () => {
    const session = TestBed.inject(Session);
    session.accept('valid-jwt', Date.now() + 60000, mockAccount, mockSessionPlayer);

    const fixture = TestBed.createComponent(MatchEntryComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.onMyCommanderSelected(dummyCommander);
    component.onTheirCommanderSelected(dummyOpponentCommander);
    component.onOpponentSelected(dummyPlayer);
    component.onSelectOutcome('DRAW');
    component.onSelectScore({ label: '1–1', preset: '1-1', wins: 1, losses: 1, isDraw: true });
    fixture.detectChanges();

    const dom = fixture.nativeElement as HTMLElement;
    expect(dom.querySelector('.entry-playdraw-wrapper')).toBeTruthy();
    expect(component.store.currentDraft().games.length).toBe(2);

    await component.onSaveMatch();
    fixture.detectChanges();

    expect(mockMatchService.recordMatch).toHaveBeenCalledWith(
      expect.objectContaining({
        isIntentionalDraw: false,
        games: [
          { gameNumber: 1, winnerPlayerId: 'pilot-1', startingPlayerId: 'pilot-1', isDraw: false },
          { gameNumber: 2, winnerPlayerId: 'player-789', startingPlayerId: 'player-789', isDraw: false },
        ],
      }),
    );
  });

  it('records intentional draw (ID) without individual games and displays ID notice', async () => {
    const session = TestBed.inject(Session);
    session.accept('valid-jwt', Date.now() + 60000, mockAccount, mockSessionPlayer);

    const fixture = TestBed.createComponent(MatchEntryComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.onMyCommanderSelected(dummyCommander);
    component.onTheirCommanderSelected(dummyOpponentCommander);
    component.onOpponentSelected(dummyPlayer);
    component.onSelectOutcome('DRAW');
    component.onSelectScore({ label: 'ID', preset: 'ID', wins: 0, losses: 0, isDraw: true });
    fixture.detectChanges();

    const dom = fixture.nativeElement as HTMLElement;
    expect(dom.querySelector('.entry-id-notice')).toBeTruthy();
    expect(dom.querySelector('.entry-playdraw-wrapper')).toBeNull();

    await component.onSaveMatch();
    fixture.detectChanges();

    expect(mockMatchService.recordMatch).toHaveBeenCalledWith(
      expect.objectContaining({
        scorePreset: 'ID',
        isIntentionalDraw: true,
        games: undefined,
      }),
    );
  });

  it('updates startingPlayerId when user chooses Draw for Game 1 and toggles micro-chips', async () => {
    const session = TestBed.inject(Session);
    session.accept('valid-jwt', Date.now() + 60000, mockAccount, mockSessionPlayer);

    const fixture = TestBed.createComponent(MatchEntryComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.onMyCommanderSelected(dummyCommander);
    component.onTheirCommanderSelected(dummyOpponentCommander);
    component.onOpponentSelected(dummyPlayer);
    component.onSelectOutcome('WIN');
    component.onSelectScore({ label: '2–0', preset: '2-0', wins: 2, losses: 0 });
    fixture.detectChanges();

    component.onSelectFirstGamePlayDraw('DRAW');
    fixture.detectChanges();

    component.store.toggleGamePlayDraw(1);
    fixture.detectChanges();

    await component.onSaveMatch();
    fixture.detectChanges();

    expect(mockMatchService.recordMatch).toHaveBeenCalledWith(
      expect.objectContaining({
        games: [
          { gameNumber: 1, winnerPlayerId: 'pilot-1', startingPlayerId: 'player-789', isDraw: false },
          { gameNumber: 2, winnerPlayerId: 'pilot-1', startingPlayerId: 'player-789', isDraw: false },
        ],
      }),
    );
  });

  it('creates ghost player if custom opponent is entered and saves match', async () => {
    const session = TestBed.inject(Session);
    session.accept('valid-jwt', Date.now() + 60000, mockAccount, mockSessionPlayer);

    const fixture = TestBed.createComponent(MatchEntryComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.onMyCommanderSelected(dummyCommander);
    component.onTheirCommanderSelected(dummyOpponentCommander);
    component.onOpponentCustomSelected('New Ghost Rival');
    component.onSelectOutcome('WIN');
    component.onSelectScore({ label: '2–0', preset: '2-0', wins: 2, losses: 0 });
    fixture.detectChanges();

    await component.onSaveMatch();
    fixture.detectChanges();

    expect(mockMatchService.createGhostPlayer).toHaveBeenCalledWith('New Ghost Rival');
    expect(mockMatchService.recordMatch).toHaveBeenCalledWith(
      expect.objectContaining({
        seat2: expect.objectContaining({ playerId: 'ghost-1' }),
      }),
    );
  });

  it('handles submission error gracefully', async () => {
    const session = TestBed.inject(Session);
    session.accept('valid-jwt', Date.now() + 60000, mockAccount, mockSessionPlayer);

    mockMatchService.recordMatch.mockReturnValue(throwError(() => new Error('Server error')));

    const fixture = TestBed.createComponent(MatchEntryComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.onMyCommanderSelected(dummyCommander);
    component.onTheirCommanderSelected(dummyOpponentCommander);
    component.onOpponentSelected(dummyPlayer);
    component.onSelectOutcome('WIN');
    component.onSelectScore({ label: '2–0', preset: '2-0', wins: 2, losses: 0 });

    await component.onSaveMatch();
    fixture.detectChanges();

    expect(component.saveError()).toBeTruthy();
    const dom = fixture.nativeElement as HTMLElement;
    expect(dom.querySelector('.auth-notice[role="alert"]')).toBeTruthy();
  });
});
