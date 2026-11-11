import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../auth/auth.service';
import { NotificationService } from '../../services/notification.service';
import { ThemeService } from '../../services/theme.service';
import { KungoLogoComponent } from '../../../shared/components/kungo-logo/kungo-logo.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatBadgeModule,
    MatTooltipModule,
    KungoLogoComponent,
  ],
  template: `
    <header class="sticky top-0 z-40 border-b-4 border-accent-500 bg-white/95 backdrop-blur dark:bg-gray-950/95">
      <div class="mx-auto flex h-[4.5rem] max-w-7xl items-center gap-4 px-4 sm:px-6">
        <a routerLink="/" class="flex shrink-0 items-center gap-2">
          <app-kungo-logo [size]="44" />
          <span class="text-2xl font-extrabold tracking-wide text-brand-600 dark:text-brand-300">
            K<span class="text-accent-500">Û</span>NGO
          </span>
        </a>

        <form class="hidden flex-1 max-w-xl md:block" (submit)="onSearchSubmit($event)">
          <div class="flex items-center gap-2 rounded-full border border-brand-100 bg-cream px-4 py-2 focus-within:border-gold-400 dark:border-gray-700 dark:bg-gray-900">
            <mat-icon class="!h-5 !w-5 text-brand-400">search</mat-icon>
            <input
              type="search"
              placeholder="DJ, photographe, traiteur..."
              class="w-full bg-transparent text-sm outline-none placeholder:text-brand-400/70 dark:text-gray-100"
              [(ngModel)]="searchKeyword"
              name="keyword"
            />
          </div>
        </form>

        <nav class="ml-auto flex items-center gap-1 sm:gap-2">
          <a
            routerLink="/recherche"
            routerLinkActive="text-accent-500"
            class="hidden rounded-full px-3 py-2 text-sm font-medium text-brand-600 hover:text-accent-500 sm:block dark:text-brand-200"
          >
            Explorer
          </a>

          <button mat-icon-button (click)="themeService.toggle()" matTooltip="Changer de thème" aria-label="Changer de thème">
            <mat-icon>{{ themeService.theme() === 'dark' ? 'light_mode' : 'dark_mode' }}</mat-icon>
          </button>

          @if (authService.isAuthenticated()) {
            <a mat-icon-button routerLink="/notifications" matTooltip="Notifications" aria-label="Notifications">
              <mat-icon [matBadge]="notificationService.unreadCount() || null" matBadgeColor="warn" matBadgeSize="small">
                notifications
              </mat-icon>
            </a>

            <button mat-icon-button [matMenuTriggerFor]="userMenu" aria-label="Menu utilisateur">
              @if (authService.currentUser()?.avatarUrl) {
                <img [src]="authService.currentUser()?.avatarUrl" alt="" class="h-8 w-8 rounded-full object-cover" />
              } @else {
                <mat-icon>account_circle</mat-icon>
              }
            </button>
            <mat-menu #userMenu="matMenu">
              <a mat-menu-item [routerLink]="dashboardLink()">
                <mat-icon>dashboard</mat-icon>
                <span>Tableau de bord</span>
              </a>
              <a mat-menu-item routerLink="/profil">
                <mat-icon>person</mat-icon>
                <span>Mon profil</span>
              </a>
              <a mat-menu-item routerLink="/favoris">
                <mat-icon>favorite</mat-icon>
                <span>Favoris</span>
              </a>
              <button mat-menu-item (click)="logout()">
                <mat-icon>logout</mat-icon>
                <span>Déconnexion</span>
              </button>
            </mat-menu>
          } @else {
            <a routerLink="/auth/login" class="rounded-full px-3 py-2 text-sm font-medium text-brand-600 hover:text-accent-500 dark:text-brand-200">
              Connexion
            </a>
            <a
              routerLink="/auth/register"
              class="rounded-full bg-accent-500 px-5 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-accent-600"
            >
              S'inscrire
            </a>
          }
        </nav>
      </div>
    </header>
  `,
})
export class HeaderComponent {
  readonly authService = inject(AuthService);
  readonly notificationService = inject(NotificationService);
  readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);

  searchKeyword = '';

  dashboardLink(): string {
    switch (this.authService.currentRole()) {
      case 'PROFESSIONNEL':
        return '/pro/tableau-de-bord';
      case 'ADMIN':
      case 'SUPER_ADMIN':
      case 'MODERATEUR':
        return '/admin/tableau-de-bord';
      default:
        return '/client/tableau-de-bord';
    }
  }

  onSearchSubmit(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/recherche'], { queryParams: { keyword: this.searchKeyword || null } });
  }

  logout(): void {
    this.authService.logout().subscribe({ next: () => this.router.navigate(['/']) });
  }
}
