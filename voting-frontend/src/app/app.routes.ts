import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
  },

  // ---- Auth (guest only) ----
  {
    path: 'auth/login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'auth/register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'auth/verify-otp',
    loadComponent: () => import('./features/auth/verify-otp/verify-otp.component').then(m => m.VerifyOtpComponent)
  },
  {
    path: 'auth/forgot-password',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
  },
  {
    path: 'auth/reset-password',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
  },

  // ---- Voter area ----
  {
    path: 'user/dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/user/dashboard/dashboard.component').then(m => m.UserDashboardComponent)
  },
  {
    path: 'user/elections',
    canActivate: [authGuard],
    loadComponent: () => import('./features/user/elections/elections.component').then(m => m.ElectionsComponent)
  },
  {
    path: 'user/elections/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/user/election-detail/election-detail.component').then(m => m.ElectionDetailComponent)
  },
  {
    path: 'user/voting-history',
    canActivate: [authGuard],
    loadComponent: () => import('./features/user/voting-history/voting-history.component').then(m => m.VotingHistoryComponent)
  },
  {
    path: 'user/profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/user/profile/profile.component').then(m => m.ProfileComponent)
  },

  // ---- Admin area ----
  {
    path: 'admin/dashboard',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
  },
  {
    path: 'admin/elections',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/elections/elections.component').then(m => m.AdminElectionsComponent)
  },
  {
    path: 'admin/elections/:id',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/election-manage/election-manage.component').then(m => m.ElectionManageComponent)
  },
  {
    path: 'admin/users',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/users/users.component').then(m => m.AdminUsersComponent)
  },
  {
    path: 'admin/login-logs',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/login-logs/login-logs.component').then(m => m.AdminLoginLogsComponent)
  },
  {
    path: 'admin/audit-logs',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/audit-logs/audit-logs.component').then(m => m.AdminAuditLogsComponent)
  },

  { path: '**', redirectTo: '' }
];
