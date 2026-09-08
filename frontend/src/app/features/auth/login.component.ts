import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '../../core/auth';
import { I18n } from '../../core/i18n/i18n';
import { RUNTIME_CONFIG } from '../../core/runtime-config';

interface GoogleIdentity {
  initialize(options: {
    client_id: string;
    nonce: string;
    auto_select: boolean;
    callback: (response: { credential: string }) => void;
  }): void;
  renderButton(
    element: HTMLElement,
    options: { type: string; theme: string; size: string; locale: string },
  ): void;
  cancel(): void;
  disableAutoSelect(): void;
}
declare global {
  interface Window {
    google?: { accounts: { id: GoogleIdentity } };
  }
}
let loading: Promise<void> | undefined;
function loadGoogle(timeoutMs: number): Promise<void> {
  if (window.google) return Promise.resolve();
  if (loading) return loading;
  loading = new Promise<void>((resolve, reject) => {
    const script = document.createElement('script');
    const timer = setTimeout(() => {
      script.remove();
      loading = undefined;
      reject(new Error('Google unavailable'));
    }, timeoutMs);
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.onload = () => {
      clearTimeout(timer);
      resolve();
    };
    script.onerror = () => {
      clearTimeout(timer);
      script.remove();
      loading = undefined;
      reject(new Error('Google unavailable'));
    };
    document.head.append(script);
  });
  return loading;
}

@Component({
  standalone: true,
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: ` <section class="auth-page">
    <div class="auth-story">
      <span class="eyebrow">DUELRECORD / CLUB</span>
      <h1>{{ i18n.t('authWelcome') }}</h1>
      <p>{{ i18n.t('authIntro') }}</p>
      <img src="/art/tamiyo.jpg" alt="" />
    </div>
    <div class="auth-card">
      <span class="eyebrow">{{ i18n.t('authAccount') }}</span>
      <h2>{{ i18n.t('authSignIn') }}</h2>
      <p>{{ i18n.t('authPrivacy') }}</p>
      @if (auth.session.expired()) {
        <p role="status">{{ i18n.t('authExpired') }}</p>
      }
      @if (!config.googleClientId) {
        <p class="auth-notice" role="status">{{ i18n.t('authNotConfigured') }}</p>
      }
      <div #googleButton class="google-button" [attr.inert]="busy() ? '' : null"></div>
      @if (busy()) {
        <p role="status">{{ i18n.t('authLoading') }}</p>
      }
      @if (failed()) {
        <p class="auth-notice" role="alert">{{ i18n.t('authError') }}</p>
        <button class="primary-button" type="button" (click)="prepare()">
          {{ i18n.t('authRetry') }}
        </button>
      }
      <a class="auth-preview" routerLink="/preview">{{ i18n.t('authPreview') }} →</a>
    </div>
  </section>`,
})
export class LoginComponent implements AfterViewInit, OnDestroy {
  readonly i18n = inject(I18n);
  readonly auth = inject(Auth);
  readonly config = inject(RUNTIME_CONFIG);
  private readonly router = inject(Router);
  readonly busy = signal(false);
  readonly failed = signal(false);
  @ViewChild('googleButton', { static: true }) button!: ElementRef<HTMLElement>;
  private alive = true;
  private attempt = 0;
  ngAfterViewInit() {
    void this.prepare();
  }
  ngOnDestroy() {
    this.alive = false;
    this.attempt++;
    this.auth.session.revision++;
    window.google?.accounts.id.cancel();
  }
  async prepare() {
    if (!this.config.googleClientId) return;
    const attempt = ++this.attempt;
    this.failed.set(false);
    this.busy.set(true);
    try {
      await loadGoogle(this.config.requestTimeoutMs);
      if (!this.alive || attempt !== this.attempt) return;
      const nonce = Array.from(crypto.getRandomValues(new Uint8Array(32)), (b) =>
        b.toString(16).padStart(2, '0'),
      ).join('');
      const google = window.google!.accounts.id;
      google.initialize({
        client_id: this.config.googleClientId,
        nonce,
        auto_select: false,
        callback: (response) => {
          void this.signIn(response.credential, nonce, attempt);
        },
      });
      this.button.nativeElement.replaceChildren();
      google.renderButton(this.button.nativeElement, {
        type: 'standard',
        theme: 'outline',
        size: 'large',
        locale: this.i18n.locale(),
      });
    } catch {
      if (this.alive) this.failed.set(true);
    } finally {
      if (this.alive) this.busy.set(false);
    }
  }
  private async signIn(credential: string, nonce: string, attempt: number) {
    if (!this.alive || this.busy() || attempt !== this.attempt) return;
    this.busy.set(true);
    this.failed.set(false);
    try {
      if ((await this.auth.signIn(credential, nonce)) && this.alive)
        await this.router.navigateByUrl('/');
    } catch {
      if (this.alive) this.failed.set(true);
    } finally {
      if (this.alive) this.busy.set(false);
    }
  }
}
