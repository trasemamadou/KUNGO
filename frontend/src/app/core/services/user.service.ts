import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api-response.model';
import { User, UserUpdateRequest } from '../models/user.model';
import { ApiService } from './api.service';

export interface UserSearchParams {
  keyword?: string;
  role?: string;
  status?: string;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly api = inject(ApiService);

  getById(id: string): Observable<User> {
    return this.api.get<User>(`/users/${id}`);
  }

  update(id: string, request: UserUpdateRequest): Observable<User> {
    return this.api.patch<User>(`/users/${id}`, request);
  }

  uploadAvatar(id: string, file: File): Observable<User> {
    const formData = new FormData();
    formData.append('file', file);
    return this.api.upload<User>(`/users/${id}/avatar`, formData);
  }

  search(params: UserSearchParams): Observable<PageResponse<User>> {
    return this.api.get<PageResponse<User>>('/users', params as Record<string, unknown>);
  }

  suspend(id: string): Observable<void> {
    return this.api.patch<void>(`/users/${id}/suspend`);
  }

  reactivate(id: string): Observable<void> {
    return this.api.patch<void>(`/users/${id}/reactivate`);
  }

  ban(id: string): Observable<void> {
    return this.api.patch<void>(`/users/${id}/ban`);
  }
}
