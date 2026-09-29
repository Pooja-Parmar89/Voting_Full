import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { Election } from '../../../core/models/models';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-admin-elections',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent],
  templateUrl: './elections.component.html'
})
export class AdminElectionsComponent implements OnInit {
  private adminService = inject(AdminService);
  private toast = inject(ToastService);

  elections = signal<Election[]>([]);
  loading = signal(true);
  showCreate = signal(false);
  saving = signal(false);

  form = {
    title: '', description: '', electionType: '',
    startDateTime: '', endDateTime: '', resultVisibility: 'AFTER_CLOSE'
  };

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.adminService.listElections().subscribe({
      next: (res) => { this.elections.set(res.data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  openCreate(): void {
    this.form = { title: '', description: '', electionType: '', startDateTime: '', endDateTime: '', resultVisibility: 'AFTER_CLOSE' };
    this.showCreate.set(true);
  }

  create(): void {
    if (!this.form.title || !this.form.startDateTime || !this.form.endDateTime) {
      this.toast.error('Title, start and end date/time are required');
      return;
    }
    this.saving.set(true);
    this.adminService.createElection(this.form).subscribe({
      next: (res) => {
        this.saving.set(false);
        this.showCreate.set(false);
        this.toast.success(res.message);
        this.load();
      },
      error: (err) => { this.saving.set(false); this.toast.error(err.error?.message || 'Could not create election'); }
    });
  }

  publish(e: Election): void {
    this.adminService.publishElection(e.id).subscribe({
      next: (res) => { this.toast.success(res.message); this.load(); },
      error: (err) => this.toast.error(err.error?.message || 'Could not publish')
    });
  }

  cancel(e: Election): void {
    if (!confirm(`Cancel election "${e.title}"?`)) return;
    this.adminService.cancelElection(e.id).subscribe({
      next: (res) => { this.toast.success(res.message); this.load(); },
      error: (err) => this.toast.error(err.error?.message || 'Could not cancel')
    });
  }

  declareResults(e: Election): void {
    this.adminService.declareResults(e.id).subscribe({
      next: (res) => { this.toast.success(res.message); this.load(); },
      error: (err) => this.toast.error(err.error?.message || 'Could not declare results')
    });
  }

  remove(e: Election): void {
    if (!confirm(`Delete DRAFT election "${e.title}"?`)) return;
    this.adminService.deleteElection(e.id).subscribe({
      next: (res) => { this.toast.success(res.message); this.load(); },
      error: (err) => this.toast.error(err.error?.message || 'Could not delete')
    });
  }
}
