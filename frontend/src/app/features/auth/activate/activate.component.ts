import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/auth/auth.service';

type ActivationState = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-activate',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatProgressSpinnerModule],
  template: `
    <div class="mx-auto flex min-h-[70vh] max-w-md flex-col items-center justify-center px-4 text-center">
      @switch (state()) {
        @case ('loading') {
          <mat-spinner diameter="40" />
          <p class="mt-4 text-gray-500 dark:text-gray-400">Activation de votre compte en cours...</p>
        }
        @case ('success') {
          <span class="text-5xl">✅</span>
          <h1 class="mt-4 text-xl font-bold text-gray-900 dark:text-gray-100">Compte activé !</h1>
          <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">Vous pouvez maintenant vous connecter.</p>
          <a mat-flat-button color="primary" routerLink="/auth/login" class="!mt-6">Se connecter</a>
        }
        @case ('error') {
          <span class="text-5xl">⚠️</span>
          <h1 class="mt-4 text-xl font-bold text-gray-900 dark:text-gray-100">Lien invalide ou expiré</h1>
          <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">
            Ce lien d'activation n'est plus valide. Vous pouvez en demander un nouveau depuis la page de connexion.
          </p>
          <a mat-flat-button color="primary" routerLink="/auth/login" class="!mt-6">Retour à la connexion</a>
        }
      }
    </div>
  `,
})
export class ActivateComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);

  readonly state = signal<ActivationState>('loading');

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.state.set('error');
      return;
    }
    this.authService.activate(token).subscribe({
      next: () => this.state.set('success'),
      error: () => this.state.set('error'),
    });
  }
}
