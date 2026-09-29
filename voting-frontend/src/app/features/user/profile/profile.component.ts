import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent {
  auth = inject(AuthService);
  private userService = inject(UserService);
  private toast = inject(ToastService);

  fullName = this.auth.currentUser()?.fullName || '';
  savingProfile = signal(false);

  oldPassword = '';
  newPassword = '';
  confirmPassword = '';
  savingPassword = signal(false);

  saveProfile(): void {
    if (!this.fullName) return;
    this.savingProfile.set(true);
    this.userService.updateProfile(this.fullName).subscribe({
      next: (res) => {
        this.savingProfile.set(false);
        this.auth.setCurrentUser(res.data);
        this.toast.success('Profile updated');
      },
      error: (err) => { this.savingProfile.set(false); this.toast.error(err.error?.message || 'Update failed'); }
    });
  }

  changePassword(): void {
    if (!this.oldPassword || !this.newPassword || this.newPassword !== this.confirmPassword) {
      this.toast.error('Please fill all fields correctly; new passwords must match');
      return;
    }
    this.savingPassword.set(true);
    this.userService.changePassword({
      oldPassword: this.oldPassword, newPassword: this.newPassword, confirmPassword: this.confirmPassword
    }).subscribe({
      next: (res) => {
        this.savingPassword.set(false);
        this.oldPassword = this.newPassword = this.confirmPassword = '';
        this.toast.success(res.message);
      },
      error: (err) => { this.savingPassword.set(false); this.toast.error(err.error?.message || 'Password change failed'); }
    });
  }
}
