import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reset-password.component.html'
})
export class ResetPasswordComponent {
  private auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private toast = inject(ToastService);

  identifier = this.route.snapshot.queryParamMap.get('identifier') || '';
  code = '';
  newPassword = '';
  confirmPassword = '';
  loading = signal(false);

  submit(): void {
    if (!this.code || !this.newPassword || this.newPassword !== this.confirmPassword) {
      this.toast.error('Please fill all fields correctly; passwords must match');
      return;
    }
    this.loading.set(true);
    this.auth.resetPassword({
      identifier: this.identifier, code: this.code,
      newPassword: this.newPassword, confirmPassword: this.confirmPassword
    }).subscribe({
      next: (res) => { this.loading.set(false); this.toast.success(res.message); this.router.navigate(['/auth/login']); },
      error: (err) => { this.loading.set(false); this.toast.error(err.error?.message || 'Reset failed'); }
    });
  }
}
