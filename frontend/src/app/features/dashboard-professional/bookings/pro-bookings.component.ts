import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Booking, BOOKING_STATUS_LABELS } from '../../../core/models/booking.model';
import { BookingService } from '../../../core/services/booking.service';
import { ProfessionalService } from '../../../core/services/professional.service';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-pro-bookings',
  standalone: true,
  imports: [DatePipe, RouterLink, EmptyStateComponent],
  template: `
    <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6">
      <h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-gray-100">Réservations reçues</h1>

      @if (!bookings().length) {
        <app-empty-state icon="📭" title="Aucune réservation reçue pour le moment" />
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
                  Client : {{ booking.clientName }} · {{ booking.eventDate | date: 'dd/MM/yyyy' }}
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
export class ProBookingsComponent implements OnInit {
  private readonly bookingService = inject(BookingService);
  private readonly professionalService = inject(ProfessionalService);

  readonly bookings = signal<Booking[]>([]);
  readonly statusLabels = BOOKING_STATUS_LABELS;

  ngOnInit(): void {
    this.professionalService.getCurrentProfile().subscribe((profile) => {
      this.bookingService.listForProfessional(profile.id).subscribe((bookings) => this.bookings.set(bookings));
    });
  }
}
