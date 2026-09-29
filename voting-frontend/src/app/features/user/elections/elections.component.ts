import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ElectionService } from '../../../core/services/election.service';
import { Election } from '../../../core/models/models';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-elections',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent],
  templateUrl: './elections.component.html'
})
export class ElectionsComponent implements OnInit {
  private electionService = inject(ElectionService);
  elections = signal<Election[]>([]);
  loading = signal(true);
  filter = signal<'ALL' | 'ACTIVE' | 'UPCOMING' | 'CLOSED'>('ALL');

  filtered(): Election[] {
    const f = this.filter();
    if (f === 'ALL') return this.elections();
    if (f === 'CLOSED') return this.elections().filter(e => e.status === 'CLOSED' || e.status === 'RESULT_DECLARED');
    return this.elections().filter(e => e.status === f);
  }

  ngOnInit(): void {
    this.electionService.list().subscribe({
      next: (res) => { this.elections.set(res.data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
