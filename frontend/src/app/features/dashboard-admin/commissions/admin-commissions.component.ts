import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Category } from '../../../core/models/category.model';
import { AdminService, CommissionRule } from '../../../core/services/admin.service';
import { CategoryService } from '../../../core/services/category.service';
import { ToastService } from '../../../shared/components/toast/toast.service';

@Component({
  selector: 'app-admin-commissions',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule],
  template: `
    <div class="mx-auto max-w-3xl px-4 py-8 sm:px-6">
      <h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-gray-100">Commissions plateforme</h1>

      <form [formGroup]="form" class="mb-8 flex flex-wrap items-end gap-3 rounded-xl border border-gray-200 p-4 dark:border-gray-800" (ngSubmit)="create()">
        <mat-form-field appearance="outline">
          <mat-label>Catégorie (vide = règle globale)</mat-label>
          <mat-select formControlName="categoryId">
            <mat-option [value]="undefined">Règle globale</mat-option>
            @for (category of categories(); track category.id) {
              <mat-option [value]="category.id">{{ category.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Pourcentage (%)</mat-label>
          <input matInput type="number" formControlName="percentage" min="0" max="100" step="0.5" />
        </mat-form-field>
        <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid">Ajouter la règle</button>
      </form>

      <div class="flex flex-col gap-2">
        @for (rule of rules(); track rule.id) {
          <div class="flex items-center justify-between rounded-lg border border-gray-200 p-4 dark:border-gray-800">
            <div>
              <p class="font-medium text-gray-900 dark:text-gray-100">
                {{ rule.categoryName || 'Règle globale' }} — {{ rule.percentage }}%
              </p>
              <p class="text-xs text-gray-400">{{ rule.active ? 'Active' : 'Inactive' }}</p>
            </div>
            @if (rule.active) {
              <button mat-icon-button (click)="deactivate(rule)" aria-label="Désactiver"><mat-icon>close</mat-icon></button>
            }
          </div>
        }
      </div>
    </div>
  `,
})
export class AdminCommissionsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly adminService = inject(AdminService);
  private readonly categoryService = inject(CategoryService);
  private readonly toastService = inject(ToastService);

  readonly rules = signal<CommissionRule[]>([]);
  readonly categories = signal<Category[]>([]);

  readonly form = this.fb.nonNullable.group({
    categoryId: [undefined as string | undefined],
    percentage: [12, [Validators.required, Validators.min(0), Validators.max(100)]],
  });

  ngOnInit(): void {
    this.categoryService.getRootCategories().subscribe((categories) => this.categories.set(categories));
    this.load();
  }

  private load(): void {
    this.adminService.listCommissionRules().subscribe((rules) => this.rules.set(rules));
  }

  create(): void {
    if (this.form.invalid) return;
    this.adminService.createCommissionRule(this.form.getRawValue()).subscribe(() => {
      this.toastService.success('Règle de commission créée');
      this.form.reset({ percentage: 12 });
      this.load();
    });
  }

  deactivate(rule: CommissionRule): void {
    this.adminService.deactivateCommissionRule(rule.id).subscribe(() => {
      this.toastService.success('Règle désactivée');
      this.load();
    });
  }
}
