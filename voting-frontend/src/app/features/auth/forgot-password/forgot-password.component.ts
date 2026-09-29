import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  identifier = '';
  loading = signal(false);

  submit(): void {
    if (!this.identifier) return;
    this.loading.set(true);
    this.auth.forgotPassword(this.identifier).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.toast.success(res.message);
        this.router.navigate(['/auth/reset-password'], { queryParams: { identifier: this.identifier } });
      },
      error: (err) => { this.loading.set(false); this.toast.error(err.error?.message || 'Something went wrong'); }
    });
  }
}
