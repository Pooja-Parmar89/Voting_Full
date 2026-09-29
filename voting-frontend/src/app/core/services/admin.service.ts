import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import {
  ApiResponse, AuditLog, Candidate, DashboardStats, Election,
  ElectionResult, LoginLog, PageResponse, User, UserStatus
} from '../models/models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly base = `${API_BASE_URL}/admin`;
  private readonly electionsBase = `${API_BASE_URL}/admin/elections`;
  private readonly candidatesBase = `${API_BASE_URL}/admin/candidates`;

  constructor(private http: HttpClient) {}

  dashboard(): Observable<ApiResponse<DashboardStats>> {
    return this.http.get<ApiResponse<DashboardStats>>(`${this.base}/dashboard`);
  }

  users(status: UserStatus | '' , search: string, page = 0, size = 10): Observable<ApiResponse<PageResponse<User>>> {
    const params: any = { page, size };
    if (status) params.status = status;
    if (search) params.search = search;
    return this.http.get<ApiResponse<PageResponse<User>>>(`${this.base}/users`, { params });
  }

  blockUser(id: number, reason?: string): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.base}/users/${id}/block`, { reason });
  }

  unblockUser(id: number): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.base}/users/${id}/unblock`, {});
  }

  loginLogs(page = 0, size = 20): Observable<ApiResponse<PageResponse<LoginLog>>> {
    return this.http.get<ApiResponse<PageResponse<LoginLog>>>(`${this.base}/login-logs`, { params: { page, size } as any });
  }

  auditLogs(page = 0, size = 20): Observable<ApiResponse<PageResponse<AuditLog>>> {
    return this.http.get<ApiResponse<PageResponse<AuditLog>>>(`${this.base}/audit-logs`, { params: { page, size } as any });
  }

  // Elections
  listElections(): Observable<ApiResponse<Election[]>> {
    return this.http.get<ApiResponse<Election[]>>(this.electionsBase);
  }

  getElection(id: number): Observable<ApiResponse<Election>> {
    return this.http.get<ApiResponse<Election>>(`${this.electionsBase}/${id}`);
  }

  createElection(payload: any): Observable<ApiResponse<Election>> {
    return this.http.post<ApiResponse<Election>>(this.electionsBase, payload);
  }

  updateElection(id: number, payload: any): Observable<ApiResponse<Election>> {
    return this.http.put<ApiResponse<Election>>(`${this.electionsBase}/${id}`, payload);
  }

  publishElection(id: number): Observable<ApiResponse<Election>> {
    return this.http.put<ApiResponse<Election>>(`${this.electionsBase}/${id}/publish`, {});
  }

  cancelElection(id: number): Observable<ApiResponse<Election>> {
    return this.http.put<ApiResponse<Election>>(`${this.electionsBase}/${id}/cancel`, {});
  }

  declareResults(id: number): Observable<ApiResponse<Election>> {
    return this.http.put<ApiResponse<Election>>(`${this.electionsBase}/${id}/declare-results`, {});
  }

  deleteElection(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.electionsBase}/${id}`);
  }

  adminResults(electionId: number): Observable<ApiResponse<ElectionResult>> {
    return this.http.get<ApiResponse<ElectionResult>>(`${this.base}/elections/${electionId}/results`);
  }

  // Candidates
  listCandidates(electionId: number): Observable<ApiResponse<Candidate[]>> {
    return this.http.get<ApiResponse<Candidate[]>>(`${this.electionsBase}/${electionId}/candidates`);
  }

  addCandidate(electionId: number, payload: any): Observable<ApiResponse<Candidate>> {
    return this.http.post<ApiResponse<Candidate>>(`${this.electionsBase}/${electionId}/candidates`, payload);
  }

  updateCandidate(id: number, payload: any): Observable<ApiResponse<Candidate>> {
    return this.http.put<ApiResponse<Candidate>>(`${this.candidatesBase}/${id}`, payload);
  }

  removeCandidate(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.candidatesBase}/${id}`);
  }
}
