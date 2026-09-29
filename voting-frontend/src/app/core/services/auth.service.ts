import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ApiResponse, AuthResponse, User } from '../models/models';

const TOKEN_KEY = 'voting_app_token';
const USER_KEY = 'voting_app_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly base = `${API_BASE_URL}/auth`;

  // Reactive current-user state so the navbar/guards update instantly on login/logout.
  private readonly currentUserSignal = signal<User | null>(this.readStoredUser());
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isLoggedIn = computed(() => !!this.currentUserSignal());
  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ADMIN');

  constructor(private http: HttpClient) {}

  register(payload: any): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.base}/register`, payload);
  }

  verifyOtp(payload: { identifier: string; otpType: string; code: string }): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.base}/verify-otp`, payload);
  }

  resendOtp(payload: { identifier: string; otpType: string }): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.base}/resend-otp`, payload);
  }

  login(identifier: string, password: string): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.base}/login`, { identifier, password })
      .pipe(tap(res => this.handleAuthResponse(res.data)));
  }

  verifyLoginOtp(identifier: string, code: string): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.base}/verify-login-otp`,
      { identifier, otpType: 'LOGIN_OTP', code })
      .pipe(tap(res => this.handleAuthResponse(res.data)));
  }

  forgotPassword(identifier: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.base}/forgot-password`, { identifier });
  }

  resetPassword(payload: { identifier: string; code: string; newPassword: string; confirmPassword: string }): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.base}/reset-password`, payload);
  }

  logout(): void {
    this.http.post<ApiResponse<void>>(`${this.base}/logout`, {}).subscribe({
      complete: () => this.clearSession(),
      error: () => this.clearSession()
    });
  }

  private handleAuthResponse(data: AuthResponse): void {
    if (data && data.token) {
      localStorage.setItem(TOKEN_KEY, data.token);
      localStorage.setItem(USER_KEY, JSON.stringify(data.user));
      this.currentUserSignal.set(data.user);
    }
  }

  clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
  }

  setCurrentUser(user: User): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private readStoredUser(): User | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) as User : null;
  }
}
