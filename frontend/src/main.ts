import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { RUNTIME_CONFIG, parseRuntimeConfig } from './app/core/runtime-config';

fetch('/runtime-config.json', { cache: 'no-store' })
  .then((response) => {
    if (!response.ok) throw new Error('Configuration unavailable');
    return response.json();
  })
  .then((value) =>
    bootstrapApplication(App, {
      ...appConfig,
      providers: [
        ...appConfig.providers,
        { provide: RUNTIME_CONFIG, useValue: parseRuntimeConfig(value) },
      ],
    }),
  )
  .catch(() => {
    const root = document.querySelector('app-root');
    if (root)
      root.textContent =
        'Configuração indisponível. / Configuration unavailable. / Configuration indisponible.';
  });
