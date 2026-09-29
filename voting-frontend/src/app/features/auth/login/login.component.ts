import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  loading = signal(false);
  otpStage = signal(false);
  otpIdentifier = signal('');
  otpCode = '';

  form = this.fb.group({
    identifier: ['', Validators.required],
    password: ['', Validators.required]
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    const { identifier, password } = this.form.getRawValue();

    this.auth.login(identifier!, password!).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.data.loginOtpRequired) {
          this.otpStage.set(true);
          this.otpIdentifier.set(identifier!);
          this.toast.info(res.message);
        } else {
          this.toast.success('Welcome back!');
          this.redirectAfterLogin();
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.error(err.error?.message || 'Login failed');
      }
    });
  }

  submitOtp(): void {
    if (!this.otpCode) return;
    this.loading.set(true);
    this.auth.verifyLoginOtp(this.otpIdentifier(), this.otpCode).subscribe({
      next: () => { this.loading.set(false); this.toast.success('Welcome back!'); this.redirectAfterLogin(); },
      error: (err) => { this.loading.set(false); this.toast.error(err.error?.message || 'Invalid OTP'); }
    });
  }

  private redirectAfterLogin(): void {
    this.router.navigate([this.auth.isAdmin() ? '/admin/dashboard' : '/user/dashboard']);
  }
}
