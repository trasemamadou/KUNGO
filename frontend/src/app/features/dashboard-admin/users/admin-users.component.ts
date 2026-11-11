import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { debounceTime, Subject } from 'rxjs';
import { User } from '../../../core/models/user.model';
import { UserService } from '../../../core/services/user.service';
import { ToastService } from '../../../shared/components/toast/toast.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatPaginatorModule],
  template: `
    <div class="mx-auto max-w-6xl px-4 py-8 sm:px-6">
      <h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-gray-100">Gestion des utilisateurs</h1>

      <div class="mb-4 flex flex-wrap gap-3">
        <mat-form-field appearance="outline">
          <mat-label>Recherche</mat-label>
          <input matInput [(ngModel)]="keyword" (ngModelChange)="onFilterChange()" placeholder="Nom, email..." />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Rôle</mat-label>
          <mat-select [(ngModel)]="role" (ngModelChange)="onFilterChange()">
            <mat-option [value]="undefined">Tous</mat-option>
            <mat-option value="CLIENT">Client</mat-option>
            <mat-option value="PROFESSIONNEL">Professionnel</mat-option>
            <mat-option value="ADMIN">Admin</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Statut</mat-label>
          <mat-select [(ngModel)]="status" (ngModelChange)="onFilterChange()">
            <mat-option [value]="undefined">Tous</mat-option>
            <mat-option value="ACTIVE">Actif</mat-option>
            <mat-option value="PENDING">En attente</mat-option>
            <mat-option value="SUSPENDED">Suspendu</mat-option>
            <mat-option value="BANNED">Banni</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div class="overflow-x-auto rounded-xl border border-gray-200 dark:border-gray-800">
        <table class="w-full text-left text-sm">
          <thead class="bg-gray-50 text-xs uppercase text-gray-500 dark:bg-gray-800 dark:text-gray-400">
            <tr>
              <th class="px-4 py-3">Nom</th>
              <th class="px-4 py-3">Email</th>
              <th class="px-4 py-3">Rôle</th>
              <th class="px-4 py-3">Statut</th>
              <th class="px-4 py-3">Actions</th>
            </tr>
          </thead>
          <tbody>
            @for (user of users(); track user.id) {
              <tr class="border-t border-gray-100 dark:border-gray-800">
                <td class="px-4 py-3">{{ user.fullName }}</td>
                <td class="px-4 py-3">{{ user.email }}</td>
                <td class="px-4 py-3">{{ user.role }}</td>
                <td class="px-4 py-3">{{ user.status }}</td>
                <td class="flex gap-1 px-4 py-3">
                  @if (user.status !== 'SUSPENDED') {
                    <button mat-button (click)="suspend(user)">Suspendre</button>
                  } @else {
                    <button mat-button (click)="reactivate(user)">Réactiver</button>
                  }
                  @if (user.status !== 'BANNED') {
                    <button mat-button color="warn" (click)="ban(user)">Bannir</button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      <mat-paginator
        class="mt-4 bg-transparent"
        [length]="total()"
        [pageSize]="pageSize()"
        [pageIndex]="pageIndex()"
        [pageSizeOptions]="[10, 20, 50]"
        (page)="onPageChange($event)"
      />
    </div>
  `,
})
export class AdminUsersComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly toastService = inject(ToastService);
  private readonly filterChange$ = new Subject<void>();

  readonly users = signal<User[]>([]);
  readonly total = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  keyword = '';
  role?: string;
  status?: string;

  ngOnInit(): void {
    this.filterChange$.pipe(debounceTime(400)).subscribe(() => {
      this.pageIndex.set(0);
      this.load();
    });
    this.load();
  }

  onFilterChange(): void {
    this.filterChange$.next();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  private load(): void {
    this.userService
      .search({ keyword: this.keyword || undefined, role: this.role, status: this.status, page: this.pageIndex(), size: this.pageSize() })
      .subscribe((page) => {
        this.users.set(page.content);
        this.total.set(page.totalElements);
      });
  }

  suspend(user: User): void {
    this.userService.suspend(user.id).subscribe(() => {
      this.toastService.success('Compte suspendu');
      this.load();
    });
  }

  reactivate(user: User): void {
    this.userService.reactivate(user.id).subscribe(() => {
      this.toastService.success('Compte réactivé');
      this.load();
    });
  }

  ban(user: User): void {
    this.userService.ban(user.id).subscribe(() => {
      this.toastService.success('Compte banni');
      this.load();
    });
  }
}
