import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ElectionService } from '../../../core/services/election.service';
import { Candidate, Election, ElectionResult, VoteResponse } from '../../../core/models/models';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-election-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent],
  templateUrl: './election-detail.component.html'
})
export class ElectionDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private electionService = inject(ElectionService);
  private toast = inject(ToastService);

  electionId = Number(this.route.snapshot.paramMap.get('id'));
  election = signal<Election | null>(null);
  candidates = signal<Candidate[]>([]);
  hasVoted = signal(false);
  electionOpen = signal(false);
  results = signal<ElectionResult | null>(null);
  voteResult = signal<VoteResponse | null>(null);

  selectedCandidate = signal<number | null>(null);
  confirming = signal(false);
  loading = signal(true);
  submitting = signal(false);

  ngOnInit(): void {
    this.electionService.get(this.electionId).subscribe({
      next: (res) => {
        this.election.set(res.data);
        this.loadCandidates();
        this.loadVotingStatus();
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  private loadCandidates(): void {
    this.electionService.candidates(this.electionId).subscribe(res => this.candidates.set(res.data));
  }

  private loadVotingStatus(): void {
    this.electionService.votingStatus(this.electionId).subscribe({
      next: (res) => {
        this.hasVoted.set(res.data.hasVoted);
        this.electionOpen.set(res.data.electionOpenForVoting);
        if (res.data.hasVoted) this.maybeLoadResults();
      },
      error: () => {}
    });
  }

  private maybeLoadResults(): void {
    this.electionService.results(this.electionId).subscribe({
      next: (res) => this.results.set(res.data),
      error: () => {} // results might not be visible yet - that's fine, just skip showing them
    });
  }

  select(candidateId: number): void {
    if (this.hasVoted() || !this.electionOpen()) return;
    this.selectedCandidate.set(candidateId);
    this.confirming.set(true);
  }

  cancelConfirm(): void {
    this.confirming.set(false);
    this.selectedCandidate.set(null);
  }

  confirmVote(): void {
    const candidateId = this.selectedCandidate();
    if (!candidateId) return;
    this.submitting.set(true);
    this.electionService.vote(this.electionId, candidateId).subscribe({
      next: (res) => {
        this.submitting.set(false);
        this.confirming.set(false);
        this.hasVoted.set(true);
        this.voteResult.set(res.data);
        this.toast.success('Vote submitted successfully!');
      },
      error: (err) => {
        this.submitting.set(false);
        this.toast.error(err.error?.message || 'Could not submit vote');
      }
    });
  }

  candidateName(id: number): string {
    return this.candidates().find(c => c.id === id)?.name || '';
  }
}
