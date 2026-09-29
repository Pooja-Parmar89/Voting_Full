import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ApiResponse, PageResponse, LoginLog, User } from '../models/models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly base = `${API_BASE_URL}/users`;

  constructor(private http: HttpClient) {}

  me(): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${this.base}/me`);
  }

  updateProfile(fullName: string): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${this.base}/me`, { fullName });
  }

  changePassword(payload: { oldPassword: string; newPassword: string; confirmPassword: string }): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.base}/me/password`, payload);
  }

  loginHistory(page = 0, size = 10): Observable<ApiResponse<PageResponse<LoginLog>>> {
    return this.http.get<ApiResponse<PageResponse<LoginLog>>>(`${this.base}/me/login-history`, { params: { page, size } as any });
  }
}
