import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ApiResponse, Candidate, Election, ElectionResult, VoteResponse, VotingStatus } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ElectionService {
  private readonly base = `${API_BASE_URL}/elections`;

  constructor(private http: HttpClient) {}

  list(): Observable<ApiResponse<Election[]>> {
    return this.http.get<ApiResponse<Election[]>>(this.base);
  }

  get(id: number): Observable<ApiResponse<Election>> {
    return this.http.get<ApiResponse<Election>>(`${this.base}/${id}`);
  }

  candidates(electionId: number): Observable<ApiResponse<Candidate[]>> {
    return this.http.get<ApiResponse<Candidate[]>>(`${this.base}/${electionId}/candidates`);
  }

  votingStatus(electionId: number): Observable<ApiResponse<VotingStatus>> {
    return this.http.get<ApiResponse<VotingStatus>>(`${this.base}/${electionId}/voting-status`);
  }

  vote(electionId: number, candidateId: number): Observable<ApiResponse<VoteResponse>> {
    return this.http.post<ApiResponse<VoteResponse>>(`${this.base}/${electionId}/votes`, { candidateId });
  }

  results(electionId: number): Observable<ApiResponse<ElectionResult>> {
    return this.http.get<ApiResponse<ElectionResult>>(`${this.base}/${electionId}/results`);
  }
}
