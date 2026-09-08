import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Auth, Session, apiInterceptor } from './auth';
import { MatchEntryStore } from '../features/matches/match-entry.store';
import { RUNTIME_CONFIG, parseRuntimeConfig } from './runtime-config';

describe('API session boundaries', () => {
  const config = {
    apiBaseUrl: 'http://localhost:8080/api',
    googleClientId: '',
    requestTimeoutMs: 15000,
  };
  const account = { id: 'account-1', email: 'test@example.com', status: 'ACTIVE' as const };
  const player = {
    id: 'player-1',
    userId: 'account-1',
    name: null,
    displayName: null,
    city: null,
    mtgoUsername: null,
    arenaUsername: null,
  };
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([apiInterceptor])),
        provideHttpClientTesting(),
        { provide: RUNTIME_CONFIG, useValue: config },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => {
    http.verify();
    TestBed.inject(Session).clear();
  });
  it('sends credentials and locale only to the configured API path', () => {
    TestBed.inject(Session).accept('private-token', Date.now() + 60000, account, player);
    const client = TestBed.inject(HttpClient);
    for (const url of [
      'http://localhost:8080/api/players/me',
      'https://elsewhere.example/api',
      'http://localhost:8080/api-evil',
    ]) {
      client.get(url).subscribe();
      const request = http.expectOne(url);
      const trusted = url.endsWith('/players/me');
      expect(request.request.headers.get('Authorization')).toBe(
        trusted ? 'Bearer private-token' : null,
      );
      expect(request.request.headers.has('Accept-Language')).toBe(trusted);
      expect(request.request.withCredentials).toBe(false);
      request.flush({});
    }
  });
  it('does not let a stale unauthorized response clear a newer login', () => {
    const session = TestBed.inject(Session);
    session.accept('old', Date.now() + 60000, account, player);
    TestBed.inject(HttpClient)
      .get(config.apiBaseUrl + '/me')
      .subscribe({ error: () => {} });
    const request = http.expectOne(config.apiBaseUrl + '/me');
    session.accept('new', Date.now() + 60000, account, player);
    request.flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(session.token()).toBe('new');
  });
  it('rejects mismatched nonce before making a request', async () => {
    const token =
      'header.' +
      btoa(JSON.stringify({ nonce: 'other', exp: Date.now() / 1000 + 60 })) +
      '.signature';
    await expect(TestBed.inject(Auth).signIn(token, 'expected')).rejects.toThrow();
    http.expectNone(config.apiBaseUrl + '/me');
  });

  it('clears account and drafts when a session ends', () => {
    const session = TestBed.inject(Session);
    const drafts = TestBed.inject(MatchEntryStore);
    session.accept('token', Date.now() + 60000, account, player);
    drafts.updateDraft({opponentName: 'Private opponent'});
    session.clear();
    expect(session.token()).toBeNull();
    expect(session.account()).toBeNull();
    expect(session.player()).toBeNull();
    expect(drafts.currentDraft().opponentName).toBe('');
  });
  it('does not restore a session when login completes after logout', async () => {
    const session = TestBed.inject(Session);
    const token =
      'header.' +
      btoa(JSON.stringify({ nonce: 'expected', exp: Date.now() / 1000 + 60 })) +
      '.signature';
    const login = TestBed.inject(Auth).signIn(token, 'expected');
    const request = http.expectOne(config.apiBaseUrl + '/me');
    session.clear();
    request.flush(account);
    expect(await login).toBe(false);
    expect(session.authenticated()).toBe(false);
    http.expectNone(config.apiBaseUrl + '/players/me');
  });
  it('accepts the session only after both account and matching profile arrive', async () => {
    const token =
      'header.' +
      btoa(JSON.stringify({ nonce: 'expected', exp: Date.now() / 1000 + 60 })) +
      '.signature';
    const login = TestBed.inject(Auth).signIn(token, 'expected');
    http.expectOne(config.apiBaseUrl + '/me').flush(account);
    await Promise.resolve();
    expect(TestBed.inject(Session).authenticated()).toBe(false);
    http.expectOne(config.apiBaseUrl + '/players/me').flush(player);
    expect(await login).toBe(true);
    expect(TestBed.inject(Session).player()).toEqual(player);
  });
  it('rejects insecure remote URLs and strips unexpected configuration fields', () => {
    expect(() =>
      parseRuntimeConfig({ ...config, apiBaseUrl: 'http://remote.example/api' }),
    ).toThrow();
    expect(() =>
      parseRuntimeConfig({ ...config, apiBaseUrl: 'https://user:secret@example.com/api' }),
    ).toThrow();
    expect(parseRuntimeConfig({ ...config, secret: 'never expose' })).toEqual(config);
  });
});
