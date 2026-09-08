import { Routes } from '@angular/router';
import { authenticatedGuard } from './core/auth';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then((c) => c.LoginComponent),
  },
  {
    path: '',
    pathMatch: 'full',
    canActivate: [authenticatedGuard],
    loadComponent: () =>
      import('./features/auth/account.component').then((c) => c.AccountComponent),
  },
  {
    path: 'profile',
    canActivate: [authenticatedGuard],
    loadComponent: () =>
      import('./features/auth/account.component').then((c) => c.AccountComponent),
  },
  {
    path: 'preview',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((c) => c.DashboardComponent),
  },
  {
    path: 'fast-match',
    loadComponent: () =>
      import('./features/matches/match-entry.component').then((c) => c.MatchEntryComponent),
  },
  { path: '**', redirectTo: '' },
];
