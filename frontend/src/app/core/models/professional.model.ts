export type VerificationStatus = 'UNVERIFIED' | 'PENDING' | 'VERIFIED' | 'REJECTED';
export type SocialPlatform = 'FACEBOOK' | 'INSTAGRAM' | 'TIKTOK' | 'YOUTUBE' | 'X' | 'WEBSITE';
export type MediaType = 'IMAGE' | 'VIDEO';

export interface Address {
  country?: string;
  region?: string;
  city?: string;
  street?: string;
  latitude?: number;
  longitude?: number;
}

export interface Skill {
  id: string;
  name: string;
}

export interface Language {
  id: string;
  code: string;
  name: string;
}

export interface SocialLink {
  id: string;
  platform: SocialPlatform;
  url: string;
}

export interface ProfessionalMedia {
  id: string;
  type: MediaType;
  url: string;
  displayOrder: number;
}

export interface Availability {
  id: string;
  specificDate?: string;
  dayOfWeek?: string;
  startTime: string;
  endTime: string;
  recurring: boolean;
  blocked: boolean;
}

export interface ProfessionalProfile {
  id: string;
  userId: string;
  fullName: string;
  avatarUrl?: string;
  businessName?: string;
  biography?: string;
  yearsExperience: number;
  address?: Address;
  skills: Skill[];
  languages: Language[];
  verificationStatus: VerificationStatus;
  premium: boolean;
  averageRating: number;
  totalReviews: number;
  totalBookings: number;
  responseTimeHours?: number;
  socialLinks: SocialLink[];
  media: ProfessionalMedia[];
}

export interface ProfessionalSummary {
  id: string;
  fullName: string;
  avatarUrl?: string;
  businessName?: string;
  city?: string;
  country?: string;
  averageRating: number;
  totalReviews: number;
  verified: boolean;
  premium: boolean;
}

export interface ProfessionalSearchParams {
  keyword?: string;
  city?: string;
  region?: string;
  country?: string;
  languageCode?: string;
  verifiedOnly?: boolean;
  premiumOnly?: boolean;
  minRating?: number;
  minExperience?: number;
  verificationStatus?: VerificationStatus;
  page?: number;
  size?: number;
}
