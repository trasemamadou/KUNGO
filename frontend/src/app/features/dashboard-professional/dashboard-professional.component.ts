import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Booking, BOOKING_STATUS_LABELS } from '../../core/models/booking.model';
import { ProfessionalProfile } from '../../core/models/professional.model';
import { BookingService } from '../../core/services/booking.service';
import { ProfessionalService } from '../../core/services/professional.service';
import { BarChartComponent, BarChartDatum } from '../../shared/components/bar-chart/bar-chart.component';
import { StatCardComponent } from '../../shared/components/stat-card/stat-card.component';

@Component({
  selector: 'app-dashboard-professional',
  standalone: true,
  imports: [RouterLink, StatCardComponent, BarChartComponent],
  template: `
    <div class="mx-auto max-w-6xl px-4 py-8 sm:px-6">
      <div class="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Tableau de bord professionnel</h1>
        <nav class="flex gap-2 text-sm">
          <a routerLink="/pro/prestations" class="rounded-full border border-gray-200 px-3 py-1.5 hover:border-brand-400 dark:border-gray-800">
            Mes prestations
          </a>
          <a routerLink="/pro/reservations" class="rounded-full border border-gray-200 px-3 py-1.5 hover:border-brand-400 dark:border-gray-800">
            Réservations
          </a>
          <a routerLink="/pro/disponibilites" class="rounded-full border border-gray-200 px-3 py-1.5 hover:border-brand-400 dark:border-gray-800">
            Disponibilités
          </a>
        </nav>
      </div>

      @if (profile(); as profile) {
        <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
          <app-stat-card icon="📅" label="Réservations" [value]="profile.totalBookings" />
          <app-stat-card icon="⭐" label="Note moyenne" [value]="profile.averageRating.toFixed(1)" />
          <app-stat-card icon="💬" label="Avis reçus" [value]="profile.totalReviews" />
          <app-stat-card
            icon="🛡️"
            label="Statut"
            [value]="profile.verificationStatus === 'VERIFIED' ? 'Vérifié' : 'Non vérifié'"
          />
        </div>

        @if (profile.verificationStatus === 'UNVERIFIED') {
          <div class="mt-6 rounded-lg bg-amber-50 p-4 text-sm text-amber-800 dark:bg-amber-950 dark:text-amber-300">
            Votre compte n'est pas encore vérifié.
            <a routerLink="/pro/prestations" class="font-medium underline">Complétez votre profil</a> pour soumettre une demande de vérification.
          </div>
        }
      }

      <h2 class="mb-4 mt-8 text-lg font-semibold text-gray-900 dark:text-gray-100">Réservations par statut</h2>
      <div class="rounded-xl border border-gray-200 p-6 dark:border-gray-800">
        <app-bar-chart [data]="bookingsByStatus()" />
      </div>
    </div>
  `,
})
export class DashboardProfessionalComponent implements OnInit {
  private readonly professionalService = inject(ProfessionalService);
  private readonly bookingService = inject(BookingService);

  readonly profile = signal<ProfessionalProfile | null>(null);
  readonly bookings = signal<Booking[]>([]);

  readonly bookingsByStatus = computed<BarChartDatum[]>(() => {
    const counts: Record<string, number> = {};
    for (const booking of this.bookings()) {
      counts[booking.status] = (counts[booking.status] || 0) + 1;
    }
    return Object.entries(counts).map(([status, value]) => ({
      label: BOOKING_STATUS_LABELS[status as Booking['status']],
      value,
    }));
  });

  ngOnInit(): void {
    this.professionalService.getCurrentProfile().subscribe((profile) => {
      this.profile.set(profile);
      this.bookingService.listForProfessional(profile.id).subscribe((bookings) => this.bookings.set(bookings));
    });
  }
}
