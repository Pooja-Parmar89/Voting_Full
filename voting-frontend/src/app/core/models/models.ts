export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export type UserRole = 'ADMIN' | 'VOTER';
export type UserStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'BLOCKED';
export type OtpType = 'EMAIL_VERIFICATION' | 'MOBILE_VERIFICATION' | 'LOGIN_OTP' | 'PASSWORD_RESET';
export type ElectionStatus = 'DRAFT' | 'UPCOMING' | 'ACTIVE' | 'CLOSED' | 'RESULT_DECLARED' | 'CANCELLED';
export type ResultVisibility = 'ALWAYS' | 'AFTER_CLOSE';
export type CandidateStatus = 'ACTIVE' | 'REMOVED';

export interface User {
  id: number;
  fullName: string;
  email: string;
  mobile: string;
  voterRefId: string | null;
  role: UserRole;
  status: UserStatus;
  emailVerified: boolean;
  mobileVerified: boolean;
}

export interface AuthResponse {
  loginOtpRequired: boolean;
  token: string | null;
  tokenType: string | null;
  user: User | null;
}

export interface Election {
  id: number;
  title: string;
  description: string | null;
  electionType: string | null;
  startDateTime: string;
  endDateTime: string;
  status: ElectionStatus;
  resultVisibility: ResultVisibility;
  candidateCount: number;
}

export interface Candidate {
  id: number;
  electionId: number;
  name: string;
  description: string | null;
  party: string | null;
  symbolUrl: string | null;
  imageUrl: string | null;
  status: CandidateStatus;
}

export interface VoteResponse {
  voteReference: string;
  electionTitle: string;
  votedAt: string;
}

export interface VotingStatus {
  hasVoted: boolean;
  electionOpenForVoting: boolean;
}

export interface CandidateResult {
  candidateId: number;
  candidateName: string;
  party: string | null;
  totalVotes: number;
  percentage: number;
}

export interface ElectionResult {
  electionId: number;
  electionTitle: string;
  totalVotesCast: number;
  totalEligibleParticipants: number;
  participationPercentage: number;
  candidates: CandidateResult[];
}

export interface DashboardStats {
  totalUsers: number;
  verifiedUsers: number;
  activeUsers: number;
  blockedUsers: number;
  totalElections: number;
  activeElections: number;
  completedElections: number;
  totalVotes: number;
}

export interface LoginLog {
  id: number;
  userFullName: string | null;
  attemptedIdentifier: string | null;
  ipAddress: string | null;
  userAgent: string | null;
  browser: string | null;
  operatingSystem: string | null;
  loginTime: string;
  logoutTime: string | null;
  status: 'SUCCESS' | 'FAILED';
  failureReason: string | null;
}

export interface AuditLog {
  id: number;
  userId: number | null;
  action: string;
  module: string;
  description: string | null;
  ipAddress: string | null;
  createdDate: string;
  createdBy: string | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
