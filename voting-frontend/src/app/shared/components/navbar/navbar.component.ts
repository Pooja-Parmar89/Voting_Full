import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar navbar-expand-lg bg-white shadow-sm sticky-top">
      <div class="container">
        <a class="navbar-brand fw-bold" [routerLink]="homeLink()">
          <i class="bi bi-check2-square navbar-brand-icon"></i> VoteSecure
        </a>
        @if (auth.isLoggedIn()) {
          <div class="d-flex align-items-center gap-3 ms-auto">
            <ul class="navbar-nav flex-row gap-3 d-none d-md-flex">
              @if (!auth.isAdmin()) {
                <li><a class="nav-link" routerLink="/user/dashboard" routerLinkActive="fw-bold">Dashboard</a></li>
                <li><a class="nav-link" routerLink="/user/elections" routerLinkActive="fw-bold">Elections</a></li>
                <li><a class="nav-link" routerLink="/user/voting-history" routerLinkActive="fw-bold">History</a></li>
                <li><a class="nav-link" routerLink="/user/profile" routerLinkActive="fw-bold">Profile</a></li>
              } @else {
                <li><a class="nav-link" routerLink="/admin/dashboard" routerLinkActive="fw-bold">Dashboard</a></li>
                <li><a class="nav-link" routerLink="/admin/elections" routerLinkActive="fw-bold">Elections</a></li>
                <li><a class="nav-link" routerLink="/admin/users" routerLinkActive="fw-bold">Users</a></li>
                <li><a class="nav-link" routerLink="/admin/login-logs" routerLinkActive="fw-bold">Login Logs</a></li>
                <li><a class="nav-link" routerLink="/admin/audit-logs" routerLinkActive="fw-bold">Audit Logs</a></li>
              }
            </ul>
            <span class="text-muted small d-none d-lg-inline">{{ auth.currentUser()?.fullName }}</span>
            <button class="btn btn-sm btn-outline-danger" (click)="logout()">
              <i class="bi bi-box-arrow-right"></i> Logout
            </button>
          </div>
        }
      </div>
    </nav>
  `
})
export class NavbarComponent {
  auth = inject(AuthService);
  private router = inject(Router);

  homeLink(): string {
    if (!this.auth.isLoggedIn()) return '/';
    return this.auth.isAdmin() ? '/admin/dashboard' : '/user/dashboard';
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/auth/login']);
  }
}
