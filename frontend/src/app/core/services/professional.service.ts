import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api-response.model';
import {
  Availability,
  ProfessionalProfile,
  ProfessionalSearchParams,
  ProfessionalSummary,
} from '../models/professional.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class ProfessionalService {
  private readonly api = inject(ApiService);

  search(params: ProfessionalSearchParams): Observable<PageResponse<ProfessionalSummary>> {
    return this.api.get<PageResponse<ProfessionalSummary>>(
      '/professionals/search',
      params as Record<string, unknown>
    );
  }

  getById(id: string): Observable<ProfessionalProfile> {
    return this.api.get<ProfessionalProfile>(`/professionals/${id}`);
  }

  getCurrentProfile(): Observable<ProfessionalProfile> {
    return this.api.get<ProfessionalProfile>('/professionals/me');
  }

  update(id: string, request: Partial<ProfessionalProfile>): Observable<ProfessionalProfile> {
    return this.api.put<ProfessionalProfile>(`/professionals/${id}`, request);
  }

  listAvailabilities(id: string): Observable<Availability[]> {
    return this.api.get<Availability[]>(`/professionals/${id}/availabilities`);
  }

  addAvailability(id: string, request: Partial<Availability>): Observable<Availability> {
    return this.api.post<Availability>(`/professionals/${id}/availabilities`, request);
  }

  removeAvailability(id: string, availabilityId: string): Observable<void> {
    return this.api.deleteVoid(`/professionals/${id}/availabilities/${availabilityId}`);
  }

  submitForVerification(id: string): Observable<void> {
    return this.api.post<void>(`/professionals/${id}/verification/submit`);
  }

  uploadMedia(id: string, file: File, type: 'IMAGE' | 'VIDEO'): Observable<unknown> {
    const formData = new FormData();
    formData.append('file', file);
    return this.api.upload(`/professionals/${id}/media`, formData, { type });
  }
}
