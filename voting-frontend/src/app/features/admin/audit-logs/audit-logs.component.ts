import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';
import { AuditLog } from '../../../core/models/models';

@Component({
  selector: 'app-admin-audit-logs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './audit-logs.component.html'
})
export class AdminAuditLogsComponent implements OnInit {
  private adminService = inject(AdminService);
  logs = signal<AuditLog[]>([]);
  loading = signal(true);
  page = signal(0);
  totalPages = signal(0);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.adminService.auditLogs(this.page(), 20).subscribe({
      next: (res) => { this.logs.set(res.data.content); this.totalPages.set(res.data.totalPages); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  nextPage(): void { if (this.page() < this.totalPages() - 1) { this.page.update(p => p + 1); this.load(); } }
  prevPage(): void { if (this.page() > 0) { this.page.update(p => p - 1); this.load(); } }
}
