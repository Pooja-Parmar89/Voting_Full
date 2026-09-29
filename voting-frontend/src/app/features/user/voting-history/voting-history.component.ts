import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../core/services/user.service';
import { LoginLog } from '../../../core/models/models';

@Component({
  selector: 'app-voting-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './voting-history.component.html'
})
export class VotingHistoryComponent implements OnInit {
  private userService = inject(UserService);
  logs = signal<LoginLog[]>([]);
  loading = signal(true);

  ngOnInit(): void {
    this.userService.loginHistory(0, 20).subscribe({
      next: (res) => { this.logs.set(res.data.content); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
