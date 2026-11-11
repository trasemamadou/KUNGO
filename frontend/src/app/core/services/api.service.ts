import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';

/**
 * Client HTTP générique : préfixe l'URL de base de l'API, déballe l'enveloppe
 * {@link ApiResponse} pour ne retourner que la donnée utile aux composants, et
 * construit les query params en ignorant les valeurs vides/undefined (pratique
 * pour les DTO de recherche où la plupart des champs sont optionnels).
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  get<T>(path: string, params?: Record<string, unknown>): Observable<T> {
    return this.http
      .get<ApiResponse<T>>(`${this.baseUrl}${path}`, { params: this.buildParams(params) })
      .pipe(map((res) => res.data));
  }

  post<T>(path: string, body?: unknown, params?: Record<string, unknown>): Observable<T> {
    return this.http
      .post<ApiResponse<T>>(`${this.baseUrl}${path}`, body, { params: this.buildParams(params) })
      .pipe(map((res) => res.data));
  }

  put<T>(path: string, body?: unknown): Observable<T> {
    return this.http.put<ApiResponse<T>>(`${this.baseUrl}${path}`, body).pipe(map((res) => res.data));
  }

  patch<T>(path: string, body?: unknown): Observable<T> {
    return this.http.patch<ApiResponse<T>>(`${this.baseUrl}${path}`, body).pipe(map((res) => res.data));
  }

  delete<T = void>(path: string): Observable<T> {
    return this.http.delete<ApiResponse<T>>(`${this.baseUrl}${path}`).pipe(map((res) => res?.data));
  }

  /** Pour les endpoints DELETE sans corps de réponse (204 No Content). */
  deleteVoid(path: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}${path}`);
  }

  upload<T>(path: string, formData: FormData, params?: Record<string, unknown>): Observable<T> {
    return this.http
      .post<ApiResponse<T>>(`${this.baseUrl}${path}`, formData, { params: this.buildParams(params) })
      .pipe(map((res) => res.data));
  }

  private buildParams(params?: Record<string, unknown>): HttpParams {
    let httpParams = new HttpParams();
    if (!params) return httpParams;
    for (const [key, value] of Object.entries(params)) {
      if (value === undefined || value === null || value === '') continue;
      httpParams = httpParams.set(key, String(value));
    }
    return httpParams;
  }
}
