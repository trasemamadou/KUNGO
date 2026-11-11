import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Favorite } from '../../core/models/favorite.model';
import { FavoriteService } from '../../core/services/favorite.service';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { RatingStarsComponent } from '../../shared/components/rating-stars/rating-stars.component';

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule, EmptyStateComponent, RatingStarsComponent],
  template: `
    <div class="mx-auto max-w-4xl px-4 py-8 sm:px-6">
      <h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-gray-100">Mes favoris</h1>

      @if (!favorites().length) {
        <app-empty-state icon="💛" title="Aucun favori" description="Ajoutez des professionnels à vos favoris pour les retrouver ici." />
      } @else {
        <div class="grid gap-4 sm:grid-cols-2">
          @for (favorite of favorites(); track favorite.id) {
            <div class="flex items-center gap-3 rounded-xl border border-gray-200 p-4 dark:border-gray-800">
              @if (favorite.professionalAvatarUrl) {
                <img [src]="favorite.professionalAvatarUrl" alt="" class="h-12 w-12 rounded-full object-cover" />
              } @else {
                <div class="flex h-12 w-12 items-center justify-center rounded-full bg-brand-100 text-brand-600 dark:bg-brand-900">
                  {{ favorite.professionalName.charAt(0) }}
                </div>
              }
              <div class="flex-1">
                <a [routerLink]="['/professionnels', favorite.professionalId]" class="font-medium text-gray-900 hover:underline dark:text-gray-100">
                  {{ favorite.businessName || favorite.professionalName }}
                </a>
                <app-rating-stars [rating]="favorite.averageRating" [size]="14" [showValue]="true" />
              </div>
              <button mat-icon-button (click)="remove(favorite)" aria-label="Retirer des favoris">
                <mat-icon>favorite</mat-icon>
              </button>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class FavoritesComponent implements OnInit {
  private readonly favoriteService = inject(FavoriteService);

  readonly favorites = signal<Favorite[]>([]);

  ngOnInit(): void {
    this.favoriteService.list().subscribe((favorites) => this.favorites.set(favorites));
  }

  remove(favorite: Favorite): void {
    this.favoriteService.remove(favorite.professionalId).subscribe(() => {
      this.favorites.update((list) => list.filter((f) => f.id !== favorite.id));
    });
  }
}
