import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { PageResponse } from '../models/api-response.model';
import { AppNotification } from '../models/notification.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly api = inject(ApiService);

  readonly unreadCount = signal(0);

  list(page = 0, size = 20): Observable<PageResponse<AppNotification>> {
    return this.api.get<PageResponse<AppNotification>>('/notifications', { page, size });
  }

  refreshUnreadCount(): Observable<number> {
    return this.api.get<number>('/notifications/unread-count').pipe(tap((count) => this.unreadCount.set(count)));
  }

  markRead(id: string): Observable<void> {
    return this.api.patch<void>(`/notifications/${id}/read`).pipe(
      tap(() => this.unreadCount.update((c) => Math.max(0, c - 1)))
    );
  }

  markAllRead(): Observable<void> {
    return this.api.patch<void>('/notifications/read-all').pipe(tap(() => this.unreadCount.set(0)));
  }
}
