import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((c) => c.DashboardComponent),
  },
  {
    path: 'fast-match',
    loadComponent: () =>
      import('./features/matches/match-entry.component').then((c) => c.MatchEntryComponent),
  },
];
