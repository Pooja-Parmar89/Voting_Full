import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { Candidate, Election, ElectionResult } from '../../../core/models/models';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-election-manage',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent],
  templateUrl: './election-manage.component.html'
})
export class ElectionManageComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private adminService = inject(AdminService);
  private toast = inject(ToastService);

  electionId = Number(this.route.snapshot.paramMap.get('id'));
  election = signal<Election | null>(null);
  candidates = signal<Candidate[]>([]);
  results = signal<ElectionResult | null>(null);
  loading = signal(true);

  editableStatus = signal(false);

  showCandidateForm = signal(false);
  editingCandidateId = signal<number | null>(null);
  candidateForm = { name: '', description: '', party: '', symbolUrl: '', imageUrl: '' };
  savingCandidate = signal(false);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.adminService.getElection(this.electionId).subscribe({
      next: (res) => {
        this.election.set(res.data);
        this.editableStatus.set(res.data.status === 'DRAFT' || res.data.status === 'UPCOMING');
        this.loading.set(false);
        if (res.data.status === 'CLOSED' || res.data.status === 'RESULT_DECLARED' || res.data.status === 'ACTIVE') {
          this.loadResults();
        }
      },
      error: () => this.loading.set(false)
    });
    this.adminService.listCandidates(this.electionId).subscribe(res => this.candidates.set(res.data));
  }

  loadResults(): void {
    this.adminService.adminResults(this.electionId).subscribe({
      next: (res) => this.results.set(res.data),
      error: () => {}
    });
  }

  openAddCandidate(): void {
    this.editingCandidateId.set(null);
    this.candidateForm = { name: '', description: '', party: '', symbolUrl: '', imageUrl: '' };
    this.showCandidateForm.set(true);
  }

  openEditCandidate(c: Candidate): void {
    this.editingCandidateId.set(c.id);
    this.candidateForm = {
      name: c.name, description: c.description || '', party: c.party || '',
      symbolUrl: c.symbolUrl || '', imageUrl: c.imageUrl || ''
    };
    this.showCandidateForm.set(true);
  }

  saveCandidate(): void {
    if (!this.candidateForm.name) { this.toast.error('Candidate name is required'); return; }
    this.savingCandidate.set(true);
    const editingId = this.editingCandidateId();
    const req = editingId
      ? this.adminService.updateCandidate(editingId, this.candidateForm)
      : this.adminService.addCandidate(this.electionId, this.candidateForm);

    req.subscribe({
      next: (res) => {
        this.savingCandidate.set(false);
        this.showCandidateForm.set(false);
        this.toast.success(res.message);
        this.load();
      },
      error: (err) => { this.savingCandidate.set(false); this.toast.error(err.error?.message || 'Could not save candidate'); }
    });
  }

  removeCandidate(c: Candidate): void {
    if (!confirm(`Remove candidate "${c.name}"?`)) return;
    this.adminService.removeCandidate(c.id).subscribe({
      next: (res) => { this.toast.success(res.message); this.load(); },
      error: (err) => this.toast.error(err.error?.message || 'Could not remove candidate')
    });
  }

  publish(): void {
    this.adminService.publishElection(this.electionId).subscribe({
      next: (res) => { this.toast.success(res.message); this.load(); },
      error: (err) => this.toast.error(err.error?.message || 'Could not publish')
    });
  }
}
