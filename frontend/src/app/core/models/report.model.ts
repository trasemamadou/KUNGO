export type ReportStatus = 'PENDING' | 'REVIEWED' | 'DISMISSED' | 'ACTION_TAKEN';

export interface Report {
  id: string;
  reporterId: string;
  reporterName: string;
  reportedUserId?: string;
  reportedReviewId?: string;
  reason: string;
  status: ReportStatus;
  createdAt: string;
}
