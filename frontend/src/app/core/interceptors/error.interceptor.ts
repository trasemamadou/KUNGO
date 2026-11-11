import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../../shared/components/toast/toast.service';

/**
 * Affiche un toast pour les erreurs serveur/réseau génériques. Les erreurs 400
 * (validation de formulaire) et 401 (gérées par {@code authInterceptor}) sont
 * volontairement laissées passer sans toast, pour ne pas doubler le retour visuel
 * déjà affiché par le formulaire concerné.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status !== 400 && error.status !== 401) {
        const message = error.error?.message || "Une erreur inattendue s'est produite.";
        toastService.error(message);
      }
      return throwError(() => error);
    })
  );
};
