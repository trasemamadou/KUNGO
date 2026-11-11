import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Booking, BOOKING_STATUS_LABELS } from '../../core/models/booking.model';
import { BookingService } from '../../core/services/booking.service';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { StatCardComponent } from '../../shared/components/stat-card/stat-card.component';

@Component({
  selector: 'app-dashboard-client',
  standalone: true,
  imports: [DatePipe, RouterLink, StatCardComponent, EmptyStateComponent],
  template: `
    <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6">
      <h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-gray-100">Mon tableau de bord</h1>

      <div class="grid grid-cols-2 gap-4 sm:grid-cols-3">
        <app-stat-card icon="📅" label="Réservations actives" [value]="activeCount()" />
        <app-stat-card icon="✅" label="Terminées" [value]="completedCount()" />
        <app-stat-card icon="💰" label="Total dépensé" [value]="totalSpent() + ' XOF'" />
      </div>

      <h2 class="mb-4 mt-8 text-lg font-semibold text-gray-900 dark:text-gray-100">Mes réservations récentes</h2>

      @if (!bookings().length) {
        <app-empty-state icon="📭" title="Aucune réservation" description="Explorez les professionnels disponibles pour réserver votre première prestation." />
      } @else {
        <div class="flex flex-col gap-2">
          @for (booking of bookings(); track booking.id) {
            <a
              [routerLink]="['/reservations', booking.id]"
              class="flex items-center justify-between rounded-lg border border-gray-200 p-4 hover:border-brand-400 dark:border-gray-800"
            >
              <div>
                <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.serviceTitle }}</p>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                  {{ booking.professionalName }} · {{ booking.eventDate | date: 'dd/MM/yyyy' }}
                </p>
              </div>
              <span class="rounded-full bg-brand-100 px-3 py-1 text-xs font-semibold text-brand-700 dark:bg-brand-900 dark:text-brand-300">
                {{ statusLabels[booking.status] }}
              </span>
            </a>
          }
        </div>
      }
    </div>
  `,
})
export class DashboardClientComponent implements OnInit {
  private readonly bookingService = inject(BookingService);

  readonly bookings = signal<Booking[]>([]);
  readonly statusLabels = BOOKING_STATUS_LABELS;

  readonly activeCount = computed(
    () =>
      this.bookings().filter(
        (b) => !['COMPLETED', 'PAYOUT_SETTLED', 'CANCELLED', 'REJECTED'].includes(b.status)
      ).length
  );
  readonly completedCount = computed(
    () => this.bookings().filter((b) => ['COMPLETED', 'PAYOUT_SETTLED'].includes(b.status)).length
  );
  readonly totalSpent = computed(() =>
    this.bookings()
      .filter((b) =>
        ['PAID', 'PREPARING', 'EN_ROUTE', 'ARRIVED', 'IN_PROGRESS', 'COMPLETED', 'PAYOUT_SETTLED'].includes(b.status)
      )
      .reduce((sum, b) => sum + b.totalPrice, 0)
  );

  ngOnInit(): void {
    this.bookingService.listForCurrentClient().subscribe((bookings) => this.bookings.set(bookings));
  }
}
