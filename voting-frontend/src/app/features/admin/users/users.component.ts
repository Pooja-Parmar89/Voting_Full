import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { User, UserStatus } from '../../../core/models/models';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html'
})
export class AdminUsersComponent implements OnInit {
  private adminService = inject(AdminService);
  private toast = inject(ToastService);

  users = signal<User[]>([]);
  loading = signal(true);
  page = signal(0);
  totalPages = signal(0);
  search = '';
  status: UserStatus | '' = '';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.adminService.users(this.status, this.search, this.page(), 10).subscribe({
      next: (res) => { this.users.set(res.data.content); this.totalPages.set(res.data.totalPages); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  applyFilters(): void { this.page.set(0); this.load(); }

  nextPage(): void { if (this.page() < this.totalPages() - 1) { this.page.update(p => p + 1); this.load(); } }
  prevPage(): void { if (this.page() > 0) { this.page.update(p => p - 1); this.load(); } }

  block(u: User): void {
    const reason = prompt(`Reason for blocking ${u.fullName}? (optional)`) || undefined;
    this.adminService.blockUser(u.id, reason).subscribe({
      next: (res) => { this.toast.success(res.message); this.load(); },
      error: (err) => this.toast.error(err.error?.message || 'Could not block user')
    });
  }

  unblock(u: User): void {
    this.adminService.unblockUser(u.id).subscribe({
      next: (res) => { this.toast.success(res.message); this.load(); },
      error: (err) => this.toast.error(err.error?.message || 'Could not unblock user')
    });
  }
}
