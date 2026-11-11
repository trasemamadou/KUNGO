import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { ProfessionalSummary } from '../../../core/models/professional.model';
import { AdminService } from '../../../core/services/admin.service';
import { ProfessionalService } from '../../../core/services/professional.service';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ToastService } from '../../../shared/components/toast/toast.service';

@Component({
  selector: 'app-admin-verifications',
  standalone: true,
  imports: [RouterLink, MatButtonModule, EmptyStateComponent],
  template: `
    <div class="mx-auto max-w-4xl px-4 py-8 sm:px-6">
      <h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-gray-100">Vérifications en attente</h1>

      @if (!professionals().length) {
        <app-empty-state icon="✅" title="Aucune demande en attente" />
      } @else {
        <div class="flex flex-col gap-2">
          @for (pro of professionals(); track pro.id) {
            <div class="flex items-center justify-between rounded-lg border border-gray-200 p-4 dark:border-gray-800">
              <div>
                <a [routerLink]="['/professionnels', pro.id]" class="font-medium text-gray-900 hover:underline dark:text-gray-100">
                  {{ pro.businessName || pro.fullName }}
                </a>
                <p class="text-sm text-gray-500 dark:text-gray-400">{{ pro.city }}</p>
              </div>
              <div class="flex gap-2">
                <button mat-flat-button color="primary" (click)="approve(pro)">Approuver</button>
                <button mat-stroked-button color="warn" (click)="reject(pro)">Rejeter</button>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class AdminVerificationsComponent implements OnInit {
  private readonly professionalService = inject(ProfessionalService);
  private readonly adminService = inject(AdminService);
  private readonly toastService = inject(ToastService);

  readonly professionals = signal<ProfessionalSummary[]>([]);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.professionalService.search({ verificationStatus: 'PENDING', size: 50 }).subscribe((page) => {
      this.professionals.set(page.content);
    });
  }

  approve(pro: ProfessionalSummary): void {
    this.adminService.approveVerification(pro.id).subscribe(() => {
      this.toastService.success('Professionnel vérifié');
      this.load();
    });
  }

  reject(pro: ProfessionalSummary): void {
    this.adminService.rejectVerification(pro.id).subscribe(() => {
      this.toastService.success('Vérification rejetée');
      this.load();
    });
  }
}
