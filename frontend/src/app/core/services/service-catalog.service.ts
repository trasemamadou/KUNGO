import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api-response.model';
import { EventService, ServiceCreateRequest, ServiceSearchParams, ServiceSummary } from '../models/service.model';
import { ApiService } from './api.service';

/** Nommé `ServiceCatalogService` pour éviter toute confusion avec les services Angular. */
@Injectable({ providedIn: 'root' })
export class ServiceCatalogService {
  private readonly api = inject(ApiService);

  search(params: ServiceSearchParams): Observable<PageResponse<ServiceSummary>> {
    return this.api.get<PageResponse<ServiceSummary>>('/services/search', params as Record<string, unknown>);
  }

  getById(id: string): Observable<EventService> {
    return this.api.get<EventService>(`/services/${id}`);
  }

  listByProfessional(professionalId: string): Observable<EventService[]> {
    return this.api.get<EventService[]>(`/services/professional/${professionalId}`);
  }

  create(professionalId: string, request: ServiceCreateRequest): Observable<EventService> {
    return this.api.post<EventService>(`/services/professional/${professionalId}`, request);
  }

  update(id: string, request: Partial<ServiceCreateRequest>): Observable<EventService> {
    return this.api.put<EventService>(`/services/${id}`, request);
  }

  publish(id: string): Observable<void> {
    return this.api.post<void>(`/services/${id}/publish`);
  }

  pause(id: string): Observable<void> {
    return this.api.post<void>(`/services/${id}/pause`);
  }

  archive(id: string): Observable<void> {
    return this.api.post<void>(`/services/${id}/archive`);
  }

  delete(id: string): Observable<void> {
    return this.api.deleteVoid(`/services/${id}`);
  }
}
