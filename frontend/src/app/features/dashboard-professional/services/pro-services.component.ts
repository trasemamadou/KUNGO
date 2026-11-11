import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Category } from '../../../core/models/category.model';
import { EventService, ServiceCreateRequest } from '../../../core/models/service.model';
import { CategoryService } from '../../../core/services/category.service';
import { ProfessionalService } from '../../../core/services/professional.service';
import { ServiceCatalogService } from '../../../core/services/service-catalog.service';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ToastService } from '../../../shared/components/toast/toast.service';

@Component({
  selector: 'app-pro-services',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatTooltipModule,
    EmptyStateComponent,
  ],
  template: `
    <div class="mx-auto max-w-4xl px-4 py-8 sm:px-6">
      <div class="mb-6 flex items-center justify-between">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Mes prestations</h1>
        <button mat-flat-button color="primary" (click)="showForm.set(!showForm())">
          <mat-icon>add</mat-icon> Nouvelle prestation
        </button>
      </div>

      @if (showForm()) {
        <form [formGroup]="form" class="mb-8 grid gap-3 rounded-xl border border-gray-200 p-4 dark:border-gray-800 sm:grid-cols-2" (ngSubmit)="create()">
          <mat-form-field appearance="outline">
            <mat-label>Titre</mat-label>
            <input matInput formControlName="title" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Catégorie</mat-label>
            <mat-select formControlName="categoryId">
              @for (category of categories(); track category.id) {
                <mat-option [value]="category.id">{{ category.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" class="sm:col-span-2">
            <mat-label>Description</mat-label>
            <textarea matInput formControlName="description" rows="3"></textarea>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Type de tarif</mat-label>
            <mat-select formControlName="priceType">
              <mat-option value="FIXED">Prix fixe</mat-option>
              <mat-option value="HOURLY">Tarif horaire</mat-option>
              <mat-option value="NEGOTIABLE">Négociable</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Prix (XOF)</mat-label>
            <input matInput type="number" formControlName="price" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Durée (minutes)</mat-label>
            <input matInput type="number" formControlName="durationMinutes" />
          </mat-form-field>
          <div class="flex items-end gap-2">
            <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid">Créer</button>
            <button mat-button type="button" (click)="showForm.set(false)">Annuler</button>
          </div>
        </form>
      }

      @if (!services().length) {
        <app-empty-state icon="🎪" title="Aucune prestation" description="Ajoutez votre première prestation pour commencer à recevoir des demandes." />
      } @else {
        <div class="flex flex-col gap-2">
          @for (service of services(); track service.id) {
            <div class="flex items-center justify-between rounded-lg border border-gray-200 p-4 dark:border-gray-800">
              <div>
                <p class="font-medium text-gray-900 dark:text-gray-100">{{ service.title }}</p>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                  {{ service.price }} {{ service.currency }} · {{ service.status }}
                </p>
              </div>
              <div class="flex gap-1">
                @if (service.status !== 'PUBLISHED') {
                  <button mat-icon-button (click)="publish(service)" matTooltip="Publier"><mat-icon>publish</mat-icon></button>
                }
                @if (service.status === 'PUBLISHED') {
                  <button mat-icon-button (click)="pause(service)" matTooltip="Mettre en pause"><mat-icon>pause</mat-icon></button>
                }
                <button mat-icon-button (click)="archive(service)" matTooltip="Archiver"><mat-icon>archive</mat-icon></button>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class ProServicesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly serviceCatalog = inject(ServiceCatalogService);
  private readonly categoryService = inject(CategoryService);
  private readonly professionalService = inject(ProfessionalService);
  private readonly toastService = inject(ToastService);

  readonly services = signal<EventService[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly showForm = signal(false);
  private professionalId = '';

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(5)]],
    categoryId: ['', Validators.required],
    description: [''],
    priceType: ['FIXED' as ServiceCreateRequest['priceType'], Validators.required],
    price: [0, [Validators.required, Validators.min(0)]],
    durationMinutes: [60],
  });

  ngOnInit(): void {
    this.categoryService.getRootCategories().subscribe((categories) => this.categories.set(categories));
    this.professionalService.getCurrentProfile().subscribe((profile) => {
      this.professionalId = profile.id;
      this.loadServices();
    });
  }

  private loadServices(): void {
    this.serviceCatalog.listByProfessional(this.professionalId).subscribe((services) => this.services.set(services));
  }

  create(): void {
    if (this.form.invalid) return;
    this.serviceCatalog.create(this.professionalId, this.form.getRawValue()).subscribe({
      next: () => {
        this.toastService.success('Prestation créée');
        this.showForm.set(false);
        this.form.reset({ priceType: 'FIXED', price: 0, durationMinutes: 60 });
        this.loadServices();
      },
    });
  }

  publish(service: EventService): void {
    this.serviceCatalog.publish(service.id).subscribe(() => this.loadServices());
  }

  pause(service: EventService): void {
    this.serviceCatalog.pause(service.id).subscribe(() => this.loadServices());
  }

  archive(service: EventService): void {
    this.serviceCatalog.archive(service.id).subscribe(() => this.loadServices());
  }
}
