import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, firstValueFrom, throwError, timeout } from 'rxjs';
import { RUNTIME_CONFIG } from './runtime-config';
import { UiPreferences } from './ui-preferences';
import { MatchEntryStore } from '../features/matches/match-entry.store';

export interface Account {
  id: string;
  email: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
}
export interface Player {
  id: string;
  userId: string;
  name: string | null;
  displayName: string | null;
  mtgoUsername: string | null;
  arenaUsername: string | null;
  city: { id?: string; name: string; country?: { code: string; name: string } } | null;
}

@Injectable({ providedIn: 'root' })
export class Session {
  private readonly drafts = inject(MatchEntryStore);
  readonly account = signal<Account | null>(null);
  readonly player = signal<Player | null>(null);
  readonly expired = signal(false);
  readonly authenticated = computed(() => this.account() !== null);
  private credential: string | null = null;
  private timer?: ReturnType<typeof setTimeout>;
  private readonly router = inject(Router);
  revision = 0;
  token() {
    return this.credential;
  }
  accept(token: string, expires: number, account: Account, player: Player) {
    this.clear();
    this.credential = token;
    this.account.set(account);
    this.player.set(player);
    this.expired.set(false);
    this.timer = setTimeout(() => this.expire(token), Math.max(0, expires - Date.now()));
  }
  clear() {
    this.drafts.reset();
    clearTimeout(this.timer);
    this.revision++;
    this.credential = null;
    this.account.set(null);
    this.player.set(null);
  }
  logout() {
    this.clear();
    this.expired.set(false);
    void this.router.navigateByUrl('/login');
  }
  expire(token: string) {
    if (this.credential !== token) return;
    this.clear();
    this.expired.set(true);
    void this.router.navigateByUrl('/login');
  }
  updatePlayer(player: Player) {
    if (player.userId !== this.account()?.id) throw new Error('Invalid profile owner');
    this.player.set(player);
  }
}

export const apiInterceptor: HttpInterceptorFn = (request, next) => {
  const config = inject(RUNTIME_CONFIG);
  const target = new URL(request.url, globalThis.location.origin);
  const base = new URL(config.apiBaseUrl);
  if (
    target.origin !== base.origin ||
    !(target.pathname === base.pathname || target.pathname.startsWith(base.pathname + '/'))
  )
    return next(request);
  const session = inject(Session);
  const token = session.token();
  let headers = request.headers.set('Accept-Language', inject(UiPreferences).locale());
  if (token && !headers.has('Authorization'))
    headers = headers.set('Authorization', `Bearer ${token}`);
  return next(request.clone({ headers, withCredentials: false })).pipe(
    timeout(config.requestTimeoutMs),
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        token &&
        headers.get('Authorization') === `Bearer ${token}`
      )
        session.expire(token);
      return throwError(() => error);
    }),
  );
};

@Injectable({ providedIn: 'root' })
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly config = inject(RUNTIME_CONFIG);
  readonly session = inject(Session);
  async signIn(credential: string, nonce: string) {
    const revision = ++this.session.revision;
    // These claims only bind the browser challenge and timer. The backend verifies the JWT.
    const encoded = credential.split('.')[1];
    if (!encoded) throw new Error('Invalid credential');
    const claims = JSON.parse(atob(encoded.replace(/-/g, '+').replace(/_/g, '/'))) as Record<
      string,
      unknown
    >;
    if (
      claims['nonce'] !== nonce ||
      typeof claims['exp'] !== 'number' ||
      claims['exp'] * 1000 <= Date.now()
    )
      throw new Error('Invalid credential');
    const options = { headers: { Authorization: `Bearer ${credential}` } };
    const account = await firstValueFrom(
      this.http.get<Account>(`${this.config.apiBaseUrl}/me`, options),
    );
    if (revision !== this.session.revision) return false;
    if (account.status !== 'ACTIVE' || !account.id || !account.email)
      throw new Error('Account unavailable');
    const player = await firstValueFrom(
      this.http.get<Player>(`${this.config.apiBaseUrl}/players/me`, options),
    );
    if (revision !== this.session.revision) return false;
    if (!player.id || player.userId !== account.id || claims['exp'] * 1000 <= Date.now())
      throw new Error('Invalid profile');
    this.session.accept(credential, claims['exp'] * 1000, account, player);
    return true;
  }
  async updateProfile(input: { displayName: string; mtgoUsername: string | null; arenaUsername: string | null; cityId?: string | null }) {
    const player = await firstValueFrom(this.http.put<Player>(`${this.config.apiBaseUrl}/players/me`, input));
    this.session.updatePlayer(player);
    return player;
  }
}
export const authenticatedGuard: CanActivateFn = () =>
  inject(Session).authenticated() || inject(Router).createUrlTree(['/login']);
