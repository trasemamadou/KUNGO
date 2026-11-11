import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AppNotification } from '../../core/models/notification.model';
import { NotificationService } from '../../core/services/notification.service';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [DatePipe, MatButtonModule, MatIconModule, MatTooltipModule, EmptyStateComponent],
  template: `
    <div class="mx-auto max-w-2xl px-4 py-8 sm:px-6">
      <div class="mb-6 flex items-center justify-between">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Notifications</h1>
        @if (notifications().length) {
          <button mat-button (click)="markAllRead()">Tout marquer comme lu</button>
        }
      </div>

      @if (!notifications().length) {
        <app-empty-state icon="🔔" title="Aucune notification" description="Vous serez notifié ici des mises à jour de vos réservations." />
      } @else {
        <ul class="flex flex-col gap-2">
          @for (notification of notifications(); track notification.id) {
            <li
              class="flex items-start gap-3 rounded-lg border border-gray-200 p-4 dark:border-gray-800"
              [class.bg-brand-50]="!notification.read"
              [class.dark:bg-brand-950]="!notification.read"
            >
              <mat-icon class="text-brand-600">{{ notification.read ? 'notifications_none' : 'notifications_active' }}</mat-icon>
              <div class="flex-1">
                <p class="font-medium text-gray-900 dark:text-gray-100">{{ notification.title }}</p>
                <p class="text-sm text-gray-500 dark:text-gray-400">{{ notification.message }}</p>
                <p class="mt-1 text-xs text-gray-400">{{ notification.sentAt | date: 'dd/MM/yyyy HH:mm' }}</p>
              </div>
              @if (!notification.read) {
                <button mat-icon-button (click)="markRead(notification)" matTooltip="Marquer comme lu">
                  <mat-icon>done</mat-icon>
                </button>
              }
            </li>
          }
        </ul>
      }
    </div>
  `,
})
export class NotificationsComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);

  readonly notifications = signal<AppNotification[]>([]);

  ngOnInit(): void {
    this.notificationService.list().subscribe((page) => this.notifications.set(page.content));
  }

  markRead(notification: AppNotification): void {
    this.notificationService.markRead(notification.id).subscribe(() => {
      notification.read = true;
      this.notifications.update((list) => [...list]);
    });
  }

  markAllRead(): void {
    this.notificationService.markAllRead().subscribe(() => {
      this.notifications.update((list) => list.map((n) => ({ ...n, read: true })));
    });
  }
}
