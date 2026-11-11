import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { Report } from '../../../core/models/report.model';
import { ReviewService } from '../../../core/services/review.service';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ToastService } from '../../../shared/components/toast/toast.service';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [DatePipe, MatButtonModule, EmptyStateComponent],
  template: `
    <div class="mx-auto max-w-4xl px-4 py-8 sm:px-6">
      <h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-gray-100">Signalements</h1>

      @if (!reports().length) {
        <app-empty-state icon="🚩" title="Aucun signalement en attente" />
      } @else {
        <div class="flex flex-col gap-2">
          @for (report of reports(); track report.id) {
            <div class="rounded-lg border border-gray-200 p-4 dark:border-gray-800">
              <div class="flex items-start justify-between">
                <div>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    Signalé par {{ report.reporterName }} — {{ report.createdAt | date: 'dd/MM/yyyy' }}
                  </p>
                  <p class="mt-1 text-gray-800 dark:text-gray-200">{{ report.reason }}</p>
                </div>
                <span class="shrink-0 rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-700 dark:bg-amber-950 dark:text-amber-300">
                  {{ report.status }}
                </span>
              </div>
              @if (report.status === 'PENDING') {
                <div class="mt-3 flex gap-2">
                  <button mat-button (click)="resolve(report)">Marquer comme traité</button>
                  <button mat-button (click)="dismiss(report)">Classer sans suite</button>
                </div>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class AdminReportsComponent implements OnInit {
  private readonly reviewService = inject(ReviewService);
  private readonly toastService = inject(ToastService);

  readonly reports = signal<Report[]>([]);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.reviewService.listReports(0, 50).subscribe((page) => this.reports.set(page.content));
  }

  resolve(report: Report): void {
    this.reviewService.resolveReport(report.id).subscribe(() => {
      this.toastService.success('Signalement traité');
      this.load();
    });
  }

  dismiss(report: Report): void {
    this.reviewService.dismissReport(report.id).subscribe(() => {
      this.toastService.success('Signalement classé sans suite');
      this.load();
    });
  }
}
