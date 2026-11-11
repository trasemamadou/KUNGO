export type ModerationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Review {
  id: string;
  bookingId: string;
  authorId: string;
  authorName: string;
  authorAvatarUrl?: string;
  targetId: string;
  rating: number;
  punctuality: number;
  quality: number;
  communication: number;
  valueForMoney: number;
  professionalism: number;
  comment?: string;
  professionalResponse?: string;
  moderationStatus: ModerationStatus;
  createdAt: string;
}

export interface ReviewCreateRequest {
  bookingId: string;
  punctuality: number;
  quality: number;
  communication: number;
  valueForMoney: number;
  professionalism: number;
  comment?: string;
}

export interface ReviewCriterion {
  key: 'punctuality' | 'quality' | 'communication' | 'valueForMoney' | 'professionalism';
  label: string;
}

export const REVIEW_CRITERIA: ReviewCriterion[] = [
  { key: 'punctuality', label: 'Ponctualité' },
  { key: 'quality', label: 'Qualité de la prestation' },
  { key: 'communication', label: 'Communication' },
  { key: 'valueForMoney', label: 'Rapport qualité/prix' },
  { key: 'professionalism', label: 'Professionnalisme' },
];

export interface ReviewResponseRequest {
  response: string;
}
