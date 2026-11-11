import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminService, DashboardStats } from '../../core/services/admin.service';
import { BarChartComponent, BarChartDatum } from '../../shared/components/bar-chart/bar-chart.component';
import { StatCardComponent } from '../../shared/components/stat-card/stat-card.component';

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [RouterLink, StatCardComponent, BarChartComponent],
  template: `
    <div class="mx-auto max-w-6xl px-4 py-8 sm:px-6">
      <div class="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Administration</h1>
        <nav class="flex flex-wrap gap-2 text-sm">
          <a routerLink="/admin/utilisateurs" class="rounded-full border border-gray-200 px-3 py-1.5 hover:border-brand-400 dark:border-gray-800">Utilisateurs</a>
          <a routerLink="/admin/verifications" class="rounded-full border border-gray-200 px-3 py-1.5 hover:border-brand-400 dark:border-gray-800">Vérifications</a>
          <a routerLink="/admin/signalements" class="rounded-full border border-gray-200 px-3 py-1.5 hover:border-brand-400 dark:border-gray-800">Signalements</a>
          <a routerLink="/admin/commissions" class="rounded-full border border-gray-200 px-3 py-1.5 hover:border-brand-400 dark:border-gray-800">Commissions</a>
        </nav>
      </div>

      @if (stats(); as stats) {
        <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
          <app-stat-card icon="🙋" label="Clients" [value]="stats.totalClients" />
          <app-stat-card icon="🎤" label="Professionnels" [value]="stats.totalProfessionals" />
          <app-stat-card icon="📅" label="Réservations" [value]="stats.totalBookings" />
          <app-stat-card icon="💰" label="Volume total (XOF)" [value]="stats.totalGrossVolume" />
          <app-stat-card icon="🏦" label="Revenus commission (XOF)" [value]="stats.totalCommissionRevenue" />
          <app-stat-card icon="🛡️" label="Vérifications en attente" [value]="stats.pendingVerifications" />
          <app-stat-card icon="🚩" label="Signalements en attente" [value]="stats.pendingReports" />
          <app-stat-card icon="🎪" label="Prestations publiées" [value]="stats.totalServices" />
        </div>

        <h2 class="mb-4 mt-8 text-lg font-semibold text-gray-900 dark:text-gray-100">Vue d'ensemble des réservations</h2>
        <div class="rounded-xl border border-gray-200 p-6 dark:border-gray-800">
          <app-bar-chart [data]="bookingChartData(stats)" />
        </div>
      }
    </div>
  `,
})
export class DashboardAdminComponent implements OnInit {
  private readonly adminService = inject(AdminService);

  readonly stats = signal<DashboardStats | null>(null);

  ngOnInit(): void {
    this.adminService.getDashboardStats().subscribe((stats) => this.stats.set(stats));
  }

  bookingChartData(stats: DashboardStats): BarChartDatum[] {
    return [
      { label: 'En attente', value: stats.pendingBookings },
      { label: 'Terminées', value: stats.completedBookings },
      { label: 'Total', value: stats.totalBookings },
    ];
  }
}
