import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { KungoLogoComponent } from '../../../shared/components/kungo-logo/kungo-logo.component';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink, KungoLogoComponent],
  template: `
    <footer class="border-t-[6px] border-gold-500 bg-brand-600 text-brand-50">
      <div class="mx-auto grid max-w-7xl gap-8 px-4 py-12 sm:px-6 md:grid-cols-4">
        <div>
          <div class="mb-3 flex items-center gap-2 text-lg font-extrabold">
            <app-kungo-logo [size]="36" />
            <span>K<span class="text-gold-400">Û</span>NGO</span>
          </div>
          <p class="text-sm text-brand-100">
            La marketplace événementielle aux racines africaines — musiciens, DJ, photographes,
            traiteurs et bien d'autres talents, partout en Afrique de l'Ouest.
          </p>
        </div>

        <div>
          <h4 class="mb-3 text-sm font-semibold text-white">Découvrir</h4>
          <ul class="space-y-2 text-sm text-brand-100">
            <li><a routerLink="/recherche" class="hover:text-gold-400">Rechercher un professionnel</a></li>
            <li><a routerLink="/auth/register" class="hover:text-gold-400">Devenir professionnel</a></li>
          </ul>
        </div>

        <div>
          <h4 class="mb-3 text-sm font-semibold text-white">Assistance</h4>
          <ul class="space-y-2 text-sm text-brand-100">
            <li><a href="mailto:contact@kungo.app" class="hover:text-gold-400">contact&#64;kungo.app</a></li>
          </ul>
        </div>

        <div>
          <h4 class="mb-3 text-sm font-semibold text-white">Légal</h4>
          <ul class="space-y-2 text-sm text-brand-100">
            <li>Conditions générales</li>
            <li>Politique de confidentialité</li>
          </ul>
        </div>
      </div>
      <div class="border-t border-brand-500/50 py-4 text-center text-xs text-brand-200">
        <span class="text-gold-400">&#9733;</span>
        La marketplace événementielle aux racines africaines
        <br />
        &copy; {{ year }} KÛNGO. Tous droits réservés.
      </div>
    </footer>
  `,
})
export class FooterComponent {
  readonly year = new Date().getFullYear();
}
