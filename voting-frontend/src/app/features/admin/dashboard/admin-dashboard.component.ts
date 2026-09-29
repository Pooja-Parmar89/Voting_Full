import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { DashboardStats } from '../../../core/models/models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminService);
  stats = signal<DashboardStats | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    this.adminService.dashboard().subscribe({
      next: (res) => { this.stats.set(res.data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  pct(part: number, total: number): number {
    return total === 0 ? 0 : Math.round((part / total) * 100);
  }
}
