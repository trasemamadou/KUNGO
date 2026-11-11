import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule],
  template: `
    <div class="mx-auto flex min-h-[70vh] max-w-md flex-col justify-center px-4 py-12">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Mot de passe oublié</h1>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
        Indiquez votre email : si un compte existe, un lien de réinitialisation vous sera envoyé.
      </p>

      @if (submitted()) {
        <p class="mt-6 rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300">
          Si cet email existe, un lien de réinitialisation a été envoyé.
        </p>
      } @else {
        <form class="mt-6 flex flex-col gap-1" [formGroup]="form" (ngSubmit)="onSubmit()">
          <mat-form-field appearance="outline">
            <mat-label>Email</mat-label>
            <input matInput type="email" formControlName="email" />
          </mat-form-field>

          <button mat-flat-button color="primary" type="submit" class="!py-2.5" [disabled]="form.invalid || loading()">
            @if (loading()) {
              <mat-spinner diameter="20" class="!inline-block" />
            } @else {
              Envoyer le lien
            }
          </button>
        </form>
      }

      <a routerLink="/auth/login" class="mt-6 text-center text-sm text-brand-600 hover:underline dark:text-brand-400">
        Retour à la connexion
      </a>
    </div>
  `,
})
export class ForgotPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly submitted = signal(false);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.authService.forgotPassword(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.submitted.set(true);
      },
      error: () => {
        this.loading.set(false);
        this.submitted.set(true); // anti énumération : même retour visuel qu'un succès
      },
    });
  }
}
