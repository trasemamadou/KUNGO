import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/auth/auth.service';

function passwordStrengthValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value as string;
  if (!value) return null;
  const valid = /(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+/.test(value) && value.length >= 8;
  return valid ? null : { weakPassword: true };
}

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirm = control.get('confirmPassword')?.value;
  return password && confirm && password !== confirm ? { passwordsMismatch: true } : null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="mx-auto flex min-h-[80vh] max-w-lg flex-col justify-center px-4 py-12">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Créer un compte</h1>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Rejoignez la marketplace événementielle KUNGO.</p>

      <form class="mt-6 flex flex-col gap-1" [formGroup]="form" (ngSubmit)="onSubmit()">
        <label class="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">Je m'inscris en tant que</label>
        <mat-button-toggle-group formControlName="role" class="mb-4 w-full" aria-label="Type de compte">
          <mat-button-toggle value="CLIENT" class="!flex-1">Client</mat-button-toggle>
          <mat-button-toggle value="PROFESSIONNEL" class="!flex-1">Professionnel</mat-button-toggle>
        </mat-button-toggle-group>

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
          <mat-label>Email</mat-label>
          <input matInput type="email" formControlName="email" autocomplete="email" />
          @if (form.controls.email.hasError('email')) {
            <mat-error>Format d'email invalide</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Téléphone (optionnel)</mat-label>
          <input matInput formControlName="phone" placeholder="+225 07 00 00 00 00" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Mot de passe</mat-label>
          <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password" autocomplete="new-password" />
          <button mat-icon-button matSuffix type="button" (click)="hidePassword.set(!hidePassword())">
            <mat-icon>{{ hidePassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          @if (form.controls.password.hasError('weakPassword') && form.controls.password.touched) {
            <mat-error>8 caractères min., avec majuscule, minuscule et chiffre</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Confirmer le mot de passe</mat-label>
          <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="confirmPassword" />
          @if (form.hasError('passwordsMismatch') && form.controls.confirmPassword.touched) {
            <mat-error>Les mots de passe ne correspondent pas</mat-error>
          }
        </mat-form-field>

        @if (errorMessage()) {
          <p class="mb-3 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
            {{ errorMessage() }}
          </p>
        }

        @if (successMessage()) {
          <p class="mb-3 rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300">
            {{ successMessage() }}
          </p>
        }

        <button mat-flat-button color="primary" type="submit" class="!py-2.5" [disabled]="form.invalid || loading()">
          @if (loading()) {
            <mat-spinner diameter="20" class="!inline-block" />
          } @else {
            Créer mon compte
          }
        </button>
      </form>

      <p class="mt-6 text-center text-sm text-gray-500 dark:text-gray-400">
        Déjà inscrit ?
        <a routerLink="/auth/login" class="font-medium text-brand-600 hover:underline dark:text-brand-400">Se connecter</a>
      </p>
    </div>
  `,
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly hidePassword = signal(true);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group(
    {
      role: ['CLIENT' as 'CLIENT' | 'PROFESSIONNEL', [Validators.required]],
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      password: ['', [Validators.required, passwordStrengthValidator]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatchValidator }
  );

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const { confirmPassword, ...request } = this.form.getRawValue();
    void confirmPassword;

    this.authService.register(request).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set('Inscription réussie ! Vérifiez votre boîte mail pour activer votre compte.');
        setTimeout(() => this.router.navigate(['/auth/login']), 3000);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || "L'inscription a échoué. Veuillez réessayer.");
      },
    });
  }
}
