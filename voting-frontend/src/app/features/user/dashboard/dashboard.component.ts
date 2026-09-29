import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ElectionService } from '../../../core/services/election.service';
import { Election } from '../../../core/models/models';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent],
  templateUrl: './dashboard.component.html'
})
export class UserDashboardComponent implements OnInit {
  auth = inject(AuthService);
  private electionService = inject(ElectionService);

  elections = signal<Election[]>([]);
  loading = signal(true);

  activeElections = computed(() => this.elections().filter(e => e.status === 'ACTIVE'));
  upcomingElections = computed(() => this.elections().filter(e => e.status === 'UPCOMING'));
  completedElections = computed(() => this.elections().filter(e => e.status === 'CLOSED' || e.status === 'RESULT_DECLARED'));

  ngOnInit(): void {
    this.electionService.list().subscribe({
      next: (res) => { this.elections.set(res.data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
