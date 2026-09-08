import { ChangeDetectionStrategy, Component, effect, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { filter } from 'rxjs';
import { UiPreferences } from './core/ui-preferences';
import { I18n } from './core/i18n/i18n';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  protected readonly preferences = inject(UiPreferences);
  protected readonly i18n = inject(I18n);
  private readonly router = inject(Router);
  private readonly title = inject(Title);
  private readonly navigation = toSignal(
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)),
  );

  constructor() {
    // Old proposal links remain valid, but no longer pin the theme on every reload.
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe((event) => {
        const url = this.router.parseUrl(event.urlAfterRedirects);
        const legacy = url.queryParams['design'];
        if (legacy !== undefined) {
          if (legacy === 'club' || legacy === 'arena')
            this.preferences.setTheme(legacy === 'arena' ? 'dark' : 'light');
          delete url.queryParams['design'];
          void this.router.navigateByUrl(url, { replaceUrl: true });
        }
      });
    effect(() => {
      this.navigation();
      this.title.setTitle(
        `${this.i18n.t(this.router.url.split(/[?#]/)[0] === '/fast-match' ? 'record' : 'overview')} · DuelRecord`,
      );
    });
  }
}
