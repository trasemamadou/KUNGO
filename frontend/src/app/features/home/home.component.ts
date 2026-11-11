import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CategoryService } from '../../core/services/category.service';
import { Category } from '../../core/models/category.model';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, FormsModule, MatButtonModule, MatIconModule, SkeletonComponent],
  template: `
    <section
      class="relative overflow-hidden rounded-b-[40px] text-white"
      style="background: linear-gradient(135deg, #6b2e1a 0%, #4d2010 100%)"
    >
      <div
        class="pointer-events-none absolute inset-0"
        style="background: repeating-linear-gradient(45deg, rgba(212,160,23,0.07) 0px, rgba(212,160,23,0.07) 20px, transparent 20px, transparent 40px)"
      ></div>
      <div class="relative mx-auto max-w-5xl px-4 py-16 text-center sm:px-6 sm:py-20">
        <h1 class="text-3xl font-extrabold tracking-tight sm:text-5xl">
          Trouvez l'artisan de votre événement
        </h1>
        <p class="mx-auto mt-4 max-w-2xl text-base text-brand-100 sm:text-lg">
          Musiciens, DJ, photographes, traiteurs, animateurs... Réservez en confiance sur KÛNGO,
          en toute sécurité, près de chez vous.
        </p>

        <form
          class="mx-auto mt-8 flex max-w-xl flex-wrap items-center gap-2 rounded-full bg-white p-2 shadow-xl"
          (submit)="onSearch($event)"
        >
          <mat-icon class="ml-2 text-brand-400">search</mat-icon>
          <input
            type="search"
            name="keyword"
            [(ngModel)]="keyword"
            placeholder="Que recherchez-vous ?"
            class="min-w-0 flex-1 bg-transparent px-1 py-2 text-sm text-gray-900 outline-none"
          />
          <button
            type="submit"
            class="rounded-full bg-accent-500 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-accent-600"
          >
            Rechercher
          </button>
        </form>

        <div class="mt-6 flex flex-wrap justify-center gap-2 text-sm text-brand-100">
          <span>Populaire :</span>
          @for (suggestion of suggestions; track suggestion) {
            <a
              [routerLink]="['/recherche']"
              [queryParams]="{ keyword: suggestion }"
              class="rounded-full bg-white/10 px-3 py-1 hover:bg-white/20"
            >
              {{ suggestion }}
            </a>
          }
        </div>
      </div>
    </section>

    <section class="mx-auto max-w-7xl px-4 py-16 sm:px-6">
      <h2 class="mb-8 text-center text-2xl font-bold text-brand-700 dark:text-brand-200">
        <mat-icon class="!align-middle text-gold-500">celebration</mat-icon>
        Parcourir par catégorie
      </h2>

      <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-6">
        @if (loading()) {
          @for (i of [1, 2, 3, 4, 5, 6]; track i) {
            <app-skeleton height="120px" />
          }
        } @else {
          @for (category of categories(); track category.id) {
            <a
              [routerLink]="['/recherche']"
              [queryParams]="{ categoryId: category.id }"
              class="flex flex-col items-center justify-center gap-2 rounded-2xl border border-brand-100 border-t-4 border-t-gold-500 bg-white p-6 text-center shadow-sm transition hover:-translate-y-0.5 hover:shadow-md dark:border-gray-800 dark:bg-gray-900"
            >
              <span class="text-3xl">{{ category.icon || '🎪' }}</span>
              <span class="text-sm font-medium text-brand-700 dark:text-gray-200">{{ category.name }}</span>
            </a>
          }
        }
      </div>
    </section>

    <section class="bg-brand-50 py-16 dark:bg-gray-900">
      <div class="mx-auto grid max-w-6xl gap-8 px-4 sm:px-6 md:grid-cols-3">
        <div class="text-center">
          <span class="text-4xl">🔎</span>
          <h3 class="mt-3 font-semibold text-brand-700 dark:text-gray-100">1. Recherchez</h3>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Filtrez par catégorie, ville, budget, note et disponibilité.
          </p>
        </div>
        <div class="text-center">
          <span class="text-4xl">📅</span>
          <h3 class="mt-3 font-semibold text-brand-700 dark:text-gray-100">2. Réservez</h3>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Envoyez votre demande, échangez avec le professionnel et payez en toute sécurité.
          </p>
        </div>
        <div class="text-center">
          <span class="text-4xl">🎉</span>
          <h3 class="mt-3 font-semibold text-brand-700 dark:text-gray-100">3. Profitez</h3>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Vivez votre événement, puis laissez un avis pour aider la communauté.
          </p>
        </div>
      </div>
    </section>

    <section class="mx-auto max-w-5xl px-4 py-16 text-center sm:px-6">
      <h2 class="text-2xl font-bold text-brand-700 dark:text-gray-100">Vous êtes un professionnel de l'événementiel ?</h2>
      <p class="mx-auto mt-3 max-w-xl text-sm text-gray-500 dark:text-gray-400">
        Rejoignez KÛNGO, publiez vos prestations et développez votre activité auprès de milliers de clients.
      </p>
      <a
        routerLink="/auth/register"
        class="mt-6 inline-block rounded-full bg-accent-500 px-8 py-2.5 text-sm font-bold text-white transition hover:bg-accent-600"
      >
        Devenir professionnel
      </a>
    </section>
  `,
})
export class HomeComponent implements OnInit {
  private readonly categoryService = inject(CategoryService);
  private readonly router = inject(Router);

  readonly categories = signal<Category[]>([]);
  readonly loading = signal(true);

  keyword = '';
  readonly suggestions = ['DJ', 'Photographe', 'Traiteur', 'Animateur', 'Décoration'];

  ngOnInit(): void {
    this.categoryService.getRootCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onSearch(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/recherche'], { queryParams: { keyword: this.keyword || null } });
  }
}
