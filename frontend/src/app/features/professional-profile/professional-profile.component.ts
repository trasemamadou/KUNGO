import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { AuthService } from '../../core/auth/auth.service';
import { EventService } from '../../core/models/service.model';
import { ProfessionalProfile } from '../../core/models/professional.model';
import { Review } from '../../core/models/review.model';
import { BookingService } from '../../core/services/booking.service';
import { FavoriteService } from '../../core/services/favorite.service';
import { ProfessionalService } from '../../core/services/professional.service';
import { ReviewService } from '../../core/services/review.service';
import { ServiceCatalogService } from '../../core/services/service-catalog.service';
import { ToastService } from '../../shared/components/toast/toast.service';
import { RatingStarsComponent } from '../../shared/components/rating-stars/rating-stars.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';

@Component({
  selector: 'app-professional-profile',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    RatingStarsComponent,
    SkeletonComponent,
  ],
  template: `
    @if (loading()) {
      <div class="mx-auto max-w-5xl px-4 py-8">
        <app-skeleton height="220px" />
      </div>
    } @else if (profile(); as profile) {
      <div class="bg-gray-50 dark:bg-gray-900">
        <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6">
          <div class="flex flex-col gap-4 rounded-xl bg-white p-6 shadow-sm dark:bg-gray-800 sm:flex-row sm:items-center">
            @if (profile.avatarUrl) {
              <img [src]="profile.avatarUrl" alt="" class="h-24 w-24 rounded-full object-cover" />
            } @else {
              <div class="flex h-24 w-24 items-center justify-center rounded-full bg-brand-100 text-3xl text-brand-600 dark:bg-brand-900">
                {{ profile.fullName.charAt(0) }}
              </div>
            }
            <div class="flex-1">
              <div class="flex items-center gap-2">
                <h1 class="text-xl font-bold text-gray-900 dark:text-gray-100">
                  {{ profile.businessName || profile.fullName }}
                </h1>
                @if (profile.verificationStatus === 'VERIFIED') {
                  <span class="rounded-full bg-brand-600 px-2 py-0.5 text-xs font-medium text-white">✓ Vérifié</span>
                }
                @if (profile.premium) {
                  <span class="rounded-full bg-amber-400 px-2 py-0.5 text-xs font-medium text-gray-900">⭐ Premium</span>
                }
              </div>
              <p class="text-sm text-gray-500 dark:text-gray-400">
                {{ profile.address?.city }}{{ profile.address?.country ? ', ' + profile.address?.country : '' }}
                · {{ profile.yearsExperience }} ans d'expérience
              </p>
              <app-rating-stars [rating]="profile.averageRating" [showValue]="true" class="mt-1 inline-block" />
              <span class="ml-1 text-sm text-gray-400">({{ profile.totalReviews }} avis)</span>
            </div>
            <button mat-stroked-button (click)="toggleFavorite()">
              <mat-icon>{{ isFavorite() ? 'favorite' : 'favorite_border' }}</mat-icon>
              {{ isFavorite() ? 'Favori' : 'Ajouter aux favoris' }}
            </button>
          </div>
        </div>
      </div>

      <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6">
        <mat-tab-group>
          <mat-tab label="À propos">
            <div class="py-6">
              @if (profile.biography) {
                <p class="whitespace-pre-line text-gray-700 dark:text-gray-300">{{ profile.biography }}</p>
              }
              @if (profile.skills.length) {
                <h3 class="mb-2 mt-6 text-sm font-semibold text-gray-800 dark:text-gray-100">Compétences</h3>
                <mat-chip-set>
                  @for (skill of profile.skills; track skill.id) {
                    <mat-chip>{{ skill.name }}</mat-chip>
                  }
                </mat-chip-set>
              }
              @if (profile.languages.length) {
                <h3 class="mb-2 mt-6 text-sm font-semibold text-gray-800 dark:text-gray-100">Langues</h3>
                <mat-chip-set>
                  @for (lang of profile.languages; track lang.id) {
                    <mat-chip>{{ lang.name }}</mat-chip>
                  }
                </mat-chip-set>
              }
            </div>
          </mat-tab>

          <mat-tab label="Prestations ({{ services().length }})">
            <div class="grid gap-4 py-6 sm:grid-cols-2">
              @for (service of services(); track service.id) {
                <div class="flex flex-col rounded-xl border border-gray-200 p-4 dark:border-gray-800">
                  <h3 class="font-semibold text-gray-900 dark:text-gray-100">{{ service.title }}</h3>
                  <p class="mt-1 line-clamp-2 text-sm text-gray-500 dark:text-gray-400">{{ service.description }}</p>
                  <div class="mt-3 flex items-center justify-between">
                    <span class="font-bold text-gray-900 dark:text-gray-100">
                      {{ service.price }} {{ service.currency }}
                    </span>
                    <button mat-flat-button color="primary" (click)="selectServiceForBooking(service)">
                      Demander une prestation
                    </button>
                  </div>
                </div>
              }
            </div>
          </mat-tab>

          <mat-tab label="Avis ({{ profile.totalReviews }})">
            <div class="flex flex-col gap-4 py-6">
              @for (review of reviews(); track review.id) {
                <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
                  <div class="flex items-center justify-between">
                    <span class="font-medium text-gray-800 dark:text-gray-100">{{ review.authorName }}</span>
                    <app-rating-stars [rating]="review.rating" [size]="14" />
                  </div>
                  @if (review.comment) {
                    <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">{{ review.comment }}</p>
                  }
                  <p class="mt-1 text-xs text-gray-400">{{ review.createdAt | date: 'dd/MM/yyyy' }}</p>
                  @if (review.professionalResponse) {
                    <div class="mt-2 rounded-md bg-gray-50 p-2 text-sm text-gray-600 dark:bg-gray-800 dark:text-gray-300">
                      <strong>Réponse :</strong> {{ review.professionalResponse }}
                    </div>
                  }
                </div>
              } @empty {
                <p class="text-sm text-gray-500 dark:text-gray-400">Aucun avis pour le moment.</p>
              }
            </div>
          </mat-tab>
        </mat-tab-group>

        @if (selectedService(); as service) {
          <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" (click)="selectedService.set(null)">
            <div
              class="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl border-t-[6px] border-t-gold-500 bg-white p-6 dark:bg-gray-800"
              (click)="$event.stopPropagation()"
            >
              <h2 class="mb-1 text-lg font-bold text-brand-700 dark:text-gray-100">Demander une prestation</h2>
              <p class="mb-4 text-sm text-gray-500 dark:text-gray-400">{{ service.title }}</p>
              <form [formGroup]="bookingForm" class="flex flex-col gap-3" (ngSubmit)="submitBooking(service.id)">
                <mat-form-field appearance="outline">
                  <mat-label>Type d'événement</mat-label>
                  <mat-select formControlName="eventType">
                    @for (type of eventTypes; track type) {
                      <mat-option [value]="type">{{ type }}</mat-option>
                    }
                  </mat-select>
                </mat-form-field>
                <div class="grid grid-cols-2 gap-3">
                  <mat-form-field appearance="outline">
                    <mat-label>Date de l'événement</mat-label>
                    <input matInput [matDatepicker]="picker" formControlName="eventDate" />
                    <mat-datepicker-toggle matSuffix [for]="picker" />
                    <mat-datepicker #picker />
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Heure</mat-label>
                    <input matInput type="time" formControlName="eventTime" />
                  </mat-form-field>
                </div>
                <mat-form-field appearance="outline">
                  <mat-label>Lieu de l'événement</mat-label>
                  <input matInput formControlName="eventAddress" />
                </mat-form-field>
                <div class="grid grid-cols-2 gap-3">
                  <mat-form-field appearance="outline">
                    <mat-label>Durée estimée (min)</mat-label>
                    <input matInput type="number" formControlName="estimatedDurationMinutes" />
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Nombre de participants</mat-label>
                    <input matInput type="number" formControlName="participantCount" />
                  </mat-form-field>
                </div>
                <mat-form-field appearance="outline">
                  <mat-label>Budget proposé ({{ service.currency }})</mat-label>
                  <input matInput type="number" formControlName="proposedBudget" [placeholder]="service.price + ''" />
                  <mat-hint>Laissez vide pour accepter le tarif affiché ({{ service.price }} {{ service.currency }})</mat-hint>
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Description détaillée</mat-label>
                  <textarea matInput formControlName="description" rows="3"></textarea>
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Demandes particulières (optionnel)</mat-label>
                  <textarea matInput formControlName="specialRequests" rows="2"></textarea>
                </mat-form-field>
                <div class="flex justify-end gap-2">
                  <button mat-button type="button" (click)="selectedService.set(null)">Annuler</button>
                  <button mat-flat-button color="primary" type="submit" [disabled]="bookingForm.invalid">
                    Envoyer la demande
                  </button>
                </div>
              </form>
            </div>
          </div>
        }
      </div>
    }
  `,
})
export class ProfessionalProfileComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly professionalService = inject(ProfessionalService);
  private readonly serviceCatalog = inject(ServiceCatalogService);
  private readonly reviewService = inject(ReviewService);
  private readonly favoriteService = inject(FavoriteService);
  private readonly bookingService = inject(BookingService);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  readonly profile = signal<ProfessionalProfile | null>(null);
  readonly services = signal<EventService[]>([]);
  readonly reviews = signal<Review[]>([]);
  readonly loading = signal(true);
  readonly isFavorite = signal(false);
  readonly selectedService = signal<EventService | null>(null);

  readonly eventTypes = ['Mariage', 'Anniversaire', 'Conférence', 'Concert', 'Soirée privée', 'Autre'];

  readonly bookingForm = this.fb.nonNullable.group({
    eventType: ['Mariage'],
    eventDate: [null as Date | null, Validators.required],
    eventTime: ['19:00'],
    eventAddress: [''],
    estimatedDurationMinutes: [null as number | null],
    participantCount: [null as number | null],
    proposedBudget: [null as number | null],
    description: [''],
    specialRequests: [''],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;

    this.professionalService.getById(id).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    this.serviceCatalog.listByProfessional(id).subscribe((services) => this.services.set(services));
    this.reviewService.listForTarget(id, 0, 20).subscribe((page) => this.reviews.set(page.content));

    if (this.authService.isAuthenticated()) {
      this.favoriteService.isFavorite(id).subscribe((fav) => this.isFavorite.set(fav));
    }
  }

  toggleFavorite(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/auth/login']);
      return;
    }
    const id = this.profile()!.id;
    if (this.isFavorite()) {
      this.favoriteService.remove(id).subscribe(() => this.isFavorite.set(false));
    } else {
      this.favoriteService.add(id).subscribe(() => this.isFavorite.set(true));
    }
  }

  selectServiceForBooking(service: EventService): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/auth/login']);
      return;
    }
    this.selectedService.set(service);
  }

  submitBooking(serviceId: string): void {
    if (this.bookingForm.invalid) return;
    const value = this.bookingForm.getRawValue();

    const eventDate = value.eventDate!;
    const [hours, minutes] = (value.eventTime || '00:00').split(':').map(Number);
    eventDate.setHours(hours, minutes, 0, 0);

    this.bookingService
      .create({
        serviceId,
        eventType: value.eventType || undefined,
        eventDate: eventDate.toISOString(),
        eventAddress: value.eventAddress || undefined,
        estimatedDurationMinutes: value.estimatedDurationMinutes || undefined,
        participantCount: value.participantCount || undefined,
        proposedBudget: value.proposedBudget || undefined,
        description: value.description || undefined,
        specialRequests: value.specialRequests || undefined,
      })
      .subscribe({
        next: (booking) => {
          this.toastService.success('Demande de réservation envoyée !');
          this.selectedService.set(null);
          this.router.navigate(['/reservations', booking.id]);
        },
      });
  }
}
