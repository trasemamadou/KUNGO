import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/user.model';
import { AuthService } from '../auth/auth.service';
import { decodeJwt } from '../auth/jwt.util';
import { TokenStorageService } from '../auth/token-storage.service';

/**
 * Restreint l'accès à une route selon le rôle. Utilise le rôle porté par le JWT
 * (pas d'appel réseau) pour une décision de navigation instantanée ; c'est le
 * backend qui reste l'autorité finale via {@code @PreAuthorize}.
 */
export function roleGuard(allowedRoles: Role[]): CanActivateFn {
  return () => {
    const tokenStorage = inject(TokenStorageService);
    const router = inject(Router);
    const authService = inject(AuthService);

    const token = tokenStorage.getAccessToken();
    if (!token) {
      return router.createUrlTree(['/auth/login']);
    }

    const role = authService.currentRole() ?? decodeJwt(token)?.role;
    if (role && allowedRoles.includes(role as Role)) {
      return true;
    }
    return router.createUrlTree(['/']);
  };
}
