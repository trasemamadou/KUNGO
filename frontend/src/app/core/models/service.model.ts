import { CategorySummary } from './category.model';
import { MediaType, ProfessionalSummary } from './professional.model';

export type PriceType = 'FIXED' | 'HOURLY' | 'NEGOTIABLE';
export type ServiceStatus = 'DRAFT' | 'PUBLISHED' | 'PAUSED' | 'ARCHIVED';

export interface ServiceMedia {
  id: string;
  type: MediaType;
  url: string;
  displayOrder: number;
}

export interface EventService {
  id: string;
  title: string;
  description?: string;
  priceType: PriceType;
  price: number;
  currency: string;
  durationMinutes?: number;
  conditions?: string;
  status: ServiceStatus;
  category: CategorySummary;
  professional: ProfessionalSummary;
  media: ServiceMedia[];
}

export interface ServiceSummary {
  id: string;
  title: string;
  price: number;
  currency: string;
  coverImageUrl?: string;
  categoryName: string;
  professionalId: string;
  professionalName: string;
  professionalAvatarUrl?: string;
  city?: string;
  professionalRating: number;
  professionalVerified: boolean;
}

export interface ServiceSearchParams {
  keyword?: string;
  categoryId?: string;
  city?: string;
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
  languageCode?: string;
  minExperience?: number;
  availableOn?: string;
  verifiedOnly?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

export interface ServiceCreateRequest {
  categoryId: string;
  title: string;
  description?: string;
  priceType: PriceType;
  price: number;
  currency?: string;
  durationMinutes?: number;
  conditions?: string;
}
