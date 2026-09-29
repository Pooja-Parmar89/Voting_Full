import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-verify-otp',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './verify-otp.component.html'
})
export class VerifyOtpComponent {
  private auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private toast = inject(ToastService);

  email = this.route.snapshot.queryParamMap.get('email') || '';
  mobile = this.route.snapshot.queryParamMap.get('mobile') || '';

  emailCode = '';
  mobileCode = '';
  emailVerified = signal(false);
  mobileVerified = signal(false);
  loadingEmail = signal(false);
  loadingMobile = signal(false);

  verifyEmail(): void {
    if (!this.emailCode) return;
    this.loadingEmail.set(true);
    this.auth.verifyOtp({ identifier: this.email, otpType: 'EMAIL_VERIFICATION', code: this.emailCode }).subscribe({
      next: (res) => { this.loadingEmail.set(false); this.emailVerified.set(true); this.toast.success(res.message); this.checkDone(); },
      error: (err) => { this.loadingEmail.set(false); this.toast.error(err.error?.message || 'Invalid OTP'); }
    });
  }

  verifyMobile(): void {
    if (!this.mobileCode) return;
    this.loadingMobile.set(true);
    this.auth.verifyOtp({ identifier: this.mobile, otpType: 'MOBILE_VERIFICATION', code: this.mobileCode }).subscribe({
      next: (res) => { this.loadingMobile.set(false); this.mobileVerified.set(true); this.toast.success(res.message); this.checkDone(); },
      error: (err) => { this.loadingMobile.set(false); this.toast.error(err.error?.message || 'Invalid OTP'); }
    });
  }

  resend(identifier: string, otpType: string): void {
    this.auth.resendOtp({ identifier, otpType }).subscribe({
      next: (res) => this.toast.success(res.message),
      error: (err) => this.toast.error(err.error?.message || 'Could not resend OTP')
    });
  }

  private checkDone(): void {
    if (this.emailVerified() && this.mobileVerified()) {
      this.toast.success('Account fully verified! You can now log in.');
      setTimeout(() => this.router.navigate(['/auth/login']), 1200);
    }
  }
}
