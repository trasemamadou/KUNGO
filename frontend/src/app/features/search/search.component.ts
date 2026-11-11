import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSliderModule } from '@angular/material/slider';
import { debounceTime } from 'rxjs';
import { Category } from '../../core/models/category.model';
import { ServiceSearchParams, ServiceSummary } from '../../core/models/service.model';
import { CategoryService } from '../../core/services/category.service';
import { ServiceCatalogService } from '../../core/services/service-catalog.service';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ServiceCardComponent } from '../../shared/components/service-card/service-card.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSliderModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    ServiceCardComponent,
    SkeletonComponent,
    EmptyStateComponent,
  ],
  template: `
    <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
      <h1 class="mb-6 text-2xl font-bold text-brand-700 dark:text-gray-100">Rechercher un professionnel</h1>

      <div class="grid gap-8 lg:grid-cols-[280px_1fr]">
        <aside class="h-fit rounded-2xl border border-brand-100 border-l-4 border-l-gold-500 bg-white p-4 shadow-sm dark:border-gray-800 dark:bg-gray-900 lg:sticky lg:top-20">
          <form [formGroup]="form" class="flex flex-col gap-4">
            <mat-form-field appearance="outline" class="!w-full">
              <mat-label>Mot-clé</mat-label>
              <input matInput formControlName="keyword" placeholder="DJ, photographe..." />
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="!w-full">
              <mat-label>Catégorie</mat-label>
              <mat-select formControlName="categoryId">
                <mat-option [value]="undefined">Toutes</mat-option>
                @for (category of categories(); track category.id) {
                  <mat-option [value]="category.id">{{ category.name }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="!w-full">
              <mat-label>Ville</mat-label>
              <input matInput formControlName="city" placeholder="Dakar, Abidjan..." />
            </mat-form-field>

            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-300">
                Budget max : {{ form.controls.maxPrice.value || 'Illimité' }}
              </label>
              <mat-slider min="0" max="2000000" step="10000" showTickMarks class="!w-full">
                <input matSliderThumb formControlName="maxPrice" />
              </mat-slider>
            </div>

            <mat-form-field appearance="outline" class="!w-full">
              <mat-label>Note minimale</mat-label>
              <mat-select formControlName="minRating">
                <mat-option [value]="undefined">Toutes</mat-option>
                <mat-option [value]="4">4 étoiles et +</mat-option>
                <mat-option [value]="4.5">4,5 étoiles et +</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="!w-full">
              <mat-label>Langue</mat-label>
              <mat-select formControlName="languageCode">
                <mat-option [value]="undefined">Toutes</mat-option>
                <mat-option value="fr">Français</mat-option>
                <mat-option value="en">Anglais</mat-option>
                <mat-option value="wo">Wolof</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-checkbox formControlName="verifiedOnly">Professionnels vérifiés uniquement</mat-checkbox>

            <button mat-button type="button" (click)="resetFilters()">Réinitialiser les filtres</button>
          </form>
        </aside>

        <section>
          @if (loading()) {
            <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-4">
              @for (i of [1, 2, 3, 4, 5, 6, 7, 8]; track i) {
                <app-skeleton height="260px" />
              }
            </div>
          } @else if (results().length === 0) {
            <app-empty-state icon="🔍" title="Aucun résultat" description="Essayez d'élargir vos critères de recherche." />
          } @else {
            <p class="mb-4 text-sm text-gray-500 dark:text-gray-400">{{ totalElements() }} résultat(s)</p>
            <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-4">
              @for (service of results(); track service.id) {
                <app-service-card [service]="service" />
              }
            </div>
            <mat-paginator
              class="mt-6 bg-transparent"
              [length]="totalElements()"
              [pageSize]="pageSize()"
              [pageIndex]="pageIndex()"
              [pageSizeOptions]="[12, 24, 48]"
              (page)="onPageChange($event)"
            />
          }
        </section>
      </div>
    </div>
  `,
})
export class SearchComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly serviceCatalog = inject(ServiceCatalogService);
  private readonly categoryService = inject(CategoryService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly categories = signal<Category[]>([]);
  readonly results = signal<ServiceSummary[]>([]);
  readonly loading = signal(true);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(12);

  readonly form = this.fb.nonNullable.group({
    keyword: [''],
    categoryId: [undefined as string | undefined],
    city: [''],
    maxPrice: [undefined as number | undefined],
    minRating: [undefined as number | undefined],
    languageCode: [undefined as string | undefined],
    verifiedOnly: [false],
  });

  ngOnInit(): void {
    this.categoryService.getRootCategories().subscribe((categories) => this.categories.set(categories));

    const queryParams = this.route.snapshot.queryParamMap;
    this.form.patchValue({
      keyword: queryParams.get('keyword') ?? '',
      categoryId: queryParams.get('categoryId') ?? undefined,
    });

    this.form.valueChanges.pipe(debounceTime(400)).subscribe(() => {
      this.pageIndex.set(0);
      this.search();
    });

    this.search();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.search();
  }

  resetFilters(): void {
    this.form.reset({ keyword: '', verifiedOnly: false });
  }

  private search(): void {
    this.loading.set(true);
    const value = this.form.getRawValue();
    const params: ServiceSearchParams = {
      keyword: value.keyword || undefined,
      categoryId: value.categoryId,
      city: value.city || undefined,
      maxPrice: value.maxPrice,
      minRating: value.minRating,
      languageCode: value.languageCode,
      verifiedOnly: value.verifiedOnly || undefined,
      page: this.pageIndex(),
      size: this.pageSize(),
    };

    this.serviceCatalog.search(params).subscribe({
      next: (page) => {
        this.results.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
