import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTabsModule } from '@angular/material/tabs';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/services/user.service';
import { ToastService } from '../../shared/components/toast/toast.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatIconModule, MatTabsModule],
  template: `
    <div class="mx-auto max-w-2xl px-4 py-8 sm:px-6">
      <h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-gray-100">Mon profil</h1>

      <div class="mb-6 flex items-center gap-4">
        <div class="relative">
          @if (user()?.avatarUrl) {
            <img [src]="user()?.avatarUrl" alt="" class="h-20 w-20 rounded-full object-cover" />
          } @else {
            <div class="flex h-20 w-20 items-center justify-center rounded-full bg-brand-100 text-2xl text-brand-600 dark:bg-brand-900">
              {{ user()?.firstName?.charAt(0) }}
            </div>
          }
          <label class="absolute -bottom-1 -right-1 flex h-7 w-7 cursor-pointer items-center justify-center rounded-full bg-brand-600 text-white">
            <mat-icon class="!text-base">edit</mat-icon>
            <input type="file" accept="image/*" class="hidden" (change)="onAvatarSelected($event)" />
          </label>
        </div>
        <div>
          <p class="font-semibold text-gray-900 dark:text-gray-100">{{ user()?.fullName }}</p>
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ user()?.email }}</p>
        </div>
      </div>

      <mat-tab-group>
        <mat-tab label="Informations">
          <form class="mt-6 flex flex-col gap-1" [formGroup]="profileForm" (ngSubmit)="saveProfile()">
            <div class="grid grid-cols-2 gap-3">
              <mat-form-field appearance="outline">
                <mat-label>Prénom</mat-label>
                <input matInput formControlName="firstName" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Nom</mat-label>
                <input matInput formControlName="lastName" />
              </mat-form-field>
            </div>
            <mat-form-field appearance="outline">
              <mat-label>Téléphone</mat-label>
              <input matInput formControlName="phone" />
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit" class="self-start" [disabled]="profileForm.invalid">
              Enregistrer
            </button>
          </form>
        </mat-tab>

        <mat-tab label="Mot de passe">
          <form class="mt-6 flex flex-col gap-1" [formGroup]="passwordForm" (ngSubmit)="changePassword()">
            <mat-form-field appearance="outline">
              <mat-label>Mot de passe actuel</mat-label>
              <input matInput type="password" formControlName="currentPassword" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Nouveau mot de passe</mat-label>
              <input matInput type="password" formControlName="newPassword" />
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit" class="self-start" [disabled]="passwordForm.invalid">
              Changer le mot de passe
            </button>
          </form>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
})
export class ProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly toastService = inject(ToastService);

  readonly user = this.authService.currentUser;

  readonly profileForm = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    phone: [''],
  });

  readonly passwordForm = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
  });

  ngOnInit(): void {
    if (!this.user()) {
      this.authService.loadCurrentUser().subscribe(() => this.patchForm());
    } else {
      this.patchForm();
    }
  }

  private patchForm(): void {
    const user = this.user();
    if (user) {
      this.profileForm.patchValue({ firstName: user.firstName, lastName: user.lastName, phone: user.phone });
    }
  }

  saveProfile(): void {
    if (this.profileForm.invalid || !this.user()) return;
    this.userService.update(this.user()!.id, this.profileForm.getRawValue()).subscribe({
      next: (updated) => {
        this.authService.setCurrentUser(updated);
        this.toastService.success('Profil mis à jour');
      },
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) return;
    this.authService.changePassword(this.passwordForm.getRawValue()).subscribe({
      next: () => {
        this.toastService.success('Mot de passe modifié');
        this.passwordForm.reset();
      },
    });
  }

  onAvatarSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file || !this.user()) return;
    this.userService.uploadAvatar(this.user()!.id, file).subscribe((updated) => {
      this.authService.setCurrentUser(updated);
      this.toastService.success('Avatar mis à jour');
    });
  }
}
