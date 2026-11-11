import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Favorite } from '../models/favorite.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class FavoriteService {
  private readonly api = inject(ApiService);

  list(): Observable<Favorite[]> {
    return this.api.get<Favorite[]>('/favorites');
  }

  add(professionalId: string): Observable<Favorite> {
    return this.api.post<Favorite>(`/favorites/${professionalId}`);
  }

  remove(professionalId: string): Observable<void> {
    return this.api.deleteVoid(`/favorites/${professionalId}`);
  }

  isFavorite(professionalId: string): Observable<boolean> {
    return this.api.get<boolean>(`/favorites/${professionalId}/status`);
  }
}
