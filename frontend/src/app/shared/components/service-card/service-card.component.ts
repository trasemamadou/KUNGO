import { DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ServiceSummary } from '../../../core/models/service.model';
import { RatingStarsComponent } from '../rating-stars/rating-stars.component';

@Component({
  selector: 'app-service-card',
  standalone: true,
  imports: [RouterLink, DecimalPipe, RatingStarsComponent],
  template: `
    <a
      [routerLink]="['/professionnels', service().professionalId]"
      class="group flex flex-col overflow-hidden rounded-2xl border border-brand-100 border-t-[6px] border-t-gold-500 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-lg dark:border-gray-800 dark:bg-gray-900"
    >
      <div class="relative aspect-[4/3] overflow-hidden bg-gray-100 dark:bg-gray-800">
        @if (service().coverImageUrl) {
          <img
            [src]="service().coverImageUrl"
            [alt]="service().title"
            class="h-full w-full object-cover transition group-hover:scale-105"
          />
        } @else {
          <div class="flex h-full w-full items-center justify-center text-4xl">🎭</div>
        }
        @if (service().professionalVerified) {
          <span class="absolute left-2 top-2 rounded-full bg-brand-600 px-2 py-0.5 text-xs font-medium text-white">
            ✓ Vérifié
          </span>
        }
      </div>

      <div class="flex flex-1 flex-col gap-1 p-3">
        <div class="flex items-center gap-2">
          @if (service().professionalAvatarUrl) {
            <img [src]="service().professionalAvatarUrl" alt="" class="h-6 w-6 rounded-full object-cover" />
          }
          <span class="truncate text-xs text-gray-500 dark:text-gray-400">{{ service().professionalName }}</span>
        </div>

        <h3 class="line-clamp-2 text-sm font-semibold text-brand-700 dark:text-gray-100">{{ service().title }}</h3>

        <div class="mt-auto flex items-center justify-between pt-2">
          <app-rating-stars [rating]="service().professionalRating" [size]="14" [showValue]="true" />
          @if (service().city) {
            <span class="text-xs text-gray-400">{{ service().city }}</span>
          }
        </div>

        <div class="mt-1 font-bold text-brand-700 dark:text-gray-100">
          {{ service().price | number: '1.0-0' }} {{ service().currency }}
        </div>

        <span
          class="mt-2 w-full rounded-full bg-brand-600 py-1.5 text-center text-xs font-semibold text-white transition group-hover:bg-brand-500"
        >
          Voir le profil
        </span>
      </div>
    </a>
  `,
})
export class ServiceCardComponent {
  readonly service = input.required<ServiceSummary>();
}
