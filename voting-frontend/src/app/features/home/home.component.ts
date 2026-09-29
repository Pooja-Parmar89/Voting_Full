import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ElectionService } from '../../core/services/election.service';
import { Election } from '../../core/models/models';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  private electionService = inject(ElectionService);
  elections = signal<Election[]>([]);

  ngOnInit(): void {
    this.electionService.list().subscribe({ next: (res) => this.elections.set(res.data.slice(0, 3)), error: () => {} });
  }
}
