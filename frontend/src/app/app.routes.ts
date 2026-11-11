import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent),
    title: 'KUNGO — Trouvez votre professionnel événementiel',
  },
  {
    path: 'auth/login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
    title: 'Connexion — KUNGO',
  },
  {
    path: 'auth/register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
    title: 'Inscription — KUNGO',
  },
  {
    path: 'auth/activate',
    loadComponent: () => import('./features/auth/activate/activate.component').then((m) => m.ActivateComponent),
    title: 'Activation de compte — KUNGO',
  },
  {
    path: 'auth/forgot-password',
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password.component').then((m) => m.ForgotPasswordComponent),
    title: 'Mot de passe oublié — KUNGO',
  },
  {
    path: 'auth/reset-password',
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password.component').then((m) => m.ResetPasswordComponent),
    title: 'Réinitialisation — KUNGO',
  },
  {
    path: 'recherche',
    loadComponent: () => import('./features/search/search.component').then((m) => m.SearchComponent),
    title: 'Recherche — KUNGO',
  },
  {
    path: 'professionnels/:id',
    loadComponent: () =>
      import('./features/professional-profile/professional-profile.component').then((m) => m.ProfessionalProfileComponent),
    title: 'Profil professionnel — KUNGO',
  },
  {
    path: 'reservations/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/booking/booking-detail/booking-detail.component').then((m) => m.BookingDetailComponent),
    title: 'Ma réservation — KUNGO',
  },
  {
    path: 'notifications',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/notifications/notifications.component').then((m) => m.NotificationsComponent),
    title: 'Notifications — KUNGO',
  },
  {
    path: 'favoris',
    canActivate: [authGuard],
    loadComponent: () => import('./features/favorites/favorites.component').then((m) => m.FavoritesComponent),
    title: 'Mes favoris — KUNGO',
  },
  {
    path: 'profil',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile.component').then((m) => m.ProfileComponent),
    title: 'Mon profil — KUNGO',
  },
  {
    path: 'client/tableau-de-bord',
    canActivate: [authGuard, roleGuard(['CLIENT'])],
    loadComponent: () =>
      import('./features/dashboard-client/dashboard-client.component').then((m) => m.DashboardClientComponent),
    title: 'Mon tableau de bord — KUNGO',
  },
  {
    path: 'pro',
    canActivate: [authGuard, roleGuard(['PROFESSIONNEL'])],
    children: [
      {
        path: 'tableau-de-bord',
        loadComponent: () =>
          import('./features/dashboard-professional/dashboard-professional.component').then(
            (m) => m.DashboardProfessionalComponent
          ),
        title: 'Tableau de bord pro — KUNGO',
      },
      {
        path: 'prestations',
        loadComponent: () =>
          import('./features/dashboard-professional/services/pro-services.component').then((m) => m.ProServicesComponent),
        title: 'Mes prestations — KUNGO',
      },
      {
        path: 'reservations',
        loadComponent: () =>
          import('./features/dashboard-professional/bookings/pro-bookings.component').then((m) => m.ProBookingsComponent),
        title: 'Mes réservations — KUNGO',
      },
      {
        path: 'disponibilites',
        loadComponent: () =>
          import('./features/dashboard-professional/availabilities/pro-availabilities.component').then(
            (m) => m.ProAvailabilitiesComponent
          ),
        title: 'Mes disponibilités — KUNGO',
      },
    ],
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard(['ADMIN', 'SUPER_ADMIN', 'MODERATEUR'])],
    children: [
      {
        path: 'tableau-de-bord',
        loadComponent: () =>
          import('./features/dashboard-admin/dashboard-admin.component').then((m) => m.DashboardAdminComponent),
        title: 'Administration — KUNGO',
      },
      {
        path: 'utilisateurs',
        loadComponent: () =>
          import('./features/dashboard-admin/users/admin-users.component').then((m) => m.AdminUsersComponent),
        title: 'Utilisateurs — KUNGO',
      },
      {
        path: 'verifications',
        loadComponent: () =>
          import('./features/dashboard-admin/verifications/admin-verifications.component').then(
            (m) => m.AdminVerificationsComponent
          ),
        title: 'Vérifications — KUNGO',
      },
      {
        path: 'signalements',
        loadComponent: () =>
          import('./features/dashboard-admin/reports/admin-reports.component').then((m) => m.AdminReportsComponent),
        title: 'Signalements — KUNGO',
      },
      {
        path: 'commissions',
        loadComponent: () =>
          import('./features/dashboard-admin/commissions/admin-commissions.component').then(
            (m) => m.AdminCommissionsComponent
          ),
        title: 'Commissions — KUNGO',
      },
    ],
  },
  {
    path: '**',
    loadComponent: () => import('./features/not-found/not-found.component').then((m) => m.NotFoundComponent),
    title: 'Page introuvable — KUNGO',
  },
];
