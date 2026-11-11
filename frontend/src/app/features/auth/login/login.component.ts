import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="mx-auto flex min-h-[80vh] max-w-md flex-col justify-center px-4 py-12">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Connexion</h1>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Ravi de vous revoir sur KUNGO.</p>

      <form class="mt-6 flex flex-col gap-1" [formGroup]="form" (ngSubmit)="onSubmit()">
        <mat-form-field appearance="outline">
          <mat-label>Email</mat-label>
          <input matInput type="email" formControlName="email" autocomplete="email" />
          @if (form.controls.email.hasError('required') && form.controls.email.touched) {
            <mat-error>L'email est requis</mat-error>
          }
          @if (form.controls.email.hasError('email')) {
            <mat-error>Format d'email invalide</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Mot de passe</mat-label>
          <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password" autocomplete="current-password" />
          <button mat-icon-button matSuffix type="button" (click)="hidePassword.set(!hidePassword())">
            <mat-icon>{{ hidePassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          @if (form.controls.password.hasError('required') && form.controls.password.touched) {
            <mat-error>Le mot de passe est requis</mat-error>
          }
        </mat-form-field>

        <a routerLink="/auth/forgot-password" class="mb-4 self-end text-sm text-brand-600 hover:underline dark:text-brand-400">
          Mot de passe oublié ?
        </a>

        @if (errorMessage()) {
          <p class="mb-3 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
            {{ errorMessage() }}
          </p>
        }

        <button mat-flat-button color="primary" type="submit" class="!py-2.5" [disabled]="form.invalid || loading()">
          @if (loading()) {
            <mat-spinner diameter="20" class="!inline-block" />
          } @else {
            Se connecter
          }
        </button>
      </form>

      <p class="mt-6 text-center text-sm text-gray-500 dark:text-gray-400">
        Pas encore de compte ?
        <a routerLink="/auth/register" class="font-medium text-brand-600 hover:underline dark:text-brand-400">S'inscrire</a>
      </p>
    </div>
  `,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly hidePassword = signal(true);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        const redirect = this.route.snapshot.queryParamMap.get('redirect');
        this.router.navigateByUrl(redirect || '/');
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Email ou mot de passe incorrect.');
      },
    });
  }
}
