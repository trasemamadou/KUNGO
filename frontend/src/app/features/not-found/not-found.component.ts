import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink, MatButtonModule],
  template: `
    <div class="mx-auto flex min-h-[70vh] max-w-md flex-col items-center justify-center px-4 text-center">
      <span class="text-6xl">🎈</span>
      <h1 class="mt-4 text-2xl font-bold text-gray-900 dark:text-gray-100">Page introuvable</h1>
      <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">Cette page n'existe pas ou plus.</p>
      <a mat-flat-button color="primary" routerLink="/" class="!mt-6">Retour à l'accueil</a>
    </div>
  `,
})
export class NotFoundComponent {}
