import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';
import { LoginLog } from '../../../core/models/models';

@Component({
  selector: 'app-admin-login-logs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login-logs.component.html'
})
export class AdminLoginLogsComponent implements OnInit {
  private adminService = inject(AdminService);
  logs = signal<LoginLog[]>([]);
  loading = signal(true);
  page = signal(0);
  totalPages = signal(0);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.adminService.loginLogs(this.page(), 20).subscribe({
      next: (res) => { this.logs.set(res.data.content); this.totalPages.set(res.data.totalPages); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  nextPage(): void { if (this.page() < this.totalPages() - 1) { this.page.update(p => p + 1); this.load(); } }
  prevPage(): void { if (this.page() > 0) { this.page.update(p => p - 1); this.load(); } }
}
