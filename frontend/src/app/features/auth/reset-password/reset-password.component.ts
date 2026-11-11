import { Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/auth/auth.service';

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('newPassword')?.value;
  const confirm = control.get('confirmPassword')?.value;
  return password && confirm && password !== confirm ? { passwordsMismatch: true } : null;
}

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule],
  template: `
    <div class="mx-auto flex min-h-[70vh] max-w-md flex-col justify-center px-4 py-12">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Réinitialiser le mot de passe</h1>

      @if (!token()) {
        <p class="mt-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
          Lien invalide. Merci de refaire une demande de réinitialisation.
        </p>
        <a routerLink="/auth/forgot-password" class="mt-4 text-center text-sm text-brand-600 hover:underline">
          Nouvelle demande
        </a>
      } @else if (success()) {
        <p class="mt-4 rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300">
          Mot de passe réinitialisé avec succès.
        </p>
        <a routerLink="/auth/login" class="mt-4 text-center text-sm text-brand-600 hover:underline">Se connecter</a>
      } @else {
        <form class="mt-6 flex flex-col gap-1" [formGroup]="form" (ngSubmit)="onSubmit()">
          <mat-form-field appearance="outline">
            <mat-label>Nouveau mot de passe</mat-label>
            <input matInput type="password" formControlName="newPassword" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Confirmer le mot de passe</mat-label>
            <input matInput type="password" formControlName="confirmPassword" />
            @if (form.hasError('passwordsMismatch') && form.controls.confirmPassword.touched) {
              <mat-error>Les mots de passe ne correspondent pas</mat-error>
            }
          </mat-form-field>

          @if (errorMessage()) {
            <p class="mb-3 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
              {{ errorMessage() }}
            </p>
          }

          <button mat-flat-button color="primary" type="submit" class="!py-2.5" [disabled]="form.invalid || loading()">
            @if (loading()) {
              <mat-spinner diameter="20" class="!inline-block" />
            } @else {
              Réinitialiser
            }
          </button>
        </form>
      }
    </div>
  `,
})
export class ResetPasswordComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly token = signal<string | null>(null);
  readonly loading = signal(false);
  readonly success = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group(
    {
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatchValidator }
  );

  ngOnInit(): void {
    this.token.set(this.route.snapshot.queryParamMap.get('token'));
  }

  onSubmit(): void {
    if (this.form.invalid || !this.token()) return;
    this.loading.set(true);

    this.authService.resetPassword({ token: this.token()!, newPassword: this.form.getRawValue().newPassword }).subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set(true);
        setTimeout(() => this.router.navigate(['/auth/login']), 2500);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Ce lien a expiré ou est invalide.');
      },
    });
  }
}
